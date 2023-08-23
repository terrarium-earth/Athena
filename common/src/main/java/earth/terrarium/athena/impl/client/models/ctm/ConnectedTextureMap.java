package earth.terrarium.athena.impl.client.models.ctm;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;

import java.util.Collection;
import java.util.EnumMap;
import java.util.function.Function;

public class ConnectedTextureMap {

    private final EnumMap<Direction, Provider> materials = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, Int2IntMap> textureMap = new EnumMap<>(Direction.class);

    public void put(Direction direction, Int2ObjectMap<Material> map) {
        materials.put(direction, new MapProvider(map));
    }

    public void put(Direction direction, Material material) {
        materials.put(direction, new SimpleProvider(material));
    }

    public int getTexture(Direction direction, int index) {
        return textureMap.get(direction).getOrDefault(index, 0);
    }

    public Int2ObjectMap<TextureAtlasSprite> getTextures(Function<Material, TextureAtlasSprite> getter) {
        Object2ObjectMap<Material, TextureAtlasSprite> materialMap = new Object2ObjectArrayMap<>();
        for (Provider value : materials.values()) {
            value.values().forEach(material -> materialMap.putIfAbsent(material, getter.apply(material)));
        }
        Int2ObjectMap<TextureAtlasSprite> textures = new Int2ObjectArrayMap<>();
        int id = 0;
        for (Direction value : Direction.values()) {
            Provider provider = materials.get(value);
            if (provider == null) {
                textureMap.put(value, new Int2IntArrayMap());
                continue;
            }
            Int2IntMap map = new Int2IntArrayMap();
            for (var entry : provider.map().int2ObjectEntrySet()) {
                int index = id;
                id++;
                textures.put(index, materialMap.get(entry.getValue()));
                map.put(entry.getIntKey(), index);
            }
            textureMap.put(value, map);
        }
        return textures;
    }

    private record SimpleProvider(Material material) implements Provider {

        @Override
        public Int2ObjectMap<Material> map() {
            return Int2ObjectMaps.singleton(0, material);
        }
    }

    private record MapProvider(Int2ObjectMap<Material> map) implements Provider {}

    private interface Provider {

        Int2ObjectMap<Material> map();

        default Collection<Material> values() {
            return map().values();
        }
    }
}
