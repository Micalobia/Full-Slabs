package dev.micalobia.fullslabs.handlers;

import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class OxidizableMixedHandler implements MixedHandler {
    private static final Requirements REQUIREMENTS = Requirements.builder().withRandomTicks().withOnUse().build();

    @Override
    public Requirements requirements() {
        return REQUIREMENTS;
    }

    @Override
    public void randomTick(Context context, BlockState state, ServerWorld world, BlockPos pos, Random random) {
        var oxidizable = (Oxidizable) state.getBlock();
        var mixed = context.blockEntity();
        var towards = context.towards();
        oxidizable.tryDegrade(state, world, pos, random).ifPresent(s -> mixed.setBlock(s.getBlock(), towards));
    }

    @Override
    public ActionResult onUse(Context context, BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return MixedHandler.super.onUse(context, state, world, pos, player, hit);
    }
}
