package earth.terrarium.athena.impl.client.models.ctm;

import com.mojang.serialization.Codec;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.CtmMaterials;
import earth.terrarium.athena.api.client.utils.CtmState;
import earth.terrarium.athena.impl.client.models.materials.MaterialStorage;
import net.minecraft.world.level.block.Rotation;

import java.util.List;

public record FourSliceCtmProvider(
        int particle, int center, int vertical, int horizontal, int empty
) implements CtmProvider {
    public static final Codec<FourSliceCtmProvider.Type> CODEC = CtmMaterials.CODEC.codec().xmap(Type::new, Type::materials);

    private int getTexture(boolean first, boolean second, boolean diagonal) {
        if (first && second) {
            return diagonal ? empty : center;
        } else if (first) {
            return vertical;
        } else if (second) {
            return horizontal;
        }
        return particle;
    }

    @Override
    public List<AthenaQuad> get(CtmState state, float depth) {
        if (state.allTrue()) {
            return List.of(AthenaQuad.square(empty, depth));
        }

        return List.of(
                new AthenaQuad(getTexture(state.up(), state.left(), state.upLeft()), 0, 0.5f, 1f, 0.5f, Rotation.NONE, depth),
                new AthenaQuad(getTexture(state.up(), state.right(), state.upRight()), 0.5f, 1f, 1f, 0.5f, Rotation.NONE, depth),
                new AthenaQuad(getTexture(state.down(), state.left(), state.downLeft()), 0, 0.5f, 0.5f, 0f, Rotation.NONE, depth),
                new AthenaQuad(getTexture(state.down(), state.right(), state.downRight()), 0.5f, 1f, 0.5f, 0f, Rotation.NONE, depth)
        );
    }

    public record Type(CtmMaterials materials) implements CtmProvider.Type {
        @Override
        public CtmProvider build(MaterialStorage materials) {
            return new FourSliceCtmProvider(
                materials.put(this.materials().particle()),
                materials.put(this.materials().center()),
                materials.put(this.materials().vertical()),
                materials.put(this.materials().horizontal()),
                materials.put(this.materials().empty())
            );
        }
    }
}
