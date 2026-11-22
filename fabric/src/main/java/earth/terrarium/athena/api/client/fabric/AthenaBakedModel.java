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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AthenaBakedModel implements BlockStateModel, FabricBlockStateModel {

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    private final AthenaBlockModel model;
    private final Int2ObjectMap<Material.Baked> materials;
    private final AthenaModelAttributes attributes;
    private final BlockStateModelMaterialInfo info;
    private final BlockStateModelPart part;

    private AthenaBakedModel(BlockState state, AthenaBlockModel model, Function<Material, Material.Baked> function) {
        this.model = model;
        this.materials = this.model.getTextures(function, state);
        this.attributes = model.getAttributes(state);
        this.info = new BlockStateModelMaterialInfo(this.materials);
        this.part = new Part(state);
    }

    public static AthenaUnbakedModel unbaked(BlockState state, AthenaBlockModel model) {
        return new AthenaUnbakedModel(model, (baker) -> new AthenaBakedModel(state, model, baker));
    }

    @Override
    public void emitQuads(@NotNull QuadEmitter emitter, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull Predicate<@Nullable Direction> cullTest) {
        WrappedGetter getter = new WrappedGetter(level);
        for (Direction value : DIRECTIONS) {
            if (cullTest.test(value)) continue;

            emitQuads(emitter, value, model.getQuads(getter, state, pos, value));
        }
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
    public void collectParts(@NotNull RandomSource randomSource, List<BlockStateModelPart> list) {
        list.add(this.part);
    }

    @Override
    public Material.@NonNull Baked particleMaterial() {
        return this.part.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.part.materialFlags();
    }

    private class Part implements BlockStateModelPart {

        private final NullableEnumMap<Direction, List<BakedQuad>> defaultQuads = new NullableEnumMap<>(Direction.class);
        private final BlockState state;

        public Part(BlockState state) {
            this.state = state;
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable Direction direction) {
            if (!this.defaultQuads.containsKey(direction)) {
                var quads = new ArrayList<BakedQuad>();

                synchronized (this) {
                    var mesh = Renderer.get().mutableMesh();
                    AthenaBakedModel.this.emitQuads(
                            mesh.emitter(),
                            direction,
                            AthenaBakedModel.this.model.getDefaultQuads(direction, state).getOrDefault(direction, List.of())
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
            return AthenaBakedModel.this.info.getParticle();
        }

        @Override
        public @BakedQuad.MaterialFlags int materialFlags() {
            return AthenaBakedModel.this.info.getFlags();
        }
    }
}
