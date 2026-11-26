package earth.terrarium.athena.api.client.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AthenaModelAttributes {
    public static final MapCodec<AthenaModelAttributes> TINT_CODEC = RecordCodecBuilder.<AthenaModelAttributes>mapCodec((instance) ->
        instance.group(
            TintProvider.CODEC.optionalFieldOf("tint").forGetter((attributes) -> attributes.tint),
            Codec.intRange(0, 15).fieldOf("light_emission").forGetter(AthenaModelAttributes::getLightEmission)
        ).apply(instance, AthenaModelAttributes::new)
    );

    public static final AthenaModelAttributes EMPTY = new AthenaModelAttributes(Optional.empty(), 0);

    private final Optional<TintProvider> tint;
    private final int lightEmission;

    public AthenaModelAttributes(Optional<TintProvider> tint, int lightEmission) {
        this.tint = tint;
        this.lightEmission = lightEmission;
    }

    public @Nullable TintProvider getTint() {
        return this.tint.orElse(null);
    }

    public int getLightEmission() {
        return this.lightEmission;
    }
}
