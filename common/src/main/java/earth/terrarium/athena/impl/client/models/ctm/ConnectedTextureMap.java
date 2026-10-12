package earth.terrarium.athena.impl.client.models.ctm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.CtmState;
import earth.terrarium.athena.impl.client.models.materials.MaterialStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import java.util.*;
import java.util.stream.Collectors;

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
        Optional<CtmProvider.Type> defaultProvider,
        Map<Direction, CtmProvider.Type> directions
    ) {
        private static final Codec<DirectionalCtmProviders> NON_DIRECTIONAL_CODEC = CtmProvider.Type.CODEC.flatComapMap(
            DirectionalCtmProviders::new,
            (providers) -> providers.defaultProvider().isPresent() && providers.directions().isEmpty() ?
                DataResult.success(providers.defaultProvider().get()) :
                DataResult.error(() -> "Can not encode DirectionalCtmProviders to a single provider when default provider isn't present or there are directional providers defined")
        );

        private static final Codec<DirectionalCtmProviders> FULL_DIRECTIONAL_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            CtmProvider.Type.CODEC.optionalFieldOf("default").forGetter(DirectionalCtmProviders::defaultProvider),
            Codec.simpleMap(Direction.CODEC, CtmProvider.Type.CODEC, StringRepresentable.keys(Direction.values())).forGetter(DirectionalCtmProviders::directions)
        ).apply(instance, DirectionalCtmProviders::new));

        public static final Codec<DirectionalCtmProviders> CODEC =
            NON_DIRECTIONAL_CODEC.withAlternative(FULL_DIRECTIONAL_CODEC.validate((providers) -> {
                if (providers.directions().size() < Direction.values().length && providers.defaultProvider().isEmpty()) {
                    return DataResult.error(() -> {
                        String directions = Arrays.stream(Direction.values()).map(StringRepresentable::getSerializedName).collect(Collectors.joining(", "));

                        return "Must define all directions (" + directions + ") or define a 'default' value";
                    });
                } else {
                    return DataResult.success(providers);
                }
            }));

        public DirectionalCtmProviders(CtmProvider.Type provider) {
            this(Optional.of(provider), Map.of());
        }

        public ConnectedTextureMap resolve(MaterialStorage materials) {
            var textures = new ConnectedTextureMap();

            for (Direction direction : Direction.values()) {
                CtmProvider.Type type = directions().get(direction);

                if (type == null) {
                    type = defaultProvider.orElseThrow();
                }

                textures.quads.put(direction, type.build(materials));
            }

            return textures;
        }
    }
}
