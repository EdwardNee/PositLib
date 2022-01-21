package posit

//32 bits
public class Posit constructor(val es: Int) /*: Number()*/ {
    companion object {
        val INFINITY: UInt = (Int.MAX_VALUE.toUInt() + 1u) //0x0x80000000
        const val ZERO: UInt = 0u //0x00000000
        const val ONE_POSITIVE: UInt = 1073741824u //0x40000000
        const val ONE_NEGATIVE: UInt = 3221225472u //0xC0000000
        const val NBITS = 32

        const val ES = 3
    }




}