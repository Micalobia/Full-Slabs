package dev.micalobia.fullslabs.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperSlabBlock;
import net.minecraft.world.level.block.state.BlockState;

public class OxidizableVerticalSlabBlock extends VerticalSlabBlock implements WeatheringCopper {
    private final WeatherState oxidationLevel;

    public OxidizableVerticalSlabBlock(WeatheringCopperSlabBlock block, Properties properties) {
        super(block, properties);
        this.oxidationLevel = block.getAge();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        this.changeOverTime(state, world, pos, random);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return WeatheringCopper.getNext(this.parent).isPresent();
    }

    @Override
    public WeatherState getAge() {
        return this.oxidationLevel;
    }
}
