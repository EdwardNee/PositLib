package posit

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
        const val NBITS = 32
        const val ES = 3
        //endregion
    }

    //Для целых чисел
    constructor(value: Int) {
        //Получаем экспоненту (для целых берем количество битов)
        var exponent = bitsLength(value) - 1
        //Regime - это масштабирование на 2^es. Получается, просто делим на левый сдвиг.
        var regimeK: Int = exponent / (1 shl ES)

        //Конструируем число.
        var regimeBits = regimeBits(regimeK)


    }

    //возвращает позицию most significant bit
    private fun bitsLength(value: Int): Int {
        var value = value
        var pos: Int = 0
        while (value != 0) {
            value = value shr 1
            pos++
        }
        return pos
    }

    //Возвращает представление битов режима
    private fun regimeBits(runningK: Int): Int {
        /* k-1 единиц и последний ноль, при положительном
        * k нулей и последняя единица, при отрицательном */
        return if (runningK > 0) {
            1 shl (runningK + 1) - 1
        } else {
            1 shr -runningK
        }
    }


}