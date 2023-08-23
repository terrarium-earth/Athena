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
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConnectedBlockModel implements AthenaBlockModel {

    public static final AthenaModelFactory FACTORY = new Factory();

    private final ConnectedTextureMap materials;
    private final BiPredicate<BlockState, BlockState> connectTo;

    public ConnectedBlockModel(ConnectedTextureMap materials, BiPredicate<BlockState, BlockState> connectTo) {
        this.materials = materials;
        this.connectTo = connectTo;
    }

    @Override
    public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        if (CtmUtils.checkRelative(level, state, pos, direction)) {
            return List.of();
        }

        final CtmState ctm = CtmState.from(level, state, pos, direction, CtmUtils.check(level, state, pos, direction, connectTo));

        if (ctm.allTrue()) {
            return List.of(AthenaQuad.withSprite(materials.getTexture(direction, 1)));
        }

        return List.of(
                AthenaQuad.withState(materials, direction, ctm.up(), ctm.left(), ctm.upLeft(), 0, 0.5f, 1f, 0.5f),
                AthenaQuad.withState(materials, direction, ctm.up(), ctm.right(), ctm.upRight(), 0.5f, 1f, 1f, 0.5f),
                AthenaQuad.withState(materials, direction, ctm.down(), ctm.left(), ctm.downLeft(), 0, 0.5f, 0.5f, 0f),
                AthenaQuad.withState(materials, direction, ctm.down(), ctm.right(), ctm.downRight(), 0.5f, 1f, 0.5f, 0f)
        );
    }

    @Override
    public Map<Direction, List<AthenaQuad>> getDefaultQuads(Direction direction) {
        if (direction == null) return Map.of();
        return Map.of(
            direction,
            List.of(
                    AthenaQuad.withState(materials, direction, false, false, false, 0, 0.5f, 1f, 0.5f),
                    AthenaQuad.withState(materials, direction, false, false, false, 0.5f, 1f, 1f, 0.5f),
                    AthenaQuad.withState(materials, direction, false, false, false, 0, 0.5f, 0.5f, 0f),
                    AthenaQuad.withState(materials, direction, false, false, false, 0.5f, 1f, 0.5f, 0f)
            )
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
            BiPredicate<BlockState, BlockState> conditions = CtmUtils.parseCondition(json);
            return () -> new ConnectedBlockModel(materialsFinal, conditions);
        }

        private static ConnectedTextureMap parseMaterials(JsonObject json) {
            final ConnectedTextureMap materials = new ConnectedTextureMap();
            for (Direction direction : Direction.values()) {
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
