package earth.terrarium.stitch.mixins.forge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import earth.terrarium.stitch.client.forge.StitchModelLoader;
import earth.terrarium.stitch.client.models.forge.FactoryManagerImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ExtendedBlockModelDeserializer.class, remap = false)
public class ExtendedBlockModelDeserializerMixin {


    @Inject(method = "deserializeGeometry", at = @At("HEAD"), cancellable = true)
    private static void stitch$deserializeGeometry(JsonDeserializationContext deserializationContext, JsonObject object, CallbackInfoReturnable<IUnbakedGeometry<?>> cir) {
        if (object.has("stitch:loader")) {
            final ResourceLocation id = ResourceLocation.tryParse(GsonHelper.getAsString(object, "stitch:loader"));
            if (id != null) {
                final StitchModelLoader loader = FactoryManagerImpl.get(id);
                if (loader != null) {
                    cir.setReturnValue(loader.read(object, deserializationContext));
                }
            }
        }
    }

}
