package com.sollace.muzic.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sollace.muzic.MuzicDiscItem;

@Mixin(JukeboxBlock.class)
abstract class MixinJukeboxBlock extends BlockWithEntity {
    MixinJukeboxBlock() {super(null);}
    @Inject(method = "getComparatorOutput(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I",
            at = @At("HEAD"))
    private void onGetComparatorOutput(BlockState state, World world, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity) {
            MuzicDiscItem.forStack(((JukeboxBlockEntity)blockEntity).getRecord()).ifPresent(disc -> {
                info.setReturnValue(disc.getComparatorOutput());
            });
        }
    }
}
