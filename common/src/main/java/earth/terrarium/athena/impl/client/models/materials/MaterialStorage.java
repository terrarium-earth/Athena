package earth.terrarium.athena.impl.client.models.materials;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class MaterialStorage {

    private final Int2ObjectMap<Material> idToMat = new Int2ObjectArrayMap<>();
    private final Object2IntMap<Material> matToId = new Object2IntArrayMap<>();
    private final AtomicInteger id = new AtomicInteger(0);

    public int put(String material) {
        return this.put(Identifier.parse(material));
    }

    public int put(Identifier material) {
        return this.put(new Material(material));
    }

    public int put(Material material) {
        if (this.matToId.containsKey(material)) {
            return this.matToId.getInt(material);
        }

        int id = this.id.getAndIncrement();
        this.idToMat.put(id, material);
        this.matToId.put(material, id);

        return id;
    }

    public Int2ObjectMap<Material.Baked> resolve(Function<Material, Material.Baked> getter) {
        Int2ObjectMap<Material.Baked> map = new Int2ObjectArrayMap<>();
        for (var entry : this.idToMat.int2ObjectEntrySet()) {
            map.put(entry.getIntKey(), getter.apply(entry.getValue()));
        }
        return map;
    }
}
