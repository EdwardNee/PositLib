package lib.helpers

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

/**
 * Класс для тестирования операций с битовыми манипуляциями.
 */
class BitHelperTest : StringSpec() {
    init {
        "testing MSB counting function" {
            forAll(
                row(4UL, 3),
                row(1UL, 1),
                row(0UL, 0),
                row(10UL, 4),
                row(127UL, 7),
            ) { value, result ->
                assertSoftly {
                    withClue("first ones bit:") {
                        BitHelper.mostSignificantBitPosition(value) shouldBe result
                    }
                }
            }
        }

        "testing LSB function"{
            forAll(
                row(4UL, 3),
                row(1UL, 1),
                row(0UL, 0),
                row(10UL, 2),
                row(127UL, 1),
            ) { value, result ->
                assertSoftly {
                    withClue("last ones bit:") {
                        BitHelper.leastSignificantBitPosition(value) shouldBe result
                    }
                }
            }
        }

        for (i in 0..30 step 3) {
            "The mask for $i should be ${(1UL shl i) - 1UL}"{
                BitHelper.onesMask(i) shouldBe (1UL shl i) - 1UL
            }
        }

        "twosComplement testing" {
            for (i in 0 until 11) {
                BitHelper.twosComplement(i.toULong(), 7) shouldBe (128 - i).toULong()
            }
        }

        "shiftNotUsedZeros testing"{
            forAll(
                row(4UL, 1UL),
                row(5UL, 5UL),
                row(0UL, 0UL),
                row(80UL, 5UL)
            ){
                value, result ->
                BitHelper.shiftNotUsedZeros(value) shouldBe result
            }
        }
    }
}