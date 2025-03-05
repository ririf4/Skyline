package net.ririfa.skyline.renderer

import com.mojang.blaze3d.systems.RenderSystem
import net.ririfa.skyline.Config
import org.lwjgl.opengl.GL46.*
import net.ririfa.skyline.config.ConfigManager
import java.nio.ByteBuffer

object ChunkCache {
	private val cells = mutableListOf<SSBOCell>()
	private val chunkToCellMapping = mutableMapOf<Pair<Int, Int>, CellLocation>()

	fun createOrUpdateChunk(chunkX: Int, chunkZ: Int, newData: IntArray) {
		val chunkKey = Pair(chunkX, chunkZ)
		var location = chunkToCellMapping[chunkKey]
		var cell = location?.let { cells[it.cellIndex] }

		if (cell == null || (Config.getCellSize() - location!!.offset) < newData.size) {
			cell = SSBOCell(Config.getCellSize())
			cells.add(cell)
			location = CellLocation(cells.size - 1, 0)
			chunkToCellMapping[chunkKey] = location
		}

		val offset = location.offset
		cell.addData(newData)
		chunkToCellMapping[chunkKey] = CellLocation(location.cellIndex, offset + newData.size)

		SSBO.updateChunkData(location.cellIndex, offset, newData)
	}

	fun rebuildCells(newCellSize: Int) {
		val allChunks = chunkToCellMapping.keys.toList()
		val allData = allChunks.map { key ->
			val location = chunkToCellMapping[key]!!
			val cell = cells[location.cellIndex]
			key to cell.readData().copyOfRange(location.offset, location.offset + newCellSize)
		}

		cells.clear()
		chunkToCellMapping.clear()

		for ((key, chunkData) in allData) {
			createOrUpdateChunk(key.first, key.second, chunkData)
		}
	}

	fun removeChunk(chunkX: Int, chunkZ: Int) {
		val key = Pair(chunkX, chunkZ)
		val location = chunkToCellMapping.remove(key) ?: return

		SSBO.clearCell(location.cellIndex, location.offset)

		if (cells[location.cellIndex].currentSize == 0) {
			cells.removeAt(location.cellIndex)
		}
	}

	object SSBO {
		var ssbo: Int = 0
		private var buffer: ByteBuffer? = null
		private val CHUNK_SIZE = Config.getCellSize()
		private val MAX_CHUNKS = ConfigManager.config.maxChunks
		private val zeroBuffer = IntArray(CHUNK_SIZE / Int.SIZE_BYTES)

		fun create() {
			ssbo = glGenBuffers()
			glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)

			val flags = GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT or GL_DYNAMIC_STORAGE_BIT
			glBufferStorage(GL_SHADER_STORAGE_BUFFER, (CHUNK_SIZE * MAX_CHUNKS).toLong(), flags)
			glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT)

			buffer = glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, (CHUNK_SIZE * MAX_CHUNKS).toLong(), flags)

			RenderSystem.recordRenderCall {
				glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo)
			}

			glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)
		}

		fun updateChunkData(cellIndex: Int, offset: Int, newData: IntArray) {
			if (buffer == null) throw IllegalStateException("SSBO buffer is not initialized!")

			val absoluteOffset = cellIndex * CHUNK_SIZE + offset
			if (absoluteOffset < 0 || absoluteOffset + newData.size > buffer!!.capacity()) {
				throw IndexOutOfBoundsException("Invalid offset!")
			}

			buffer!!.asIntBuffer().apply {
				position(absoluteOffset / Int.SIZE_BYTES)
				newData.forEachIndexed { index, value ->
					put(index, value)
				}
			}
		}

		fun clearCell(cellIndex: Int, offset: Int) {
			if (buffer == null) throw IllegalStateException("SSBO buffer is not initialized!")

			val absoluteOffset = cellIndex * CHUNK_SIZE + offset
			if (absoluteOffset in 0 until buffer!!.capacity() - CHUNK_SIZE) {
				buffer!!.position(absoluteOffset)
				buffer!!.asIntBuffer().put(zeroBuffer, 0, zeroBuffer.size)
			} else {
				throw IndexOutOfBoundsException("Invalid offset for clearCell")
			}
		}

		fun destroy() {
			buffer = null
			if (ssbo != 0) {
				glDeleteBuffers(ssbo)
			}
			ssbo = 0
		}
	}
}
