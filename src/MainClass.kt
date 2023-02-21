import lib.posit.Posit


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


