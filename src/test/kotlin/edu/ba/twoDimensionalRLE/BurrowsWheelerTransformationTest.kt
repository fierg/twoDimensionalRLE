package edu.ba.twoDimensionalRLE

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTrasformation
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class BurrowsWheelerTransformationTest {

    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    @Test
    fun simpleTest() {
        val tests = listOf(
            "banana",
            "hello this is a normal sentence.",
            "this is a sentence ii with iiii many iiiiis"
        )

        val transformer = BurrowsWheelerTrasformation()

        for (test in tests) {
            println(transformer.makePrintable(test))
            print(" --> ")
            var encoded = ""
            try {
                encoded = transformer.transform(test)
                println(transformer.makePrintable(encoded))
            } catch (ex: RuntimeException) {
                println("ERROR: " + ex.message)
            }
            val decoded = transformer.invertTransform(encoded)

            assert(decoded == test)
            println(" --> $decoded\n")
        }
    }

    @Test
    fun simpleTestFail() {
        val tests = listOf("\u0002ABC\u0003")

        val transformer = BurrowsWheelerTrasformation()

        assertFailsWith<RuntimeException> {
            for (test in tests) {
                println(transformer.makePrintable(test))
                print(" --> ")
                var encoded = ""
                encoded = transformer.transform(test)
                println(transformer.makePrintable(encoded))
            }
        }.message?.let { log.error(it) }
    }
}