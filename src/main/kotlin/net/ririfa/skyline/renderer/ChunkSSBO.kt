package net.ririfa.skyline.renderer

object ChunkSSBO {

    @JvmRecord
    data class ChunkSnapshot(
        val x: Int,
        val z: Int,
        val height: Int
    )
}