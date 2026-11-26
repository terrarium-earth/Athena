package earth.terrarium.athena.impl.client.models;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import net.minecraft.client.renderer.block.dispatch.VariantSelector;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MultiVariantBlockModel implements AthenaBlockModel {

    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Codec<WeightedList<AthenaBlockModel>> VARIANTS_CODEC = Codec.either(
        AthenaBlockModel.CODEC.codec(),
        WeightedList.nonEmptyCodec(AthenaBlockModel.CODEC)
    ).xmap(
        (either) -> either.map(WeightedList::of, Function.identity()),
        (variants) -> {
            if (variants.unwrap().size() == 1) {
                return Either.left(variants.unwrap().getFirst().value());
            }

            return Either.right(variants);
        }
    );

    public static final MapCodec<MultiVariantBlockModel> CODEC = Codec
            .unboundedMap(Codec.STRING, VARIANTS_CODEC)
            .fieldOf("variants")
            .xmap(MultiVariantBlockModel::new, (model) -> model.variants);

    public static final AthenaModelType TYPE = new AthenaModelType(CODEC);

    private final Map<String, WeightedList<AthenaBlockModel>> variants;
    private Map<BlockState, WeightedList<AthenaBlockModel>> resolvedModels;

    public MultiVariantBlockModel(Map<String, WeightedList<AthenaBlockModel>> variants) {
        this.variants = variants;
    }

    private Map<BlockState, WeightedList<AthenaBlockModel>> resolvedModels(Block block) {
        if (this.resolvedModels != null) {
            return this.resolvedModels;
        }

        Map<BlockState, WeightedList<AthenaBlockModel>> resolvedVariants = new HashMap<>();

        for (var entry : variants.entrySet()) {
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

        this.resolvedModels = resolvedVariants;

        return resolvedVariants;
    }

    private WeightedList<AthenaBlockModel> getModels(BlockState state) {
        return resolvedModels(state.getBlock()).get(state);
    }

    @Override
    public AthenaModelType type() {
        return TYPE;
    }

    @Override
    public List<Part> selectParts(RandomSource random, BlockState state) {
        WeightedList<AthenaBlockModel> weightedModels = getModels(state);

        if (weightedModels == null) {
            return List.of();
        }

        return weightedModels.getRandomOrThrow(random).selectParts(random, state);
    }

    @Override
    public List<Part> getAllParts(BlockState state) {
        WeightedList<AthenaBlockModel> weightedModels = getModels(state);

        if (weightedModels == null) {
            return List.of();
        }

        List<Part> parts = new ArrayList<>();
        for (var weightedParts : weightedModels.unwrap()) {
            parts.addAll(weightedParts.value().getAllParts(state));
        }

        return parts;
    }
}
