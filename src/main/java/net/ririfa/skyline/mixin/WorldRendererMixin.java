package net.ririfa.skyline.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.WorldChunk;
import net.ririfa.skyline.AliasesKt;
import net.ririfa.skyline.ConfigManager;
import net.ririfa.skyline.renderer.ChunkSSBO;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

@SuppressWarnings({"DuplicatedCode"})
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "updateChunks", at = @At("RETURN"))
    private void injectSkylineSSBO(CallbackInfo ci) {
        var client = AliasesKt.getClient();
        if (client.world == null) return;

        ClientChunkManager chunkManager = client.world.getChunkManager();
        if (!(chunkManager instanceof ClientChunkManager)) return;

        ClientChunkManager.ClientChunkMap chunkMap = chunkManager.chunks;
        AtomicReferenceArray<WorldChunk> chunkArray = chunkMap.chunks;

        int CHUNK_SIZE_X = 16;
        int CHUNK_SIZE_Y = 256;
        int CHUNK_SIZE_Z = 16;
        int CHUNK_BLOCK_COUNT = CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z;

        for (int i = 0; i < chunkArray.length(); i++) {
            WorldChunk chunk = chunkArray.get(i);
            if (chunk == null) continue;

            int cx = chunk.getPos().x;
            int cz = chunk.getPos().z;
            int chunkID = ChunkSSBO.getChunkID(cx, cz);

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

            ChunkSSBO.updateChunk(chunkID, blockIds);
        }
    }

    @Inject(method = "renderMain", at = @At("HEAD"))
    private void injectSkylineRender(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Camera camera, Matrix4f projMatrix, Matrix4f viewMatrix, Fog fog, boolean bl, boolean bl2, RenderTickCounter renderTickCounter, Profiler profiler, CallbackInfo ci) {

    }

    @Unique
    private @NotNull FloatBuffer matrixToBuffer(@NotNull Matrix4f matrix) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        buffer.flip();
        return buffer;
    }

    @Unique
    private @NotNull Matrix4f getViewMatrix(@NotNull Camera camera) {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();

        Vec3d pos = camera.getPos();
        viewMatrix.translate((float) -pos.x, (float) -pos.y, (float) -pos.z);

        Quaternionf rotation = camera.getRotation();
        Matrix3f rotationMatrix = new Matrix3f();
        rotation.get(rotationMatrix);

        viewMatrix.mul(new Matrix4f(rotationMatrix));

        return viewMatrix;
    }
}
