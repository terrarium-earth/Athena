package earth.terrarium.athena.api.client.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.resources.model.sprite.Material;

public record CtmMaterials(
    Material particle,
    Material center,
    Material vertical,
    Material horizontal,
    Material empty
) {

    private static final Codec<Material> ATHENA_MATERIAL_CODEC = Material.CODEC.validate(
            it -> it.sprite().getPath().startsWith("athena/ctm/") ? DataResult.success(it) : DataResult.error(() -> "Material '%s' must be in the athena/ctm/ namespace".formatted(it.sprite().toString()))
    );
    private static final MapCodec<CtmMaterials> SIMPLE_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Material.CODEC.fieldOf("base").forGetter(CtmMaterials::particle),
            ATHENA_MATERIAL_CODEC.fieldOf("ctm").forGetter(CtmMaterials::center)
    ).apply(instance, CtmMaterials::new));

    private static final MapCodec<CtmMaterials> FULL_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
        Codec.mapEither(Material.CODEC.fieldOf("base"), Material.CODEC.fieldOf("particle"))
                .xmap(Either::unwrap, Either::right)
                .forGetter(CtmMaterials::particle),
        Material.CODEC.fieldOf("center").forGetter(CtmMaterials::center),
        Material.CODEC.fieldOf("vertical").forGetter(CtmMaterials::vertical),
        Material.CODEC.fieldOf("horizontal").forGetter(CtmMaterials::horizontal),
        Material.CODEC.fieldOf("empty").forGetter(CtmMaterials::empty)
    ).apply(instance, CtmMaterials::new));

    public static final MapCodec<CtmMaterials> CODEC = Codec.mapEither(SIMPLE_CODEC, FULL_CODEC).xmap(Either::unwrap, Either::right);

    private CtmMaterials(Material particle, Material ctm) {
        this(particle, ctm.withSuffix("/center"), ctm.withSuffix("/vertical"), ctm.withSuffix("/horizontal"), ctm.withSuffix("/empty"));
    }
}