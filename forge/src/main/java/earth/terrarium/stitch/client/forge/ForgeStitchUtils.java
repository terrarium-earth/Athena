package earth.terrarium.stitch.client.forge;

import com.mojang.math.Transformation;
import earth.terrarium.stitch.client.models.StitchQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ForgeStitchUtils {

    public static final ModelState LOCKED_STATE = new SimpleModelState(Transformation.identity(), true);
    public static final ModelResourceLocation DEFAULT_MODEL = new ModelResourceLocation(new ResourceLocation("minecraft:missingno"), "");

    public static BakedQuad bakeQuad(StitchQuad quad, Direction direction, TextureAtlasSprite sprite) {
        final BlockElement element = new StitchBlockElement(quad, direction);
        return UnbakedGeometryHelper.bakeElementFace(
                element,
                new StitchBlockElementFace(quad, direction, element),
                sprite,
                direction.getOpposite(),
                ForgeStitchUtils.LOCKED_STATE,
                ForgeStitchUtils.DEFAULT_MODEL
        );
    }
}
