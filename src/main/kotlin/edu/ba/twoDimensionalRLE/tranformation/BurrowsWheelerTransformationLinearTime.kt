package edu.ba.twoDimensionalRLE.tranformation

class BurrowsWheelerTransformationLinearTime {


    fun transform(input: String): Pair<String, Int> {
        val table = Array(input.length) { input.substring(it) + input.substring(0, it) }
        table.sort()
        val index = table.indexOfFirst { it == input }
        return Pair(String(table.map { it[it.lastIndex] }.toCharArray()), index)
    }
}