package edu.ba.twoDimensionalRLE

tailrec fun runLengthEncoding(text:String,prev:String=""):String {
    if (text.isEmpty()){
        return prev
    }
    val initialChar = text[0]
    val count = text.takeWhile{ it==initialChar }.count()
    return runLengthEncoding(text.substring(count),prev + "$count$initialChar" )
}

