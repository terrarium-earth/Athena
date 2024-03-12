package earth.terrarium.athena.api.client.fabric;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
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
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        WrappedGetter getter = new WrappedGetter(blockView);
        for (Direction value : DIRECTIONS) {
            emitQuads(context.getEmitter(), value, model.getQuads(getter, state, pos, value));
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        for (var direction : DIRECTIONS) {
            emitQuads(context.getEmitter(), direction, model.getDefaultQuads(direction).getOrDefault(direction, List.of()));
        }
    }

    private void emitQuads(QuadEmitter emitter, @Nullable Direction side, List<AthenaQuad> quads) {
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
            emitter.color(-1, -1, -1, -1);
            emitter.emit();
        }
    }

    private List<BakedQuad>[] createDefaultQuads() {
        var renderer = RendererAccess.INSTANCE.getRenderer();
        if (renderer == null) {
            return new List[] { List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of() };
        }
        var meshBuilder = renderer.meshBuilder();

        for (var direction : DIRECTIONS) {
            emitQuads(meshBuilder.getEmitter(), direction, model.getDefaultQuads(direction).getOrDefault(direction, List.of()));
        }

        return ModelHelper.toQuadLists(meshBuilder.build());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
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
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        if (this.textures.containsKey(0)) {
            return this.textures.get(0);
        }
        return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(MissingTextureAtlasSprite.getLocation());
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
