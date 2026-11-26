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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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
    private Map<BlockState, WeightedList<AthenaBlockModel>> resolvedVariantParts;

    public MultiVariantBlockModel(Map<String, WeightedList<AthenaBlockModel>> variants) {
        this.variants = variants;
    }

    private Map<BlockState, WeightedList<AthenaBlockModel>> resolvedParts(Block block) {
        if (this.resolvedVariantParts != null) {
            return this.resolvedVariantParts;
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

        this.resolvedVariantParts = resolvedVariants;

        return resolvedVariants;
    }

    @Override
    public AthenaModelType type() {
        return TYPE;
    }

    @Override
    public void selectParts(RandomSource random, BlockState state, Consumer<Part> output) {
        WeightedList<AthenaBlockModel> weightedModels = this.resolvedParts(state.getBlock()).get(state);

        if (weightedModels == null) {
            return;
        }

        weightedModels.getRandomOrThrow(random).selectParts(random, state, output);
    }

    @Override
    public void collectPossibleParts(BlockState state, Consumer<Part> parts) {
        WeightedList<AthenaBlockModel> weightedModels = resolvedParts(state.getBlock()).get(state);

        if (weightedModels == null) {
            return;
        }

        for (var weightedParts : weightedModels.unwrap()) {
            weightedParts.value().collectPossibleParts(state, parts);
        }
    }
}
