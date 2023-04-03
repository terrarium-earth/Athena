package earth.terrarium.stitch.api.client.forge;

import com.mojang.math.Transformation;
import earth.terrarium.stitch.api.client.models.StitchQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class ForgeStitchUtils {

    public static final ModelState LOCKED_STATE = new SimpleModelState(Transformation.identity(), true);
    public static final ModelResourceLocation DEFAULT_MODEL = new ModelResourceLocation(new ResourceLocation("minecraft:missingno"), "");

    public static List<BakedQuad> bakeQuad(StitchQuad quad, Direction direction, TextureAtlasSprite sprite) {
        final Vector3f start = getStartPos(quad, direction);
        final Vector3f end = getEndPos(quad, direction);
        final BlockElementFace face = new StitchBlockElementFace(quad, direction, start, end);
        final BlockElement element = new BlockElement(start, end, Map.of(direction.getOpposite(), face), null, true);
        return UnbakedGeometryHelper.bakeElements(
                List.of(element),
                mat -> sprite,
                ForgeStitchUtils.LOCKED_STATE,
                ForgeStitchUtils.DEFAULT_MODEL
        );
    }

    private static Vector3f getStartPos(StitchQuad quad, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3f((1 - quad.right()) * 16f, quad.top() * 16f, quad.depth() * 16f);
            case SOUTH -> new Vector3f(quad.left() * 16f, quad.top() * 16f, (1-quad.depth()) * 16f);
            case WEST -> new Vector3f(quad.depth() * 16f, quad.top() * 16f, quad.left() * 16f);
            case EAST -> new Vector3f((1 - quad.depth()) * 16f, quad.top() * 16f,  (1 - quad.right()) * 16f);
            case DOWN -> new Vector3f(quad.left() * 16f, quad.depth() * 16f, quad.top() * 16f);
            case UP -> new Vector3f(quad.left() * 16f, (1 - quad.depth()) * 16f, (1 - quad.bottom()) * 16f);
        };
    }

    private static Vector3f getEndPos(StitchQuad quad, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3f((1 - quad.left()) * 16f, quad.bottom() * 16f, quad.depth() * 16f);
            case SOUTH -> new Vector3f(quad.right() * 16f, quad.bottom() * 16f, (1 - quad.depth()) * 16f);
            case WEST -> new Vector3f(quad.depth() * 16f, quad.bottom() * 16f, quad.right() * 16f);
            case EAST -> new Vector3f((1 - quad.depth()) * 16f, quad.bottom() * 16f, (1 - quad.left()) * 16f);
            case DOWN -> new Vector3f(quad.right() * 16f, quad.depth() * 16f, quad.bottom() * 16f);
            case UP -> new Vector3f(quad.right() * 16f, quad.depth() * 16f, (1 - quad.top()) * 16f);
        };
    }
}
