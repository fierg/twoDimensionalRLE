package edu.ba.twoDimensionalRLE.tranformation.bijectiveJavaWrapper

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.LogLevel
import kanzi.SliceByteArray
import kanzi.transform.BWTS
import java.io.File

class BWTSWrapper {


    private val log = Log.kotlinInstance()
    private val DEBUG = true

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
        if (!DEBUG) log.logLevel = LogLevel.INFO
    }

    fun transformFile(inputFile: File, outputFile: File) {
        val bwts = BWTS()
        log.debug("Starting bwt on file $inputFile")

        val buf1 = inputFile.readBytes()
        val buf2 = ByteArray(buf1.size)
        val sa1 = SliceByteArray(buf1, 0)
        val sa2 = SliceByteArray(buf2, 0)
        bwts.forward(sa1, sa2)
        outputFile.writeBytes(sa2.array)


        log.debug("Result:")
        if (DEBUG) println(outputFile.readText())
    }

    fun invert(inputFile: File, outputFile: File) {
        val bwts = BWTS()
        log.debug("Starting inverse transformation on $inputFile")

        val buf3 = inputFile.readBytes()
        val buf4 = ByteArray(buf3.size)
        val sa3 = SliceByteArray(buf3, 0)
        val sa4 = SliceByteArray(buf4, 0)

        bwts.inverse(sa3, sa4)
        outputFile.writeBytes(sa4.array)

        log.info("Result:")
        if (DEBUG) println(outputFile.readText())
    }
}