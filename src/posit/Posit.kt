package posit

import sun.awt.image.ImageRepresentation
import java.lang.Float.floatToIntBits
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private data class RegimeInfo(val regimeLen: Int, val regimeK: Int, val regimeBits: ULong)
private data class ExponentInfo(var exponentLen: Int, var exponent: Int)
private data class FractionInfo(var fractionLen: Int, var fraction: ULong)

data class PositRepr(
    var sign: ULong,
    var regimeK: Int,
    var exponent: ULong,
    var fraction: ULong
)


//32 bits
public class Posit : Number, Comparable<Posit> {
    companion object {
        //region Constants
        val INFINITY: ULong = (Int.MAX_VALUE.toULong() + 1UL) //0x0x80000000
        const val ZERO: ULong = 0u //0x00000000
        const val ONE_POSITIVE: ULong = 1073741824UL //0x40000000
        const val ONE_NEGATIVE: ULong = 3221225472uL //0xC0000000
        //endregion

        //region Posit structure
        const val NBITS = 32
        const val ES = 2
        var USEED = -1
        //endregion
    }

    var positChunk: ULong = ZERO
        private set

    private constructor(positValue: ULong, boolean: Boolean) {
        positChunk = positValue
    }

    //Для целых чисел
    constructor(value: Int) {
        positChunk = x2p(value.toFloat())
    }

    constructor(value: Float) {
        positChunk = x2p(value)
    }

    //region Operations
    operator fun plus(otherPosit: Posit): Posit {
        if (this.positChunk == 0UL) {
            return otherPosit
        }
        if (otherPosit.positChunk == 0UL) {
            return this
        }
        if (this.positChunk == INFINITY || otherPosit.positChunk == INFINITY) {
            return Posit(INFINITY, true)
        }
        val posit1 = decodePosit(this.positChunk)
        val posit2 = decodePosit(otherPosit.positChunk)

        //Scale of new posit
        val scale1 = (2.0.pow(ES) * posit1.regimeK + posit1.exponent.toInt()).toInt()
        val scale2 = (2.0.pow(ES) * posit2.regimeK + posit2.exponent.toInt()).toInt()
        var scaleNew = max(scale1, scale2)

        val resF = alignBoth(posit1.fraction, posit2.fraction)
        var fraction1Aligned = resF.first
        var fraction2Aligned = resF.second


        if (scale1 > scale2) {
            fraction1Aligned = fraction1Aligned shl (scale1 - scale2)
        } else {
            fraction2Aligned = fraction2Aligned shl (scale2 - scale1)
        }

        val estimatedLen = if (scale1 > scale2)
            mostSignificantBitPosition(fraction1Aligned)
        else
            mostSignificantBitPosition(fraction2Aligned)

        var fractionNew =
            ((-1.0).pow(posit1.sign.toDouble()) * fraction1Aligned.toDouble() + (-1.0).pow(posit2.sign.toDouble()) * fraction2Aligned.toDouble()).toInt()
        val signNew = signBit(fractionNew).toInt()
        fractionNew = abs(fractionNew)

        val resultLen = mostSignificantBitPosition(fractionNew.toULong())//ТУТ может быть минус, потестить

        scaleNew += (resultLen - estimatedLen)
        //Remove redundant last zeros
        fractionNew = fractionNew shr (leastSignificantBitPosition(fractionNew.toULong()) - 1)

        val res = makePositValue(signNew, scaleNew, fractionNew)
        return Posit(res, true)
    }

    operator fun minus(otherPosit: Posit): Posit {
        return this + (-otherPosit)
    }

    operator fun unaryMinus(): Posit {
        return Posit(twosComplement(positChunk, NBITS), true)
    }

    private fun alignBoth(v1: ULong, v2: ULong): Pair<ULong, ULong> {
        var v1 = v1
        var v2 = v2
        val v1Len = mostSignificantBitPosition(v1)
        val v2Len = mostSignificantBitPosition(v2)

        if (v1Len > v2Len) {
            v2 = v2 shl abs(v1Len - v2Len)
        } else if (v2Len > v1Len) {
            v1 = v1 shl abs(v2Len - v1Len)
        }

        val v1RedundantZeros = leastSignificantBitPosition(v1) - 1
        val v2RedundantZeros = leastSignificantBitPosition(v2) - 1
        val shift = min(v1RedundantZeros, v2RedundantZeros)
        v1 = v1 shr shift
        v2 = v2 shr shift

        return v1 to v2
    }

    operator fun times(otherPosit: Posit): Posit {
        if (this.positChunk == 0UL || otherPosit.positChunk == 0UL) {
            return Posit(0u, true)
        }
        if (this.positChunk == INFINITY || otherPosit.positChunk == INFINITY) {
            return Posit(INFINITY, true)
        }

        val posit1 = decodePosit(this.positChunk)
        val posit2 = decodePosit(otherPosit.positChunk)

        //Таблица истинности
        val signNew = posit1.sign xor posit2.sign
        var scaleNew =
            2.0.pow(ES) * (posit1.regimeK + posit2.regimeK) + posit1.exponent.toDouble() + posit2.exponent.toDouble()
        val fractionNew = posit1.fraction * posit2.fraction

        val msbF1 = mostSignificantBitPosition(posit1.fraction) - 1
        val msbF2 = mostSignificantBitPosition(posit2.fraction) - 1
        val msbNew = mostSignificantBitPosition(fractionNew) - 1

        scaleNew += (msbNew - msbF1 - msbF2)


        val result = makePositValue(signNew.toInt(), scaleNew.toInt(), fractionNew.toInt())

        return Posit(result, true)
    }

    operator fun div(otherPosit: Posit): Posit {
        if (this.positChunk == ZERO || this.positChunk == INFINITY) {
            return this
        }

        if (otherPosit.positChunk == ZERO || this.positChunk == INFINITY ||
            otherPosit.positChunk == INFINITY
        ) {
            return Posit(INFINITY, true) //NaR
        }

        val posit2 = decodePosit(otherPosit.positChunk)

        if (posit2.fraction and (posit2.fraction - 1UL) == 0UL) {
            val neg = twosComplement(otherPosit.positChunk, NBITS)
            val setBit = (neg or (1UL shl (NBITS - 1))) xor (1UL shl (NBITS - 1))
            return this * Posit(setBit, true)
        }

        val posit1 = decodePosit(this.positChunk)

        val signNew = posit1.sign xor posit2.sign
        var scaleNew =
            2.0.pow(ES) * (posit1.regimeK - posit2.regimeK) + posit1.exponent.toDouble() - posit2.exponent.toDouble()
        val aligned = alignBoth(posit1.fraction, posit2.fraction)
        val fraction1Aligned = aligned.first shl (NBITS * 4)
        val fraction2Aligned = aligned.second

        val fractionNew = fraction1Aligned / fraction2Aligned

        val msbF1 = mostSignificantBitPosition(fraction1Aligned) - 1
        val msbF2 = mostSignificantBitPosition(fraction2Aligned) - 1
        val msbNew = mostSignificantBitPosition(fractionNew) - 1

        scaleNew -= (msbF1 - msbF2 - msbNew)

        val result = makePositValue(signNew.toInt(), scaleNew.toInt(), fractionNew.toInt())

        return Posit(result, true)
    }

    operator fun rem(otherPosit: Posit): Posit {
        return this - (this / otherPosit).floor() * otherPosit
    }

    fun floor(): Posit {
        if (this.positChunk == ZERO || this.positChunk == INFINITY) {
            return this
        }
        val posit = decodePosit(this.positChunk)
        var scaleNew = (2.0.pow(ES) * posit.regimeK + posit.exponent.toDouble()).toInt()

        var fractionNew = posit.fraction

        if (scaleNew >= 0) {
            val fracBitsLen = mostSignificantBitPosition(fractionNew)

            if (scaleNew <= fracBitsLen - 1) {
                fractionNew = fractionNew and (1UL shl (fracBitsLen - 1 - scaleNew)).inv()
            }
        } else {
            fractionNew = 0u
            scaleNew = 0
        }

        val result = makePositValue(posit.sign.toInt(), scaleNew, fractionNew.toInt())
        return Posit(result, true)
    }

    override fun equals(other: Any?): Boolean {
        val otherPosit: Posit = if (other !is Posit) {
            Posit(other as Float)
        } else {
            Posit(other.positChunk, true)
        }

        return this.positChunk == otherPosit.positChunk
    }

    override fun toByte(): Byte {
        val decoded = decodePosit(positChunk)
        return getDoubleRepresentation(
            decoded.sign,
            decoded.regimeK,
            decoded.exponent.toInt(),
            decoded.fraction
        )
            .toInt()
            .toByte()
    }

    override fun toChar(): Char {
        val decoded = decodePosit(positChunk)
        return getDoubleRepresentation(
            decoded.sign,
            decoded.regimeK,
            decoded.exponent.toInt(),
            decoded.fraction
        )
            .toInt()
            .toChar()
    }

    override fun toDouble(): Double {
        val decoded = decodePosit(positChunk)
        return getDoubleRepresentation(
            decoded.sign,
            decoded.regimeK,
            decoded.exponent.toInt(),
            decoded.fraction
        )
    }

    override fun toFloat(): Float {
        val decoded = decodePosit(positChunk)
        return getDoubleRepresentation(
            decoded.sign,
            decoded.regimeK,
            decoded.exponent.toInt(),
            decoded.fraction
        ).toFloat()
    }

    override fun toInt(): Int {
        val decoded = decodePosit(positChunk)
        return getDoubleRepresentation(
            decoded.sign,
            decoded.regimeK,
            decoded.exponent.toInt(),
            decoded.fraction
        ).toInt()
    }

    override fun toLong(): Long {
        val decoded = decodePosit(positChunk)
        return getDoubleRepresentation(
            decoded.sign,
            decoded.regimeK,
            decoded.exponent.toInt(),
            decoded.fraction
        ).toLong()
    }

    override fun toShort(): Short {
        val decoded = decodePosit(positChunk)
        return getDoubleRepresentation(
            decoded.sign,
            decoded.regimeK,
            decoded.exponent.toInt(),
            decoded.fraction
        ).toInt()
            .toShort()
    }

    private fun signedRepresentation(positValue: ULong): Int {
        val pV = positValue
        val signBit = (pV shr (NBITS - 1)) and 1u

        if (signBit == 1UL) {
            if (pV != INFINITY) {
                val setBit = (pV or (1UL shl (NBITS - 1))) xor (1UL shl (NBITS - 1))
                return -twosComplement(setBit, NBITS).toInt()
            }
        }

        return pV.toInt()
    }

    override operator fun compareTo(other: Posit): Int {
        return signedRepresentation(this.positChunk).compareTo(signedRepresentation(other.positChunk))
    }
    //endregion

    //region Posit construction
    private fun x2p(value: Float): ULong {
        if (value == 0f) {
            return 0UL
        }

        USEED = 1 shl (1 shl ES) //2^2^es
        var `val`: Float = abs(value)

        if (`val` < 1) {
            return southeastQ(value)
        }

        var expCounter = 2f.pow(ES - 1)
        var positVal: Double = 0.0
        var iter = 1
        //Northeast quadrant
        if (`val` >= 1) {
            positVal = 1.0
            iter = 2
            while (`val` >= USEED && iter < NBITS) {
                positVal = 2 * positVal + 1
                `val` /= USEED
                ++iter
            }
            positVal *= 2
            iter = 2
        }

        //Extract exponent bits
        while (expCounter > 1f / 2f && iter <= NBITS) {
            positVal *= 2
            if (`val` >= 2f.pow(expCounter)) {
                `val` /= 2f.pow(expCounter)
                positVal++
            }
            expCounter /= 2f
            iter++
        }
        `val`--

        //Extract fraction bits
        while (`val` > 0 && iter <= NBITS) {
            `val` *= 2
            positVal = 2 * positVal + `val`.toLong()
            `val` -= `val`.toInt()
            ++iter
        }

        positVal *= (1 shl (NBITS - iter + 1))
        ++iter
        iter = (positVal.toInt() and 1)
        positVal = (positVal / 2.0).toULong().toDouble()
        var resultPositVal = positVal.toULong()

        //If the length of the bits is overflowing
        while (resultPositVal > onesMask(NBITS - 1)) { //((1 shl NBITS - 1) - 1)
            resultPositVal = resultPositVal shr 1
        }


        resultPositVal = if (value < 0)
            twosComplement(resultPositVal, NBITS)
        else
            resultPositVal
//        if (iter == 0) {
//            println("\t0^ $resultPositVal ${resultPositVal.toString(2)}")
//        }
//        if (`val` == 1f) {
//            println("\tv^ ${resultPositVal - 1UL} ${(resultPositVal - 1UL).toString(2)}")
//        } else { //0f
//            println("\t+^ ${resultPositVal + 1UL} ${(resultPositVal + 1UL).toString(2)}")
//        }

//        return if (iter == 0) {
//            resultPositVal
//        } else if (`val` == 1f) {
//            resultPositVal - 1u
//        } else {    //0f
//            resultPositVal + 1u
//        }

        return resultPositVal
    }

    private fun constructFinal(regimeInfo: RegimeInfo, exponentInfo: ExponentInfo, fractionInfo: FractionInfo): ULong {

        var finalVal = if (regimeInfo.regimeK >= 0)
            onesMask(regimeInfo.regimeLen - 1) shl (NBITS - regimeInfo.regimeLen)
        else
            1UL shl (NBITS - 1 - regimeInfo.regimeLen)

        val shift =
            //The first stage consists only of exponent
            if (fractionInfo.fractionLen == 0)
                ES - (leastSignificantBitPosition(exponentInfo.exponent.toULong()) - 1)
            else ES + fractionInfo.fractionLen  //The state is with fraction

        val trail = NBITS - 1 - regimeInfo.regimeLen

        val expFrac =
            shiftNotUsedZeros(fractionInfo.fraction or (exponentInfo.exponent shl fractionInfo.fractionLen).toULong())

        if (trail < shift) {
            //Getting overflown bits
            val overflown = expFrac and onesMask(shift - trail)
            finalVal = finalVal or (expFrac shr (shift - trail))

            if (overflown == (1UL shl (shift - trail - 1))) {
                if (((expFrac shr (shift - trail)) and 1UL) == 1UL) {
                    finalVal += 1u
                }
            } else if (overflown > (1u shl (shift - trail - 1))) {
                finalVal++
            }
        } else {
            finalVal = finalVal or (expFrac shl (trail - shift))
        }

        return finalVal
    }

    private fun countScale(intRepresentation: Int): Int {
        return ((intRepresentation and ((1 shl 31) - 1)) shr 23) - 127
    }

    private fun countRegime(scale: Int): RegimeInfo {
        val regimeK: Int = scale shr ES
        //Конструируем число.
        val regimeBits = regimeBits(regimeK)
        //Длина режима - биты единиц и ноль или биты нулей и единица.
        val regimeLen = if (regimeK >= 0) regimeK + 2 else -regimeK + 1

        return RegimeInfo(regimeLen, regimeK, regimeBits)
    }

    private fun makePositValue(sign: Int, scale: Int, fraction: Int): ULong {
        if (fraction == 0) {
            return 0UL
        }
        val regimeInfo = countRegime(scale)
        //scale % 2^es
        val exponentInfo = ExponentInfo(
            ES.coerceAtMost(NBITS - 1 - regimeInfo.regimeLen).coerceAtLeast(0),
            scale and ((1 shl ES) - 1)
        )

        val fractionInfo = fractionBits(
            fraction.toULong()
        )

        val positVal = constructFinal(regimeInfo, exponentInfo, fractionInfo)

        return if (sign == 1) {
            twosComplement(positVal, NBITS)
        } else {
            positVal
        }
    }

    private fun southeastQ(value: Float): ULong {
        val intRepresentation = floatToIntBits(value)
        val exponentScale = countScale(intRepresentation)
        val regimeInfo = countRegime(exponentScale)
        val exponentInfo = ExponentInfo(
            ES.coerceAtMost(NBITS - 1 - regimeInfo.regimeLen).coerceAtLeast(0),
            exponentScale and ((1 shl ES) - 1)
        )

        val fractionInfo = fractionBits(
            (1UL shl 23) or (intRepresentation.toULong() and ((1UL shl 23) - 1UL))
        )

        val positVal = constructFinal(regimeInfo, exponentInfo, fractionInfo)

        return if (value < 0) {
            twosComplement(positVal, NBITS - 1)
        } else {
            positVal
        }
    }

    private fun signBit(value: Int): Byte = if (value > 0) 0 else 1

    private fun signBit(value: Float): Byte = if (value > 0) 0 else 1

    //Возвращает представление битов режима
    private fun regimeBits(runningK: Int): ULong {
        /* k-1 единиц и последний ноль, при положительном
        * k нулей и последняя единица, при отрицательном */
        val regimeBits = if (runningK > 0) {
            ((1UL shl (runningK + 1)) - 1UL) shl 1
        } else {
            /*получаем число вида 0..010...0. убираем ненужные нули, чтобы взять только режим
            * -> 0..01*/
            var mask = 1UL shl (NBITS - ES) + 1
            mask = mask shr (-runningK)
            mask shr (leastSignificantBitPosition(mask) - 1)
        }

        return regimeBits
    }

    private fun fractionBits(value: ULong): FractionInfo {
        //Удаляем лишние нули
        val lsb = leastSignificantBitPosition(value)
        var fractionBits = value shr (lsb - 1)

        val fractionLen = mostSignificantBitPosition(fractionBits) - 1//NBITS - 1 - regimeLen - exponentLen
        /*Удаляем hidden бит 1. Маска 1<<(frac_len)-1 - 1 */
        fractionBits = fractionBits and ((1UL shl (mostSignificantBitPosition(fractionBits) - 1)) - 1UL)
        return FractionInfo(fractionLen, fractionBits.coerceAtLeast(0UL))
    }

    private fun shiftNotUsedZeros(value: ULong): ULong {
        val lsb = leastSignificantBitPosition(value)
        return value shr (lsb - 1)
    }
    //endregion

    //region Posit decoding
    private fun decodePosit(positValue: ULong): PositRepr {
        var value = positValue
        if (value == 0UL) {
            return PositRepr(ZERO, 0, ZERO, ZERO)
        }
        if (value == INFINITY) {
            return PositRepr(1u, 0, ZERO, ZERO)
        }

        //Sign bit
        val signBit = (value shr (NBITS - 1)) and 1UL
        if (signBit == 1UL) {
            value = twosComplement(value, NBITS)
        }

        //Regime
        val regimeSign = ((value shr (NBITS - 2)) and 1UL).toInt()
        val regimeLen =
            if (regimeSign == 1) {
                //unset bit (1`s complement)
                NBITS - (mostSignificantBitPosition(
                    (twosComplement(
                        value,
                        mostSignificantBitPosition(value)
                    ) - 1u)
                ) - 1) - 1
            } else {
                NBITS - (mostSignificantBitPosition(value) - 1) - 1
            }
        val exponentLen = 0.coerceAtLeast(ES.coerceAtMost(NBITS - 1 - regimeLen))
        val fractionLen = 0.coerceAtLeast(NBITS - 1 - regimeLen - exponentLen)

        //Get values
        val regimeK = if (regimeSign == 0) -regimeLen + 1 else regimeLen - 2
        //Mask with 111.. and ..000 in the end.
        val exponent = (((onesMask(exponentLen) shl fractionLen) and value) shr fractionLen) shl (ES - exponentLen)

        val fracMask = (((onesMask(fractionLen)) and value) or ((1UL shl fractionLen)))
        val fraction = fracMask shr (leastSignificantBitPosition(fracMask) - 1)

        return PositRepr(
            signBit,
            regimeK,
            exponent,
            fraction
        )
    }

    private fun getDoubleRepresentation(sign: ULong, regimeK: Int, exponent: Int, fraction: ULong): Double {
        val nBits = mostSignificantBitPosition(fraction) - 1
        return (-1.0).pow(sign.toDouble()) * 2.0.pow(2.0.pow(1.0 * ES) * regimeK + exponent - nBits) * (fraction.toDouble())
    }
    //endregion

    //region Bit manipulation
    //возвращает позицию most significant bit
    private fun mostSignificantBitPosition(value: ULong): Int {
        var value = value
        var pos = 0
        while (value != 0UL) {
            value = value shr 1
            pos++
        }
        return pos
    }

    //Возвращает позицию least significant bit, но с обратной индексацией
    private fun leastSignificantBitPosition(value: ULong): Int {
        var mask = 1UL
        var pos = 0
        while (pos < 32 - 1) {
            val newVal = mask or value
//            ++pos
            if (newVal == value) {
                pos++
                break
            }
            ++pos
            mask = mask shl 1
        }
        return pos
    }

    //Создает битовый набор из len единиц
    private fun onesMask(len: Int) = (1UL shl len) - 1UL

    private fun twosComplement(bits: ULong, nbits: Int) = (onesMask(nbits) xor bits) + 1u // n-1
    //endregion
}