package earth.terrarium.athena.api.client.models;

import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import earth.terrarium.athena.impl.internal.AthenaSimpleUnbakedPartCache;
import earth.terrarium.athena.impl.loading.AthenaResourceLoader;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface AthenaBlockModel {
    MapCodec<AthenaBlockModel> CODEC = AthenaModelType.CODEC
        .dispatchMap(
            AthenaResourceLoader.KEY,
            AthenaBlockModel::type,
            AthenaModelType::codec
        );

    AthenaModelType type();

    /**
     * @deprecated Use {@link Part#getQuads} instead
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    default List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        return List.of();
    }

    /**
     * Returns the default quads for this model. These quads will be rendered if the model does not have any quads for
     *
     * @param direction The direction to get the quads for or null if the quads are not cullable
     * @return The default quads for this model
     * @deprecated Use {@link Part#getDefaultQuads} instead
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    default Map<Direction, List<AthenaQuad>> getDefaultQuads(@Nullable Direction direction) {
        return Map.of();
    }

    /**
     * @deprecated Use {@link Part#getTextures} instead
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    default Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter) {
        return Int2ObjectMap.ofEntries();
    }

    /**
     * @deprecated Use {@link Part#getAttributes} instead
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    default AthenaModelAttributes getAttributes() {
        return AthenaModelAttributes.EMPTY;
    }

    /**
     * Select 1 or more {@link Part} to render, parts this method select must have been chosen as possible before via {@link getAllParts}
     *
     * @param random The positional random to select the parts based on
     * @param state  The block state to select parts for
     * @return The parts to be rendered
     */
    default List<Part> selectParts(RandomSource random, BlockState state) {
        return AthenaSimpleUnbakedPartCache.simpleUnbakedPart(this);
    }

    /**
     * {@return every {@link Part} that this model can select via {@link selectParts}}
     *
     * @param state The block state to select the possible parts for
     */
    default List<Part> getAllParts(BlockState state) {
        return AthenaSimpleUnbakedPartCache.simpleUnbakedPart(this);
    }

    interface Part {
        List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction);

        /**
         * Returns the default quads for this model part. These quads will be rendered if the part does not have any quads for
         *
         * @param direction The direction to get the quads for or null if the quads are not cullable
         * @return The default quads for this model
         */
        default Map<Direction, List<AthenaQuad>> getDefaultQuads(@Nullable Direction direction) {
            return Object2ObjectMaps.emptyMap();
        }

        Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter);

        default AthenaModelAttributes getAttributes() {
            return AthenaModelAttributes.EMPTY;
        }
    }
}
