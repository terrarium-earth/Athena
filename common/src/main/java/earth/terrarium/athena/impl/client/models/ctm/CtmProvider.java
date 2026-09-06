package earth.terrarium.athena.impl.client.models.ctm;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.CtmState;
import earth.terrarium.athena.impl.client.models.materials.MaterialStorage;

import java.util.List;
import java.util.function.Function;

public interface CtmProvider {
    List<AthenaQuad> get(CtmState state, float depth);

    sealed interface Type permits FourSliceCtmProvider.Type, FourtySevenSliceCtmProvider.Type, SingleSpriteCtmProvider.Type {
        Codec<CtmProvider.Type> CODEC = Codec.either(
            Codec.either(FourSliceCtmProvider.CODEC, FourtySevenSliceCtmProvider.CODEC),
            SingleSpriteCtmProvider.CODEC
        ).xmap(
            (value) -> value.map(Either::unwrap, Function.identity()),
            (type) -> switch (type) {
                case FourSliceCtmProvider.Type fourSlice -> Either.left(Either.left(fourSlice));
                case FourtySevenSliceCtmProvider.Type fourtySeven -> Either.left(Either.right(fourtySeven));
                case SingleSpriteCtmProvider.Type singleSprite -> Either.right(singleSprite);
            }
        );

        CtmProvider build(MaterialStorage materials);
    }
}
