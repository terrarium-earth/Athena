package earth.terrarium.athena.impl.client.models;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GiantBlockModel implements AthenaBlockModel {
    public static final AthenaModelType GIANT_TYPE = new AthenaModelType(Materials.CODEC.xmap((materials -> new GiantBlockModel(materials, false)), (model) -> model.materials));
    public static final AthenaModelType MURAL_TYPE = new AthenaModelType(Materials.CODEC.xmap((materials -> new GiantBlockModel(materials, true)), (model) -> model.materials));

    private final Materials materials;
    private final boolean mural;

    public GiantBlockModel(Materials materials, boolean mural) {
        this.materials = materials;
        this.mural = mural;
    }

    @Override
    public AthenaModelType type() {
        return mural ? MURAL_TYPE : GIANT_TYPE;
    }

    @Override
    public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState blockState, BlockPos pos, Direction direction) {
        int width = materials.dimensions().width();
        int height = materials.dimensions().height();
        int x = Math.abs(pos.getX());
        int y = Math.abs(pos.getY());
        int z = Math.abs(pos.getZ());

        return switch (direction.getAxis()) {
            case X -> {
                if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    z = Math.abs(width - z % width - 1);
                }
                yield List.of(AthenaQuad.withSprite(1 + (z % width) + (y % height) * width));
            }
            case Z -> {
                if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                    x = Math.abs(width - x % width - 1);
                }
                yield List.of(AthenaQuad.withSprite(1 + (x % width) + (y % height) * width));
            }
            default -> {
                if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                    z = Math.abs(width - z % width - 1);
                }
                yield List.of(AthenaQuad.withSprite(1 + (x % width) + (z % height) * width));
            }
        };
    }

    @Override
    public Map<Direction, List<AthenaQuad>> getDefaultQuads(Direction direction) {
        Map<Direction, List<AthenaQuad>> quads = new HashMap<>(Direction.values().length);
        for (Direction dir : Direction.values()) {
            quads.put(dir, List.of(AthenaQuad.withSprite(0)));
        }
        return quads;
    }

    @Override
    public Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter) {
        Int2ObjectMap<Material.Baked> textures = new Int2ObjectArrayMap<>();

        for (Map.Entry<MaterialKey, Material> entry : materials.sections().entrySet()) {
            textures.put(entry.getKey().textureIndex(), getter.apply(entry.getValue()));
        }

        return textures;
    }

    public record Dimensions(int width, int height) {
        public static final MapCodec<Dimensions> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(Dimensions::width),
            ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(Dimensions::height)
        ).apply(instance, Dimensions::new));
    }

    private sealed interface MaterialKey {
        String PARTICLE_KEY = "particle";

        Codec<MaterialKey> CODEC = Codec.STRING.comapFlatMap((value) -> {
            if (value.equals(PARTICLE_KEY)) {
                return DataResult.success(ParticleKey.INSTANCE);
            }

            try {
                return DataResult.success(new SectionKey(Integer.parseInt(value)));
            } catch (NumberFormatException error) {
                return DataResult.error(error::getMessage);
            }
        }, MaterialKey::toString);

        static Keyable keys(Dimensions dimensions) {
            return Keyable.forStrings(() -> Stream.concat(SectionKey.keys(dimensions), Stream.of(PARTICLE_KEY)));
        }

        int textureIndex();

        record SectionKey(int section) implements MaterialKey {
            @Override
            public @NotNull String toString() {
                return String.valueOf(section());
            }

            private static Stream<String> keys(Dimensions dimensions) {
                return IntStream
                    .range(1, dimensions.width * dimensions.height + 1)
                    .mapToObj(String::valueOf);
            }

            @Override
            public int textureIndex() {
                return section();
            }
        }

        final class ParticleKey implements MaterialKey {
            public static final ParticleKey INSTANCE = new ParticleKey();

            @Override
            public String toString() {
                return PARTICLE_KEY;
            }

            @Override
            public int textureIndex() {
                return 0;
            }
        }
    }

    public record Materials(
        Dimensions dimensions,
        Map<MaterialKey, Material> sections
    ) {
        public static final MapCodec<Materials> CODEC = Dimensions.CODEC.dispatchMap(Materials::dimensions, Materials::codec);

        private static MapCodec<Materials> codec(Dimensions dimensions) {
            MapCodec<Materials> baseCodec = Codec.simpleMap(
                    MaterialKey.CODEC,
                    Material.CODEC,
                    MaterialKey.keys(dimensions)
            ).xmap((sections) -> new Materials(dimensions, sections), Materials::sections);

            return baseCodec.fieldOf("ctm_textures");
        }
    }
}
