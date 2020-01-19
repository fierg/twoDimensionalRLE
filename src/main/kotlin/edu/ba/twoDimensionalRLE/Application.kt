package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.mixed.ModifiedMixedEncoder
import edu.ba.twoDimensionalRLE.encoder.rle.StringRunLengthEncoder
import java.io.File
import kotlin.system.exitProcess

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
fun main(args: Array<String>) {

    var decompress = false
    var applyBurrowsWheelerTransformation = false
    var applyByteMapping = false
    var applyHuffmanEncoding = false
    var binaryRle = false
    var byteWiseRle = false

    val map = args.fold(Pair(emptyMap<String, List<String>>(), "")) { (map, lastKey), elem ->
        if (elem.startsWith("-")) Pair(map + (elem to emptyList()), elem)
        else Pair(map + (lastKey to map.getOrDefault(lastKey, emptyList()) + elem), lastKey)
    }.first

    if (map.containsKey("-h")) {
        printUsage()
        exitProcess(0)
    }

    val files: List<String>
    files = if (map.containsKey("-f")) map.getValue("-f") else emptyList()


    if (map.containsKey("-c") && map.containsKey("-d")) {
        println("Invalid arguments -c and -d!")
        printUsage()
        exitProcess(1)
    } else if (map.containsKey("-d")) {
        decompress = true
    }
    if (map.containsKey("-bin")) binaryRle = true
    if (map.containsKey("-byte")) byteWiseRle = true
    if (binaryRle && byteWiseRle) {
        println("Invalid arguments!")
        printUsage()
        exitProcess(1)
    }
    if (map.containsKey("-bwt")) applyBurrowsWheelerTransformation = true
    if (map.containsKey("-m")) applyByteMapping = true
    if (map.containsKey("-h")) applyHuffmanEncoding = true



    if (!decompress) {
        when {
            byteWiseRle -> {
                val encoder = StringRunLengthEncoder()
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
                val encoder = StringRunLengthEncoder()
                files.forEach {
                    encoder.encodeSimpleBinaryRLE(it, "$it.byteRLE", map["-bin"]?.firstOrNull()?.toIntOrNull() ?: 3)
                }
            }
            else -> {
                files.forEach {
                    val encoder = ModifiedMixedEncoder()
                    encoder.encodeInternal(
                        it, "$it.mixed", applyByteMapping, applyBurrowsWheelerTransformation, applyHuffmanEncoding,
                        mapOf(0 to 8, 1 to 8, 2 to 8, 3 to 8, 4 to 8, 5 to 8, 6 to 8, 7 to 8)
                    )
                }
            }
        }
    } else {
        when {
            byteWiseRle -> {
                val encoder = StringRunLengthEncoder()
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
                TODO("Not implemented jet")
            }
            else -> {
                val encoder = ModifiedMixedEncoder()
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
    println("Usage:\n [action] [method] [preprocessing] [files...]\n")
    println("ACTION:")
    println("-c \t\t compress file (default)")
    println("-d \t\t decompress file\n")
    println("METHOD:")
    println("-v \t\t vertical reading (default)")
    println("-bin #N \t binary reading (with N bits per RLE encoded number, default 3)")
    println("-byte #N \t byte wise reading (with N bits per RLE encoded number, default 3)\n")
    println("PREPROCESSING:")
    println("-bwt \t apply Burrows-Wheeler-Transformation during encoding")
    println("-m \t\t apply Byte-mapping during encoding")
    println("-h \t\t encode with Huffman encoding\n")
    println("DEBUG:")
    println("-D \t debug log level")
}