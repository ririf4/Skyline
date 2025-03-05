package net.ririfa.skyline.renderer

import java.nio.IntBuffer

import java.nio.*

class SSBOCell(val maxSize: Int) {
    private val buffer: IntBuffer = ByteBuffer.allocateDirect(maxSize * Int.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer()

    val currentSize: Int
        get() = buffer.position()

    fun addData(newData: IntArray) {
        if (currentSize + newData.size > maxSize) {
            throw IllegalArgumentException("Data size exceeds maximum size!")
        }
        buffer.put(newData)
    }

    fun readData(): IntArray {
        val result = IntArray(currentSize)
        buffer.rewind()
        buffer.get(result)
        return result
    }
}


data class CellLocation(val cellIndex: Int, val offset: Int)