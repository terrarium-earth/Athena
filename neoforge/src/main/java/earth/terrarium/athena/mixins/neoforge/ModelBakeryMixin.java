package earth.terrarium.athena.mixins.neoforge;

import earth.terrarium.athena.api.client.models.neoforge.FactoryManagerImpl;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Shadow protected abstract void cacheAndQueueDependencies(ResourceLocation resourceLocation, UnbakedModel unbakedModel);

    @Inject(
            method = "Lnet/minecraft/client/resources/model/ModelBakery;loadModel(Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void stitch$loadModel(ResourceLocation location, CallbackInfo ci) {
        if (location instanceof ModelResourceLocation modelId) {
            for (ResourceLocation type : FactoryManagerImpl.getTypes()) {
                UnbakedModel model = FactoryManagerImpl.get(type).loadModel(modelId);
                if (model != null) {
                    cacheAndQueueDependencies(location, model);
                    ci.cancel();
                }
            }
        }
    }
}
