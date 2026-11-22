package earth.terrarium.athena.api.client.neoforge;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelAttributes;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import earth.terrarium.athena.impl.internal.BlockStateModelMaterialInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AthenaBakedModel implements BlockStateModel {

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    private final AthenaBlockModel model;
    private final Int2ObjectMap<Material.Baked> materials;
    private final AthenaModelAttributes attributes;
    private final BlockStateModelMaterialInfo info;
    private final BlockStateModelPart part;

    private AthenaBakedModel(BlockState state, AthenaBlockModel model, Function<Material, Material.Baked> function) {
        this.model = model;
        this.materials = this.model.getTextures(function);
        this.attributes = model.getAttributes();
        this.info = new BlockStateModelMaterialInfo(this.materials);
        this.part = new Part();
    }

    public static AthenaUnbakedModel unbaked(BlockState state, AthenaBlockModel model) {
        return new AthenaUnbakedModel(model, (baker) -> new AthenaBakedModel(state, model, baker));
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<BlockStateModelPart> parts) {
        parts.add(new AthenaModelPart(
                this.part,
                this.createGeometryKey(level, pos, state, random),
                this.materials,
                this.attributes.getTint()
        ));
    }

    @Override
    public void collectParts(@NotNull RandomSource arg, @NotNull List<BlockStateModelPart> list) {
        list.add(this.part);
    }

    @Override
    public @NotNull NullableEnumMap<Direction, Map<Direction, List<AthenaQuad>>> createGeometryKey(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random) {
        var getter = new WrappedGetter(level);
        var quads = new NullableEnumMap<Direction, Map<Direction, List<AthenaQuad>>>(Direction.class);
        var nonCullQuads = new HashMap<Direction, List<AthenaQuad>>();
        for (Direction direction : DIRECTIONS) {
            List<AthenaQuad> culledQuads = new ArrayList<>();
            List<AthenaQuad> unculledQuads = new ArrayList<>();
            for (AthenaQuad quad : this.model.getQuads(getter, state, pos, direction)) {
                if (quad.cull()) {
                    culledQuads.add(quad);
                } else {
                    unculledQuads.add(quad);
                }
            }
            quads.put(direction, Map.of(direction, culledQuads));
            nonCullQuads.put(direction, unculledQuads);
        }
        quads.put(null, nonCullQuads);

        return quads;
    }

    @Override
    public @NotNull Material.Baked particleMaterial() {
        return this.part.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.part.materialFlags();
    }

    private class Part implements BlockStateModelPart {

        private final NullableEnumMap<Direction, List<BakedQuad>> defaultQuads = new NullableEnumMap<>(Direction.class);

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable Direction direction) {
            var quads = this.defaultQuads.get(direction);
            if (quads == null) {
                quads = new ArrayList<>();

                var defaults = AthenaBakedModel.this.model.getDefaultQuads(direction);
                var tint = AthenaBakedModel.this.attributes.getTint();
                var materials = AthenaBakedModel.this.materials;
                for (var entry : defaults.entrySet()) {
                    for (var quad : entry.getValue()) {
                        var material = materials.get(quad.sprite());
                        if (material == null) continue;
                        quads.add(ForgeAthenaUtils.bakeQuad(quad, entry.getKey(), material, tint));
                    }
                }

                this.defaultQuads.put(direction, quads);
            }
            return quads;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public @NotNull Material.Baked particleMaterial() {
            return AthenaBakedModel.this.info.getParticle();
        }

        @Override
        public @BakedQuad.MaterialFlags int materialFlags() {
            return AthenaBakedModel.this.info.getFlags();
        }
    }
}
