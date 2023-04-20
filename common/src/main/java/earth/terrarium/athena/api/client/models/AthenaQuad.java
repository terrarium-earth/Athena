package earth.terrarium.athena.api.client.models;

import earth.terrarium.athena.api.client.utils.CtmUtils;
import earth.terrarium.athena.impl.client.models.ctm.ConnectedTextureMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public record AthenaQuad(int sprite, float left, float right, float top, float bottom, Rotation rotation, float depth, boolean cull) {

    public AthenaQuad(int sprite, float left, float right, float top, float bottom, Rotation rotation, float depth) {
        this(sprite, left, right, top, bottom, rotation, depth, depth == 0);
    }

    public static AthenaQuad withSprite(int sprite) {
        return withRotation(sprite, Rotation.NONE);
    }

    public static AthenaQuad withRotation(int sprite, Rotation rotation) {
        return new AthenaQuad(sprite, 0, 1, 1, 0, rotation, 0f);
    }

    public static AthenaQuad withState(ConnectedTextureMap map, Direction direction, boolean first, boolean second, boolean firstSecond, float left, float right, float top, float bottom) {
        final int texture = map.getTexture(direction, CtmUtils.getTexture(first, second, firstSecond));
        return new AthenaQuad(texture, left, right, top, bottom, Rotation.NONE, 0f);
    }

    public static AthenaQuad withState(boolean first, boolean second, boolean firstSecond, float left, float right, float top, float bottom) {
        return new AthenaQuad(CtmUtils.getTexture(first, second, firstSecond), left, right, top, bottom, Rotation.NONE, 0f);
    }

    public static AthenaQuad withState(boolean first, boolean second, boolean firstSecond, float left, float right, float top, float bottom, float depth) {
        return new AthenaQuad(CtmUtils.getTexture(first, second, firstSecond), left, right, top, bottom, Rotation.NONE, depth);
    }
}
