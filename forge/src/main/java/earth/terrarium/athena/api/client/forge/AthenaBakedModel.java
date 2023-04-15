package earth.terrarium.athena.api.client.forge;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.forge.NullableEnumMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AthenaBakedModel implements IDynamicBakedModel {

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    public static final ModelProperty<NullableEnumMap<Direction, List<BakedQuad>>> DATA = new ModelProperty<>();

    private final AthenaBlockModel model;
    private final Int2ObjectMap<TextureAtlasSprite> textures;

    public AthenaBakedModel(AthenaBlockModel model, Function<Material, TextureAtlasSprite> function) {
        this.model = model;
        this.textures = this.model.getTextures(function);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @NotNull RandomSource random, @NotNull ModelData data, @Nullable RenderType type) {
        if (data.has(DATA)) {
            return data.get(DATA).getOrDefault(direction, List.of());
        }
        return List.of();
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData data) {
        final NullableEnumMap<Direction, List<BakedQuad>> quads = new NullableEnumMap<>(Direction.class);
        List<BakedQuad> nonCullQuads = new ArrayList<>();
        for (Direction direction : DIRECTIONS) {
            List<AthenaQuad> culledQuads = new ArrayList<>();
            List<AthenaQuad> unculledQuads = new ArrayList<>();
            for (AthenaQuad quad : this.model.getQuads(level, state, pos, direction)) {
                if (quad.cull()) {
                    culledQuads.add(quad);
                } else {
                    unculledQuads.add(quad);
                }
            }
            quads.put(direction, bakeQuads(culledQuads, direction));
            nonCullQuads.addAll(bakeQuads(unculledQuads, direction));
        }
        quads.put(null, nonCullQuads);
        return data.derive().with(DATA, quads).build();
    }

    private List<BakedQuad> bakeQuads(List<AthenaQuad> quads, Direction direction) {
        List<BakedQuad> bakedQuads = new ArrayList<>(quads.size());
        for (AthenaQuad quad : quads) {
            bakedQuads.addAll(ForgeAthenaUtils.bakeQuad(quad, direction, this.textures.get(quad.sprite())));
        }
        return bakedQuads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
