package net.ririfa.skyline.mixin;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.world.chunk.WorldChunk;
import net.ririfa.skyline.config.ConfigManager;
import net.ririfa.skyline.renderer.ChunkCache;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public abstract class ClientChunkManagerMixin {

    @Inject(method = "loadChunkFromPacket", at = @At("HEAD"))
    public void onLoadChunkFromPacket(
            int chunkX, int chunkZ, PacketByteBuf packetByteBuf, NbtCompound nbtCompound,
            Consumer<ChunkData.BlockEntityVisitor> consumer,
            CallbackInfoReturnable<WorldChunk> cir
    ) {
        int[] chunkData = extractChunkData(packetByteBuf);
        ChunkCache.INSTANCE.cacheChunk(chunkX, chunkZ, chunkData);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(BooleanSupplier booleanSupplier, boolean bl, CallbackInfo ci) {
        ChunkCache.INSTANCE.flush();
    }

    @Unique
    private int @NotNull [] extractChunkData(PacketByteBuf packetByteBuf) {
        int chunkSizeX = ConfigManager.config.chunkSizeX;
        int chunkSizeZ = ConfigManager.config.chunkSizeZ;

        int dataSize = chunkSizeX * 320 * chunkSizeZ;
        int[] chunkData = new int[dataSize];

        for (int i = 0; i < dataSize; i++) {
            chunkData[i] = packetByteBuf.readInt();
        }

        return chunkData;
    }
}