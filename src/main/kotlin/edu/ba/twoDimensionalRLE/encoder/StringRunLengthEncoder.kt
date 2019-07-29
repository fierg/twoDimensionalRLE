package edu.ba.twoDimensionalRLE.encoder

tailrec fun runLengthEncodingString(text:String, prev:String=""):String {
    if (text.isEmpty()){
        return prev
    }
    val initialChar = text[0]
    val count = text.takeWhile{ it==initialChar }.count()
    return runLengthEncodingString(text.substring(count), prev + "$count$initialChar")
}

