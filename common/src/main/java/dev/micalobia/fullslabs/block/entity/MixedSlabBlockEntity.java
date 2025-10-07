package dev.micalobia.fullslabs.block.entity;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class MixedSlabBlockEntity extends BlockEntity {
    // I'd like to find a better way to do what this does
    private static Pair<SlabBlock, SlabBlock> CACHE = new Pair<>((SlabBlock) Blocks.STONE_SLAB, (SlabBlock) Blocks.STONE_SLAB);

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
        return getCachedState().get(MixedSlabBlock.TYPE).state(this.towards, true);
    }

    public BlockState getAwayState() {
        return getCachedState().get(MixedSlabBlock.TYPE).state(this.away, false);
    }

    public BlockState getState(boolean towards) {
        return getCachedState().get(MixedSlabBlock.TYPE).state(towards ? this.towards : this.away, towards);
    }

    public BlockState getTargetedState(BlockHitResult crosshair) {
        var type = getCachedState().get(MixedSlabBlock.TYPE);
        var towards = type.isAxisTargetTowards(crosshair.getPos(), crosshair.getBlockPos());
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

    public boolean setTowards(SlabBlock slab) {
        return setBlock(slab, true);
    }

    public boolean setAway(SlabBlock slab) {
        return setBlock(slab, false);
    }

    public boolean setBlock(Block block, boolean towards) {
        var slab = VerticalSlabBlock.getRoot(block);
        if (!MixedHandlers.hasHandler(slab)) return false;
        if (towards) this.towards = slab;
        else this.away = slab;
        markDirty();
        return true;
    }

    public SlabBlock getTargetedSlab(BlockHitResult crosshair) {
        var type = getCachedState().get(MixedSlabBlock.TYPE);
        return getBlock(type.isAxisTargetTowards(crosshair.getPos(), crosshair.getBlockPos()));
    }

    @ApiStatus.Internal
    public static void writeCache(SlabBlock towards, SlabBlock away) {
        CACHE = new Pair<>(towards, away);
    }

    @ApiStatus.Internal
    public void readCache() {
        towards = CACHE.getLeft();
        away = CACHE.getRight();
    }

    @Override
    protected void writeData(WriteView view) {
        view.putString("towards_id", Registries.BLOCK.getId(towards).toString());
        view.putString("away_id", Registries.BLOCK.getId(away).toString());
        sync();
    }

    @Override
    protected void readData(ReadView view) {
        var towardsStr = view.getString("towards_id", "minecraft:stone_slab");
        var awayStr = view.getString("away_id", "minecraft:stone_slab");
        if (Registries.BLOCK.get(Identifier.of(towardsStr)) instanceof SlabBlock slab) this.towards = slab;
        else {
            FullSlabs.LOGGER.warn("missing \"{}\": replacing with \"minecraft:stone_slab\"", towardsStr);
            this.towards = (SlabBlock) Blocks.STONE_SLAB;
        }
        if (Registries.BLOCK.get(Identifier.of(awayStr)) instanceof SlabBlock slab) this.away = slab;
        else {
            FullSlabs.LOGGER.warn("missing \"{}\": replacing with \"minecraft:stone_slab\"", awayStr);
            this.away = (SlabBlock) Blocks.STONE_SLAB;
        }
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        markDirty();
        return true;
    }

    private void sync() {
        if (this.world != null)
            this.world.addSyncedBlockEvent(this.pos, getCachedState().getBlock(), 0, 0);
    }
}
