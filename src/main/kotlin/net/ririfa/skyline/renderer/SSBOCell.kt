package net.ririfa.skyline.renderer

import java.nio.IntBuffer

import java.nio.*

/**
 * a fixed-size buffer of integer data. This class is used to efficiently handle buffer operations.
 * This class is not thread-safe; external synchronization is required for concurrent usage.
 * a fixed-size buffer of integer data. This class is used to efficiently handle buffer operations
 * such as adding new data and reading existing data from the buffer within the defined constraints.
 *
 * @property maxSize The maximum number of integers that can be stored in the buffer.
 *                   Any attempt to add more data than this limit will result in an exception.
 */
class SSBOCell(val maxSize: Int) {

    init {
        if (maxSize <= 0) {
            throw IllegalArgumentException("Buffer size must be positive!")
        }
    }

    private val buffer: IntBuffer = ByteBuffer.allocateDirect(maxSize * Int.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer()

    private var currentSizeInternal: Int = 0

    val currentSize: Int
        get() = currentSizeInternal

    /**
     * Adds new integer data to the buffer. The provided data is appended to the existing buffer
     * if there is sufficient space available. Throws an `IllegalArgumentException` when adding
     * the new data would cause the total size of data in the buffer to exceed its maximum size.
     *
     * @param newData An array of integers to be added to the buffer. Its size must not
     *                exceed the available space in the buffer.
     * @throws IllegalArgumentException If the buffer size is invalid or the combined size of
     *         and `newData` exceeds the buffer's maximum size.
     */
    @Synchronized
    fun addData(newData: IntArray) {

        if (currentSizeInternal + newData.size > maxSize) {
            throw IllegalArgumentException("Data size exceeds maximum size!")
        }
        buffer.put(newData)
        currentSizeInternal += newData.size
    }

    /**
     * Reads the integer data currently stored in the buffer and returns it as an array.
     * The buffer's position is reset to the start before the data is read to ensure all
     * stored data is included in the result.
     *
     * @return An array containing the data currently stored in the buffer.
     */
    fun readData(): IntArray {
        val result = IntArray(currentSizeInternal)
        val duplicateBuffer = buffer.duplicate()
        duplicateBuffer.rewind()
        duplicateBuffer.get(result)
        return result

        @Synchronized
        fun readData(): IntArray {
            val result = IntArray(currentSize)
            buffer.rewind()
            buffer.get(result)
            return result
        }

        /**
         * Clears the buffer and resets all internal state to its default values.
         */
        fun clear() {
            buffer.clear()
            currentSizeInternal = 0
        }
    }
}


data class CellLocation(val cellIndex: Int, val offset: Int)