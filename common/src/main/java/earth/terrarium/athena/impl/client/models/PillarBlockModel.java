package earth.terrarium.athena.impl.client.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import earth.terrarium.athena.api.client.utils.AthenaUtils;
import earth.terrarium.athena.api.client.utils.CtmUtils;
import earth.terrarium.athena.api.client.utils.PillarMaterials;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PillarBlockModel implements AthenaBlockModel {

    public static MapCodec<PillarBlockModel> CODEC = PillarMaterials.CODEC.fieldOf("ctm_textures").xmap(PillarBlockModel::new, (model) -> model.materials);
    public static final AthenaModelType TYPE = new AthenaModelType(CODEC);

    private static final List<AthenaQuad> CAP = List.of(AthenaQuad.withSprite(0));

    private final PillarMaterials materials;

    public PillarBlockModel(PillarMaterials materials) {
        this.materials = materials;
    }

    @Override
    public AthenaModelType type() {
        return TYPE;
    }

    @Override
    public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        BlockPos occludingPos = pos.relative(direction);
        BlockState occludingState = level.getBlockState(occludingPos);
        BlockState appearance = level.getAppearance(state, pos, direction, occludingState, occludingPos);

        if (!appearance.hasProperty(BlockStateProperties.AXIS)) return List.of(AthenaQuad.withRotation(4, Rotation.NONE));
        Direction.Axis axis = appearance.getValue(BlockStateProperties.AXIS);
        if (axis == direction.getAxis()) {
            return CAP;
        }

        final Rotation rotate = CtmUtils.getPillarRotation(axis, direction);
        final var minMax = AthenaUtils.getMinMax(axis);
        BlockPos posOne = pos.relative(minMax.getFirst());
        BlockPos posTwo = pos.relative(minMax.getSecond());
        BlockState appearanceOne = level.getAppearance(state, pos, direction, level.getBlockState(posOne), posOne);
        BlockState appearanceTwo = level.getAppearance(state, pos, direction, level.getBlockState(posTwo), posTwo);
        final boolean min = !appearanceOne.isAir() && level.getAppearance(posOne, direction, state, pos) == appearanceOne;
        final boolean max = !appearanceTwo.isAir() && level.getAppearance(posTwo, direction, state, pos) == appearanceTwo;

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
    public Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter) {
        return materials.baked(getter);
    }
}
