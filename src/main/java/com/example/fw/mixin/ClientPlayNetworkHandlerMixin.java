package com.example.fw.mixin;

import com.example.fw.util.ChunkActivityTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onBlockUpdate", at = @At("TAIL"))
    private void fw$blockUpdate(BlockUpdateS2CPacket p, CallbackInfo ci) {
        var player = MinecraftClient.getInstance().player;
        if (player == null || p.getPos().isWithinDistance(player.getBlockPos(), 12)) return;
        ChunkActivityTracker.record(new ChunkPos(p.getPos()));
    }

    @Inject(method = "onChunkDeltaUpdate", at = @At("TAIL"))
    private void fw$delta(ChunkDeltaUpdateS2CPacket p, CallbackInfo ci) {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;
        p.visitUpdates((pos, state) -> {
            if (!pos.isWithinDistance(player.getBlockPos(), 12))
                ChunkActivityTracker.record(new ChunkPos(pos));
        });
    }
}
