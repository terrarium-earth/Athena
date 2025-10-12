package earth.terrarium.athena.api.client.models;

import com.google.gson.JsonObject;
import earth.terrarium.athena.api.client.utils.AthenaUtils;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class AthenaModelAttributes {

    public static final AthenaModelAttributes EMPTY = new AthenaModelAttributes(null, null);

    private final TintProvider tint;
    private final ChunkSectionLayer layer;

    public AthenaModelAttributes(@Nullable TintProvider tint, @Nullable ChunkSectionLayer layer) {
        this.tint = tint;
        this.layer = layer;
    }

    public TintProvider getTint() {
        return this.tint;
    }

    public ChunkSectionLayer getLayer() {
        return this.layer;
    }

    @ApiStatus.Internal
    public static AthenaModelAttributes fromJson(JsonObject json) {
        var tint = TintProvider.fromJson(json);
        var layer = AthenaUtils.layerFromJson(json);
        return new AthenaModelAttributes(tint, layer);
    }
}
