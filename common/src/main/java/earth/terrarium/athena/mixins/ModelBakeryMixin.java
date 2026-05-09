package earth.terrarium.athena.mixins;

import earth.terrarium.athena.impl.loading.AthenaResourceLoader;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;registerModel(Lnet/minecraft/client/resources/model/ModelResourceLocation;Lnet/minecraft/client/resources/model/UnbakedModel;)V", ordinal = 0))
    private void stitch$onStart(
            BlockColors blockColors,
            ProfilerFiller profilerFiller,
            Map<ResourceLocation, BlockModel> map,
            Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> map2,
            CallbackInfo ci
    ) {
        AthenaResourceLoader.setGetter(map2::get);
    }

}