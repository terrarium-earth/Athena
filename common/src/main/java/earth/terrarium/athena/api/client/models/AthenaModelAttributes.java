package earth.terrarium.athena.api.client.models;

import com.google.gson.JsonObject;
import earth.terrarium.athena.api.client.utils.AthenaUtils;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class AthenaModelAttributes {

    public static final AthenaModelAttributes EMPTY = new AthenaModelAttributes(null, null);

    private final TintProvider tint;
    private final RenderType layer;

    public AthenaModelAttributes(@Nullable TintProvider tint, @Nullable RenderType layer) {
        this.tint = tint;
        this.layer = layer;
    }

    public TintProvider getTint() {
        return this.tint;
    }

    public RenderType getLayer() {
        return this.layer;
    }

    @ApiStatus.Internal
    public static AthenaModelAttributes fromJson(JsonObject json) {
        var tint = TintProvider.fromJson(json);
        var layer = AthenaUtils.renderTypeFromJson(json);
        return new AthenaModelAttributes(tint, layer);
    }
}
