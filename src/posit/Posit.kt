package posit

import java.lang.Float.floatToIntBits
import kotlin.math.abs
import kotlin.math.pow

private data class RegimeInfo(val regimeLen: Int, val regimeK: Int, val regimeBits: Int)
private data class ExponentInfo(var exponentLen: Int, var exponent: Int)
private data class FractionInfo(var fractionLen: Int, var fraction: Int)

//32 bits
public class Posit /*: Number()*/ {
    companion object {
        //region Constants
        val INFINITY: UInt = (Int.MAX_VALUE.toUInt() + 1u) //0x0x80000000
        const val ZERO: UInt = 0u //0x00000000
        const val ONE_POSITIVE: UInt = 1073741824u //0x40000000
        const val ONE_NEGATIVE: UInt = 3221225472u //0xC0000000
        //endregion

        //region Posit structure
        const val NBITS = 7
        const val ES = 3
        var USEED = -1
        //endregion
    }

    //Для целых чисел
    constructor(value: Int) {
        if (value == 0)
            return
        val sign = signBit(value)
        //Получаем экспоненту (для целых берем количество битов)
        var exponent = mostSignificantBitPosition(value) - 1

//        makePositValue(value, sign, exponent)

    }

    constructor(value: Float) {
        println(value)
        var signBit: Byte = 0
        var exponent = 0
        var fractionBits = 0

        if (value == 0f)
            return
        if (value.isInfinite() or value.isNaN()) {
            //posit = inf
            return
        }

        val intRepresentation = floatToIntBits(value)
        signBit = signBit(value)//if ((intRepresentation.toUInt() and INFINITY) == 0U) 0 else 1

        //Сдвигаем и вычитаем биас.
        exponent = ((intRepresentation and ((1 shl 31) - 1)) shr 23) - 127
//        exponent = intRepresentation and (((intRepresentation shl 1) shr 24) - 127)

        //Добавляю скрытый бит к маске float fraction
        fractionBits = (1 shl 23) or (intRepresentation and ((1 shl 23) - 1))
//        print("F ${exponent} \n")
//        makePositValue(fractionBits, signBit, exponent)
    }

    //region Posit construction
    fun x2p(value: Float) {
        if (value == 0f) {
            println(0)
            return
        }

        USEED = 1 shl (1 shl ES) //2^2^es
        var `val`: Float = abs(value)

        if (`val` < 1) {
            southeastQ(value)
            return
        }

        var expCounter = 2f.pow(ES - 1)
        var positVal = 0f
        var iter = 1
        //Northeast quadrant
        if (`val` >= 1) {
            positVal = 1f
            iter = 2
            while (`val` >= USEED && iter < NBITS) {
                positVal = 2 * positVal + 1
                `val` /= USEED
                ++iter
            }
            positVal *= 2
            iter = 2
        }
//        else {//Southeast quadrant
//            while (`val` < 1 && iter <= NBITS) {
//                `val` *= USEED
//                ++iter
//            }
//            if (iter >= NBITS) {
//                positVal = 2f
//                iter = NBITS + 1
//                positVal = 1f
//                iter++
//            }
//        }

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
            positVal = 2 * positVal + `val`.toInt()
            `val` -= `val`.toInt()
            ++iter
        }

        positVal *= (1 shl (NBITS - iter + 1))
        ++iter
        iter = (positVal.toInt() and 1)
        positVal = (positVal / 2).toInt().toFloat()
        var resultPositVal = positVal.toInt()
//        if (bigger) {
        //If the length of the bits is overflowing
        //TODO -1 убрать кажется надо
        while (resultPositVal > onesMask(NBITS - 1)) { //((1 shl NBITS - 1) - 1)
            resultPositVal = resultPositVal shr 1
        }
//        }

        resultPositVal = if (value < 0) twosComplement(resultPositVal.toUInt(), NBITS - 1).toInt() else resultPositVal
        if (iter == 0) {
            println("\t0^ $resultPositVal ${resultPositVal.toString(2)}")
        } else if (`val` == 1f || `val` == 0f) {
            println("\tv^ ${resultPositVal - 1} ${(resultPositVal - 1).toString(2)}")
        } else {
            println("\t+^ ${resultPositVal + 1} ${(resultPositVal + 1).toString(2)}")
        }
    }

    private fun constructFinal(regimeInfo: RegimeInfo, exponentInfo: ExponentInfo, fractionInfo: FractionInfo): Int {
        var finalVal = fractionInfo.fraction
//        print("LSB ${leastSignificantBitPosition(exponentInfo.exponent)} ")
        val shift =
            //The first stage consists only of exponent
            if (fractionInfo.fractionLen == 0)
                ES - (leastSignificantBitPosition(exponentInfo.exponent) - 1)
            else ES + fractionInfo.fractionLen  //The state is with fraction


        val trail = NBITS - 1 - regimeInfo.regimeLen
//        println((regimeInfo.regimeBits shl (fractionInfo.fractionLen + exponentInfo.exponentLen)))
        finalVal =
            shiftNotUsedZeros(finalVal or (exponentInfo.exponent shl fractionInfo.fractionLen)) shl (trail - shift)


        finalVal =
            finalVal or (1 shl (NBITS - 1 - regimeInfo.regimeLen))//(regimeInfo.regimeBits shl (fractionInfo.fractionLen + exponentInfo.exponentLen))
        return finalVal
    }

    private fun countScale(value: Float): Int {
        val intRepresentation = floatToIntBits(value)
        return ((intRepresentation and ((1 shl 31) - 1)) shr 23) - 127
    }

    private fun countRegime(scale: Int): RegimeInfo {
        var regimeK: Int = scale shr ES
        //Конструируем число.
        var regimeBits = regimeBits(regimeK)
        //Длина режима - биты единиц и ноль или биты нулей и единица.
        var regimeLen = if (regimeK >= 0) regimeK + 2 else -regimeK + 1

        return RegimeInfo(regimeLen, regimeK, regimeBits)
    }

    private fun makePositValue(value: Int, sign: Byte, exponent: Int) {

//        print("EXP ${exponent}")
        //Regime - это масштабирование на 2^es. Получается, просто делим на левый сдвиг.
        var regimeK: Int = exponent shr ES//(exponent) / (1 shl ES)
//        print("exp ${exponent}  ${(1 shl ES)} ${(exponent) % (1 shl ES)}")
//        print("exp ${exponent and (1 shl ES)} ${(1 shl ES)}")
        var exponent = (exponent) and ((1 shl ES) - 1)

        //Конструируем число.
        var regimeBits = regimeBits(regimeK)
        //Длина режима - биты единиц и ноль или биты нулей и единица.
        var regimeLen = if (regimeK >= 0) regimeK + 2 else -regimeK + 1
        //fraction
        var fractionBits = fractionBits(value, regimeLen, 555)

//        println("Posit: r.$regimeLen e.$exponent f.$fractionBits")

        //Remained length of bits for exp and fraction
        var expFracBitsLen = NBITS - regimeLen - 1
//        var fractionLen =


//        println("\trl: $regimeLen es: $ES fl: $fractionLen")
    }

    private fun southeastQ(value: Float) {
        val exponentScale = countScale(value)
        val regimeInfo = countRegime(exponentScale)
        val exponentInfo = ExponentInfo(
            ES.coerceAtMost(NBITS - 1 - regimeInfo.regimeLen).coerceAtLeast(0),
            exponentScale and ((1 shl ES) - 1)
        )

        val intRepresentation = floatToIntBits(value)
        val fractionInfo = fractionBits(
            (1 shl 23) or (intRepresentation and ((1 shl 23) - 1)),
            regimeInfo.regimeLen,
            exponentInfo.exponentLen
        )

        //reduce overflow
//        while (exponentInfo.exponent > ((1 shl exponentInfo.exponentLen) - 1)) {
//            exponentInfo.exponent = exponentInfo.exponent shr 1
//        }
        println("r.${regimeInfo.regimeLen} e.${exponentInfo.exponent} f.${fractionInfo.fractionLen}")
        val positVal = constructFinal(regimeInfo, exponentInfo, fractionInfo)

        if (value < 0) {
            println((twosComplement(positVal.toUInt(), NBITS - 1)).toString(2))
        } else {
            println(positVal.toString(2))
        }
    }

    private fun signBit(value: Int): Byte = if (value > 0) 0 else 1

    private fun signBit(value: Float): Byte = if (value > 0) 0 else 1

    //Возвращает представление битов режима
    private fun regimeBits(runningK: Int): Int {
        /* k-1 единиц и последний ноль, при положительном
        * k нулей и последняя единица, при отрицательном */
        var regimeBits = if (runningK > 0) {
            ((1 shl (runningK + 1)) - 1) shl 1
        } else {
            /*получаем число вида 0..010...0. убираем ненужные нули, чтобы взять только режим
            * -> 0..01*/

            var mask = 1 shl (NBITS - ES) + 1
            mask = mask shr (-runningK)
            mask shr (leastSignificantBitPosition(mask) - 1)

        }

        return regimeBits
    }

    private fun fractionBits(value: Int, regimeLen: Int, exponentLen: Int): FractionInfo {
        //Удаляем лишние нули
        val lsb = leastSignificantBitPosition(value)
        var fractionBits = value shr (lsb - 1)

        val fractionLen = mostSignificantBitPosition(fractionBits) - 1//NBITS - 1 - regimeLen - exponentLen
        /*Удаляем hidden бит 1. Маска 1<<(frac_len)-1 - 1 */
        fractionBits = fractionBits and ((1 shl (mostSignificantBitPosition(fractionBits) - 1)) - 1)
        return FractionInfo(fractionLen, fractionBits.coerceAtLeast(0))
    }

    private fun shiftNotUsedZeros(value: Int): Int {
        val lsb = leastSignificantBitPosition(value)
        return value shr (lsb - 1)
    }
    //endregion

    //region Posit decoding
     fun decodePosit(positValue: UInt) {
        var value = positValue
        if (value == 0u)
            return //0 all the bit types
        if (value == INFINITY)
            return
        //Sign bit
        val signBit = (value shr (NBITS - 1)) and 1u
        if (signBit == 1u) {
            value = twosComplement(value, NBITS)
        }

        //Regime
        val regimeSign = ((value shr (NBITS - 2)) and 1u).toInt()
        val regimeLen =
            if (regimeSign == 1) {
                //unset bit (1`s complement)
                NBITS - (mostSignificantBitPosition(
                    (twosComplement(
                        value,
                        mostSignificantBitPosition(value.toInt())
                    ) - 1u).toInt()
                ) - 1) - 1
            } else {
                NBITS - (mostSignificantBitPosition(value.toInt()) - 1) - 1
            }
        val exponentLen = 0.coerceAtLeast(ES.coerceAtMost(NBITS - 1 - regimeLen))
        val fractionLen = 0.coerceAtLeast(NBITS - 1 - regimeLen - exponentLen)

        //Get values
        val regimeK = if(regimeSign == 0) -regimeLen + 1 else regimeLen - 2
        //Mask with 111.. and ..000 in the end.
        val exponent = ((onesMask(exponentLen) shl fractionLen).toUInt() and value) shr fractionLen

        val fracMask = (((onesMask(fractionLen)).toUInt() and value) or ((1 shl fractionLen).toUInt())).toInt()
        val fraction =  fracMask shr (leastSignificantBitPosition(fracMask) - 1)


        println("Decoded r.$regimeK e.$exponent f.$fraction")
        println(getDoubleRepresentation(signBit, regimeK, exponent.toInt(), fraction))
    }

    fun getDoubleRepresentation(sign: UInt, regimeK: Int, exponent: Int, fraction: Int): Double{
        val nBits = mostSignificantBitPosition(fraction) - 1
        return (-1.0).pow(sign.toDouble()) * 2.0.pow(2.0.pow(1.0 * ES) * regimeK + exponent - nBits) * (1.0 * fraction)
    }
    //endregion

    //region Bit manipulation
    //возвращает позицию most significant bit
    private fun mostSignificantBitPosition(value: Int): Int {
        var value = if (value < 0) -value else value
        var pos: Int = 0
        while (value != 0) {
            value = value shr 1
            pos++
        }
        return pos
    }

    //Возвращает позицию least significant bit, но с обратной индексацией
    private fun leastSignificantBitPosition(value: Int): Int {
        var mask = 1
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
    private fun onesMask(len: Int) = (1 shl len) - 1

    private fun twosComplement(bits: UInt, nbits: Int) = (onesMask(nbits).toUInt() xor bits) + 1u // n-1
    //endregion
}