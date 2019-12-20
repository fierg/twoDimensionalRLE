package edu.ba.twoDimensionalRLE.other

import edu.ba.twoDimensionalRLE.extensions.toBinStringBuffer
import edu.ba.twoDimensionalRLE.extensions.toBitSet
import edu.ba.twoDimensionalRLE.extensions.toByteArray
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

@ExperimentalUnsignedTypes
class UtilsAndExtensionsTest {

    @Test
    @Order(1)
    fun assertEqualityFails() {

        val buffer =
            StringBuffer("1100000000000110000000000000000001111111111111111100000000000000111010101000000000001000000000000000000000000000000000000011111111110011100011100000000000000000000000000000000000000000000000000000000000110001100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")
        val arr = buffer.toByteArray()
        buffer.toBitSet().toByteArray()

        assertFailsWith<AssertionError> {
            assert(arr.toBinStringBuffer().toString() == buffer.toString())
        }
    }

    @Test
    @Order(1)
    fun assertEquality() {

        val buffer =
            StringBuffer("1100000000000110000000000000000000000001111111111111111100000000000000111010101000000000001000000000000000000000000000000000000011111111110011100011100000000000000000000000000000000000000000000000000000000000110001100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")
        val arr = buffer.toByteArray()

        assert(arr.toBinStringBuffer().toString() == buffer.toString())
    }
}