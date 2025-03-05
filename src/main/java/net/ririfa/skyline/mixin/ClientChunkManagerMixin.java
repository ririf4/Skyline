package net.ririfa.skyline.mixin;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public abstract class ClientChunkManagerMixin {
    @Inject(method = "loadChunkFromPacket", at = @At("HEAD"))
    public void onLoadChunkFromPacket(
            int chunkX,
            int chunkZ,
            PacketByteBuf packetByteBuf,
            NbtCompound nbtCompound,
            Consumer<ChunkData.BlockEntityVisitor> consumer,
            CallbackInfoReturnable<WorldChunk> cir
    ) {
        // TODO
    }
}