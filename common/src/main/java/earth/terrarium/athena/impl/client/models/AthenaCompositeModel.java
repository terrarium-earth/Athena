package earth.terrarium.athena.impl.client.models;

import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class AthenaCompositeModel implements AthenaBlockModel {
    public static final MapCodec<AthenaCompositeModel> CODEC = ExtraCodecs
        .nonEmptyList(AthenaBlockModel.CODEC.codec().listOf())
        .fieldOf("models")
        .xmap(AthenaCompositeModel::new, (model) -> model.models);

    public static final AthenaModelType TYPE = new AthenaModelType(CODEC);

    private final List<AthenaBlockModel> models;

    public AthenaCompositeModel(List<AthenaBlockModel> models) {
        this.models = models;
    }

    @Override
    public AthenaModelType type() {
        return TYPE;
    }

    @Override
    public List<Part> selectParts(RandomSource random, BlockState state) {
        List<Part> parts = new ArrayList<>();

        for (AthenaBlockModel model : this.models) {
            parts.addAll(model.selectParts(random, state));
        }

        return parts;
    }

    @Override
    public List<Part> getAllParts(BlockState state) {
        List<Part> parts = new ArrayList<>();

        for (AthenaBlockModel model : this.models) {
            parts.addAll(model.getAllParts(state));
        }

        return parts;
    }
}
