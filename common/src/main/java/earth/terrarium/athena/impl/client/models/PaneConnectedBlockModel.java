package earth.terrarium.athena.impl.client.models;

import com.google.gson.JsonObject;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.CtmState;
import earth.terrarium.athena.api.client.utils.CtmUtils;
import earth.terrarium.athena.api.client.utils.AthenaUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class PaneConnectedBlockModel implements AthenaBlockModel {

    public static final AthenaModelFactory FACTORY = new Factory();

    private static final List<AthenaQuad> CENTER = List.of(new AthenaQuad(1, 0, 1, 1, 0, Rotation.NONE, 0.4375f));
    private static final List<AthenaQuad> MIDDLE = List.of(new AthenaQuad(6, 0.4375f, 0.5625f, 1f, 0f, Rotation.NONE, 0.4375f));

    private final Int2ObjectMap<Material> materials;

    public PaneConnectedBlockModel(Int2ObjectMap<Material> materials) {
        this.materials = materials;
    }

    @Override
    public List<AthenaQuad> getQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        if (direction.getAxis().isVertical()) {
            if (level.getBlockState(pos.relative(direction)) == state) {
                return List.of();
            }
            return getTopQuad(state, direction.getAxisDirection());
        }

        final var rightState = AthenaUtils.getFromDir(state, direction.getCounterClockWise());
        final var leftState = AthenaUtils.getFromDir(state, direction.getClockWise());

        final CtmState ctmState = CtmState.from(level, pos, direction, other -> isConnected(other, state, direction));

        if (ctmState.allTrue()) {
            return CENTER;
        }

        if (leftState && rightState) {
            final float min = AthenaUtils.getFromDir(state, direction) ? 0.4375f : 0.5f;
            return List.of(
                    new AthenaQuad(CtmUtils.getTexture(ctmState.up(), ctmState.left(), ctmState.upLeft()), 0, min, 1f, 0.5f, Rotation.NONE, 0.4375f),
                    new AthenaQuad(CtmUtils.getTexture(ctmState.up(), ctmState.right(), ctmState.upRight()), 1 - min, 1f, 1f, 0.5f, Rotation.NONE, 0.4375f),
                    new AthenaQuad(CtmUtils.getTexture(ctmState.down(), ctmState.left(), ctmState.downLeft()), 0, min, 0.5f, 0f, Rotation.NONE, 0.4375f),
                    new AthenaQuad(CtmUtils.getTexture(ctmState.down(), ctmState.right(), ctmState.downRight()), 1 - min, 1f, 0.5f, 0f, Rotation.NONE, 0.4375f)
            );
        } else if (leftState) {
            final float min = AthenaUtils.getFromDir(state, direction) ? 0.5625f : 0.4375f;
            return List.of(new AthenaQuad(0, 0, 1 - min, 1f, 0f, Rotation.NONE, 0.4375f));
        } else if (rightState) {
            final float min = AthenaUtils.getFromDir(state, direction) ? 0.5625f : 0.4375f;
            return List.of(new AthenaQuad(0, min, 1f, 1f, 0f, Rotation.NONE, 0.4375f));
        } else if (level.getBlockState(pos.relative(direction)).getBlock() != state.getBlock() && !AthenaUtils.getFromDir(state, direction)) {
            return MIDDLE;
        }
        return List.of();
    }

    @Override
    public Int2ObjectMap<TextureAtlasSprite> getTextures(Function<Material, TextureAtlasSprite> getter) {
        final var textures = new Int2ObjectArrayMap<TextureAtlasSprite>();
        for (var entry : this.materials.int2ObjectEntrySet()) {
            textures.put(entry.getIntKey(), getter.apply(entry.getValue()));
        }
        return textures;
    }

    private static final AthenaQuad TOP_MIDDLE = new AthenaQuad(5, 0.4375f, 0.5625f, 0.5625f, 0.4375f, Rotation.NONE, 0f, false);
    private static final AthenaQuad NORTH = new AthenaQuad(5, 0.4375f, 0.5625f, 1f, 0.5625f, Rotation.NONE, 0f, false);
    private static final AthenaQuad SOUTH = new AthenaQuad(5, 0.4375f, 0.5625f, 0.4375f, 0f, Rotation.NONE, 0f, false);
    private static final AthenaQuad EAST = new AthenaQuad(5, 0.5625f, 1f, 0.5625f, 0.4375f, Rotation.NONE, 0f, false);
    private static final AthenaQuad WEST = new AthenaQuad(5, 0f, 0.4375f, 0.5625f, 0.4375f, Rotation.NONE, 0f, false);

    private List<AthenaQuad> getTopQuad(BlockState state, Direction.AxisDirection direction) {
        boolean north = AthenaUtils.getFromDir(state, Direction.NORTH);
        boolean south = AthenaUtils.getFromDir(state, Direction.SOUTH);
        boolean east = AthenaUtils.getFromDir(state, Direction.EAST);
        boolean west = AthenaUtils.getFromDir(state, Direction.WEST);
        if (direction == Direction.AxisDirection.NEGATIVE) {
            var tempNorth = north;
            north = south;
            south = tempNorth;
            var tempEast = east;
            east = west;
            west = tempEast;
        }

        final List<AthenaQuad> quads = new ArrayList<>();
        quads.add(TOP_MIDDLE);

        if (north) quads.add(NORTH);
        if (south) quads.add(SOUTH);
        if (east) quads.add(EAST);
        if (west) quads.add(WEST);

        return quads;
    }

    protected boolean isConnected(BlockState other, BlockState state, Direction direction) {
        if (other.is(state.getBlock())) {
            return AthenaUtils.getFromDir(other, direction.getCounterClockWise()) && AthenaUtils.getFromDir(other, direction.getClockWise());
        }
        return false;
    }

    private static class Factory implements AthenaModelFactory {

        @Override
        public Supplier<AthenaBlockModel> create(JsonObject json) {
            final var textureObject = GsonHelper.getAsJsonObject(json, "ctm_textures");
            final var materials = CtmUtils.parseCtmMaterials(textureObject);
            materials.put(5, CtmUtils.blockMat(GsonHelper.getAsString(textureObject, "edge", GsonHelper.getAsString(textureObject, "particle"))));
            materials.put(6, CtmUtils.blockMat(GsonHelper.getAsString(textureObject, "side_edge", GsonHelper.getAsString(textureObject, "particle"))));
            return () -> new PaneConnectedBlockModel(materials);
        }
    }
}
