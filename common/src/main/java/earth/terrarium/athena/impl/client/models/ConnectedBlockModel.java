package earth.terrarium.athena.impl.client.models;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import earth.terrarium.athena.api.client.utils.CtmState;
import earth.terrarium.athena.api.client.utils.CtmUtils;
import earth.terrarium.athena.impl.client.models.ctm.ConnectedTextureMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConnectedBlockModel implements AthenaBlockModel {

    public static final AthenaModelFactory FACTORY = new Factory();

    private final ConnectedTextureMap materials;

    public ConnectedBlockModel(ConnectedTextureMap materials) {
        this.materials = materials;
    }

    @Override
    public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState blockState, BlockPos pos, Direction direction) {
        if (level.getBlockState(pos.relative(direction)).is(blockState.getBlock())) {
            return List.of();
        }

        final CtmState state = CtmState.from(level, pos, direction, other -> other == blockState);

        if (state.allTrue()) {
            return List.of(AthenaQuad.withSprite(materials.getTexture(direction, 1)));
        }

        return List.of(
                AthenaQuad.withState(materials, direction, state.up(), state.left(), state.upLeft(), 0, 0.5f, 1f, 0.5f),
                AthenaQuad.withState(materials, direction, state.up(), state.right(), state.upRight(), 0.5f, 1f, 1f, 0.5f),
                AthenaQuad.withState(materials, direction, state.down(), state.left(), state.downLeft(), 0, 0.5f, 0.5f, 0f),
                AthenaQuad.withState(materials, direction, state.down(), state.right(), state.downRight(), 0.5f, 1f, 0.5f, 0f)
        );
    }

    @Override
    public Int2ObjectMap<TextureAtlasSprite> getTextures(Function<Material, TextureAtlasSprite> getter) {
        return materials.getTextures(getter);
    }

    private static class Factory implements AthenaModelFactory {

        @Override
        public Supplier<AthenaBlockModel> create(JsonObject json) {
            ConnectedTextureMap materials = CtmUtils.tryParse(GsonHelper.getAsJsonObject(json, "ctm_textures"), Factory::parseDefaultMaterials);
            if (materials == null) {
                materials = CtmUtils.tryParse(GsonHelper.getAsJsonObject(json, "ctm_textures"), Factory::parseMaterials);
            }
            if (materials == null) {
                throw new JsonSyntaxException("Expected either ctm_textures to have 5 entries for all textures or " +
                    "have directional textures for each direction or to have some directions and a default textures object.");
            }
            final var materialsFinal = materials;
            return () -> new ConnectedBlockModel(materialsFinal);
        }

        private static ConnectedTextureMap parseMaterials(JsonObject json) {
            final ConnectedTextureMap materials = new ConnectedTextureMap();
            for (var direction : Direction.values()) {
                if (GsonHelper.isStringValue(json, direction.getSerializedName())) {
                    materials.put(direction, CtmUtils.blockMat(GsonHelper.getAsString(json, direction.getSerializedName())));
                    continue;
                }
                final var directionMaterials = CtmUtils.parseCtmMaterials(GsonHelper.getAsJsonObject(json, direction.getSerializedName(), GsonHelper.getAsJsonObject(json, "default")));
                materials.put(direction, directionMaterials);
            }
            return materials;
        }

        private static ConnectedTextureMap parseDefaultMaterials(JsonObject json) {
            final var materials = CtmUtils.parseCtmMaterials(json);
            final ConnectedTextureMap connectedTextureMap = new ConnectedTextureMap();
            for (var direction : Direction.values()) {
                connectedTextureMap.put(direction, materials);
            }
            return connectedTextureMap;
        }
    }
}
