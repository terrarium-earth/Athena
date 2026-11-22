package earth.terrarium.athena.impl.client.models;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelAttributes;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.block.dispatch.VariantSelector;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiVariantBlockModel implements AthenaBlockModel {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final MapCodec<MultiVariantBlockModel> CODEC = Codec
            .unboundedMap(Codec.STRING, AthenaBlockModel.CODEC.codec())
            .fieldOf("variants")
            .xmap(MultiVariantBlockModel::new, (model) -> model.variants);

    public static final AthenaModelType TYPE = new AthenaModelType(CODEC);

    private final Map<String, AthenaBlockModel> variants;
    private Map<BlockState, AthenaBlockModel> resolvedVariants;

    public MultiVariantBlockModel(Map<String, AthenaBlockModel> variants) {
        this.variants = variants;
    }

    private Map<BlockState, AthenaBlockModel> resolvedModels(Block block) {
        if (this.resolvedVariants != null) {
            return this.resolvedVariants;
        }

        Map<BlockState, AthenaBlockModel> resolvedVariants = new HashMap<>();

        for (Map.Entry<String, AthenaBlockModel> entry : variants.entrySet()) {
            try {
                var predicate = VariantSelector.predicate(block.getStateDefinition(), entry.getKey());

                for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                    if (!resolvedVariants.containsKey(state) && predicate.test(state)) {
                        resolvedVariants.put(state, entry.getValue());
                    }
                }
            } catch (RuntimeException e) {
                LOGGER.error("Invalid variant selector '{}' used in Athena multi-variant model for {}", entry.getKey(), block);
            }
        }

        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            if (!resolvedVariants.containsKey(state)) {
                LOGGER.error("No variant selector matches {} in Athena multi-variant model", state);
            }
        }

        this.resolvedVariants = resolvedVariants;

        return resolvedVariants;
    }

    private <T> T delegate(BlockState state, Function<AthenaBlockModel, T> resolve, Supplier<T> fallback) {
        AthenaBlockModel model = resolvedModels(state.getBlock()).get(state);

        if (model != null) {
            return resolve.apply(model);
        }

        return fallback.get();
    }

    @Override
    public AthenaModelType type() {
        return TYPE;
    }

    @Override
    public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        return delegate(
            state,
            (model) -> model.getQuads(level, state, pos, direction),
            List::of
        );
    }

    @Override
    public Map<Direction, List<AthenaQuad>> getDefaultQuads(@Nullable Direction direction, BlockState state) {
        return delegate(state, (model) -> model.getDefaultQuads(direction, state), () -> getDefaultQuads(direction));
    }

    @Override
    public Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter, BlockState state) {
        return delegate(state, (model) -> model.getTextures(getter, state), () -> getTextures(getter));
    }

    @Override
    public AthenaModelAttributes getAttributes(BlockState state) {
        return delegate(state, (model) -> model.getAttributes(state), this::getAttributes);
    }

    @Override
    public Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter) {
        return getTextures(getter, Blocks.AIR.defaultBlockState());
    }
}
