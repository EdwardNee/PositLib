/**
 * @author <a href="mailto:eni@edu.hse.ru"> Eduard Ni</a>
 */
package lib.helpers

/**
 * Класс для хранения информации о битах режима.
 */
internal data class RegimeInfo(
    val regimeLen: Int,
    val regimeK: Int,
    val regimeBits: ULong
)

/**
 * Класс для хранения информации о битах экспоненты.
 */
internal data class ExponentInfo(
    var exponentLen: Int,
    var exponent: Int
)

/**
 * Класс для хранения информации о битах дробной части.
 */
internal data class FractionInfo(
    var fractionLen: Int,
    var fraction: ULong
)

/**
 * Класс для хранения информации о представлении числа Posit.
 */
data class PositRepr(
    var sign: ULong,
    var regimeK: Int,
    var exponent: ULong,
    var fraction: ULong
)