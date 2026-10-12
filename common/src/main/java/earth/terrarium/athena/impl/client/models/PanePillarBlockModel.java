package earth.terrarium.athena.impl.client.models;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import earth.terrarium.athena.api.client.utils.AthenaUtils;
import earth.terrarium.athena.api.client.utils.CtmUtils;
import earth.terrarium.athena.api.client.utils.PillarMaterials;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class PanePillarBlockModel implements AthenaBlockModel {

    public static final MapCodec<PanePillarBlockModel> CODEC = Materials.CODEC.fieldOf("ctm_textures").xmap(PanePillarBlockModel::new, (model) -> model.materials);
    public static final AthenaModelType TYPE = new AthenaModelType(CODEC);

    private static final List<AthenaQuad> MIDDLE = List.of(new AthenaQuad(6, 0.4375f, 0.5625f, 1f, 0f, Rotation.NONE, 0.4375f));

    private final Materials materials;

    public PanePillarBlockModel(Materials materials) {
        this.materials = materials;
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

        final var upBlockState = level.getAppearance(pos.above(), direction, state, pos);
        final var downBlockState = level.getAppearance(pos.below(), direction, state, pos);

        final var upState = upBlockState.is(state.getBlock()) && AthenaUtils.getFromDir(upBlockState, direction.getCounterClockWise()) && AthenaUtils.getFromDir(upBlockState, direction.getClockWise());
        final var belowState = downBlockState.is(state.getBlock()) && AthenaUtils.getFromDir(downBlockState, direction.getCounterClockWise()) && AthenaUtils.getFromDir(downBlockState, direction.getClockWise());

        int texture = upState && belowState ? 2 : upState ? 3 : belowState ? 1 : 4;

        if (leftState && rightState) {
            final float min = AthenaUtils.getFromDir(state, direction) ? 0.4375f : 0.5f;
            return List.of(
                    new AthenaQuad(texture, 0, min, 1f, 0.5f, Rotation.NONE, 0.4375f),
                    new AthenaQuad(texture, 1 - min, 1f, 1f, 0.5f, Rotation.NONE, 0.4375f),
                    new AthenaQuad(texture, 0, min, 0.5f, 0f, Rotation.NONE, 0.4375f),
                    new AthenaQuad(texture, 1 - min, 1f, 0.5f, 0f, Rotation.NONE, 0.4375f)
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

        materials.baseMaterials.addMaterials(textures, getter);
        textures.put(5, getter.apply(materials.edge().orElse(materials.baseMaterials().particle())));
        textures.put(6, getter.apply(materials.sideEdge().orElse(materials.baseMaterials().particle())));

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

    public record Materials(
        PillarMaterials baseMaterials,
        Optional<Material> edge,
        Optional<Material> sideEdge
    ) {
        public static final MapCodec<Materials> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            PillarMaterials.CODEC.forGetter(Materials::baseMaterials),
            Material.CODEC.optionalFieldOf("edge").forGetter(Materials::edge),
            Material.CODEC.optionalFieldOf("side_edge").forGetter(Materials::sideEdge)
        ).apply(instance, Materials::new));
    }
}
