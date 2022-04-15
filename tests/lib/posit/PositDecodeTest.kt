package lib.posit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.math.abs

/**
 * Тесты для декодирования числа Posit.
 */
class PositDecodeTest : StringSpec() {
    init {
        val array = doubleArrayOf(0.0, 1.0, 113.0, 1923.0, 0.003456, 12312.9899, -1.0, -1321.11)
        for (i in array.indices){
            "Decode to double"{
                abs(Posit(array[i]).toDouble() - array[i]) shouldBeLessThan 0.01
            }
        }
        for (i in array.indices){
            "Decode to float"{
                abs(Posit(array[i]).toFloat() - array[i].toFloat()) shouldBeLessThan 0.01f
            }
        }
        for (i in array.indices){
            "Decode to int"{
                Posit(array[i]).toInt() shouldBe array[i].toInt()
            }
        }
        for (i in array.indices){
            "Decode to long"{
                Posit(array[i]).toLong() shouldBe array[i].toLong()
            }
        }

    }
}
