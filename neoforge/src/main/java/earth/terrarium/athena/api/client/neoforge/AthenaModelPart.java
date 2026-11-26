package earth.terrarium.athena.api.client.neoforge;

import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.models.TintProvider;
import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public record AthenaModelPart(
        @NotNull BlockStateModelPart parent,
        @NotNull NullableEnumMap<Direction, Map<Direction, List<AthenaQuad>>> quads,
        @NotNull Int2ObjectMap<Material.Baked> materials,
        @Nullable TintProvider tint,
        int lightEmission
) implements BlockStateModelPart {

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable Direction direction) {
        List<BakedQuad> quads = new ArrayList<>();
        this.quads.getOrDefault(direction, Map.of()).forEach((dir, quadList) -> {
            for (var quad : quadList) {
                var material = materials.get(quad.sprite());
                if (material == null) continue;
                quads.add(ForgeAthenaUtils.bakeQuad(quad, dir, material, this.tint, this.lightEmission));
            }
        });
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public Material.@NotNull Baked particleMaterial() {
        return this.parent.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.parent.materialFlags();
    }
}
