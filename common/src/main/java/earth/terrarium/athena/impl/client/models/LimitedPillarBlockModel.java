package earth.terrarium.athena.impl.client.models;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import earth.terrarium.athena.api.client.utils.CtmUtils;
import earth.terrarium.athena.api.client.utils.PillarMaterials;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class LimitedPillarBlockModel implements AthenaBlockModel {

    public static final MapCodec<LimitedPillarBlockModel> CODEC = PillarMaterials.CODEC.fieldOf("ctm_textures").xmap(LimitedPillarBlockModel::new, (model) -> model.materials);
    public static final AthenaModelType TYPE = new AthenaModelType(CODEC);

    private static final List<AthenaQuad> CENTER = List.of(AthenaQuad.withSprite(2));
    private static final List<AthenaQuad> TOP = List.of(AthenaQuad.withSprite(1));
    private static final List<AthenaQuad> BOTTOM = List.of(AthenaQuad.withSprite(3));
    private static final List<AthenaQuad> SELF = List.of(AthenaQuad.withSprite(4));
    private static final List<AthenaQuad> CAP = List.of(AthenaQuad.withSprite(0));

    private final PillarMaterials materials;

    public LimitedPillarBlockModel(PillarMaterials materials) {
        this.materials = materials;
    }

    @Override
    public AthenaModelType type() {
        return TYPE;
    }

    @Override
    public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        BlockPos occludingPos = pos.relative(direction);
        BlockState appearance = level.getAppearance(state, pos, direction, level.getBlockState(occludingPos), occludingPos);
        BlockState occludingAppearance = level.getAppearance(occludingPos, direction.getOpposite(), state, pos);
        if (!appearance.isAir() && occludingAppearance.is(appearance.getBlock())) {
            return List.of();
        }

        if (direction.getAxis().isVertical()) {
            return CAP;
        }

        BlockPos posAbove = pos.above();
        BlockPos posBelow = pos.below();
        BlockState appearanceAbove = level.getAppearance(state, pos, direction, level.getBlockState(posAbove), posAbove);
        BlockState appearanceBelow = level.getAppearance(state, pos, direction, level.getBlockState(posBelow), posBelow);
        final boolean min = !appearanceAbove.isAir() && level.getAppearance(posAbove, direction, state, pos).is(appearanceAbove.getBlock());
        final boolean max = !appearanceBelow.isAir() && level.getAppearance(posBelow, direction, state, pos).is(appearanceBelow.getBlock());

        if (min && max) {
            return CENTER;
        } else if (min) {
            return BOTTOM;
        } else if (max) {
            return TOP;
        }
        return SELF;
    }

    @Override
    public Map<Direction, List<AthenaQuad>> getDefaultQuads(Direction direction) {
        Map<Direction, List<AthenaQuad>> quads = new HashMap<>(Direction.values().length);
        for (Direction dir : Direction.values()) {
            quads.put(dir, SELF);
        }
        return quads;
    }

    @Override
    public Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter) {
        return materials.baked(getter);
    }
}
