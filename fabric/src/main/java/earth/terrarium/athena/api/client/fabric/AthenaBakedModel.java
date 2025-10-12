package earth.terrarium.athena.api.client.fabric;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.models.TintProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class AthenaBakedModel implements BlockStateModel, FabricBlockStateModel {

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    private final AthenaBlockModel model;
    private final Int2ObjectMap<TextureAtlasSprite> textures;
    private final BlockModelPart part;

    @Nullable
    private List<BakedQuad>[] defaultQuads = null;

    public AthenaBakedModel(AthenaBlockModel model, Function<Material, TextureAtlasSprite> function) {
        this.model = model;
        this.textures = this.model.getTextures(function);
        this.part = new Part(this);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        WrappedGetter getter = new WrappedGetter(level);
        for (Direction value : DIRECTIONS) {
            emitQuads(emitter, value, model.getQuads(getter, state, pos, value));
        }
    }

    private void emitQuads(QuadEmitter emitter, @Nullable Direction side, List<AthenaQuad> quads) {
        var attributes = this.model.getAttributes();
        for (var sprite : quads) {
            TextureAtlasSprite texture = this.textures.get(sprite.sprite());
            if (texture == null) {
                continue;
            }
            emitter.square(side, sprite.left(), sprite.bottom(), sprite.right(), sprite.top(), sprite.depth());

            int flag = MutableQuadView.BAKE_LOCK_UV;

            switch (sprite.rotation()) {
                case CLOCKWISE_90 -> flag |= MutableQuadView.BAKE_ROTATE_90;
                case CLOCKWISE_180 -> flag |= MutableQuadView.BAKE_ROTATE_180;
                case COUNTERCLOCKWISE_90 -> flag |= MutableQuadView.BAKE_ROTATE_270;
            }

            emitter.spriteBake(texture, flag);

            switch (attributes.getTint()) {
                case TintProvider.Index(var index) -> emitter.tintIndex(index);
                case TintProvider.Static(var color) -> emitter.color(color, color, color, color);
                case null -> {}
            }

            emitter.emit();
        }
    }

    private List<BakedQuad>[] createDefaultQuads() {
        var mesh = Renderer.get().mutableMesh();

        for (var direction : DIRECTIONS) {
            emitQuads(mesh.emitter(), direction, model.getDefaultQuads(direction).getOrDefault(direction, List.of()));
        }

        return ModelHelper.toQuadLists(mesh.immutableCopy());
    }

    @Override
    public void collectParts(RandomSource randomSource, List<BlockModelPart> list) {
        list.add(this.part);
    }

    @Override
    public @NotNull TextureAtlasSprite particleIcon() {
        return this.part.particleIcon();
    }

    private record Part(AthenaBakedModel model) implements BlockModelPart {

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable Direction direction) {
            var defaultQuads = this.model.defaultQuads;
            if (defaultQuads == null) {
                synchronized (this) {
                    if ((defaultQuads = this.model.defaultQuads) == null) {
                        this.model.defaultQuads = defaultQuads = this.model.createDefaultQuads();
                    }
                }
            }
            return Objects.requireNonNullElse(defaultQuads[ModelHelper.toFaceIndex(direction)], List.of());
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public @NotNull TextureAtlasSprite particleIcon() {
            if (this.model.textures.containsKey(0)) {
                return this.model.textures.get(0);
            }
            return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(MissingTextureAtlasSprite.getLocation());
        }
    }
}
