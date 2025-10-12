package earth.terrarium.athena.api.client.fabric;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.models.TintProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AthenaBakedModel implements BakedModel, FabricBakedModel {

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    private final AthenaBlockModel model;
    private final Int2ObjectMap<TextureAtlasSprite> textures;
    @Nullable
    private List<BakedQuad>[] defaultQuads = null;

    public AthenaBakedModel(AthenaBlockModel model, Function<Material, TextureAtlasSprite> function) {
        this.model = model;
        this.textures = this.model.getTextures(function);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
        WrappedGetter getter = new WrappedGetter(level);
        for (Direction value : DIRECTIONS) {
            emitQuads(emitter, value, model.getQuads(getter, state, pos, value));
        }
    }

    @Override
    public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
        for (Direction direction : DIRECTIONS) {
            emitQuads(emitter, direction, model.getDefaultQuads(direction).getOrDefault(direction, List.of()));
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
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        var defaultQuads = this.defaultQuads;
        if (defaultQuads == null) {
            synchronized (this) {
                if ((defaultQuads = this.defaultQuads) == null) {
                    this.defaultQuads = defaultQuads = createDefaultQuads();
                }
            }
        }

        return defaultQuads[ModelHelper.toFaceIndex(direction)];
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        if (this.textures.containsKey(0)) {
            return this.textures.get(0);
        }
        return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(MissingTextureAtlasSprite.getLocation());
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }
}
