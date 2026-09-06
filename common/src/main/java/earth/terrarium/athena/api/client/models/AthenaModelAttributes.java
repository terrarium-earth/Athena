package earth.terrarium.athena.api.client.models;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

public class AthenaModelAttributes {
    public static final MapCodec<AthenaModelAttributes> TINT_CODEC = TintProvider.CODEC.fieldOf("tint").xmap(AthenaModelAttributes::new, AthenaModelAttributes::getTint);

    public static final AthenaModelAttributes EMPTY = new AthenaModelAttributes(null);

    private final TintProvider tint;

    public AthenaModelAttributes(@Nullable TintProvider tint) {
        this.tint = tint;
    }

    public TintProvider getTint() {
        return this.tint;
    }
}
