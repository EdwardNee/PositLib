import posit.Posit
import java.lang.RuntimeException
import java.lang.Float.*

typealias tt<T> = (T) -> Boolean
typealias itt = Int // define

class MainClass {


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            var v = 4

            val s = floatArrayOf(0.0625f, 0.078125f, 0.09375f, 0.109375f, 0.125f, 0.15625f,
                0.1875f, 0.21875f,  0.25f, 0.28125f, 0.3125f, 0.34375f, 0.375f, 0.40625f, 0.4375f,
                0.46875f, 0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f)
            for (ss in s){
                Posit(1).x2p(ss)
            }
//            print(0xff and 2.inv())
//            Posit(1)
//            Posit(6)
//            Posit(12)
//            for (i in 1..16) {
//                print("$i ")
//                Posit(i)
//            }
        }
    }
}


