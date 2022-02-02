package posit

import java.lang.Integer.max


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
        //endregion
    }

    //Для целых чисел
    constructor(value: Int) {
        //Получаем экспоненту (для целых берем количество битов)
        var exponent = mostSignificantBitPosition(value) - 1
        //Regime - это масштабирование на 2^es. Получается, просто делим на левый сдвиг.
        var regimeK: Int = exponent / (1 shl ES)
        exponent %= (1 shl ES)

        //Конструируем число.
        var regimeBits = regimeBits(regimeK)
        //Длина режима - биты единиц и ноль или биты нулей и единица.
        var regimeLen = if (regimeK >= 0) regimeK + 2 else -regimeK + 1

        var fractionLen = NBITS - 1 - regimeLen - ES
        //Удаляем лишние нули
        var fractionBits = value shl (leastSignificantBitPosition(value))
        /*Удаляем спрятанный бит 1. Маска 1<<(frac_len)-1 - 1 */
        fractionBits = fractionBits and (1 shl (mostSignificantBitPosition(fractionBits)-1)) -1
    }

    //Возвращает представление битов режима
    private fun regimeBits(runningK: Int): Int {
        /* k-1 единиц и последний ноль, при положительном
        * k нулей и последняя единица, при отрицательном */
        return if (runningK > 0) {
            ((1 shl (runningK + 1)) - 1) shl 1
        } else {
            /*получаем число вида 0..010...0. убираем ненужные нули, чтобы взять только режим
            * -> 0..01*/
            var mask = 1 shl (NBITS - ES)
            mask = mask shr -runningK
            mask shr leastSignificantBitPosition(mask)
        }
    }

    //region Bit manipulation
    //возвращает позицию most significant bit
     fun mostSignificantBitPosition(value: Int): Int {
        var value = value
        var pos: Int = 0
        while (value != 0) {
            value = value shr 1
            pos++
        }
        return pos
    }

    //Возвращает позицию least significant bit, но с обратной индексацией
    fun leastSignificantBitPosition(value: Int): Int {
        var mask = 1
        var pos = 0
        while (pos < NBITS - 1){
            val newVal = mask or value
            if (newVal == value){
                if (pos == 0)
                    pos++
                break
            }
            mask = mask shl pos++
        }
        return pos
    }
    //endregion
}