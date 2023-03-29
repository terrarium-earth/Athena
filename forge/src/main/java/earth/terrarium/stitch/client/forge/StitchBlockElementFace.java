package earth.terrarium.stitch.client.forge;

import earth.terrarium.stitch.client.models.StitchQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;

public class StitchBlockElementFace extends BlockElementFace {

    public StitchBlockElementFace(StitchQuad quad, Direction direction) {
        super(direction, -1, "", new BlockFaceUV(new float[]{quad.right() * 16f, quad.top() * 16f, quad.left() * 16f, quad.bottom() * 16f}, quad.rotation().ordinal() * 90));
    }
}
