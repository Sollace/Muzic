package com.sollace.muzic;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;

public class MuzicDiscItem extends MusicDiscItem {
    protected MuzicDiscItem() {
        super(0, SoundEvents.ENTITY_ITEM_PICKUP, new Item.Settings()
                .maxCount(1)
                .group(ItemGroup.MISC)
                .rarity(Rarity.RARE)
        );
        addPropertyGetter(new Identifier("pattern"), (stack, world, entity) -> {
            return forStack(stack)
                    .map(MuzicDisc::getPattern)
                    .map(MuzicDiscPattern::ordinal)
                    .orElse(0);
        });
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() == Blocks.JUKEBOX && !state.get(JukeboxBlock.HAS_RECORD)) {
            ItemStack stack = context.getStack();

            if (!world.isClient) {
                forStack(stack).ifPresent(disc -> {
                    ((JukeboxBlock)Blocks.JUKEBOX).setRecord(world, pos, state, stack);
                    world.playLevelEvent(null, 1010, pos, disc.getId().hashCode());
                });
                stack.decrement(1);
                PlayerEntity playerEntity = context.getPlayer();
                if (playerEntity != null) {
                    playerEntity.incrementStat(Stats.PLAY_RECORD);
                }
            }

            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (isIn(group)) {
            MuzicDiscRegistry.INSTANCE.values().forEach(disc -> {
                stacks.add(onStack(disc, new ItemStack(this)));
            });
        }
    }

    public int getColor(ItemStack stack, int layer) {
        return forStack(stack).map(disc -> disc.getColor(layer)).orElse(0xFFFFFF);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        forStack(stack).ifPresent(disc -> {
            tooltip.add(disc.getDescription().formatted(Formatting.GRAY));
        });
    }

    static ItemStack onStack(MuzicDisc disc, ItemStack stack) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putString("song", disc.getId().toString());
        return stack;
    }

    @Nullable
    public static Optional<MuzicDisc> forStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains("song", 8)) {
            return Optional.empty();
        }

        return MuzicDiscRegistry.INSTANCE.forId(new Identifier(tag.getString("song")));
    }
}
