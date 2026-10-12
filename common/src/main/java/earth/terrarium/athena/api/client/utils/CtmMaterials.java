package earth.terrarium.athena.api.client.utils;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.resources.model.sprite.Material;

public record CtmMaterials(
    Material particle,
    Material center,
    Material vertical,
    Material horizontal,
    Material empty
) {
    public static final MapCodec<CtmMaterials> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
        Material.CODEC.fieldOf("particle").forGetter(CtmMaterials::particle),
        Material.CODEC.fieldOf("center").forGetter(CtmMaterials::center),
        Material.CODEC.fieldOf("vertical").forGetter(CtmMaterials::vertical),
        Material.CODEC.fieldOf("horizontal").forGetter(CtmMaterials::horizontal),
        Material.CODEC.fieldOf("empty").forGetter(CtmMaterials::empty)
    ).apply(instance, CtmMaterials::new));
}