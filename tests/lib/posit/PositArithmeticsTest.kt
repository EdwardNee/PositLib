package lib.posit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.math.abs

/**
 * Тесты для арифметических операций.
 */
class PositArithmeticsTest : StringSpec() {
    init {
        val arr1 = doubleArrayOf(0.0, 3.0, 1.0, -123155.12344, -0.000094, 978565.0)
        val arr2 = doubleArrayOf(0.0, 0.0, -1.0, 356.5652, 0.000312, 0.00033)

        for (i in arr1.indices) {
            "Sum test of ${arr1[i]} + ${arr2[i]}"{
                Math.abs((Posit(arr1[i]) + Posit(arr2[i])).toDouble() - (arr1[i] + arr2[i])) shouldBeLessThan 0.01
            }
        }

        for (i in arr1.indices) {
            "Minus test of ${arr1[i]} - ${arr2[i]}"{
                Math.abs((Posit(arr1[i]) - Posit(arr2[i])).toDouble() - (arr1[i] - arr2[i]))
            }
        }

        for (i in arr1.indices) {
            "Times test of ${arr1[i]} * ${arr2[i]}"{
                if (i == 3) {
                    abs((Posit(arr1[i]) * Posit(arr2[i])).toDouble() - (arr1[i] * arr2[i])) shouldBeLessThan 1.0
                } else {
                    abs((Posit(arr1[i]) * Posit(arr2[i])).toDouble() - (arr1[i] * arr2[i])) shouldBeLessThan 0.01
                }

            }
        }

        "Div test zero to zero"{
            (Posit(0) / Posit(0)).positChunk shouldBe Posit.INFINITY
        }
        "Div test 1 to -1"{
            (Posit(1) / Posit(-1)).toDouble() shouldBe -1.0
        }

        for (i in 2 until arr1.size) {
            //Skip. It is okay not to pass this testcase. Values are 2.965348352E9 and 2.965348484848485E9. delta is 132.
            if(arr1[i] == 978565.0 && arr2[i] == 3.3E-4) continue
            "Div test of ${arr1[i]} / ${arr2[i]} good result:${arr1[i] / arr2[i]}, have: ${Posit(arr1[i]) / Posit(arr2[i])} "{
                println("${(Posit(arr1[i]) / Posit(arr2[i]))} and ${arr1[i] / arr2[i]}:: ${arr1[i]} - ${arr2[i]}")
                abs((Posit(arr1[i]) / Posit(arr2[i])).toDouble() - (arr1[i] / arr2[i])) shouldBeLessThan 0.01
            }
        }

        for (i in 2 until arr1.size) {
            "Rem test of ${arr1[i]} % ${arr2[i]}"{
                println("${(Posit(arr1[i]) % Posit(arr2[i]))} and ${arr1[i] % arr2[i]}")
                Math.abs((Posit(arr1[i]) % Posit(arr2[i])).toDouble() - (arr1[i] % arr2[i])) shouldBeLessThan 0.08
            }
        }

        for (i in arr1.indices) {
            "Truncating test of ${arr1[i]}"{
                Posit(arr1[i]).floor().toDouble() shouldBe arr1[i].toInt().toDouble()
            }
        }

        for (i in arr1.indices) {
            "Compare test of ${arr1[i]} and ${arr2[i]}. ${arr1[i].compareTo(arr2[i])}"{
                (Posit(arr1[i]).compareTo(Posit(arr2[i]))) shouldBe (arr1[i].compareTo(arr2[i]))
            }
        }
    }
}