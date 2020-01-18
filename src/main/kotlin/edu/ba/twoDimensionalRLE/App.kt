package edu.ba.twoDimensionalRLE

fun main(args: Array<String>) {
    val map = args.fold(Pair(emptyMap<String, List<String>>(), "")) { (map, lastKey), elem ->
        if (elem.startsWith("-"))  Pair(map + (elem to emptyList()), elem)
        else Pair(map + (lastKey to map.getOrDefault(lastKey, emptyList()) + elem), lastKey)
    }.first

    if (map.containsKey("-h")) {
        println("HELP: ")
    } else {

    }
}