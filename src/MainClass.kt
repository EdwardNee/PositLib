import lib.posit.Posit
import java.lang.Double.doubleToLongBits
import java.math.BigInteger
import kotlin.reflect.jvm.internal.impl.builtins.StandardNames.FqNames.number


class MainClass {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val posit1 = Posit(11.1)
            val posit2 = Posit(0)
            println("${posit1 / posit2}")

        }
    }
}


