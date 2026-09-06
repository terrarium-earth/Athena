package earth.terrarium.athena.api.client.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Optionull;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

public sealed interface ModelConnectionCondition extends BiPredicate<BlockState, BlockState> {
    BiMap<String, MapCodec<? extends ModelConnectionCondition>> CONDITION_TYPES = HashBiMap.create();

    Codec<MapCodec<? extends ModelConnectionCondition>> TYPE_CODEC = Codec.STRING
        .flatComapMap(
            (type) -> CONDITION_TYPES.getOrDefault(type, False.CODEC),
            (type) -> Optionull.mapOrElse(
                CONDITION_TYPES.inverse().get(type),
                DataResult::success,
                () -> DataResult.error(() -> "Unregistered condition type " + type)
            )
        );

    Codec<ModelConnectionCondition> CODEC = TYPE_CODEC
        .dispatch(ModelConnectionCondition::codec, Function.identity())
        .withAlternative(False.CODEC.codec());

    MapCodec<ModelConnectionCondition> CONNECTS_TO_CODEC = CODEC
        .optionalFieldOf("connect_to", SameState.INSTANCE);

    MapCodec<? extends ModelConnectionCondition> codec();

    record Not(Optional<ModelConnectionCondition> condition) implements ModelConnectionCondition {
        public static final MapCodec<Not> CODEC = ModelConnectionCondition.CODEC
            .optionalFieldOf("condition")
            .xmap(Not::new, Not::condition);

        @Override
        public MapCodec<Not> codec() {
            return CODEC;
        }

        @Override
        public boolean test(BlockState selfState, BlockState otherState) {
            return condition().isPresent() && !condition().get().test(selfState, otherState);
        }
    }

    record And(List<ModelConnectionCondition> conditions) implements ModelConnectionCondition {
        public static final MapCodec<And> CODEC = ModelConnectionCondition.CODEC
            .listOf()
            .optionalFieldOf("conditions", List.of())
            .xmap(And::new, And::conditions);

        @Override
        public MapCodec<And> codec() {
            return CODEC;
        }

        @Override
        public boolean test(BlockState selfState, BlockState otherState) {
            if (conditions().isEmpty()) {
                return false;
            }

            return conditions()
                .stream()
                .reduce(
                    true,
                    (base, condition) -> condition.test(selfState, otherState),
                    (a, b) -> a && b
                );
        }
    }

    record Or(List<ModelConnectionCondition> conditions) implements ModelConnectionCondition {
        public static final MapCodec<Or> CODEC = ModelConnectionCondition.CODEC
            .listOf()
            .optionalFieldOf("conditions", List.of())
            .xmap(Or::new, Or::conditions);

        @Override
        public MapCodec<Or> codec() {
            return CODEC;
        }

        @Override
        public boolean test(BlockState selfState, BlockState otherState) {
            return conditions()
                .stream()
                .reduce(
                    false,
                    (base, condition) -> condition.test(selfState, otherState),
                    (a, b) -> a || b
                );
        }
    }

    record Xor(Optional<Pair<ModelConnectionCondition, ModelConnectionCondition>> conditions) implements ModelConnectionCondition {
        public static final MapCodec<Xor> CODEC = ModelConnectionCondition.CODEC
            .listOf(2, 2)
            .optionalFieldOf("conditions")
            .xmap(
                (optionalList) ->
                    optionalList.map((list) -> Pair.of(list.get(0), list.get(1))),
                (optionalPair) ->
                    optionalPair.map((pair) -> List.of(pair.getFirst(), pair.getSecond()))
            )
            .xmap(Xor::new, Xor::conditions);


        @Override
        public MapCodec<Xor> codec() {
            return CODEC;
        }

        @Override
        public boolean test(BlockState selfState, BlockState otherState) {
            if (conditions().isEmpty()) {
                return false;
            }

            var conditions = this.conditions().get();

            return conditions.getFirst().test(selfState, otherState) ^
                conditions.getSecond().test(selfState, otherState);
        }
    }

    record State(Optional<Block> block, StatePropertiesPredicate propertiesPredicate) implements ModelConnectionCondition {
        public static final MapCodec<State> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block").forGetter(State::block),
            StatePropertiesPredicate.CODEC.optionalFieldOf("properties", StatePropertiesPredicate.Builder.properties().build().get()).forGetter(State::propertiesPredicate)
        ).apply(instance, State::new));

        @Override
        public MapCodec<State> codec() {
            return CODEC;
        }

        @Override
        public boolean test(BlockState selfState, BlockState otherState) {
            if (this.block().isEmpty()) {
                return false;
            }

            var block = this.block().get();

            if (!otherState.is(block)) {
                return false;
            }

            return propertiesPredicate().matches(otherState);
        }
    }

    record Tag(TagKey<Block> tag) implements ModelConnectionCondition {
        public static final MapCodec<Tag> CODEC = TagKey
            .codec(Registries.BLOCK)
            .fieldOf("tag")
            .xmap(Tag::new, Tag::tag);

        @Override
        public MapCodec<Tag> codec() {
            return CODEC;
        }

        @Override
        public boolean test(BlockState selfState, BlockState otherState) {
            return otherState.is(tag);
        }
    }

    final class SameBlock implements ModelConnectionCondition {
        public static final SameBlock INSTANCE = new SameBlock();
        public static final MapCodec<SameBlock> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<SameBlock> codec() {
            return CODEC;
        }

        @Override
        public boolean test(BlockState selfState, BlockState otherState) {
            return selfState.is(otherState.getBlock());
        }
    }

    final class SameState implements ModelConnectionCondition {
        public static final SameState INSTANCE = new SameState();
        public static final MapCodec<SameState> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<SameState> codec() {
            return CODEC;
        }

        @Override
        public boolean test(BlockState selfState, BlockState otherState) {
            return selfState == otherState;
        }
    }

    final class False implements ModelConnectionCondition {
        public static final False INSTANCE = new False();
        public static final MapCodec<False> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<False> codec() {
            return CODEC;
        }

        @Override
        public boolean test(BlockState selfState, BlockState otherState) {
            return false;
        }
    }
}
