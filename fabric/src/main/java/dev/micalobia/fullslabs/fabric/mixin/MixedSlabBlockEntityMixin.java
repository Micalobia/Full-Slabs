package dev.micalobia.fullslabs.fabric.mixin;

import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity.ModelContext;
import dev.micalobia.fullslabs.ducks.MixedSlabBlockEntityDuck;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MixedSlabBlockEntity.class)
@SuppressWarnings("AddedMixinMembersNamePattern") // This is our class
public abstract class MixedSlabBlockEntityMixin extends BlockEntity implements RenderDataBlockEntity, MixedSlabBlockEntityDuck {
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

    @Override
    public void syncPlatformModel() {
        if (this.world != null)
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL_AND_REDRAW | Block.FORCE_STATE);
    }
}
