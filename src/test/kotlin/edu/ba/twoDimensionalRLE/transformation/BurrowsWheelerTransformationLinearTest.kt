package edu.ba.twoDimensionalRLE.transformation

import kanzi.ByteTransform
import kanzi.SliceByteArray
import kanzi.transform.BWT
import kanzi.transform.BWTS
import org.junit.Assert
import org.junit.Test
import java.util.*


class BurrowsWheelerTransformationLinearTest {
        @Test
        fun testBWT() {
            Assert.assertTrue(testCorrectness(true, 200))
            Assert.assertTrue(testCorrectness(false, 200))
        }

        companion object {
            @JvmStatic
            fun main(args: Array<String>) {
                if (args.size > 0) {
                    val buf1 = args[0].toByteArray()
                    val buf2 = ByteArray(buf1.size)
                    val sa1 = SliceByteArray(buf1, 0)
                    val sa2 = SliceByteArray(buf2, 0)
                    val bwt = BWT()
                    bwt.forward(sa1, sa2)
                    print("BWT:  " + String(buf2))
                    System.out.println(" (" + bwt.getPrimaryIndex(0).toString() + ")")
                    sa1.index = 0
                    sa2.index = 0
                    val bwts = BWTS()
                    bwts.forward(sa1, sa2)
                    println("BWTS: " + String(buf2))
                    System.exit(0)
                }
                println("TestBWT and TestBWTS")
                if (testCorrectness(true, 20) == false) System.exit(1)
                if (testCorrectness(false, 20) == false) System.exit(1)
                testSpeed(true)
                testSpeed(false)
            }

            fun testCorrectness(isBWT: Boolean, iters: Int): Boolean {
                println("\nBWT" + (if (!isBWT) "S" else "") + " Correctness test")
                // Test behavior
                for (ii in 1..iters) {
                    println("\nTest $ii")
                    val start = 0
                    var buf1: ByteArray
                    val rnd = Random()
                    if (ii == 1) {
                        buf1 = "mississippi".toByteArray()
                    } else if (ii == 2) {
                        buf1 = "3.14159265358979323846264338327950288419716939937510".toByteArray()
                    } else if (ii == 3) {
                        buf1 = "SIX.MIXED.PIXIES.SIFT.SIXTY.PIXIE.DUST.BOXES".toByteArray()
                    } else if (ii < iters) {
                        buf1 = ByteArray(128)
                        for (i in buf1.indices) {
                            buf1[i] = (65 + rnd.nextInt(4 * ii)).toByte()
                        }
                    } else {
                        buf1 = ByteArray(8 * 1024 * 1024)
                        for (i in buf1.indices) buf1[i] = i.toByte()
                    }
                    val buf2 = ByteArray(buf1.size)
                    val buf3 = ByteArray(buf1.size)
                    val sa1 = SliceByteArray(buf1, 0)
                    val sa2 = SliceByteArray(buf2, 0)
                    val sa3 = SliceByteArray(buf3, 0)
                    var transform: ByteTransform = if (isBWT) BWT() else BWTS()
                    val str1 = String(buf1, start, buf1.size - start)
                    if (str1.length < 512) println("Input:   $str1")
                    sa1.index = start
                    sa2.index = 0
                    transform.forward(sa1, sa2)
                    val str2 = String(buf2)
                    if (str2.length < 512) print("Encoded: $str2  ")
                    if (isBWT) {
                        var burrowsWheelerTransformation: BWT = transform as BWT
                        val chunks: Int = BWT.getBWTChunks(buf1.size)
                        val pi = IntArray(chunks)
                        for (i in 0 until chunks) {
                            pi[i] = burrowsWheelerTransformation.getPrimaryIndex(i)
                            println("(Primary index=" + pi[i] + ")")
                        }
                        transform = BWT()
                        burrowsWheelerTransformation = transform
                        for (i in 0 until chunks) burrowsWheelerTransformation.setPrimaryIndex(i, pi[i])
                    } else {
                        transform = BWTS()
                        println("")
                    }
                    sa2.index = 0
                    sa3.index = start
                    transform.inverse(sa2, sa3)
                    val str3 = String(buf3, start, buf3.size - start)
                    if (str3.length < 512) println("Output:  $str3")
                    if (str1 == str3) {
                        println("Identical")
                    } else {
                        var idx = -1
                        for (i in buf1.indices) {
                            if (buf1[i] != buf3[i]) {
                                idx = i
                                break
                            }
                        }
                        println("Different at index " + idx + " " + buf1[idx] + " <-> " + buf3[idx])
                        return false
                    }
                }
                return true
            }

            fun testSpeed(isBWT: Boolean) {
                println("\nBWT" + (if (!isBWT) "S" else "") + " Speed test")
                val iter = 2000
                val size = 256 * 1024
                val buf1 = ByteArray(size)
                val buf2 = ByteArray(size)
                val buf3 = ByteArray(size)
                val sa1 = SliceByteArray(buf1, 0)
                val sa2 = SliceByteArray(buf2, 0)
                val sa3 = SliceByteArray(buf3, 0)
                println("Iterations: $iter")
                println("Transform size: $size")
                for (jj in 0..2) {
                    var delta1: Long = 0
                    var delta2: Long = 0
                    val bwt: ByteTransform = if (isBWT) BWT() else BWTS()
                    val random = Random()
                    var before: Long
                    var after: Long
                    for (ii in 0 until iter) {
                        for (i in 0 until size) buf1[i] = (random.nextInt(255) + 1).toByte()
                        before = System.nanoTime()
                        sa1.index = 0
                        sa2.index = 0
                        bwt.forward(sa1, sa2)
                        after = System.nanoTime()
                        delta1 += after - before
                        before = System.nanoTime()
                        sa2.index = 0
                        sa3.index = 0
                        bwt.inverse(sa2, sa3)
                        after = System.nanoTime()
                        delta2 += after - before
                        var idx = -1
                        // Sanity check
                        for (i in 0 until size) {
                            if (buf1[i] != buf3[i]) {
                                idx = i
                                break
                            }
                        }
                        if (idx >= 0) println("Failure at index " + idx + " (" + buf1[idx] + "<->" + buf3[idx] + ")")
                    }
                    val prod = iter.toLong() * size.toLong()
                    println("Forward transform [ms] : " + delta1 / 1000000)
                    println("Throughput [KB/s]      : " + prod * 1000000L / delta1 * 1000L / 1024)
                    println("Inverse transform [ms] : " + delta2 / 1000000)
                    println("Throughput [KB/s]      : " + prod * 1000000L / delta2 * 1000L / 1024)
                }
            }
        }
}