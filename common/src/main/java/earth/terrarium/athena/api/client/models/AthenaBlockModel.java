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
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    default Map<Direction, List<AthenaQuad>> getDefaultQuads(@Nullable Direction direction) {
        return Map.of();
    }

    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    default Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter) {
        return Int2ObjectMap.ofEntries();
    }

    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    default AthenaModelAttributes getAttributes() {
        return AthenaModelAttributes.EMPTY;
    }

    default void selectParts(RandomSource random, BlockState state, Consumer<Part> output) {
        output.accept(AthenaSimpleUnbakedPartCache.simpleUnbakedPart(this));
    }

    default void collectPossibleParts(BlockState state, Consumer<Part> parts) {
        parts.accept(AthenaSimpleUnbakedPartCache.simpleUnbakedPart(this));
    }

    interface Part {
        List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction);

        /**
         * Returns the default quads for this model. These quads will be rendered if the model does not have any quads for
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
