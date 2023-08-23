package earth.terrarium.athena.api.client.models;

import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface AthenaBlockModel {

    List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction);

    /**
     * Returns the default quads for this model. These quads will be rendered if the model does not have any quads for
     * @param direction The direction to get the quads for or null if the quads are not cullable
     * @return The default quads for this model
     */
    default Map<Direction, List<AthenaQuad>> getDefaultQuads(@Nullable Direction direction) {
        return Map.of();
    }

    Int2ObjectMap<TextureAtlasSprite> getTextures(Function<Material, TextureAtlasSprite> getter);
}
