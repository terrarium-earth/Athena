package earth.terrarium.athena.api.client.models;

import earth.terrarium.athena.api.client.utils.CtmUtils;
import net.minecraft.world.level.block.Rotation;

public record AthenaQuad(int sprite, float left, float right, float top, float bottom, Rotation rotation, float depth) {

    public static AthenaQuad withSprite(int sprite) {
        return withRotation(sprite, Rotation.NONE);
    }

    public static AthenaQuad withRotation(int sprite, Rotation rotation) {
        return new AthenaQuad(sprite, 0, 1, 1, 0, rotation, 0f);
    }

    public static AthenaQuad withState(boolean first, boolean second, boolean firstSecond, float left, float right, float top, float bottom) {
        return new AthenaQuad(CtmUtils.getTexture(first, second, firstSecond), left, right, top, bottom, Rotation.NONE, 0f);
    }

    public static AthenaQuad withState(boolean first, boolean second, boolean firstSecond, float left, float right, float top, float bottom, float depth) {
        return new AthenaQuad(CtmUtils.getTexture(first, second, firstSecond), left, right, top, bottom, Rotation.NONE, depth);
    }
}
