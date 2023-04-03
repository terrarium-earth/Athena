package earth.terrarium.stitch.impl.client.models;

import com.google.gson.JsonObject;
import earth.terrarium.stitch.api.client.models.StitchBlockModel;
import earth.terrarium.stitch.api.client.models.StitchModelFactory;
import earth.terrarium.stitch.api.client.models.StitchQuad;
import earth.terrarium.stitch.api.client.utils.CtmUtils;
import earth.terrarium.stitch.api.client.utils.StitchUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class PillarBlockModel implements StitchBlockModel {

    public static final StitchModelFactory FACTORY = new Factory();

    private static final List<StitchQuad> CAP = List.of(StitchQuad.withSprite(0));

    private final Int2ObjectMap<Material> materials;

    public PillarBlockModel(Int2ObjectMap<Material> materials) {
        this.materials = materials;
    }

    @Override
    public List<StitchQuad> getQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        if (!state.hasProperty(BlockStateProperties.AXIS)) return List.of();

        if (state.getValue(BlockStateProperties.AXIS) == direction.getAxis()) {
            return CAP;
        }

        final Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);

        final Rotation rotate = CtmUtils.getPillarRotation(axis, direction);

        final var minMax = StitchUtils.getMinMax(axis);
        final boolean min = level.getBlockState(pos.relative(minMax.getFirst())) == state;
        final boolean max = level.getBlockState(pos.relative(minMax.getSecond())) == state;

        if (min && max) {
            return List.of(StitchQuad.withRotation(2, rotate));
        } else if (min) {
            return List.of(StitchQuad.withRotation(3, rotate));
        } else if (max) {
            return List.of(StitchQuad.withRotation(1, rotate));
        }
        return List.of(StitchQuad.withRotation(4, rotate));
    }

    @Override
    public Int2ObjectMap<TextureAtlasSprite> getTextures(Function<Material, TextureAtlasSprite> getter) {
        Int2ObjectMap<TextureAtlasSprite> textures = new Int2ObjectArrayMap<>();
        for (var entry : materials.int2ObjectEntrySet()) {
            textures.put(entry.getIntKey(), getter.apply(entry.getValue()));
        }
        return textures;
    }

    private static class Factory implements StitchModelFactory {

        @Override
        public Supplier<StitchBlockModel> create(JsonObject json) {
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
