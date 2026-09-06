package earth.terrarium.athena.impl.client.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PaneConnectedBlockModel implements AthenaBlockModel {

    public static final MapCodec<PaneConnectedBlockModel> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
        Materials.CODEC.fieldOf("ctm_textures").forGetter((model) -> model.materials),
        Codec.BOOL.optionalFieldOf("connect_corners", false).forGetter((model) -> model.connectCorners)
    ).apply(instance, PaneConnectedBlockModel::new));

    public static final AthenaModelType TYPE = new AthenaModelType(CODEC);

    private static final List<AthenaQuad> CENTER = List.of(new AthenaQuad(1, 0, 1, 1, 0, Rotation.NONE, 0.4375f));
    private static final List<AthenaQuad> MIDDLE = List.of(new AthenaQuad(6, 0.4375f, 0.5625f, 1f, 0f, Rotation.NONE, 0.4375f));

    private final Materials materials;
    private final boolean connectCorners;

    public PaneConnectedBlockModel(Materials materials, boolean connectCorners) {
        this.materials = materials;
        this.connectCorners = connectCorners;
    }

    @Override
    public AthenaModelType type() {
        return TYPE;
    }

    @Override
    public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        if (direction.getAxis().isVertical()) {
            if (level.getBlockState(pos.relative(direction)) == state) {
                return List.of();
            }
            return getTopQuad(state, direction.getAxisDirection());
        }

        final var rightState = AthenaUtils.getFromDir(state, direction.getCounterClockWise());
        final var leftState = AthenaUtils.getFromDir(state, direction.getClockWise());

        final CtmState ctmState = CtmState.from(level, state, pos, direction, (ignoredPos, other, ignoredApp) -> isConnected(other, state, direction));

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
        } else if (this.connectCorners && leftState) {
            final float min = AthenaUtils.getFromDir(state, direction) ? 0.5625f : 0.4375f;
            return List.of(
                    new AthenaQuad(CtmUtils.getTexture(ctmState.up(), ctmState.left(), ctmState.upLeft()), 0, 1 - min, 1f, 0.5f, Rotation.NONE, 0.4375f),
                    new AthenaQuad(CtmUtils.getTexture(ctmState.down(), ctmState.left(), ctmState.downLeft()), 0, 1 - min, 0.5f, 0f, Rotation.NONE, 0.4375f)
            );
        } else if (this.connectCorners && rightState) {
            final float min = AthenaUtils.getFromDir(state, direction) ? 0.5625f : 0.4375f;
            return List.of(
                    new AthenaQuad(CtmUtils.getTexture(ctmState.up(), ctmState.right(), ctmState.upRight()), min, 1, 1f, 0.5f, Rotation.NONE, 0.4375f),
                    new AthenaQuad(CtmUtils.getTexture(ctmState.down(), ctmState.right(), ctmState.downRight()), min, 1, 0.5f, 0f, Rotation.NONE, 0.4375f)
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
    public Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter) {
        final var textures = new Int2ObjectArrayMap<Material.Baked>();

        CtmMaterials baseMaterials = materials.baseMaterials();
        textures.put(0, getter.apply(baseMaterials.particle()));
        textures.put(2, getter.apply(baseMaterials.center()));
        textures.put(3, getter.apply(baseMaterials.vertical()));
        textures.put(4, getter.apply(baseMaterials.horizontal()));
        textures.put(1, getter.apply(baseMaterials.empty()));
        textures.put(5, getter.apply(materials.edge.orElse(baseMaterials.particle())));
        textures.put(6, getter.apply(materials.sideEdge.orElse(baseMaterials.particle())));

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
            boolean left = AthenaUtils.getFromDir(other, direction.getCounterClockWise());
            boolean right = AthenaUtils.getFromDir(other, direction.getClockWise());
            boolean front = AthenaUtils.getFromDir(other, direction);
            boolean back = AthenaUtils.getFromDir(other, direction.getOpposite());
            return left && right || (left && (front || back)) || (right && (front || back));
        }
        return false;
    }

    public record Materials(
        CtmMaterials baseMaterials,
        Optional<Material> edge,
        Optional<Material> sideEdge
    ) {
        public static final MapCodec<Materials> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            CtmMaterials.CODEC.fieldOf("particle").forGetter(Materials::baseMaterials),
            Material.CODEC.optionalFieldOf("edge").forGetter(Materials::edge),
            Material.CODEC.optionalFieldOf("side_edge").forGetter(Materials::sideEdge)
        ).apply(instance, Materials::new));
    }
}
