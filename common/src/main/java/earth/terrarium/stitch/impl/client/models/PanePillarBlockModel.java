package earth.terrarium.stitch.impl.client.models;

import com.google.gson.JsonObject;
import earth.terrarium.stitch.api.client.models.StitchBlockModel;
import earth.terrarium.stitch.api.client.models.StitchModelFactory;
import earth.terrarium.stitch.api.client.models.StitchQuad;
import earth.terrarium.stitch.api.client.utils.CtmUtils;
import earth.terrarium.stitch.api.client.utils.StitchUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PanePillarBlockModel extends ConnectedBlockModel {

    public static final StitchModelFactory FACTORY = new Factory();

    private static final List<StitchQuad> MIDDLE = List.of(new StitchQuad(0, 0.4375f, 0.5625f, 1f, 0f, Rotation.NONE, 0.4375f));

    public PanePillarBlockModel(Int2ObjectMap<Material> materials) {
        super(materials);
    }

    @Override
    public List<StitchQuad> getQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        if (direction.getAxis().isVertical()) {
            if (level.getBlockState(pos.relative(direction)) == state) {
                return List.of();
            }
            return getTopQuad(state, direction.getAxisDirection());
        }

        final var rightState = StitchUtils.getFromDir(state, direction.getCounterClockWise());
        final var leftState = StitchUtils.getFromDir(state, direction.getClockWise());

        final var upBlockState = level.getBlockState(pos.above());
        final var downBlockState = level.getBlockState(pos.below());

        final var upState = upBlockState.is(state.getBlock()) && StitchUtils.getFromDir(upBlockState, direction.getCounterClockWise()) && StitchUtils.getFromDir(upBlockState, direction.getClockWise());
        final var belowState = downBlockState.is(state.getBlock()) && StitchUtils.getFromDir(downBlockState, direction.getCounterClockWise()) && StitchUtils.getFromDir(downBlockState, direction.getClockWise());

        int texture = upState && belowState ? 2 : upState ? 3 : belowState ? 1 : 4;

        if (leftState && rightState) {
            final float min = StitchUtils.getFromDir(state, direction) ? 0.4375f : 0.5f;
            return List.of(
                    new StitchQuad(texture, 0, min, 1f, 0.5f, Rotation.NONE, 0.4375f),
                    new StitchQuad(texture, 1 - min, 1f, 1f, 0.5f, Rotation.NONE, 0.4375f),
                    new StitchQuad(texture, 0, min, 0.5f, 0f, Rotation.NONE, 0.4375f),
                    new StitchQuad(texture, 1 - min, 1f, 0.5f, 0f, Rotation.NONE, 0.4375f)
            );
        } else if (leftState) {
            final float min = StitchUtils.getFromDir(state, direction) ? 0.5625f : 0.4375f;
            return List.of(new StitchQuad(0, 0, 1 - min, 1f, 0f, Rotation.NONE, 0.4375f));
        } else if (rightState) {
            final float min = StitchUtils.getFromDir(state, direction) ? 0.5625f : 0.4375f;
            return List.of(new StitchQuad(0, min, 1f, 1f, 0f, Rotation.NONE, 0.4375f));
        } else if (level.getBlockState(pos.relative(direction)).getBlock() != state.getBlock() && !StitchUtils.getFromDir(state, direction)) {
            return MIDDLE;
        }
        return List.of();
    }

    private static final StitchQuad TOP_MIDDLE = new StitchQuad(0, 0.4375f, 0.5625f, 0.5625f, 0.4375f, Rotation.NONE, 0f);
    private static final StitchQuad NORTH = new StitchQuad(0, 0.4375f, 0.5625f, 1f, 0.5625f, Rotation.NONE, 0f);
    private static final StitchQuad SOUTH = new StitchQuad(0, 0.4375f, 0.5625f, 0.4375f, 0f, Rotation.NONE, 0f);
    private static final StitchQuad EAST = new StitchQuad(0, 0.5625f, 1f, 0.5625f, 0.4375f, Rotation.NONE, 0f);
    private static final StitchQuad WEST = new StitchQuad(0, 0f, 0.4375f, 0.5625f, 0.4375f, Rotation.NONE, 0f);

    private List<StitchQuad> getTopQuad(BlockState state, Direction.AxisDirection direction) {
        boolean north = StitchUtils.getFromDir(state, Direction.NORTH);
        boolean south = StitchUtils.getFromDir(state, Direction.SOUTH);
        boolean east = StitchUtils.getFromDir(state, Direction.EAST);
        boolean west = StitchUtils.getFromDir(state, Direction.WEST);
        if (direction == Direction.AxisDirection.NEGATIVE) {
            var tempNorth = north;
            north = south;
            south = tempNorth;
            var tempEast = east;
            east = west;
            west = tempEast;
        }


        final List<StitchQuad> quads = new ArrayList<>();
        quads.add(TOP_MIDDLE);

        if (north) quads.add(NORTH);
        if (south) quads.add(SOUTH);
        if (east) quads.add(EAST);
        if (west) quads.add(WEST);

        return quads;
    }

    protected boolean isConnected(BlockState other, BlockState state, Direction direction) {
        if (other.getBlock() == state.getBlock()) {
            return StitchUtils.getFromDir(other, direction.getCounterClockWise()) && StitchUtils.getFromDir(other, direction.getClockWise());
        }
        return false;
    }

    private static class Factory implements StitchModelFactory {

        @Override
        public Supplier<StitchBlockModel> create(JsonObject json) {
            final var materials = parseMaterials(GsonHelper.getAsJsonObject(json, "ctm_textures"));
            return () -> new PanePillarBlockModel(materials);
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
