package earth.terrarium.stitch.api.client.models;

import earth.terrarium.stitch.api.client.utils.CtmUtils;
import net.minecraft.world.level.block.Rotation;

public record StitchQuad(int sprite, float left, float right, float top, float bottom, Rotation rotation, float depth) {

    public static StitchQuad withSprite(int sprite) {
        return withRotation(sprite, Rotation.NONE);
    }

    public static StitchQuad withRotation(int sprite, Rotation rotation) {
        return new StitchQuad(sprite, 0, 1, 1, 0, rotation, 0f);
    }

    public static StitchQuad withState(boolean first, boolean second, boolean firstSecond, float left, float right, float top, float bottom) {
        return new StitchQuad(CtmUtils.getTexture(first, second, firstSecond), left, right, top, bottom, Rotation.NONE, 0f);
    }

    public static StitchQuad withState(boolean first, boolean second, boolean firstSecond, float left, float right, float top, float bottom, float depth) {
        return new StitchQuad(CtmUtils.getTexture(first, second, firstSecond), left, right, top, bottom, Rotation.NONE, depth);
    }
}
