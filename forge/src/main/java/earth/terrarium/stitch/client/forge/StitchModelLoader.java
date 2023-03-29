package earth.terrarium.stitch.client.forge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import earth.terrarium.stitch.client.models.StitchBlockModel;
import earth.terrarium.stitch.client.models.StitchModelFactory;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;

import java.util.function.Function;
import java.util.function.Supplier;

public class StitchModelLoader implements IGeometryLoader<StitchModelLoader.Geometry> {

    private final StitchModelFactory factory;

    public StitchModelLoader(StitchModelFactory factory) {
        this.factory = factory;
    }

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        final var model = this.factory.create(json);
        final var loaderLessJson = json.deepCopy();
        loaderLessJson.remove("stitch:loader");
        final var fallback = BlockModel.fromString(loaderLessJson.toString());
        return new Geometry(model, fallback);
    }

    public record Geometry(Supplier<StitchBlockModel> model, BlockModel fallback) implements IUnbakedGeometry<Geometry> {

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> getter, ModelState state, ItemOverrides overrides, ResourceLocation id) {
            if ((id instanceof ModelResourceLocation mrl && mrl.getVariant().equals("inventory"))) {
                //noinspection removal, UnstableApiUsage
                return UnbakedGeometryHelper.bakeVanilla(fallback, baker, fallback, getter, state, id);
            }

            return new StitchBakedModel(this.model.get(), getter);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.fallback.resolveParents(modelGetter);
        }
    }
}
