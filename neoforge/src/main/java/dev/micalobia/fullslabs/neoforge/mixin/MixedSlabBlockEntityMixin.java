package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity.ModelContext;
import dev.micalobia.fullslabs.ducks.MixedSlabBlockEntityDuck;
import dev.micalobia.fullslabs.neoforge.FullSlabsNeoForge;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
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
@SuppressWarnings("AddedMixinMembersNamePattern") // This is our class
public abstract class MixedSlabBlockEntityMixin extends BlockEntity implements MixedSlabBlockEntityDuck {
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

    @Override
    public void syncPlatformModel() {
        this.requestModelDataUpdate();
        if (this.world == null) return;
        var state = getCachedState();
        this.world.updateListeners(this.pos, state, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void updateModelAfterRead(ReadView view, CallbackInfo ci) {
        this.syncPlatformModel();
    }
}



