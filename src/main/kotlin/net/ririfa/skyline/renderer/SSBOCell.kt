package net.ririfa.skyline.renderer

import java.nio.IntBuffer

import java.nio.*

/**
 * Represents a Shader Storage Buffer Object (SSBO) cell specialized for storing and managing
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

    @Volatile
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
     * @throws IllegalArgumentException If the combined size of current data in the buffer
     *         and `newData` exceeds the buffer's maximum size.
     */
    @Synchronized
    fun addData(newData: IntArray) {
        if (currentSize + newData.size > maxSize) {
            throw IllegalArgumentException("Data size exceeds maximum size!")
        }
        buffer.put(newData)
        currentSizeInternal += newData.size
    }

    /**
     * Reads the current data stored in the buffer up to the current size.
     * The method duplicates the buffer, rewinds it, and retrieves the data
     * into a new integer array.
     *
     * @return An array of integers containing the current data in the buffer.
     */
    @Synchronized
    fun readData(): IntArray {
        val result = IntArray(currentSize)
        buffer.duplicate().rewind().get(result)
        return result
    }

    @Synchronized
    fun clear() {
        buffer.clear()
        currentSizeInternal = 0
    }
}


data class CellLocation(val cellIndex: Int, val offset: Int)