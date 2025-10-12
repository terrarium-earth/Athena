package earth.terrarium.athena.api.client.neoforge;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelAttributes;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
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

import java.util.*;
import java.util.function.Function;

public class AthenaBakedModel implements BlockStateModel {

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    private final AthenaBlockModel model;
    private final Int2ObjectMap<TextureAtlasSprite> textures;
    private final AthenaModelAttributes attributes;
    private final BlockModelPart part;

    public AthenaBakedModel(AthenaBlockModel model, Function<Material, TextureAtlasSprite> function) {
        this.model = model;
        this.textures = this.model.getTextures(function);
        this.attributes = model.getAttributes();
        //noinspection deprecation
        this.part = new Part(attributes.getLayer() != null ? attributes.getLayer() : this.model.getLayerType());
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<BlockModelPart> parts) {
        WrappedGetter getter = new WrappedGetter(level);
        final NullableEnumMap<Direction, Map<Direction, List<AthenaQuad>>> quads = new NullableEnumMap<>(Direction.class);
        Map<Direction, List<AthenaQuad>> nonCullQuads = new HashMap<>();
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
        parts.add(new AthenaModelPart(this.part, quads, this.textures, this.attributes.getTint()));
    }

    @Override
    public void collectParts(@NotNull RandomSource arg, @NotNull List<BlockModelPart> list) {
        list.add(this.part);
    }

    @Override
    public @NotNull TextureAtlasSprite particleIcon() {
        return this.part.particleIcon();
    }

    private class Part implements BlockModelPart {

        private final NullableEnumMap<Direction, List<BakedQuad>> defaultQuads = new NullableEnumMap<>(Direction.class);
        private final ChunkSectionLayer layerType;

        public Part(ChunkSectionLayer layerType) {
            this.layerType = layerType;
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable Direction arg) {
            var quads = this.defaultQuads.get(arg);
            if (quads == null) {
                quads = new ArrayList<>();

                var defaults = AthenaBakedModel.this.model.getDefaultQuads(arg);
                var tint = AthenaBakedModel.this.attributes.getTint();
                var textures = AthenaBakedModel.this.textures;
                for (var entry : defaults.entrySet()) {
                    for (var quad : entry.getValue()) {
                        TextureAtlasSprite sprite = textures.get(quad.sprite());
                        if (sprite == null) continue;
                        quads.addAll(ForgeAthenaUtils.bakeQuad(quad, entry.getKey(), sprite, tint));
                    }
                }

                this.defaultQuads.put(arg, quads);
            }
            return quads;
        }

        @Override
        public @NotNull ChunkSectionLayer getRenderType(@NotNull BlockState state) {
            return Objects.requireNonNullElseGet(this.layerType, () -> BlockModelPart.super.getRenderType(state));
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public @NotNull TextureAtlasSprite particleIcon() {
            if (AthenaBakedModel.this.textures.containsKey(0)) {
                return AthenaBakedModel.this.textures.get(0);
            }
            return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(MissingTextureAtlasSprite.getLocation());
        }
    }
}
