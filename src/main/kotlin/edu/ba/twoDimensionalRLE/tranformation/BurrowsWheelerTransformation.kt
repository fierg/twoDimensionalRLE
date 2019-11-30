package edu.ba.twoDimensionalRLE.tranformation

import edu.ba.twoDimensionalRLE.model.DataChunk
import java.nio.charset.Charset

class BurrowsWheelerTransformation {

    companion object {
        const val STX = "\u0002"
        const val ETX = "\u0003"
    }

    fun transformDataChunk(input: DataChunk): DataChunk {
        return DataChunk(transform(input.bytes.toString(Charset.defaultCharset())).toByteArray())
    }

    fun invertTransformDataChunk(input: DataChunk): DataChunk {
        return DataChunk(invertTransform(input.bytes.toString(Charset.defaultCharset())).toByteArray())
    }

    fun transform(input: String): String {
        if (input.contains(STX) || input.contains(ETX)) {
            throw RuntimeException("String can't contain STX or ETX")
        }
        val ss = STX + input + ETX
        val table = Array(ss.length) { ss.substring(it) + ss.substring(0, it) }
        table.sort()
        return String(table.map { it[it.lastIndex] }.toCharArray())
    }

    fun invertTransform(input: String): String {
        val length = input.length
        val table = Array(length) { "" }
        repeat(length) {
            for (i in 0 until length) {
                table[i] = input[i].toString() + table[i]
            }
            table.sort()
        }
        for (row in table) {
            if (row.endsWith(ETX)) {
                return row.substring(1, length - 1)
            }
        }
        return ""
    }

    fun makePrintable(input: String): String {
        return input.replace(STX, "^").replace(ETX, "|")
    }
}