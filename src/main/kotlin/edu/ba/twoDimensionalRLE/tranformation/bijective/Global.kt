package edu.ba.twoDimensionalRLE.tranformation.bijective

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.experimental.and


object Global {
    const val INFINITE_VALUE = 0
    // array with 256 elements: int(Math.log2(x-1))
    val LOG2 = intArrayOf(
        0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5,
        5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
        5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8
    )
    // array with 256 elements: 4096*Math.log2(x)
    private val LOG2_4096 = intArrayOf(
        0, 0, 4096, 6492, 8192, 9511, 10588, 11499, 12288, 12984,
        13607, 14170, 14684, 15157, 15595, 16003, 16384, 16742, 17080, 17400,
        17703, 17991, 18266, 18529, 18780, 19021, 19253, 19476, 19691, 19898,
        20099, 20292, 20480, 20662, 20838, 21010, 21176, 21338, 21496, 21649,
        21799, 21945, 22087, 22226, 22362, 22495, 22625, 22752, 22876, 22998,
        23117, 23234, 23349, 23462, 23572, 23680, 23787, 23892, 23994, 24095,
        24195, 24292, 24388, 24483, 24576, 24668, 24758, 24847, 24934, 25021,
        25106, 25189, 25272, 25354, 25434, 25513, 25592, 25669, 25745, 25820,
        25895, 25968, 26041, 26112, 26183, 26253, 26322, 26390, 26458, 26525,
        26591, 26656, 26721, 26784, 26848, 26910, 26972, 27033, 27094, 27154,
        27213, 27272, 27330, 27388, 27445, 27502, 27558, 27613, 27668, 27722,
        27776, 27830, 27883, 27935, 27988, 28039, 28090, 28141, 28191, 28241,
        28291, 28340, 28388, 28437, 28484, 28532, 28579, 28626, 28672, 28718,
        28764, 28809, 28854, 28898, 28943, 28987, 29030, 29074, 29117, 29159,
        29202, 29244, 29285, 29327, 29368, 29409, 29450, 29490, 29530, 29570,
        29609, 29649, 29688, 29726, 29765, 29803, 29841, 29879, 29916, 29954,
        29991, 30027, 30064, 30100, 30137, 30172, 30208, 30244, 30279, 30314,
        30349, 30384, 30418, 30452, 30486, 30520, 30554, 30587, 30621, 30654,
        30687, 30719, 30752, 30784, 30817, 30849, 30880, 30912, 30944, 30975,
        31006, 31037, 31068, 31099, 31129, 31160, 31190, 31220, 31250, 31280,
        31309, 31339, 31368, 31397, 31426, 31455, 31484, 31513, 31541, 31569,
        31598, 31626, 31654, 31681, 31709, 31737, 31764, 31791, 31818, 31846,
        31872, 31899, 31926, 31952, 31979, 32005, 32031, 32058, 32084, 32109,
        32135, 32161, 32186, 32212, 32237, 32262, 32287, 32312, 32337, 32362,
        32387, 32411, 32436, 32460, 32484, 32508, 32533, 32557, 32580, 32604,
        32628, 32651, 32675, 32698, 32722, 32745, 32768
    )
    // array with 100 elements: 10 * (4096*Math.log10(x))
    private val TEN_LOG10_100 = intArrayOf(
        0, 0, 12330, 19542, 24660, 28629, 31873, 34615, 36990, 39085,
        40960, 42655, 44203, 45627, 46945, 48172, 49320, 50399, 51415, 52377,
        53290, 54158, 54985, 55776, 56533, 57259, 57957, 58628, 59275, 59899,
        60502, 61086, 61650, 62198, 62729, 63245, 63746, 64233, 64707, 65170,
        65620, 66059, 66488, 66906, 67315, 67715, 68106, 68489, 68863, 69230,
        69589, 69942, 70287, 70626, 70958, 71285, 71605, 71920, 72230, 72534,
        72833, 73127, 73416, 73700, 73981, 74256, 74528, 74796, 75059, 75319,
        75575, 75827, 76076, 76321, 76563, 76802, 77038, 77270, 77500, 77726,
        77950, 78171, 78389, 78605, 78818, 79028, 79237, 79442, 79646, 79847,
        80045, 80242, 80436, 80629, 80819, 81007, 81193, 81378, 81560, 81741
    )
    const val PI_1024 = 3217
    private const val PI_1024_MULT2 = PI_1024 shl 1
    private const val SMALL_RAD_ANGLE_1024 = 256 // arbitrarily set to 0.25 rad
    private const val CONST1 = 326 // 326 >> 12 === 1/(4*Math.PI)
    // array with 256 elements: 1024*Math.sin(x) x in [0..Math.PI[
    private val SIN_1024 = intArrayOf(
        0, 12, 25, 37, 50, 62, 75, 87, 100, 112, 125, 137, 150, 162, 175, 187,
        199, 212, 224, 236, 248, 260, 273, 285, 297, 309, 321, 333, 344, 356, 368, 380,
        391, 403, 414, 426, 437, 449, 460, 471, 482, 493, 504, 515, 526, 537, 547, 558,
        568, 579, 589, 599, 609, 620, 629, 639, 649, 659, 668, 678, 687, 696, 706, 715,
        724, 732, 741, 750, 758, 767, 775, 783, 791, 799, 807, 814, 822, 829, 837, 844,
        851, 858, 865, 871, 878, 884, 890, 897, 903, 908, 914, 920, 925, 930, 936, 941,
        946, 950, 955, 959, 964, 968, 972, 976, 979, 983, 986, 990, 993, 996, 999, 1001,
        1004, 1006, 1008, 1010, 1012, 1014, 1016, 1017, 1019, 1020, 1021, 1022, 1022, 1023, 1023, 1023,
        1023, 1023, 1023, 1023, 1022, 1022, 1021, 1020, 1019, 1017, 1016, 1014, 1012, 1010, 1008, 1006,
        1004, 1001, 999, 996, 993, 990, 986, 983, 979, 976, 972, 968, 964, 959, 955, 950,
        946, 941, 936, 930, 925, 920, 914, 908, 903, 897, 890, 884, 878, 871, 865, 858,
        851, 844, 837, 829, 822, 814, 807, 799, 791, 783, 775, 767, 758, 750, 741, 732,
        724, 715, 706, 696, 687, 678, 668, 659, 649, 639, 629, 620, 609, 599, 589, 579,
        568, 558, 547, 537, 526, 515, 504, 493, 482, 471, 460, 449, 437, 426, 414, 403,
        391, 380, 368, 356, 344, 333, 321, 309, 297, 285, 273, 260, 248, 236, 224, 212,
        199, 187, 175, 162, 150, 137, 125, 112, 100, 87, 75, 62, 50, 37, 25, 12
    )
    // array with 256 elements: 1024*Math.cos(x) x in [0..Math.PI[
    private val COS_1024 = intArrayOf(
        1024, 1023, 1023, 1023, 1022, 1022, 1021, 1020, 1019, 1017, 1016, 1014, 1012, 1010, 1008, 1006,
        1004, 1001, 999, 996, 993, 990, 986, 983, 979, 976, 972, 968, 964, 959, 955, 950,
        946, 941, 936, 930, 925, 920, 914, 908, 903, 897, 890, 884, 878, 871, 865, 858,
        851, 844, 837, 829, 822, 814, 807, 799, 791, 783, 775, 767, 758, 750, 741, 732,
        724, 715, 706, 696, 687, 678, 668, 659, 649, 639, 629, 620, 609, 599, 589, 579,
        568, 558, 547, 537, 526, 515, 504, 493, 482, 471, 460, 449, 437, 426, 414, 403,
        391, 380, 368, 356, 344, 333, 321, 309, 297, 285, 273, 260, 248, 236, 224, 212,
        199, 187, 175, 162, 150, 137, 125, 112, 100, 87, 75, 62, 50, 37, 25, 12,
        0, -12, -25, -37, -50, -62, -75, -87, -100, -112, -125, -137, -150, -162, -175, -187,
        -199, -212, -224, -236, -248, -260, -273, -285, -297, -309, -321, -333, -344, -356, -368, -380,
        -391, -403, -414, -426, -437, -449, -460, -471, -482, -493, -504, -515, -526, -537, -547, -558,
        -568, -579, -589, -599, -609, -620, -629, -639, -649, -659, -668, -678, -687, -696, -706, -715,
        -724, -732, -741, -750, -758, -767, -775, -783, -791, -799, -807, -814, -822, -829, -837, -844,
        -851, -858, -865, -871, -878, -884, -890, -897, -903, -908, -914, -920, -925, -930, -936, -941,
        -946, -950, -955, -959, -964, -968, -972, -976, -979, -983, -986, -990, -993, -996, -999, -1001,
        -1004, -1006, -1008, -1010, -1012, -1014, -1016, -1017, -1019, -1020, -1021, -1022, -1022, -1023, -1023, -1023
    )
    //  65536/(1 + exp(-alpha*x))
    private val INV_EXP = intArrayOf( // alpha = 0.54
        0, 8, 22, 47, 88, 160, 283, 492,
        848, 1451, 2459, 4117, 6766, 10819, 16608, 24127,
        32768, 41409, 48928, 54717, 58770, 61419, 63077, 64085,
        64688, 65044, 65253, 65376, 65448, 65489, 65514, 65528,
        65536
    )
    private val SQUASH = initSquash()
    private fun initSquash(): IntArray {
        val res = IntArray(4096)
        for (x in -2047..2047) {
            val w = x and 127
            val y = (x shr 7) + 16
            res[x + 2047] = INV_EXP[y] * (128 - w) + INV_EXP[y + 1] * w shr 11
        }
        return res
    }

    // return p = 1/(1 + exp(-d)), d scaled by 8 bits, p scaled by 12 bits
    fun squash(d: Int): Int {
        return if (d >= 2048) 4095 else SQUASH[positiveOrNull(d + 2047)]
    }

    // Inverse of squash. d = ln(p/(1-p)), d scaled by 8 bits, p by 12 bits.
// d has range -2047 to 2047 representing -8 to 8.  p has range 0 to 4095.
    val STRETCH = initStretch()

    private fun initStretch(): IntArray {
        val res = IntArray(4096)
        var pi = 0
        var x = -2047
        while (x <= 2047 && pi < 4096) {
            val i = squash(x)
            while (pi <= i) res[pi++] = x
            x++
        }
        res[4095] = 2047
        return res
    }

    // Return 1024 * 10 * log10(x)
    @Throws(ArithmeticException::class)
    fun tenLog10(x: Int): Int {
        if (x <= 0) throw ArithmeticException("Cannot calculate log of a negative or null value")
        return if (x < 100) TEN_LOG10_100[x] + 2 shr 2 else log2_1024(x) * 6165 shr 11
        // 10 * 1/log2(10)
    }

    // Return 1024 * sin(1024*x) [x in radians]
// Max error is less than 1.5%
    fun sin(rad1024: Int): Int {
        var rad1024 = rad1024
        if (rad1024 >= PI_1024_MULT2 || rad1024 <= -PI_1024_MULT2) rad1024 %= PI_1024_MULT2
        // If x is small enough, return sin(x) === x
        if (rad1024 < SMALL_RAD_ANGLE_1024 && -rad1024 < SMALL_RAD_ANGLE_1024) return rad1024
        val x = rad1024 + (rad1024 shr 31) xor (rad1024 shr 31) // abs(rad1024)
        return if (x >= PI_1024) -((rad1024 shr 31 xor SIN_1024[(x - PI_1024) * CONST1 shr 12]) - (rad1024 shr 31)) else (rad1024 shr 31 xor SIN_1024[x * CONST1 shr 12]) - (rad1024 shr 31)
    }

    // Return 1024 * cos(1024*x) [x in radians]
// Max error is less than 1.5%
    fun cos(rad1024: Int): Int {
        var rad1024 = rad1024
        if (rad1024 >= PI_1024_MULT2 || rad1024 <= -PI_1024_MULT2) rad1024 %= PI_1024_MULT2
        // If x is small enough, return cos(x) === 1 - (x*x)/2
        if (rad1024 < SMALL_RAD_ANGLE_1024 && -rad1024 < SMALL_RAD_ANGLE_1024) return 1024 - (rad1024 * rad1024 shr 11)
        val x = rad1024 + (rad1024 shr 31) xor (rad1024 shr 31) // abs(rad1024)
        return if (x >= PI_1024) -COS_1024[(x - PI_1024) * CONST1 shr 12] else COS_1024[x * CONST1 shr 12]
    }

    @Throws(ArithmeticException::class)
    fun log2(x: Int): Int {
        if (x <= 0) throw ArithmeticException("Cannot calculate log of a negative or null value")
        return if (x <= 256) LOG2[x - 1] else 31 - Integer.numberOfLeadingZeros(x)
    }

    // Return 1024 * log2(x)
// Max error is less than 0.1%
    @Throws(ArithmeticException::class)
    fun log2_1024(x: Int): Int {
        if (x <= 0) throw ArithmeticException("Cannot calculate log of a negative or null value")
        if (x < 256) return LOG2_4096[x] + 2 shr 2
        val log = 31 - Integer.numberOfLeadingZeros(x)
        return if (x and x - 1 == 0) log shl 10 else (log - 7) * 1024 + (LOG2_4096[x shr log - 7] + 2 shr 2)
    }

    // Branchless conditional variable set
// if x = (a<b) ? c : d; is ((((a-b) >> 31) & (c^d)) ^ d)
    fun isIn(x: Int, a: Int, b: Int): Boolean {
        return x - a < b - a
    }

    fun max(x: Int, y: Int): Int {
        return x - (x - y shr 31 and x - y)
    }

    fun min(x: Int, y: Int): Int {
        return y + (x - y shr 31 and x - y)
    }

    // Limitation: fails for Integer.MIN_VALUE
    fun clip0_255(x: Int): Int {
        return if (x >= 255) 255 else positiveOrNull(x)
    }

    // Limitation: fails for Integer.MIN_VALUE
    fun abs(x: Int): Int { // Patented (!) :  return (x ^ (x >> 31)) - (x >> 31);
        return x + (x shr 31) xor (x shr 31)
    }

    // Limitation: fails for Integer.MIN_VALUE
    fun positiveOrNull(x: Int): Int {
        return x and (x shr 31).inv()
    }

    // return 1 or 0
// Limitation: fails for Integer.MIN_VALUE
    fun isNotNullFlag(x: Int): Int {
        return x ushr 31 xor (-x ushr 31)
    }

    // Limitation: fails for Integer.MIN_VALUE
    fun isPowerOf2(x: Int): Boolean {
        return x and x - 1 == 0
    }

    // Limitation: fails for Integer.MIN_VALUE
    fun resetLSB(x: Int): Int {
        return x and x - 1
    }

    // Least significant bit
// Limitation: fails for Integer.MIN_VALUE
    fun lsb(x: Int): Int {
        return x and -x
    }

    // Most significant bit
// Limitation: fails for Integer.MIN_VALUE
    fun msb(x: Int): Int {
        var x = x
        x = x or (x shr 1)
        x = x or (x shr 2)
        x = x or (x shr 4)
        x = x or (x shr 8)
        x = x or (x shr 16)
        return x and (x shr 1).inv()
    }

    // Limitation: fails for Integer.MIN_VALUE and Integer.MAX_VALUE
    fun roundUpPowerOfTwo(x: Int): Int {
        var x = x
        x--
        x = x or (x shr 1)
        x = x or (x shr 2)
        x = x or (x shr 4)
        x = x or (x shr 8)
        x = x or (x shr 16)
        return x + 1
    }

    private val SQRT = intArrayOf(
        0, 16, 23, 28, 32, 36, 39, 42, 45, 48, 51, 53, 55, 58, 60, 62,
        64, 66, 68, 70, 72, 73, 75, 77, 78, 80, 82, 83, 85, 86, 88, 89,
        91, 92, 93, 95, 96, 97, 99, 100, 101, 102, 104, 105, 106, 107, 109, 110,
        111, 112, 113, 114, 115, 116, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127,
        128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 139, 140, 141, 142,
        143, 144, 145, 146, 147, 148, 148, 149, 150, 151, 152, 153, 153, 154, 155, 156,
        157, 158, 158, 159, 160, 161, 162, 162, 163, 164, 165, 166, 166, 167, 168, 169,
        169, 170, 171, 172, 172, 173, 174, 175, 175, 176, 177, 177, 178, 179, 180, 180,
        181, 182, 182, 183, 184, 185, 185, 186, 187, 187, 188, 189, 189, 190, 191, 191,
        192, 193, 193, 194, 195, 195, 196, 197, 197, 198, 199, 199, 200, 200, 201, 202,
        202, 203, 204, 204, 205, 206, 206, 207, 207, 208, 209, 209, 210, 210, 211, 212,
        212, 213, 213, 214, 215, 215, 216, 216, 217, 218, 218, 219, 219, 220, 221, 221,
        222, 222, 223, 223, 224, 225, 225, 226, 226, 227, 227, 228, 229, 229, 230, 230,
        231, 231, 232, 232, 233, 234, 234, 235, 235, 236, 236, 237, 237, 238, 238, 239,
        239, 240, 241, 241, 242, 242, 243, 243, 244, 244, 245, 245, 246, 246, 247, 247,
        248, 248, 249, 249, 250, 250, 251, 251, 252, 252, 253, 253, 254, 254, 255, 255
    )
    private const val THRESHOLD0 = 1 shl 8
    private const val THRESHOLD1 = 1 shl 16
    private const val THRESHOLD2 = (1 shl 10) - 3
    private const val THRESHOLD3 = (1 shl 14) - 28
    private const val THRESHOLD4 = 1 shl 24
    private const val THRESHOLD5 = 1 shl 20
    private const val THRESHOLD6 = 1 shl 28
    private const val THRESHOLD7 = 1 shl 26
    private const val THRESHOLD8 = 1 shl 30
    // Integer SQRT implementation based on algorithm at
// http://guru.multimedia.cx/fast-integer-square-root/
// Return 1024*sqrt(x) with a precision higher than 0.1%
    @Throws(IllegalArgumentException::class)
    fun sqrt(x: Int): Int {
        var x = x
        require(x >= 0) { "Cannot calculate sqrt of a negative value" }
        if (x <= 1) return x shl 10
        val shift = if (x < THRESHOLD5) if (x < THRESHOLD0) 16 else 10 else 0
        x = x shl shift // scale up for better precision
        var `val`: Int
        if (x < THRESHOLD1) {
            `val` = if (x < THRESHOLD2) {
                SQRT[x + 3 shr 2] shr 3
            } else {
                if (x < THRESHOLD3) SQRT[x + 28 shr 6] shr 1 else SQRT[x shr 8]
            }
        } else {
            if (x < THRESHOLD4) {
                if (x < THRESHOLD5) {
                    `val` = SQRT[x shr 12]
                    `val` = (x / `val` shr 3) + (`val` shl 1)
                } else {
                    `val` = SQRT[x shr 16]
                    `val` = (x / `val` shr 5) + (`val` shl 3)
                }
            } else {
                if (x < THRESHOLD6) {
                    if (x < THRESHOLD7) {
                        `val` = SQRT[x shr 18]
                        `val` = (x / `val` shr 6) + (`val` shl 4)
                    } else {
                        `val` = SQRT[x shr 20]
                        `val` = (x / `val` shr 7) + (`val` shl 5)
                    }
                } else {
                    if (x < THRESHOLD8) {
                        `val` = SQRT[x shr 22]
                        `val` = (x / `val` shr 8) + (`val` shl 6)
                    } else {
                        `val` = SQRT[x shr 24]
                        `val` = (x / `val` shr 9) + (`val` shl 7)
                    }
                }
            }
        }
        // return 1024 * sqrt(x)
        return `val` - (x - `val` * `val` ushr 31) shl 10 - (shift shr 1)
    }

    // If withTotal is true, the last spot in the frequencies array is for the total
    fun computeHistogramOrder0(
        block: ByteArray,
        start: Int,
        end: Int,
        freqs: IntArray,
        withTotal: Boolean
    ) {
        run {
            var i = 0
            while (i < 256) {
                freqs[i] = 0
                freqs[i + 1] = 0
                freqs[i + 2] = 0
                freqs[i + 3] = 0
                freqs[i + 4] = 0
                freqs[i + 5] = 0
                freqs[i + 6] = 0
                freqs[i + 7] = 0
                i += 8
            }
        }
        if (withTotal == true) freqs[256] = end - start
        val f0 = IntArray(256)
        val f1 = IntArray(256)
        val f2 = IntArray(256)
        val f3 = IntArray(256)
        val end4 = start + (end - start and -4)
        run {
            var i = start
            while (i < end4) {
                f0[(block[i] and 0xFF.toByte()).toInt()]++
                f1[(block[i + 1] and 0xFF.toByte()).toInt()]++
                f2[(block[i + 2] and 0xFF.toByte()).toInt()]++
                f3[(block[i + 3] and 0xFF.toByte()).toInt()]++
                i += 4
            }
        }
        for (i in end4 until end) freqs[(block[i] and 0xFF.toByte()).toInt()]++
        for (i in 0..255) freqs[i] += f0[i] + f1[i] + f2[i] + f3[i]
    }

    // If withTotal is true, the last spot in each frequencies order 0 array is for the total
    fun computeHistogramOrder1(
        block: ByteArray,
        start: Int,
        end: Int,
        freqs: Array<IntArray>,
        withTotal: Boolean
    ) {
        for (j in 0..255) {
            val f = freqs[j]
            var i = 0
            while (i < 256) {
                f[i] = 0
                f[i + 1] = 0
                f[i + 2] = 0
                f[i + 3] = 0
                f[i + 4] = 0
                f[i + 5] = 0
                f[i + 6] = 0
                f[i + 7] = 0
                i += 8
            }
            if (withTotal) f[256] = 0
        }
        var prv = 0
        if (withTotal) {
            for (i in start until end) {
                val cur: Int = (block[i] and 0xFF.toByte()).toInt()
                freqs[prv][cur]++
                freqs[prv][256]++
                prv = cur
            }
        } else {
            for (i in start until end) {
                val cur: Int = (block[i] and 0xFF.toByte()).toInt()
                freqs[prv][cur]++
                prv = cur
            }
        }
    }

    fun computeJobsPerTask(jobsPerTask: IntArray, jobs: Int, tasks: Int): IntArray {
        require(tasks > 0) { "Invalid number of tasks provided: $tasks" }
        require(jobs > 0) { "Invalid number of jobs provided: $jobs" }
        val q = if (jobs <= tasks) 1 else jobs / tasks
        var r = if (jobs <= tasks) 0 else jobs - q * tasks
        Arrays.fill(jobsPerTask, q)
        var n = 0
        while (r != 0) {
            jobsPerTask[n]++
            r--
            n++
            if (n == tasks) n = 0
        }
        return jobsPerTask
    }

    fun sortFilesByPathAndSize(files: List<Path>?, sortBySize: Boolean) {
        val c = Comparator<Path> { p1, p2 ->
            if (sortBySize == false) return@Comparator p1.compareTo(p2)
            // Compare parent directory paths
            val res = p1.parent.compareTo(p2.parent)
            if (res != 0) return@Comparator res
            try {
                return@Comparator ((Files.size(p1) - Files.size(p2)).toInt())
            } catch (e: IOException) {
                return@Comparator -1
            }
        }
        Collections.sort(files, c)
    }
}