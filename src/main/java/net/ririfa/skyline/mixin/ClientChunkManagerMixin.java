package net.ririfa.skyline.mixin;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import net.ririfa.skyline.renderer.ChunkSSBO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public abstract class ClientChunkManagerMixin {
    @Shadow
    public volatile ClientChunkManager.ClientChunkMap chunks;

    @Inject(method = "loadChunkFromPacket", at = @At("HEAD"))
    public void onLoadChunkFromPacket(
            int chunkX,
            int chunkZ,
            PacketByteBuf packetByteBuf,
            Map<Heightmap.Type, long[]> map,
            Consumer<ChunkData.BlockEntityVisitor> consumer,
            CallbackInfoReturnable<WorldChunk> cir
    ) {
        if (!chunks.isInRadius(chunkX, chunkZ)) return;

        long[] heightmap = map.get(Heightmap.Type.WORLD_SURFACE);
        if (heightmap == null) return;

        int totalHeight = 0;
        int count = 0;
        for (long packed : heightmap) {
            for (int shift = 0; shift < 64; shift += 9) {
                int height = (int)((packed >> shift) & 0x1FF); // 9bit per height
                if (height > 0) {
                    totalHeight += height;
                    count++;
                }
            }
        }
        int averageHeight = count > 0 ? totalHeight / count : 64;

        ChunkSSBO.ChunkSnapshot snapshot = new ChunkSSBO.ChunkSnapshot(
                chunkX,
                chunkZ,
                averageHeight
        );
    }
}