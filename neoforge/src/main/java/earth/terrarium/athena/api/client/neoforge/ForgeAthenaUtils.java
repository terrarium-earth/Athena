package earth.terrarium.athena.api.client.neoforge;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Quadrant;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.models.TintProvider;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.GeometryUtils;
import org.joml.Vector3f;

import java.util.Objects;

@ApiStatus.Internal
public class ForgeAthenaUtils {

    public static BakedQuad bakeQuad(AthenaQuad quad, Direction direction, Material.Baked material, TintProvider tint) {
        final var sprite = material.sprite();
        final var start = getStartPos(quad, direction);
        final var end = getEndPos(quad, direction);
        final var faceInfo = FaceInfo.fromFacing(direction.getOpposite());

        final var uvs = getUVs(start, end, direction);
        final var rotation = switch (quad.rotation()) {
            case NONE -> Quadrant.R0;
            case CLOCKWISE_90 -> Quadrant.R90;
            case CLOCKWISE_180 -> Quadrant.R180;
            case COUNTERCLOCKWISE_90 -> Quadrant.R270;
        };

        final var pos1 = faceInfo.getVertexInfo(0).select(start, end).div(16f);
        final var pos2 = faceInfo.getVertexInfo(1).select(start, end).div(16f);
        final var pos3 = faceInfo.getVertexInfo(2).select(start, end).div(16f);
        final var pos4 = faceInfo.getVertexInfo(3).select(start, end).div(16f);
        final var normalDirection = findClosestDirection(pos1, pos2, pos3);

        final var uv1 = packUV(uvs, rotation, sprite, 0);
        final var uv2 = packUV(uvs, rotation, sprite, 1);
        final var uv3 = packUV(uvs, rotation, sprite, 2);
        final var uv4 = packUV(uvs, rotation, sprite, 3);

        final var transparency = material.forceTranslucent() ? Transparency.TRANSLUCENT : computeTransparency(sprite, uvs);
        final var materialInfo = BakedQuad.MaterialInfo.of(
                material,
                transparency,
                tint instanceof TintProvider.Index(var index) ? index : -1,
                null,
                0,
                true
        );
        final var colors = tint instanceof TintProvider.Static(var color) ? new BakedColors.PerQuad(color) : BakedColors.DEFAULT;

        return new BakedQuad(
                pos1, pos2, pos3, pos4,
                uv1, uv2, uv3, uv4,
                Objects.requireNonNullElse(normalDirection, Direction.UP),
                materialInfo,
                BakedNormals.UNSPECIFIED,
                colors
        );
    }

    private static Transparency computeTransparency(TextureAtlasSprite sprite, CuboidFace.UVs uvs) {
        return sprite.contents().computeTransparency(
                Math.min(uvs.minU(), uvs.maxU()) / 16.0F,
                Math.min(uvs.minV(), uvs.maxV()) / 16.0F,
                Math.max(uvs.minU(), uvs.maxU()) / 16.0F,
                Math.max(uvs.minV(), uvs.maxV()) / 16.0F
        );
    }

    private static long packUV(CuboidFace.UVs uvs, Quadrant rotation, TextureAtlasSprite sprite, int index) {
        var u = CuboidFace.getU(uvs, rotation, index);
        var v = CuboidFace.getV(uvs, rotation, index);
        return UVPair.pack(sprite.getU(u), sprite.getV(v));
    }

    private static @Nullable Direction findClosestDirection(Vector3f pos1, Vector3f pos2, Vector3f pos3) {
        Vector3f normal = new Vector3f();
        GeometryUtils.normal(pos1, pos2, pos3, normal);
        if (!normal.isFinite()) {
            return null;
        } else {
            Direction best = null;
            float closestProduct = 0.0F;

            for(Direction candidate : Direction.values()) {
                float product = normal.dot(candidate.getUnitVec3f());
                if (product >= 0.0F && product > closestProduct) {
                    closestProduct = product;
                    best = candidate;
                }
            }

            return best;
        }
    }

    private static CuboidFace.UVs getUVs(Vector3f from, Vector3f to, Direction direction) {
        float[] uvs = switch (direction) {
            case UP -> new float[]{from.x(), to.z(), to.x(), from.z()};
            case DOWN -> new float[]{from.x(), 16 - from.z(), to.x(), 16 - to.z()};
            case NORTH -> new float[]{16 - from.x, 16 - to.y, 16 - to.x, 16 - from.y};
            case SOUTH -> new float[]{to.x, 16 - to.y, from.x, 16 - from.y};
            case WEST -> new float[]{to.z(), 16.0F - to.y(), from.z(), 16.0F - from.y()};
            case EAST -> new float[]{16.0F - from.z, 16.0F - to.y(), 16.0F - to.z, 16.0F - from.y()};
        };
        return new CuboidFace.UVs(uvs[0], uvs[1], uvs[2], uvs[3]);
    }

    private static Vector3f getStartPos(AthenaQuad quad, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3f((1 - quad.left()) * 16f, quad.bottom() * 16f, quad.depth() * 16f);
            case SOUTH -> new Vector3f(quad.right() * 16f, quad.bottom() * 16f, (1-quad.depth()) * 16f);
            case WEST -> new Vector3f(quad.depth() * 16f, quad.bottom() * 16f, quad.right() * 16f);
            case EAST -> new Vector3f((1 - quad.depth()) * 16f, quad.bottom() * 16f,  (1 - quad.left()) * 16f);
            case DOWN -> new Vector3f(quad.left() * 16f, quad.depth() * 16f, quad.top() * 16f);
            case UP -> new Vector3f(quad.left() * 16f, (1 - quad.depth()) * 16f, (1 - quad.bottom()) * 16f);
        };
    }

    private static Vector3f getEndPos(AthenaQuad quad, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3f((1 - quad.right()) * 16f, quad.top() * 16f, quad.depth() * 16f);
            case SOUTH -> new Vector3f(quad.left() * 16f, quad.top() * 16f, (1 - quad.depth()) * 16f);
            case WEST -> new Vector3f(quad.depth() * 16f, quad.top() * 16f, quad.left() * 16f);
            case EAST -> new Vector3f((1 - quad.depth()) * 16f, quad.top() * 16f, (1 - quad.right()) * 16f);
            case DOWN -> new Vector3f(quad.right() * 16f, quad.depth() * 16f, quad.bottom() * 16f);
            case UP -> new Vector3f(quad.right() * 16f, quad.depth() * 16f, (1 - quad.top()) * 16f);
        };
    }
}
