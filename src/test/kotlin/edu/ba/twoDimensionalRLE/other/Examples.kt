package edu.ba.twoDimensionalRLE.other

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.model.DataChunk
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder


@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class Examples {


    private var log = Log.kotlinInstance()
    private val testWord = "Lorem ipsum dolor sit amet."

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    @Test
    @Order(1)
    fun example1() {
        var sb = StringBuffer()
        testWord.forEach {
            it.toByte().toUByte().toString(2).padStart(8, '0').map {
                sb.append(it)
                sb.append(' ')
            }
            sb.append("\n")
        }
        log.info("Result :\n $sb")

        sb = StringBuffer()
        testWord.forEach {
            sb.append(it.toByte().toUByte().toString(2).padStart(8, '0'))
        }

        var lastChar = 'x'
        var count = 0
        val countMap = mutableMapOf<Int, Int>()
        sb.forEach {
            if (it == lastChar) {
                count++
            } else {
                lastChar = it
                countMap[count] = countMap.getOrDefault(count, 0) + 1
                count = 1
            }
        }

        log.info("binary run counts:  ${countMap.filter { it.key != 0 }}")
    }

    @Test
    @Order(2)
    fun example2() {
        var sb = StringBuffer()
        val analyzer = Analyzer()

        analyzer.analyzeString(testWord)

        val mapping = analyzer.getByteMapping()

        testWord.forEach {
            mapping[it.toByte()]!!.toString(2).padStart(8, '0').map {
                sb.append(it)
                sb.append(' ')
            }
            sb.append("\n")
        }
        log.info("Result :\n $sb")

        sb = StringBuffer()
        testWord.forEach {
            sb.append(it.toByte().toUByte().toString(2).padStart(8, '0'))
        }

        var lastChar = 'x'
        var count = 0
        val countMap = mutableMapOf<Int, Int>()
        sb.forEach {
            if (it == lastChar) {
                count++
            } else {
                lastChar = it
                countMap[count] = countMap.getOrDefault(count, 0) + 1
                count = 1
            }
        }

        log.info("binary run counts:  ${countMap.filter { it.key != 0 }}")
    }

}