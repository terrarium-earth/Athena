package earth.terrarium.athena.api.client.forge;

import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.model.data.ModelDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record WrappedGetter(BlockAndTintGetter getter) implements AppearanceAndTintGetter {

    @Override
    public float getShade(@NotNull Direction direction, boolean bl) {
        return getter.getShade(direction, bl);
    }

    @Override
    public @NotNull LevelLightEngine getLightEngine() {
        return getter.getLightEngine();
    }

    @Override
    public int getBlockTint(@NotNull BlockPos blockPos, @NotNull ColorResolver colorResolver) {
        return getter.getBlockTint(blockPos, colorResolver);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull BlockPos blockPos) {
        return getter.getBlockEntity(blockPos);
    }

    @Override
    public @NotNull BlockState getBlockState(@NotNull BlockPos blockPos) {
        return getter.getBlockState(blockPos);
    }

    @Override
    public @NotNull FluidState getFluidState(@NotNull BlockPos blockPos) {
        return getter.getFluidState(blockPos);
    }

    @Override
    public int getHeight() {
        return getter.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return getter.getMinBuildHeight();
    }

    @Override
    public @Nullable BlockEntity getExistingBlockEntity(BlockPos pos) {
        return getter.getExistingBlockEntity(pos);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @Nullable ModelDataManager getModelDataManager() {
        return getter.getModelDataManager();
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockPos pos, Direction direction, BlockState fromState, BlockPos fromPos) {
        return state.getAppearance(this, pos, direction, fromState, fromPos);
    }

    @Override
    public BlockState getAppearance(BlockPos pos, Direction direction) {
        BlockState state = getter.getBlockState(pos);
        return state.getAppearance(this, pos, direction, null, null);
    }

    public BlockState getAppearance(BlockPos pos, Direction direction, BlockState fromState, BlockPos fromPos) {
        return query(pos, direction, fromState, fromPos).appearance();
    }

    @Override
    public Query query(BlockPos pos, Direction direction, BlockState fromState, BlockPos fromPos) {
        BlockState state = getter.getBlockState(pos);
        return new Query(state, state.getAppearance(this, pos, direction, fromState, fromPos));
    }
}
