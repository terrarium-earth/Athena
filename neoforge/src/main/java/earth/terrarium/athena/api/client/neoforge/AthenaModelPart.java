package earth.terrarium.athena.api.client.neoforge;

import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.models.TintProvider;
import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public record AthenaModelPart(
        @NotNull BlockModelPart parent,
        @NotNull NullableEnumMap<Direction, Map<Direction, List<AthenaQuad>>> quads,
        @NotNull Int2ObjectMap<TextureAtlasSprite> textures,
        @Nullable TintProvider tint
) implements BlockModelPart {

    @Override
    public @NotNull ChunkSectionLayer getRenderType(@NotNull BlockState state) {
        return this.parent.getRenderType(state);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable Direction direction) {
        List<BakedQuad> quads = new ArrayList<>();
        this.quads.getOrDefault(direction, Map.of()).forEach((dir, quadList) -> {
            for (var quad : quadList) {
                TextureAtlasSprite sprite = textures.get(quad.sprite());
                if (sprite == null) continue;
                quads.addAll(ForgeAthenaUtils.bakeQuad(quad, dir, sprite, this.tint));
            }
        });
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public @NotNull TextureAtlasSprite particleIcon() {
        return this.parent.particleIcon();
    }
}
