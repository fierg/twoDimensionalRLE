package edu.ba.twoDimensionalRLE.tranformation

class BurrowsWheelerTrasformation {

    fun transform(input: String): String {
        if (input.contains(Companion.STX) || input.contains(Companion.ETX)) {
            throw RuntimeException("String can't contain STX or ETX")
        }
        val ss = Companion.STX + input + Companion.ETX
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
            if (row.endsWith(Companion.ETX)) {
                return row.substring(1, length - 1)
            }
        }
        return ""
    }

    fun makePrintable(input: String): String {
        return input.replace(Companion.STX, "^").replace(Companion.ETX, "|")
    }

    companion object {
        const val STX = "\u0002"
        const val ETX = "\u0003"
    }
}