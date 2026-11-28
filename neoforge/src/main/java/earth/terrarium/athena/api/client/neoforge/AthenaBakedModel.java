package earth.terrarium.athena.api.client.neoforge;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelAttributes;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import earth.terrarium.athena.impl.internal.BlockStateModelMaterialInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.EnumMap;
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
    private static final float DEPTH_OFFSET = 0.001f;

    private final Map<AthenaBlockModel.Part, BakedPart> bakedParts = new HashMap<>();

    private final BlockState state;
    private final AthenaBlockModel model;
    private final int materialFlags;
    private final Material.Baked particleMaterial;

    private AthenaBakedModel(BlockState state, AthenaBlockModel model, Function<Material, Material.Baked> materialBaker) {
        this.state = state;
        this.model = model;

        int materialFlags = 0;
        Material.Baked particleMaterial = null;

        float depthOffset = 0f;
        for (AthenaBlockModel.Part part : model.getAllParts(state)) {
            var bakedPart = new BakedPart(part, materialBaker, depthOffset);

            if (particleMaterial == null) {
                particleMaterial = bakedPart.particleMaterial();
            }

            materialFlags |= bakedPart.materialFlags();

            bakedParts.put(part, bakedPart);
            depthOffset += DEPTH_OFFSET;
        }

        this.materialFlags = materialFlags;
        this.particleMaterial = particleMaterial;
    }

    public static AthenaUnbakedModel unbaked(BlockState state, AthenaBlockModel model) {
        return new AthenaUnbakedModel(model, (baker) -> new AthenaBakedModel(state, model, baker));
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<BlockStateModelPart> parts) {
        float depthOffset = 0f;
        for (AthenaBlockModel.Part part : this.model.selectParts(random, this.state)) {
            var baked = this.bakedParts.get(part);

            parts.add(
                new AthenaModelPart(
                    baked,
                    collectDirectionQuads(
                            part,
                            level,
                            pos,
                            state
                    ),
                    baked.materials,
                    baked.attributes.getTint(),
                    baked.attributes.getLightEmission(),
                    depthOffset
                )
            );
            depthOffset += DEPTH_OFFSET;
        }
    }

    @Override
    public void collectParts(@NotNull RandomSource arg, @NotNull List<BlockStateModelPart> list) {
        for (AthenaBlockModel.Part part : this.model.selectParts(arg, this.state)) {
            list.add(this.bakedParts.get(part));
        }
    }

    private NullableEnumMap<Direction, Map<Direction, List<AthenaQuad>>> collectDirectionQuads(
        @NotNull AthenaBlockModel.Part part,
        @NotNull BlockAndTintGetter level,
        @NotNull BlockPos pos,
        @NotNull BlockState state
    ) {
        var getter = new WrappedGetter(level);
        var quads = new NullableEnumMap<Direction, Map<Direction, List<AthenaQuad>>>(Direction.class);
        var nonCullQuads = new HashMap<Direction, List<AthenaQuad>>();
        for (Direction direction : DIRECTIONS) {
            List<AthenaQuad> culledQuads = new ArrayList<>();
            List<AthenaQuad> unculledQuads = new ArrayList<>();
            for (AthenaQuad quad : part.getQuads(getter, state, pos, direction)) {
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
    public @NotNull Object createGeometryKey(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random) {
        var getter = new WrappedGetter(level);
        Map<Direction, List<AthenaQuad>> geometryKey = new EnumMap<>(Direction.class);
        for (AthenaBlockModel.Part part : this.model.selectParts(random, this.state)) {
            for (Direction direction : DIRECTIONS) {
                geometryKey.computeIfAbsent(direction, _ -> new ArrayList<>()).addAll(part.getQuads(getter, state, pos, direction));
            }
        }
        return geometryKey;
    }

    @Override
    public @NotNull Material.Baked particleMaterial() {
        return this.particleMaterial;
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.materialFlags;
    }

    private static class BakedPart implements BlockStateModelPart {

        private final NullableEnumMap<Direction, List<BakedQuad>> defaultQuads = new NullableEnumMap<>(Direction.class);

        private final AthenaBlockModel.Part part;
        private final BlockStateModelMaterialInfo info;
        private final Int2ObjectMap<Material.Baked> materials;
        private final AthenaModelAttributes attributes;
        private final float depthOffset;

        public BakedPart(AthenaBlockModel.Part part, Function<Material, Material.Baked> materialBaker, float depthOffset) {
            this.part = part;
            this.materials = part.getTextures(materialBaker);
            this.attributes = part.getAttributes();
            this.info = new BlockStateModelMaterialInfo(this.materials);
            this.depthOffset = depthOffset;
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable Direction direction) {
            var quads = this.defaultQuads.get(direction);
            if (quads == null) {
                quads = new ArrayList<>();

                var defaults = this.part.getDefaultQuads(direction);
                var tint = attributes.getTint();
                var lightEmission = attributes.getLightEmission();
                for (var entry : defaults.entrySet()) {
                    for (var quad : entry.getValue()) {
                        var material = materials.get(quad.sprite());
                        if (material == null) continue;
                        quads.add(ForgeAthenaUtils.bakeQuad(quad, entry.getKey(), material, tint, lightEmission, this.depthOffset));
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
            return info.getParticle();
        }

        @Override
        public @BakedQuad.MaterialFlags int materialFlags() {
            return info.getFlags();
        }
    }
}
