/**
 * @author <a href="mailto:eni@edu.hse.ru"> Eduard Ni</a>
 */
package lib.helpers

/**
 * Синглтон для представления манимуляций с битами.
 */
object BitHelper {
    //region Bit manipulation
    /**
     * Подсчитывает и возвращает позицию most significant bit.
     * @return Возвращает позицию most significant bit.
     */
    public fun mostSignificantBitPosition(value: ULong): Int {
        var value = value
        var pos = 0
        while (value != 0UL) {
            value = value shr 1
            pos++
        }
        return pos
    }

    /**
     * Подсчитывает и возвращает позицию least significant bit.
     * @return Возвращает позицию least significant bit.
     */
    public fun leastSignificantBitPosition(value: ULong): Int {
        if (value == 0UL){
            return 0
        }
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

    /**
     * Возвращает битовый набор из [len] единиц.
     * @return Возвращает битовый набор из [len] единиц.
     */
    public fun onesMask(len: Int)
    = (1UL shl len) - 1UL

    /**
     * Возвращает дополнительный код для представления [bits] из [nbits] битов.
     * @return Возвращает дополнительный код для представления [bits] из [nbits] битов.
     */
    public fun twosComplement(bits: ULong, nbits: Int)
    = (onesMask(nbits) xor bits) + 1u // n-1

    /**
     * Сдвигает позадистоящие ненужные биты нулей.
     * @return возвращает представление без задних нулевых битов.
     */
    public fun shiftNotUsedZeros(value: ULong): ULong {
        val lsb = BitHelper.leastSignificantBitPosition(value)
        return value shr (lsb - 1)
    }
    //endregion
}
