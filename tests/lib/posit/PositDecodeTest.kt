package lib.posit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PositDecodeTest : StringSpec() {
    init {
        val array = floatArrayOf(0f, 1f, 113f, 1923f, 0.003456f, 12312.9899f, -1f, -1321.11f)
        for (i in array.indices){
            "Decode to double"{
                Posit(array[i]).toDouble() shouldBe array[i]
            }
        }
        for (i in array.indices){
            "Decode to float"{
                Posit(array[i]).toFloat() shouldBe array[i]
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
