package earth.terrarium.stitch.client.models;

import net.minecraft.world.level.block.Rotation;

public record StitchQuad(int sprite, float left, float right, float top, float bottom, Rotation rotation, float depth) {

    public static StitchQuad withSprite(int sprite) {
        return withRotation(sprite, Rotation.NONE);
    }

    public static StitchQuad withRotation(int sprite, Rotation rotation) {
        return new StitchQuad(sprite, 0, 1, 1, 0, rotation, 0f);
    }
}
