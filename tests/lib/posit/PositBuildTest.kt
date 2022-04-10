package lib.posit

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class PositBuildTest : StringSpec() {
    init {
        "Using float constructor test" {
            forAll(
                row(0f, 0UL),
                row(1f, 1073741824UL),
                row(0.003456f, 260700220UL),
                row(-0.000136f, 4175612302UL),
                row(1.21312312f, 1102346720UL),
                row(113f, 1796210688UL),
                row(-1321.11f, 2339074540UL),
                row(Float.POSITIVE_INFINITY, Posit.INFINITY),
                row(Float.NEGATIVE_INFINITY, Posit.INFINITY)
            ) { value, positChunk ->
                Posit(value).positChunk shouldBe positChunk
            }
        }

        "Test int constructor"{
            forAll(
                row(1, 1073741824UL),
                row(0, 0UL),
                row(-57889, 2218507776UL),
                row(57889, 2076459520UL),
                row(214748364, 2138256179UL)
            ) { value, positChunk ->
                Posit(value).positChunk shouldBe positChunk
            }
        }
    }
}