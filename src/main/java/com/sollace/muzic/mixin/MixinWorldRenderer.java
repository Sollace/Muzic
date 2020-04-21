package com.sollace.muzic.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sollace.muzic.MuzicDisc;
import com.sollace.muzic.MuzicDiscRegistry;

@Mixin(WorldRenderer.class)
abstract class MixinWorldRenderer {
    @Redirect(method = "playSong(Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/MusicDiscItem;bySound(Lnet/minecraft/sound/SoundEvent;)Lnet/minecraft/item/MusicDiscItem;"))
    private MusicDiscItem onBySound(SoundEvent sound) {
        MuzicDisc disc = MuzicDiscRegistry.INSTANCE.forSound(sound).orElse(null);

        if (disc != null) {
            MinecraftClient.getInstance().inGameHud.setRecordPlayingOverlay(disc.getDescription().asFormattedString());
            return null;
        }

        return MusicDiscItem.bySound(sound);
    }

    @Inject(method = "playLevelEvent(Lnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onPlayLevelEvent(PlayerEntity source, int type, BlockPos pos, int data,
            CallbackInfo info) {

        if (type == 1010) {
            MuzicDiscRegistry.INSTANCE.forRawId(data).ifPresent(disc -> {
                info.cancel();
                ((WorldRenderer)(Object)this).playSong(disc.getSound(), pos);
            });
        }
    }
}
