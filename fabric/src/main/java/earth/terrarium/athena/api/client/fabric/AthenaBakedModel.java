package earth.terrarium.athena.api.client.fabric;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class AthenaBakedModel implements BakedModel, FabricBakedModel {

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    private final AthenaBlockModel model;
    private final Int2ObjectMap<TextureAtlasSprite> textures;

    public AthenaBakedModel(AthenaBlockModel model, Function<Material, TextureAtlasSprite> function) {
        this.model = model;
        this.textures = this.model.getTextures(function);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        this.emitQuads(context.getEmitter(), blockView, state, pos);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        this.emitQuads(context.getEmitter(), null, null, null);
    }

    private void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockState state, @Nullable BlockPos pos) {
        for (Direction value : DIRECTIONS) {
            if (level != null && state != null && pos != null) {
                for (var sprite : model.getQuads(level, state, pos, value)) {
                    TextureAtlasSprite texture = textures.get(sprite.sprite());
                    if (texture == null) {
                        continue;
                    }
                    emitter.square(value, sprite.left(), sprite.bottom(), sprite.right(), sprite.top(), sprite.depth());

                    int flag = MutableQuadView.BAKE_LOCK_UV;

                    switch (sprite.rotation()) {
                        case CLOCKWISE_90 -> flag |= MutableQuadView.BAKE_ROTATE_90;
                        case CLOCKWISE_180 -> flag |= MutableQuadView.BAKE_ROTATE_180;
                        case COUNTERCLOCKWISE_90 -> flag |= MutableQuadView.BAKE_ROTATE_270;
                    }

                    emitter.spriteBake(0, texture, flag);
                    emitter.spriteColor(0, -1, -1, -1, -1);
                    emitter.emit();
                }
            }
        }
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return List.of();
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
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return this.textures.get(0);
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
