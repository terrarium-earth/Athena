package earth.terrarium.athena.api.client.fabric;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelAttributes;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.models.TintProvider;
import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import earth.terrarium.athena.impl.internal.BlockStateModelMaterialInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class AthenaBakedModel implements BlockStateModel, FabricBlockStateModel {

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    private final Map<AthenaBlockModel.Part, BakedPart> bakedParts = new HashMap<>();

    private final BlockState state;
    private final AthenaBlockModel model;
    private final int materialFlags;
    private final Material.Baked particleMaterial;

    private AthenaBakedModel(BlockState state, AthenaBlockModel model, Function<Material, Material.Baked> materialBaker) {
        this.state = state;
        this.model = model;

        MutableInt materialFlags = new MutableInt();
        MutableObject<Material.Baked> particleMaterial = new MutableObject<>();

        model.collectPossibleParts(state, (part) -> {
            var bakedPart = new BakedPart(part, materialBaker);

            if (particleMaterial.get() == null) {
                particleMaterial.setValue(bakedPart.particleMaterial());
            }

            materialFlags.setValue(materialFlags.intValue() | bakedPart.materialFlags());

            bakedParts.put(part, bakedPart);
        });

        this.materialFlags = materialFlags.intValue();
        this.particleMaterial = particleMaterial.get();
    }

    public static AthenaUnbakedModel unbaked(BlockState state, AthenaBlockModel model) {
        return new AthenaUnbakedModel(model, (baker) -> new AthenaBakedModel(state, model, baker));
    }

    @Override
    public void emitQuads(@NotNull QuadEmitter emitter, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull Predicate<@Nullable Direction> cullTest) {
        WrappedGetter getter = new WrappedGetter(level);
        List<BakedPart> parts = new ArrayList<>();
        model.selectParts(random, state, (part) -> parts.add(this.bakedParts.get(part)));

        for (Direction side : DIRECTIONS) {
            if (cullTest.test(side)) continue;

            for (BakedPart part : parts) {
                part.emitQuads(emitter, side, part.part.getQuads(getter, state, pos, side));
            }
        }
    }

    @Override
    public void collectParts(@NotNull RandomSource randomSource, List<BlockStateModelPart> list) {
        model.selectParts(randomSource, this.state, (part) -> list.add(this.bakedParts.get(part)));
    }

    @Override
    public Material.@NonNull Baked particleMaterial() {
        return this.particleMaterial;
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return materialFlags;
    }

    private static class BakedPart implements BlockStateModelPart {

        private final NullableEnumMap<Direction, List<BakedQuad>> defaultQuads = new NullableEnumMap<>(Direction.class);

        private final AthenaBlockModel.Part part;
        private final BlockStateModelMaterialInfo info;
        private final Int2ObjectMap<Material.Baked> materials;
        private final AthenaModelAttributes attributes;

        public BakedPart(AthenaBlockModel.Part part, Function<Material, Material.Baked> materialBaker) {
            this.part = part;
            this.materials = part.getTextures(materialBaker);
            this.attributes = part.getAttributes();
            this.info = new BlockStateModelMaterialInfo(this.materials);
        }

        private void emitQuads(QuadEmitter emitter, @Nullable Direction side, List<AthenaQuad> quads) {
            for (var sprite : quads) {
                var material = this.materials.get(sprite.sprite());
                if (material == null) {
                    continue;
                }
                emitter.square(side, sprite.left(), sprite.bottom(), sprite.right(), sprite.top(), sprite.depth());

                int flag = MutableQuadView.BAKE_LOCK_UV;

                switch (sprite.rotation()) {
                    case CLOCKWISE_90 -> flag |= MutableQuadView.BAKE_ROTATE_90;
                    case CLOCKWISE_180 -> flag |= MutableQuadView.BAKE_ROTATE_180;
                    case COUNTERCLOCKWISE_90 -> flag |= MutableQuadView.BAKE_ROTATE_270;
                }

                emitter.materialBake(material, flag);

                switch (this.attributes.getTint()) {
                    case TintProvider.Index(var index) -> emitter.tintIndex(index);
                    case TintProvider.Static(var color) -> emitter.color(color, color, color, color);
                    case null -> {}
                }

                emitter.emit();
            }
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable Direction direction) {
            if (!this.defaultQuads.containsKey(direction)) {
                var quads = new ArrayList<BakedQuad>();

                synchronized (this) {
                    var mesh = Renderer.get().mutableMesh();
                    this.emitQuads(
                        mesh.emitter(),
                        direction,
                        this.part.getDefaultQuads(direction).getOrDefault(direction, List.of())
                    );

                    var atlases = Minecraft.getInstance().getAtlasManager();
                    var itemFinder = atlases.getAtlasOrThrow(AtlasIds.ITEMS).spriteFinder();
                    var blockFinder = atlases.getAtlasOrThrow(AtlasIds.BLOCKS).spriteFinder();

                    mesh.forEach(view -> {
                        var finder = switch (view.atlas()) {
                            case ITEM -> itemFinder;
                            case BLOCK -> blockFinder;
                            case null ->
                                    throw new IllegalStateException("Quad with no atlas, this should never happen");
                        };
                        quads.add(view.toBakedQuad(finder.find(view)));
                    });
                }

                this.defaultQuads.put(direction, quads);

                return quads;
            }
            return this.defaultQuads.get(direction);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public Material.@NonNull Baked particleMaterial() {
            return info.getParticle();
        }

        @Override
        public @BakedQuad.MaterialFlags int materialFlags() {
            return info.getFlags();
        }
    }
}
