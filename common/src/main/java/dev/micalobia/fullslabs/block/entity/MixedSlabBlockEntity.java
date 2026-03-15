package dev.micalobia.fullslabs.block.entity;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MixedSlabBlockEntity extends BlockEntity {
    // I'd like to find a better way to do what this does
    private static Tuple<SlabBlock, SlabBlock> CACHE = new Tuple<>((SlabBlock) Blocks.STONE_SLAB, (SlabBlock) Blocks.STONE_SLAB);

    private SlabBlock towards;
    private SlabBlock away;

    public MixedSlabBlockEntity(BlockPos pos, BlockState state, SlabBlock towards, SlabBlock away) {
        super(SlabRegistry.MIXED_SLAB_ENTITY.get(), pos, state);
        this.towards = towards;
        this.away = away;
    }

    public MixedSlabBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, (SlabBlock) Blocks.STONE_SLAB, (SlabBlock) Blocks.STONE_SLAB);
    }

    public BlockState getTowardsState() {
        return getBlockState().getValue(MixedSlabBlock.TYPE).state(this.towards, true);
    }

    public BlockState getAwayState() {
        return getBlockState().getValue(MixedSlabBlock.TYPE).state(this.away, false);
    }

    public BlockState getState(boolean towards) {
        return getBlockState().getValue(MixedSlabBlock.TYPE).state(towards ? this.towards : this.away, towards);
    }

    public BlockState getTargetedState(BlockHitResult crosshair) {
        return getTargetedState(crosshair.getBlockPos(), crosshair.getLocation());
    }

    public BlockState getTargetedState(BlockPos location, Vec3 pos) {
        var type = getBlockState().getValue(MixedSlabBlock.TYPE);
        var towards = type.isAxisTargetTowards(pos, location);
        return type.state(towards ? this.towards : this.away, towards);
    }

    public SlabBlock getTowards() {
        return this.towards;
    }

    public SlabBlock getAway() {
        return this.away;
    }

    public SlabBlock getBlock(boolean towards) {
        return towards ? this.towards : this.away;
    }

    public boolean setTowards(Block slab) {
        return setBlock(slab, true);
    }

    public boolean setAway(Block slab) {
        return setBlock(slab, false);
    }

    public boolean setBlock(Block block, boolean towards) {
        var slab = VerticalSlabBlock.tryGetRoot(block);
        if (slab.isEmpty()) return false;
        if (!MixedHandlers.hasHandler(slab.get())) return false;
        if (towards) this.towards = slab.get();
        else this.away = slab.get();
        refreshLight();
        setChanged();
        syncModel();
        return true;
    }

    public boolean setBlocks(Block towards, Block away) {
        var towardsRoot = VerticalSlabBlock.tryGetRoot(towards);
        if (towardsRoot.isEmpty() || !MixedHandlers.hasHandler(towardsRoot.get())) return false;
        var awayRoot = VerticalSlabBlock.tryGetRoot(away);
        if (awayRoot.isEmpty() || !MixedHandlers.hasHandler(awayRoot.get())) return false;
        this.towards = towardsRoot.get();
        this.away = awayRoot.get();
        refreshLight();
        setChanged();
        syncModel();
        return true;
    }

    private void refreshLight() {
        if (this.level == null) return;
        var towardsEmission = getTowardsState().getLightEmission();
        var awayEmission = getAwayState().getLightEmission();
        var emission = Math.max(towardsEmission, awayEmission);
        var state = getBlockState().setValue(MixedSlabBlock.LEVEL, emission);
        this.level.setBlock(this.worldPosition, state, 0);
    }

    public SlabBlock getTargetedSlab(BlockHitResult crosshair) {
        var type = getBlockState().getValue(MixedSlabBlock.TYPE);
        return getBlock(type.isAxisTargetTowards(crosshair.getLocation(), crosshair.getBlockPos()));
    }

    @ApiStatus.Internal
    public static void writeCache(SlabBlock towards, SlabBlock away) {
        CACHE = new Tuple<>(towards, away);
    }

    @ApiStatus.Internal
    public void readCache() {
        this.towards = CACHE.getA();
        this.away = CACHE.getB();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.putString("towards_id", BuiltInRegistries.BLOCK.getKey(this.towards).toString());
        output.putString("away_id", BuiltInRegistries.BLOCK.getKey(this.away).toString());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        var towardsStr = input.getStringOr("towards_id", "minecraft:stone_slab");
        var awayStr = input.getStringOr("away_id", "minecraft:stone_slab");
        if (BuiltInRegistries.BLOCK.getValue(Identifier.parse(towardsStr)) instanceof SlabBlock slab)
            this.towards = slab;
        else {
            FullSlabs.LOGGER.warn("missing \"{}\": replacing with \"minecraft:stone_slab\"", towardsStr);
            this.towards = (SlabBlock) Blocks.STONE_SLAB;
        }
        if (BuiltInRegistries.BLOCK.getValue(Identifier.parse(awayStr)) instanceof SlabBlock slab)
            this.away = slab;
        else {
            FullSlabs.LOGGER.warn("missing \"{}\": replacing with \"minecraft:stone_slab\"", awayStr);
            this.away = (SlabBlock) Blocks.STONE_SLAB;
        }
        refreshLight();
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void syncModel() {
        Objects.requireNonNull(this.level).blockEvent(this.worldPosition, this.getBlockState().getBlock(), 0, 0);
    }

    public record ModelContext(int towards, int away) {
        public static ModelContext fromStates(BlockState towards, BlockState away) {
            return new ModelContext(Block.getId(towards), Block.getId(away));
        }

        public BlockState towardsState() {
            return Block.stateById(this.towards);
        }

        public BlockState awayState() {
            return Block.stateById(this.away);
        }
    }
}
