package dev.micalobia.fullslabs.fabric.mixin;

import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity.ModelContext;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MixedSlabBlockEntity.class)
public abstract class MixedSlabBlockEntityMixin extends BlockEntity implements RenderDataBlockEntity {
    public MixedSlabBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    public abstract BlockState getTowardsState();

    @Shadow
    public abstract BlockState getAwayState();

    @Override
    public Object getRenderData() {
        var towardsState = this.getTowardsState();
        var awayState = this.getAwayState();
        return ModelContext.fromStates(towardsState, awayState);
    }
}
