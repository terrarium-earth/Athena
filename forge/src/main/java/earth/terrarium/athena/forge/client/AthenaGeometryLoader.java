package earth.terrarium.athena.forge.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import earth.terrarium.athena.api.client.forge.AthenaUnbakedModelLoader;
import earth.terrarium.athena.api.client.models.forge.FactoryManagerImpl;
import earth.terrarium.athena.impl.client.DefaultModels;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class AthenaGeometryLoader implements IGeometryLoader<AthenaGeometryLoader.Unbaked> {

    @Override
    public Unbaked read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        String id = GsonHelper.getAsString(json, DefaultModels.MODID + ":loader");
        ResourceLocation loaderId = ResourceLocation.tryParse(id);
        if (loaderId == null) throw new JsonParseException("Invalid loader id: " + id);
        AthenaUnbakedModelLoader loader = FactoryManagerImpl.get(loaderId);
        if (loader == null) throw new JsonParseException("Unknown loader: " + loaderId);
        return new Unbaked(loader, json);
    }

    public record Unbaked(AthenaUnbakedModelLoader loader, JsonObject json) implements IUnbakedGeometry<Unbaked> {

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation id) {
            return loader.loadModel(json).bake(baker, spriteGetter, modelState, id);
        }
    }
}
