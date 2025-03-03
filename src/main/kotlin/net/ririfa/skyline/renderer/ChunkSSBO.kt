package net.ririfa.skyline.renderer

import net.ririfa.skyline.ConfigManager
import net.ririfa.skyline.Logger
import org.lwjgl.opengl.GL46.*

object ChunkSSBO {
	var ssboId = 0
	private val chunkSizeX = ConfigManager.config.chunkSizeX
	private val chunkSizeZ = ConfigManager.config.chunkSizeZ
	private val CHUNK_SIZE = chunkSizeX * 256 * chunkSizeZ * Int.SIZE_BYTES
	private val MAX_CHUNKS = ConfigManager.config.maxChunks
	private val chunkMap = object : LinkedHashMap<Int, Int>(MAX_CHUNKS, 0.75f, true) {
		override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Int>?): Boolean {
			if (size > MAX_CHUNKS) {
				eldest?.let {
					Logger.debug("Removing old chunk: ${it.key}")
					removeChunk(it.key)
				}
				return true
			}
			return false
		}
	}

	fun create() {
		ssboId = glGenBuffers()
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboId)
		glBufferData(GL_SHADER_STORAGE_BUFFER, CHUNK_SIZE * MAX_CHUNKS.toLong(), GL_DYNAMIC_DRAW)
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssboId)
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)
	}

	@JvmStatic
	fun updateChunk(chunkID: Int, data: IntArray) {
		val chunkOffset = chunkMap[chunkID] ?: return
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboId)
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, chunkOffset.toLong(), data)
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)
	}

	@JvmStatic
	fun addChunk(chunkID: Int, data: IntArray) {
		val newChunkIndex = chunkMap.size
		if (newChunkIndex >= MAX_CHUNKS) {
			Logger.debug("ChunkSSBO is full, old chunks will be automatically removed")
		}

		val chunkOffset = newChunkIndex * CHUNK_SIZE
		chunkMap[chunkID] = chunkOffset

		glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboId)
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, chunkOffset.toLong(), data)
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)
	}

	@JvmStatic
	fun hasChunk(chunkID: Int): Boolean {
		return chunkMap.containsKey(chunkID)
	}

	private fun removeChunk(chunkID: Int) {
		chunkMap.remove(chunkID)
	}

	@JvmStatic
	fun getChunkID(cx: Int, cz: Int): Int {
		return ((cx / chunkSizeX and 0xFFFF) shl 16) or (cz / chunkSizeZ and 0xFFFF)
	}

	fun delete() {
		if (ssboId != 0) {
			glDeleteBuffers(ssboId)
			ssboId = 0
		}
	}
}
