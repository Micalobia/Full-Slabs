package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity.ModelContext;
import dev.micalobia.fullslabs.neoforge.FullSlabsNeoForge;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.model.data.ModelData;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(MixedSlabBlockEntity.class)
@MethodsReturnNonnullByDefault
public abstract class MixedSlabBlockEntityMixin extends BlockEntity {
    @Shadow
    public abstract BlockState getTowardsState();

    @Shadow
    public abstract BlockState getAwayState();

    public MixedSlabBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public ModelData getModelData() {
        var towardsState = this.getTowardsState();
        var awayState = this.getAwayState();
        var context = ModelContext.fromStates(towardsState, awayState);
        return ModelData.of(FullSlabsNeoForge.MIXED_CONTEXT_MODEL_PROPERTY, context);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void updateModelAfterRead(ValueInput view, CallbackInfo ci) {
        this.requestModelDataUpdate();
        if (this.level == null) return;
        var state = getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
    }
}



