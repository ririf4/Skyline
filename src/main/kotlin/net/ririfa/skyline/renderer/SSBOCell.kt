package net.ririfa.skyline.renderer

data class SSBOCell(
	val startX: Int,
	val startZ: Int,
	val cellSize: Int
) {
	companion object {
		fun generateGrid(totalChunkSize: Pair<Int, Int>, cellSize: Int): List<SSBOCell> {
			val (totalX, totalZ) = totalChunkSize
			val grid = mutableListOf<SSBOCell>()

			for (z in 0 until totalZ step cellSize) {
				for (x in 0 until totalX step cellSize) {
					grid.add(SSBOCell(x, z, cellSize))
				}
			}

			return grid
		}
	}


	fun getChunks(): Sequence<Pair<Int, Int>> {
		return sequence {
			for (z in startZ until startZ + cellSize) {
				for (x in startX until startX + cellSize) {
					yield(x to z)
				}
			}
		}
	}

	fun getFlatIndex(chunkX: Int, chunkZ: Int): Int {
		val dx = chunkX - startX
		val dz = chunkZ - startZ
		return dz * cellSize + dx
	}

	fun getSSBORange(flatIndex: Int, perChunkSize: Int, totalSSBOSize: Int): IntRange {
		val offset = flatIndex * perChunkSize
		val end = (offset + perChunkSize).coerceAtMost(totalSSBOSize)
		return offset until end
	}
}
