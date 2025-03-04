package net.ririfa.skyline.renderer

import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL46.*
import net.ririfa.skyline.config.ConfigManager
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

object ChunkCache {
	private val pendingChunks = ConcurrentHashMap<Pair<Int, Int>, IntArray>()

	fun cacheChunk(chunkX: Int, chunkZ: Int, newData: IntArray) {
		val key = Pair(chunkX, chunkZ)
		val existingData = pendingChunks[key]

		if (existingData == null || !existingData.contentEquals(newData)) {
			pendingChunks[key] = newData
		}
	}

	fun flush() {
		if (pendingChunks.isEmpty()) return

		pendingChunks.forEach { (key, data) ->
			SSBO.updateChunkData(key.first, key.second, data)
		}
		pendingChunks.clear()
	}

	object SSBO {
		var ssbo: Int = 0
		private var buffer: ByteBuffer? = null
		private val chunkSizeX = ConfigManager.config.chunkSizeX
		private val chunkSizeZ = ConfigManager.config.chunkSizeZ
		private val CHUNK_SIZE = chunkSizeX * 320 * chunkSizeZ * Int.SIZE_BYTES
		private val MAX_CHUNKS = ConfigManager.config.maxChunks

		fun create() {
			ssbo = glGenBuffers()
			glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)

			val flags = GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT or GL_DYNAMIC_STORAGE_BIT
			glBufferStorage(GL_SHADER_STORAGE_BUFFER, (CHUNK_SIZE * MAX_CHUNKS).toLong(), flags)

			buffer = glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, (CHUNK_SIZE * MAX_CHUNKS).toLong(), flags)

			RenderSystem.recordRenderCall {
				glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo)
			}

			glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)
		}

		fun updateChunkData(chunkX: Int, chunkZ: Int, chunkData: IntArray) {
			if (buffer == null) return

			val offset = (chunkX * chunkSizeX + chunkZ * chunkSizeZ) * Int.SIZE_BYTES
			buffer!!.position(offset)
			buffer!!.asIntBuffer().put(chunkData)
		}

		fun destroy() {
			if (ssbo != 0) {
				glDeleteBuffers(ssbo)
			}
			ssbo = 0
		}
	}
}