package net.ririfa.skyline.mixin;

import net.minecraft.block.Block;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.ririfa.skyline.renderer.ChunkSSBO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@SuppressWarnings({"DuplicatedCode"})
@Mixin(ClientChunkManager.class)
public abstract class ClientChunkManagerMixin {
    @Inject(method = "loadChunkFromPacket", at = @At("RETURN"))
    public void onLoadChunkFromPacket(
            int cx,
            int cz,
            PacketByteBuf packetByteBuf,
            NbtCompound nbtCompound,
            Consumer<ChunkData.BlockEntityVisitor> consumer,
            CallbackInfoReturnable<WorldChunk> cir
    ) {
        WorldChunk chunk = cir.getReturnValue();
        if (chunk == null) return;

        int CHUNK_SIZE_X = 16;
        int CHUNK_SIZE_Y = 256;
        int CHUNK_SIZE_Z = 16;
        int CHUNK_BLOCK_COUNT = CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z;

        int[] blockIds = new int[CHUNK_BLOCK_COUNT];

        for (int x = 0; x < CHUNK_SIZE_X; x++) {
            for (int y = 0; y < CHUNK_SIZE_Y; y++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    BlockPos pos = new BlockPos(cx * 16 + x, y, cz * 16 + z);
                    BlockState state = chunk.getBlockState(pos);
                    int blockIndex = ((y * CHUNK_SIZE_Z + z) * CHUNK_SIZE_X + x);
                    blockIds[blockIndex] = Block.getRawIdFromState(state);
                }
            }
        }

        int chunkID = ChunkSSBO.getChunkID(cx, cz);

        if (ChunkSSBO.hasChunk(chunkID)) {
            ChunkSSBO.updateChunk(chunkID, blockIds);
        } else {
            ChunkSSBO.addChunk(chunkID, blockIds);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTick(BooleanSupplier booleanSupplier, boolean bl, CallbackInfo ci) {

    }
}
