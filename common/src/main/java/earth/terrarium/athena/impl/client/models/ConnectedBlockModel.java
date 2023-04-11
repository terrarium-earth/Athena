package earth.terrarium.athena.impl.client.models;

import com.google.gson.JsonObject;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.CtmState;
import earth.terrarium.athena.api.client.utils.CtmUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConnectedBlockModel implements AthenaBlockModel {

    public static final AthenaModelFactory FACTORY = new Factory();

    private static final List<AthenaQuad> CENTER = List.of(AthenaQuad.withSprite(1));

    private final Int2ObjectMap<Material> materials;

    public ConnectedBlockModel(Int2ObjectMap<Material> materials) {
        this.materials = materials;
    }

    @Override
    public List<AthenaQuad> getQuads(BlockAndTintGetter level, BlockState blockState, BlockPos pos, Direction direction) {
        if (level.getBlockState(pos.relative(direction)).is(blockState.getBlock())) {
            return List.of();
        }

        final CtmState state = CtmState.from(level, pos, direction, other -> other == blockState);

        if (state.allTrue()) {
            return CENTER;
        }

        return List.of(
                AthenaQuad.withState(state.up(), state.left(), state.upLeft(), 0, 0.5f, 1f, 0.5f),
                AthenaQuad.withState(state.up(), state.right(), state.upRight(), 0.5f, 1f, 1f, 0.5f),
                AthenaQuad.withState(state.down(), state.left(), state.downLeft(), 0, 0.5f, 0.5f, 0f),
                AthenaQuad.withState(state.down(), state.right(), state.downRight(), 0.5f, 1f, 0.5f, 0f)
        );
    }

    @Override
    public Int2ObjectMap<TextureAtlasSprite> getTextures(Function<Material, TextureAtlasSprite> getter) {
        Int2ObjectMap<TextureAtlasSprite> textures = new Int2ObjectArrayMap<>();
        for (var entry : materials.int2ObjectEntrySet()) {
            textures.put(entry.getIntKey(), getter.apply(entry.getValue()));
        }
        return textures;
    }

    private static class Factory implements AthenaModelFactory {

        @Override
        public Supplier<AthenaBlockModel> create(JsonObject json) {
            final var materials = CtmUtils.parseCtmMaterials(GsonHelper.getAsJsonObject(json, "ctm_textures"));
            return () -> new ConnectedBlockModel(materials);
        }
    }
}
