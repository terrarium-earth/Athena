package earth.terrarium.athena.api.client.neoforge;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AthenaUtils;
import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AthenaBakedModel implements IDynamicBakedModel {

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    public static final ModelProperty<NullableEnumMap<Direction, Map<Direction, List<AthenaQuad>>>> DATA = new ModelProperty<>();

    private final AthenaBlockModel model;
    private final Int2ObjectMap<TextureAtlasSprite> textures;

    public AthenaBakedModel(AthenaBlockModel model, Function<Material, TextureAtlasSprite> function) {
        this.model = model;
        this.textures = this.model.getTextures(function);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @NotNull RandomSource random, @NotNull ModelData data, @Nullable RenderType type) {
        List<BakedQuad> quads = new ArrayList<>();
        try {
            Map<Direction, List<AthenaQuad>> values = data.has(DATA) ?
                    data.get(DATA).getOrDefault(direction, Map.of()) :
                    this.model.getDefaultQuads(direction);
            values.forEach((dir, quadList) -> quads.addAll(bakeQuads(quadList, dir)));
        }catch (Exception e) {
            AthenaUtils.LOGGER.error("Error occurred while getting quads of Athena block model", e);
            throw e; //We do this because Mojang tends to capture and do nothing with the error messages print error type.
        }
        return quads;
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData data) {
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
        return data.derive().with(DATA, quads).build();
    }

    private List<BakedQuad> bakeQuads(List<AthenaQuad> quads, Direction direction) {
        List<BakedQuad> bakedQuads = new ArrayList<>(quads.size());
        for (AthenaQuad quad : quads) {
            TextureAtlasSprite sprite = this.textures.get(quad.sprite());
            if (sprite == null) continue;
            bakedQuads.addAll(ForgeAthenaUtils.bakeQuad(quad, direction, sprite));
        }
        return bakedQuads;
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
        return true;
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
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
