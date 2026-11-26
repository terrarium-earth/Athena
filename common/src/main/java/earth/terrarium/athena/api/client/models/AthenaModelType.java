package earth.terrarium.athena.api.client.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

public record AthenaModelType(MapCodec<? extends AthenaBlockModel> codec, boolean disallowedInBlockStates) {
    public static final Codec<AthenaModelType> CODEC = Identifier.CODEC
        .xmap(
            FactoryManager::getType,
            FactoryManager::getTypeId
        );

    public AthenaModelType(MapCodec<? extends AthenaBlockModel> codec) {
        this(codec, false);
    }
}
