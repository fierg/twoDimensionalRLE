package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.mixed.ModifiedMixedEncoder
import edu.ba.twoDimensionalRLE.encoder.rle.StringRunLengthEncoder
import java.io.File
import kotlin.system.exitProcess

/*
MIT License

Copyright (c) 2019 Sven Fiergolla

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
open class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            var vertBitRLEset = false
            var decompress = false
            var applyBurrowsWheelerTransformation = false
            var applyByteMapping = false
            var applyHuffmanEncoding = false
            var binaryRle = false
            var byteWiseRle = false
            var debugLog = false

            val map = args.fold(Pair(emptyMap<String, List<String>>(), "")) { (map, lastKey), elem ->
                if (elem.startsWith("-")) Pair(map + (elem to emptyList()), elem)
                else Pair(map + (lastKey to map.getOrDefault(lastKey, emptyList()) + elem), lastKey)
            }.first

            if (map.containsKey("-h") || map.containsKey("help") || map.containsKey("--h") || map.containsKey("-help") || map.containsKey(
                    "--help"
                ) || map.isNullOrEmpty()
            ) {
                printUsage()
                exitProcess(0)
            }

            val files: List<String>
            files = if (map.containsKey("-f")) map.getValue("-f") else emptyList()

            if (files.isEmpty()) {
                println("No file provided! nothing to do.")
                printUsage()
                exitProcess(0)
            }


            if (map.containsKey("-c") && map.containsKey("-d")) {
                println("Invalid arguments -c and -d!")
                printUsage()
                exitProcess(1)
            } else if (map.containsKey("-d")) {
                decompress = true
            }

            if (map.containsKey("-v")) vertBitRLEset = true
            if (map.containsKey("-bin")) binaryRle = true
            if (map.containsKey("-byte")) byteWiseRle = true
            if ((binaryRle && byteWiseRle) || (binaryRle && vertBitRLEset) || (vertBitRLEset && byteWiseRle)) {
                println("Invalid arguments! Only one method usable.")
                printUsage()
                exitProcess(1)
            }

            var userBitsTousemap: Map<Int, Int>? = null

            if (map.containsKey("-v") && !map["-v"].isNullOrEmpty()) {
                val bitsToUse = (map["-v"] ?: error("Unexpected mapping parsed!")).first()
                if (Regex("(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)").matches(bitsToUse)) {
                    userBitsTousemap = mutableMapOf()
                    val bitsToUseMatches =
                        Regex("(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)").find(bitsToUse)
                    for (i in 0..7) {
                        if (bitsToUseMatches!!.groupValues[i + 1].toInt() in 2..32) userBitsTousemap[i] =
                            bitsToUseMatches.groupValues[i + 1].toInt() else {
                            println("bit per run has to bin range [2,32]!")
                            printUsage()
                            exitProcess(1)
                        }
                    }
                } else {
                    println("Unexpected mapping parsed!")
                    println("expecting 8 comma separated numbers: -v N,N,N,N,N,N,N,N")
                    printUsage()
                    exitProcess(1)
                }
            }

            if (map.containsKey("-bwt")) applyBurrowsWheelerTransformation = true
            if (map.containsKey("-map")) applyByteMapping = true
            if (map.containsKey("-huf")) applyHuffmanEncoding = true
            if (map.containsKey("-D")) debugLog = true



            if (!decompress) {
                when {
                    byteWiseRle -> {
                        val encoder = StringRunLengthEncoder(debugLog)
                        files.forEach {
                            encoder.encodeVarLength(
                                it,
                                "$it.bin",
                                map["-byte"]?.firstOrNull()?.toIntOrNull() ?: 3,
                                applyByteMapping,
                                false,
                                applyBurrowsWheelerTransformation,
                                512
                            )
                        }
                    }
                    binaryRle -> {
                        val encoder = StringRunLengthEncoder(debugLog)
                        files.forEach {
                            encoder.encodeSimpleBinaryRLE(
                                it,
                                "$it.byteRLE",
                                map["-bin"]?.firstOrNull()?.toIntOrNull() ?: 3
                            )
                        }
                    }
                    else -> {
                        files.forEach {
                            val encoder = ModifiedMixedEncoder(debugLog)
                            encoder.encodeInternal(
                                it,
                                "$it.mixed",
                                applyByteMapping,
                                applyBurrowsWheelerTransformation,
                                applyHuffmanEncoding,
                                if (applyHuffmanEncoding) userBitsTousemap ?: mapOf(
                                    0 to 8,
                                    1 to 8,
                                    2 to 8,
                                    3 to 8,
                                    4 to 8,
                                    5 to 8,
                                    6 to 8,
                                    7 to 8
                                ) else {
                                    if (applyByteMapping && !applyBurrowsWheelerTransformation) userBitsTousemap
                                        ?: mapOf(
                                            0 to 2,
                                            1 to 2,
                                            2 to 3,
                                            3 to 3,
                                            4 to 3,
                                            5 to 4,
                                            6 to 5,
                                            7 to 8
                                        )
                                    else if (!applyByteMapping && applyBurrowsWheelerTransformation) userBitsTousemap
                                        ?: mapOf(
                                            0 to 4,
                                            1 to 4,
                                            2 to 4,
                                            3 to 4,
                                            4 to 4,
                                            5 to 6,
                                            6 to 6,
                                            7 to 8
                                        )
                                    else userBitsTousemap ?: mapOf(
                                        0 to 4,
                                        1 to 4,
                                        2 to 4,
                                        3 to 4,
                                        4 to 5,
                                        5 to 7,
                                        6 to 8,
                                        7 to 8
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                when {
                    byteWiseRle -> {
                        val encoder = StringRunLengthEncoder(debugLog)
                        files.forEach {
                            encoder.decodeVarLength(
                                it,
                                File(it).nameWithoutExtension,
                                map["-byte"]?.firstOrNull()?.toIntOrNull() ?: 3,
                                applyByteMapping,
                                false,
                                applyBurrowsWheelerTransformation,
                                512
                            )
                        }
                    }
                    binaryRle -> {
                        val encoder = StringRunLengthEncoder(debugLog)
                        files.forEach {
                            encoder.encodeSimpleBinaryRLE(
                                it,
                                File(it).nameWithoutExtension,
                                map["-bin"]?.firstOrNull()?.toIntOrNull() ?: 3
                            )
                        }
                    }
                    else -> {
                        val encoder = ModifiedMixedEncoder(debugLog)
                        files.forEach {
                            encoder.decodeInternal(
                                it,
                                File(it).nameWithoutExtension,
                                applyByteMapping,
                                applyBurrowsWheelerTransformation,
                                applyHuffmanEncoding,
                                mapOf(0 to 8, 1 to 8, 2 to 8, 3 to 8, 4 to 8, 5 to 8, 6 to 8, 7 to 8)
                            )
                        }
                    }
                }
            }
        }


        private fun printUsage() {
            println("Advanced RLE Encoder\n")
            println("Usage:\n [action] [method] [preprocessing] -f [files...]\n")
            println("ACTION:")
            println("-c \t\t\t compress file (default)")
            println("-d \t\t\t decompress file\n")
            println("METHOD:")
            println("-v #,#,#,#,#,#,#,#\t vertical reading (default), optional run lengths used on bit of significance 1 to 8 (N in range [2,32])")
            println("-bin #N \t\t binary reading (with N bits per RLE encoded number, default 3)")
            println("-byte #N \t\t byte wise reading (with N bits per RLE encoded number, default 3)\n")
            println("PREPROCESSING:")
            println("-bwt \t\t\t apply Burrows-Wheeler-Transformation during encoding")
            println("-map \t\t\t apply Byte-mapping during encoding")
            println("-huf \t\t\t encode with Huffman encoding\n")
            println("DEBUG:")
            println("-D \t\t\t debug log level")
        }
    }
}