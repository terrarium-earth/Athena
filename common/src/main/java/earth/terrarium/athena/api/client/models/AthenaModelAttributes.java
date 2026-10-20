package earth.terrarium.athena.api.client.models;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AthenaModelAttributes {
    public static final MapCodec<AthenaModelAttributes> TINT_CODEC = TintProvider.CODEC
            .optionalFieldOf("tint")
            .xmap(AthenaModelAttributes::new, (attributes) -> attributes.tint);

    public static final AthenaModelAttributes EMPTY = new AthenaModelAttributes(Optional.empty());

    private final Optional<TintProvider> tint;

    public AthenaModelAttributes(Optional<TintProvider> tint) {
        this.tint = tint;
    }

    public @Nullable TintProvider getTint() {
        return this.tint.orElse(null);
    }
}
