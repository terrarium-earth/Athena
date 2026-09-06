package earth.terrarium.athena.impl.client.models.ctm;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.CtmState;
import earth.terrarium.athena.impl.client.models.materials.MaterialStorage;

import java.util.List;

public record FourtySevenSliceCtmProvider(
        int[] sprites
) implements CtmProvider {
    private static final String SLICE_47_KEY = "[$index]";

    private static final Codec<String> SLICE_47_STRING = Codec.STRING.validate((value) -> value.contains(SLICE_47_KEY) ?
        DataResult.success(value) :
        DataResult.error(() -> "String " + value + " is not a 47 slice string.")
    );

    public static final Codec<FourtySevenSliceCtmProvider.Type> CODEC = SLICE_47_STRING.xmap(Type::new, Type::texture);

    private static final int EXPECTED_SPRITES = 47;
    private static final int[] BIT_TO_INDEX = new int[]{
            0, 36, 12, 24, 3, 17, 5, 19, 1, 16, 4, 6, 2, 18, 7, 46,
            0, 36, 12, 24, 3, 39, 5, 41, 1, 16, 4, 6, 2, 42, 7, 20,
            0, 36, 12, 24, 3, 17, 5, 19, 1, 37, 4, 30, 2, 40, 7, 8,
            0, 36, 12, 24, 3, 39, 5, 41, 1, 37, 4, 30, 2, 38, 7, 11,
            0, 36, 12, 24, 3, 17, 15, 43, 1, 16, 4, 6, 2, 18, 29, 21,
            0, 36, 12, 24, 3, 39, 15, 27, 1, 16, 4, 6, 2, 42, 29, 10,
            0, 36, 12, 24, 3, 17, 15, 43, 1, 37, 4, 30, 2, 40, 29, 34,
            0, 36, 12, 24, 3, 39, 15, 27, 1, 37, 4, 30, 2, 38, 29, 32,
            0, 36, 12, 24, 3, 17, 5, 19, 1, 16, 13, 28, 2, 18, 31, 9,
            0, 36, 12, 24, 3, 39, 5, 41, 1, 16, 13, 28, 2, 42, 31, 35,
            0, 36, 12, 24, 3, 17, 5, 19, 1, 37, 13, 25, 2, 40, 31, 23,
            0, 36, 12, 24, 3, 39, 5, 41, 1, 37, 13, 25, 2, 38, 31, 33,
            0, 36, 12, 24, 3, 17, 15, 43, 1, 16, 13, 28, 2, 18, 14, 22,
            0, 36, 12, 24, 3, 39, 15, 27, 1, 16, 13, 28, 2, 42, 14, 44,
            0, 36, 12, 24, 3, 17, 15, 43, 1, 37, 13, 25, 2, 40, 14, 45,
            0, 36, 12, 24, 3, 39, 15, 27, 1, 37, 13, 25, 2, 38, 14, 26
    };

    public FourtySevenSliceCtmProvider {
        Preconditions.checkArgument(sprites.length == EXPECTED_SPRITES, "Expected 47 sprites for 47 slice CTM, got " + sprites.length);
    }

    @Override
    public List<AthenaQuad> get(CtmState state, float depth) {
        int index = BIT_TO_INDEX[toBits(state) & 0xFF];
        return List.of(AthenaQuad.square(this.sprites[index], depth));
    }

    private static int toBits(CtmState state) {
        int bits = 0;
        if (state.up()) bits |= 1;
        if (state.down()) bits |= 2;
        if (state.left()) bits |= 4;
        if (state.right()) bits |= 8;
        if (state.upLeft()) bits |= 16;
        if (state.upRight()) bits |= 32;
        if (state.downLeft()) bits |= 64;
        if (state.downRight()) bits |= 128;
        return bits;
    }

    public record Type(String texture) implements CtmProvider.Type {
        @Override
        public CtmProvider build(MaterialStorage materials) {
            int[] sprites = new int[47];

            for (int i = 0; i < sprites.length; i++) {
                sprites[i] = materials.put(texture.replace(SLICE_47_KEY, String.valueOf(i)));
            }

            return new FourtySevenSliceCtmProvider(sprites);
        }
    }
}
