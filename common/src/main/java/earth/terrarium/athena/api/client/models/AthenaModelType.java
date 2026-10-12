package earth.terrarium.athena.api.client.models;

import com.mojang.serialization.MapCodec;

public record AthenaModelType(MapCodec<? extends AthenaBlockModel> codec) {
}
