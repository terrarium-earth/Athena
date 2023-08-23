package earth.terrarium.athena.impl.client.models;

import com.google.gson.JsonObject;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import earth.terrarium.athena.api.client.utils.CtmUtils;
import earth.terrarium.athena.api.client.utils.AthenaUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class PillarBlockModel implements AthenaBlockModel {

    public static final AthenaModelFactory FACTORY = new Factory();

    private static final List<AthenaQuad> CAP = List.of(AthenaQuad.withSprite(0));

    private final Int2ObjectMap<Material> materials;

    public PillarBlockModel(Int2ObjectMap<Material> materials) {
        this.materials = materials;
    }

    @Override
    public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        if (!state.hasProperty(BlockStateProperties.AXIS)) return List.of(AthenaQuad.withRotation(4, Rotation.NONE));

        if (state.getValue(BlockStateProperties.AXIS) == direction.getAxis()) {
            return CAP;
        }

        final BlockState appearance = level.getAppearance(pos, direction, state, pos);

        final Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);

        final Rotation rotate = CtmUtils.getPillarRotation(axis, direction);

        final var minMax = AthenaUtils.getMinMax(axis);
        final boolean min = level.getAppearance(pos.relative(minMax.getFirst()), direction, appearance, pos) == appearance;
        final boolean max = level.getAppearance(pos.relative(minMax.getSecond()), direction, appearance, pos) == appearance;

        if (min && max) {
            return List.of(AthenaQuad.withRotation(2, rotate));
        } else if (min) {
            return List.of(AthenaQuad.withRotation(3, rotate));
        } else if (max) {
            return List.of(AthenaQuad.withRotation(1, rotate));
        }
        return List.of(AthenaQuad.withRotation(4, rotate));
    }

    @Override
    public Map<Direction, List<AthenaQuad>> getDefaultQuads(Direction direction) {
        Map<Direction, List<AthenaQuad>> quads = new HashMap<>(Direction.values().length);
        for (Direction dir : Direction.values()) {
            quads.put(dir, List.of(AthenaQuad.withRotation(4, Rotation.NONE)));
        }
        return quads;
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
            final var materials = parseMaterials(GsonHelper.getAsJsonObject(json, "ctm_textures"));
            return () -> new PillarBlockModel(materials);
        }

        private static Int2ObjectMap<Material> parseMaterials(JsonObject json) {
            Int2ObjectMap<Material> materials = new Int2ObjectArrayMap<>();
            materials.put(0, CtmUtils.blockMat(GsonHelper.getAsString(json, "particle")));
            materials.put(4, CtmUtils.blockMat(GsonHelper.getAsString(json, "self")));

            materials.put(1, CtmUtils.blockMat(GsonHelper.getAsString(json, "top")));
            materials.put(2, CtmUtils.blockMat(GsonHelper.getAsString(json, "center")));
            materials.put(3, CtmUtils.blockMat(GsonHelper.getAsString(json, "bottom")));

            return materials;
        }
    }
}
