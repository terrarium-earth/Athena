package earth.terrarium.athena.api.client.utils;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.sprite.Material;

import java.util.function.Function;

public record PillarMaterials(
    Material particle,
    Material self,
    Material top,
    Material center,
    Material bottom
) {
    public static final MapCodec<PillarMaterials> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
        Material.CODEC.fieldOf("particle").forGetter(PillarMaterials::particle),
        Material.CODEC.fieldOf("self").forGetter(PillarMaterials::self),
        Material.CODEC.fieldOf("top").forGetter(PillarMaterials::top),
        Material.CODEC.fieldOf("center").forGetter(PillarMaterials::center),
        Material.CODEC.fieldOf("bottom").forGetter(PillarMaterials::bottom)
    ).apply(instance, PillarMaterials::new));

    public void addMaterials(Int2ObjectMap<Material.Baked> builder, Function<Material, Material.Baked> getter) {
        builder.put(0, getter.apply(particle()));
        builder.put(4, getter.apply(self()));

        builder.put(1, getter.apply(top()));
        builder.put(2, getter.apply(center()));
        builder.put(3, getter.apply(bottom()));
    }

    public Int2ObjectMap<Material.Baked> baked(Function<Material, Material.Baked> getter) {
        Int2ObjectMap<Material.Baked> textures = new Int2ObjectArrayMap<>();

        addMaterials(textures, getter);

        return textures;
    }
}
