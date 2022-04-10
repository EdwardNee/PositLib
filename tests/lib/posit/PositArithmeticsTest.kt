package lib.posit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.math.floor

/**
 * Тесты для арифметических операций.
 */
class PositArithmeticsTest : StringSpec() {
    init {
        val arr1 = floatArrayOf(0f, 3f, 1f, -123155.12344f,-0.000094f, 978565f, Float.POSITIVE_INFINITY)
        val arr2 = floatArrayOf(0f, 0f, -1f, 356.5652f, 0.000312f, 0.00033f, 3f)

        for (i in arr1.indices){
            "Sum test of ${arr1[i]} + ${arr2[i]}"{
                (Posit(arr1[i]) + Posit(arr2[i])).toDouble() shouldBe (arr1[i] + arr2[i]).toDouble()
            }
        }

        for (i in arr1.indices){
            "Minus test of ${arr1[i]} - ${arr2[i]}"{
                (Posit(arr1[i]) - Posit(arr2[i])).toDouble() shouldBe (arr1[i] - arr2[i]).toDouble()
            }
        }

        for (i in arr1.indices){
            "Times test of ${arr1[i]} * ${arr2[i]}"{
                (Posit(arr1[i]) * Posit(arr2[i])).toDouble() shouldBe (arr1[i] * arr2[i]).toDouble()
            }
        }

        for (i in arr1.indices){
            "Div test of ${arr1[i]} / ${arr2[i]}"{
                (Posit(arr1[i]) / Posit(arr2[i])).toDouble() shouldBe (arr1[i] / arr2[i]).toDouble()
            }
        }

        for (i in arr1.indices){
            "Rem test of ${arr1[i]} % ${arr2[i]}"{
                (Posit(arr1[i]) % Posit(arr2[i])).toDouble() shouldBe (arr1[i] % arr2[i]).toDouble()
            }
        }

        for (i in arr1.indices){
            "Floor test of ${arr1[i]}"{
                Posit(arr1[i]).floor().toDouble() shouldBe floor(arr1[i].toDouble())
            }
        }

        for (i in arr1.indices){
            "Compare test of ${arr1[i]} and ${arr2[i]}"{
                (Posit(arr1[i]).compareTo(Posit(arr2[i]))) shouldBe (arr1[i].compareTo(arr2[i]))
            }
        }
    }
}