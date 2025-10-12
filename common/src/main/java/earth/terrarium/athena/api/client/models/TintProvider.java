package earth.terrarium.athena.api.client.models;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public sealed interface TintProvider {

    record Static(int color) implements TintProvider {}
    record Index(int index) implements TintProvider {}

    @ApiStatus.Internal
    static @Nullable TintProvider fromJson(@Nullable JsonObject json) {
        if (json == null) return null;
        var tint = json.get("tint");
        if (tint == null) return null;
        if (tint.isJsonPrimitive() && tint.getAsJsonPrimitive().isNumber()) {
            return new Index(tint.getAsInt());
        } else if (tint.isJsonObject()) {
            var obj = tint.getAsJsonObject();
            int r = obj.has("r") ? obj.get("r").getAsInt() : 0xFF;
            int g = obj.has("g") ? obj.get("g").getAsInt() : 0xFF;
            int b = obj.has("b") ? obj.get("b").getAsInt() : 0xFF;
            int a = obj.has("a") ? obj.get("a").getAsInt() : 0xFF;
            return new Static(a << 24 | r << 16 | g << 8 | b);
        } else if (tint.isJsonArray()) {
            var arr = tint.getAsJsonArray();
            if (arr.size() != 3 && arr.size() != 4) {
                throw new JsonSyntaxException("Tint array must have exactly 3 or 4 elements");
            }
            int r = arr.get(0).getAsInt();
            int g = arr.get(1).getAsInt();
            int b = arr.get(2).getAsInt();
            int a = arr.size() == 4 ? arr.get(3).getAsInt() : 0xFF;
            return new Static(a << 24 | r << 16 | g << 8 | b);
        }
        throw new JsonSyntaxException("Invalid tint value: " + tint);
    }
}
