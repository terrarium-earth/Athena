package earth.terrarium.athena.api.client.models;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ARGB;

import java.util.List;

public sealed interface TintProvider {
    Codec<TintProvider> CODEC = Codec.either(
        Codec.INT.xmap(Index::new, Index::index),
        Static.CODEC
    ).xmap(
        Either::unwrap,
        (provider) -> switch (provider) {
            case Index tintIndex -> Either.left(tintIndex);
            case Static staticTint -> Either.right(staticTint);
        }
    );

    record Static(int color) implements TintProvider {
        private static final Codec<Integer> INT_COLOR_CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                Codec.INT.optionalFieldOf("r", 0xff).forGetter(ARGB::red),
                Codec.INT.optionalFieldOf("g", 0xff).forGetter(ARGB::green),
                Codec.INT.optionalFieldOf("b", 0xff).forGetter(ARGB::blue),
                Codec.INT.optionalFieldOf("a", 0xff).forGetter(ARGB::alpha)
            ).apply(instance, ARGB::color)
        );

        public static final Codec<Static> CODEC = Codec.withAlternative(
            INT_COLOR_CODEC.xmap(Static::new, Static::color),
            Codec.INT.listOf(3, 4).xmap(Static::fromList, Static::toList)
        );

        private List<Integer> toList() {
            int r = ARGB.red(color());
            int g = ARGB.green(color());
            int b = ARGB.blue(color());
            int a = ARGB.alpha(color());

            if (a == 0xFF) {
                return List.of(r, g, b);
            } else {
                return List.of(r, g, b, a);
            }
        }

        private static Static fromList(List<Integer> colorList) {
            int r = colorList.get(0);
            int g = colorList.get(1);
            int b = colorList.get(2);
            int a = colorList.size() == 4 ? colorList.get(3) : 0xFF;

            return new Static(ARGB.color(a, r, g, b));
        }
    }

    record Index(int index) implements TintProvider {
    }
}
