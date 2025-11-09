package dev.micalobia.fullslabs.fabric.mixin;

import dev.micalobia.fullslabs.block.MixedSlabBlock;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePickItemFromBlock", at = @At("HEAD"))
    private void skimPlayer(ServerboundPickItemFromBlockPacket packet, CallbackInfo ci) {
        MixedSlabBlock.cachedPlayer = this.player;
    }
}
