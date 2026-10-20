package earth.terrarium.athena.impl.client.models.ctm;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.CtmState;
import earth.terrarium.athena.impl.client.models.materials.MaterialStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectedTextureMap {

    private final EnumMap<Direction, CtmProvider> quads = new EnumMap<>(Direction.class);

    public ConnectedTextureMap() {}

    public List<AthenaQuad> getQuads(Direction direction, CtmState state, float depth) {
        var provider = quads.get(direction);
        if (provider == null) {
            throw new IllegalStateException("No CTM provider for direction " + direction);
        }
        return provider.get(state, depth);
    }

    public Map<Direction, List<AthenaQuad>> getDefaultQuads(Direction direction, CtmState state, float depth) {
        return Object2ObjectMaps.singleton(direction, getQuads(direction, state, depth));
    }

    public record DirectionalCtmProviders(
        Map<Optional<Direction>, CtmProvider.Type> directions
    ) {
        private static final String OPTIONAL_KEY = "default";

        private static final Codec<DirectionalCtmProviders> NON_DIRECTIONAL_CODEC = CtmProvider.Type.CODEC.flatComapMap(
            DirectionalCtmProviders::new,
            (providers) -> providers.defaultProvider().isPresent() && providers.directions().isEmpty() ?
                DataResult.success(providers.defaultProvider().get()) :
                DataResult.error(() -> "Can not encode DirectionalCtmProviders to a single provider when default provider isn't present or there are directional providers defined")
        );

        private static final Codec<Optional<Direction>> OPTIONAL_DIRECTION_CODEC = Codec.either(
            Codec.STRING.comapFlatMap(
                (value) -> value.equals(OPTIONAL_KEY) ?
                    DataResult.success(Optional.<Direction>empty()) :
                    DataResult.error(() -> "Expected '" + OPTIONAL_KEY + "', got '" + value + "'"),
                (unit) -> OPTIONAL_KEY
            ),
            Direction.CODEC.xmap(Optional::of, Optional::get)
        ).xmap(Either::unwrap, (optional) -> optional.isEmpty() ? Either.left(optional) : Either.right(optional));

        private static final MapCodec<Map<Optional<Direction>, CtmProvider.Type>> DIRECTIONAL_TYPES_MAP_CODEC = Codec.simpleMap(
                OPTIONAL_DIRECTION_CODEC,
                CtmProvider.Type.CODEC,
                Keyable.forStrings(() -> Stream.concat(
                    Arrays.stream(Direction.values()).map(Direction::getSerializedName),
                    Stream.of(OPTIONAL_KEY)
                ))
        );

        private static final Codec<DirectionalCtmProviders> FULL_DIRECTIONAL_CODEC = DIRECTIONAL_TYPES_MAP_CODEC
            .xmap(DirectionalCtmProviders::new, DirectionalCtmProviders::directions)
            .codec();

        private static final Codec<DirectionalCtmProviders> VALIDATED_DIRECTIONAL_CODEC = FULL_DIRECTIONAL_CODEC.validate((providers) -> {
            if (providers.directions().size() < Direction.values().length && providers.defaultProvider().isEmpty()) {
                return DataResult.error(() -> {
                    String directions = Arrays.stream(Direction.values()).map(StringRepresentable::getSerializedName).collect(Collectors.joining(", "));

                    return "Must define all directions (" + directions + ") or define a 'default' value";
                });
            } else {
                return DataResult.success(providers);
            }
        });

        public static final Codec<DirectionalCtmProviders> CODEC = VALIDATED_DIRECTIONAL_CODEC.withAlternative(NON_DIRECTIONAL_CODEC);

        public DirectionalCtmProviders(CtmProvider.Type provider) {
            this(Map.of(Optional.empty(), provider));
        }

        public ConnectedTextureMap resolve(MaterialStorage materials) {
            var textures = new ConnectedTextureMap();

            for (Direction direction : Direction.values()) {
                CtmProvider.Type type = directions().get(Optional.of(direction));

                if (type == null) {
                    type = defaultProvider().orElseThrow();
                }

                textures.quads.put(direction, type.build(materials));
            }

            return textures;
        }

        private Optional<CtmProvider.Type> defaultProvider() {
            return Optional.ofNullable(directions().get(Optional.<Direction>empty()));
        }
    }
}
