package posit

import java.lang.Float.floatToIntBits
import kotlin.math.pow


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
        const val ES = 1
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

        //Добавляю скрытый бит к маске float fraction
        fractionBits = (1 shl 23) or (intRepresentation and ((1 shl 23) - 1))

        makePositValue(fractionBits, signBit, exponent)
    }

    fun x2p(value: Float) {
        println(value)
        var bigger: Boolean = false
        USEED = 1 shl (1 shl ES)//2^2^es
        var value: Float = value.toFloat()

        var expCounter = 2f.pow(ES - 1)
        var positVal = 0f
        var iter = 1
        //Northeast quadrant
        if (value >= 1) {
            bigger = true
            positVal = 1f
            iter = 2
            while (value >= USEED && iter < NBITS) {
                positVal = 2 * positVal + 1
                value /= USEED
                ++iter
            }
            positVal *= 2
            iter = 2
        }
        else{//Southeast quadrant
            while (value < 1 && iter <= NBITS){
                value *= USEED
                ++iter
            }
            if (iter >= NBITS){
                positVal = 2f
                iter = NBITS + 1
                positVal = 1f
                iter++
            }
        }

        //Extract exponent bits
        while (expCounter > 1f/2f && iter <= NBITS){
            positVal *= 2
            if (value >= 2f.pow(expCounter)){
                value /= 2f.pow(expCounter)
                positVal++
            }
            expCounter /= 2f
            iter++
        }
        value--

        //Extract fraction bits
        while (value > 0 && iter <= NBITS){
            value *= 2
            positVal = 2 * positVal + value.toInt()
            value -= value.toInt()
            ++iter
        }

        positVal *= (1 shl (NBITS - iter + 1))
        ++iter
        iter = (positVal.toInt() and 1)
        positVal = (positVal / 2).toInt().toFloat()
        var resultPositVal = positVal.toInt()
        if(bigger){
            //If the length of the bits is overflowing
            println(((1 shl NBITS - 1) - 1))
            while (resultPositVal > ((1 shl NBITS - 1) - 1)){
                resultPositVal = resultPositVal shr 1
            }
        }
        else{
            //Count the regime bits, N - 1 - ES - regimeLen
            resultPositVal = resultPositVal shr 1
            //resultPositVal consists of exp_bits and fraction bits
        }

        if(iter == 0){
            println("\t0 $resultPositVal ${resultPositVal.toString(2)}")
        }
        else if (value == 1f || value == 0f){
            println("\tv ${resultPositVal - 1} ${(resultPositVal - 1).toString(2)}")
        }else{
            println("\t+ ${resultPositVal + 1} ${(resultPositVal + 1).toString(2)}")
        }
    }

    private fun makePositValue(value: Int, sign: Byte, exponent: Int) {

        //Regime - это масштабирование на 2^es. Получается, просто делим на левый сдвиг.
        var regimeK: Int = exponent / (1 shl ES)
        var exponent = exponent % (1 shl ES)

        //Конструируем число.
        var regimeBits = regimeBits(sign, regimeK)
        //Длина режима - биты единиц и ноль или биты нулей и единица.
        var regimeLen = if (regimeK >= 0) regimeK + 2 else -regimeK + 1
        //fraction
        var fractionBits = fractionBits(sign, value, regimeLen)

        println("Posit: $regimeLen $exponent $fractionBits")

        //Remained length of bits for exp and fraction
        var expFracBitsLen = NBITS - regimeLen - 1
//        var fractionLen =


//        println("\trl: $regimeLen es: $ES fl: $fractionLen")
    }


    private fun signBit(value: Int): Byte = if (value > 0) 0 else 1

    private fun signBit(value: Float): Byte = if (value > 0) 0 else 1

    //Возвращает представление битов режима
    private fun regimeBits(signBit: Byte, runningK: Int): Int {
        /* k-1 единиц и последний ноль, при положительном
        * k нулей и последняя единица, при отрицательном */
        var regimeBits = if (runningK > 0) {
            ((1 shl (runningK + 1)) - 1) shl 1
        } else {
            /*получаем число вида 0..010...0. убираем ненужные нули, чтобы взять только режим
            * -> 0..01*/
            var mask = 1 shl (NBITS - ES)
            mask = mask shr (-runningK)
            mask shr (leastSignificantBitPosition(mask) - 1)
        }

        return if (signBit == 0.toByte()) regimeBits else (regimeBits.inv())
    }

    private fun fractionBits(sign: Byte, value: Int, regimeLen: Int): Int {
        var fractionLen = NBITS - 1 - regimeLen - ES

        //Удаляем лишние нули
        val lsb = leastSignificantBitPosition(value)
        var fractionBits = value shr (lsb - 1)
        /*Удаляем hidden бит 1. Маска 1<<(frac_len)-1 - 1 */
        fractionBits = fractionBits and ((1 shl (mostSignificantBitPosition(fractionBits) - 1)) - 1)
        return if (sign == 0.toByte()) fractionBits else ((fractionBits.inv() and onesBit(fractionLen)) + 1)
    }

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
        while (pos < NBITS - 1) {
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

    //Создает битовый набор из shift единиц
    private fun onesBit(shift: Int): Int = 1 shl (shift + 1) - 1
    //endregion
}