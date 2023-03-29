package earth.terrarium.stitch.client.utils;

import com.google.gson.JsonObject;
import dev.architectury.injectables.annotations.ExpectPlatform;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Rotation;

public final class CtmUtils {

    public static int getTexture(boolean first, boolean second, boolean firstSecond) {
        if (first && second) {
            return firstSecond ? 1 : 2;
        }
        return first ? 3 : second ? 4 : 0;
    }

    public static Int2ObjectMap<Material> parseCtmMaterials(JsonObject json) {
        Int2ObjectMap<Material> materials = new Int2ObjectArrayMap<>();
        materials.put(0, blockMat(GsonHelper.getAsString(json, "particle")));
        materials.put(2, blockMat(GsonHelper.getAsString(json, "center")));
        materials.put(3, blockMat(GsonHelper.getAsString(json, "vertical")));
        materials.put(4, blockMat(GsonHelper.getAsString(json, "horizontal")));
        materials.put(1, blockMat(GsonHelper.getAsString(json, "empty")));
        return materials;
    }

    public static Material blockMat(String id) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(id));
    }

    /**
     * This requires a method because forge has a different implementation.
     */
    @ExpectPlatform
    public static Rotation getPillarRotation(Direction.Axis axis, Direction direction) {
        throw new AssertionError();
    }
}
