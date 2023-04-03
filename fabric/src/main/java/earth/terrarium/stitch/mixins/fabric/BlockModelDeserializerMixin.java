package earth.terrarium.stitch.mixins.fabric;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import earth.terrarium.stitch.api.client.models.fabric.BlockModelHook;
import earth.terrarium.stitch.api.client.models.fabric.FactoryManagerImpl;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(BlockModel.Deserializer.class)
public class BlockModelDeserializerMixin {

    @Inject(
        method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;",
        at = @At("RETURN")
    )
    private void stitch$onLoadModel(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<BlockModel> cir) {
        BlockModel model = cir.getReturnValue();
        if (model instanceof BlockModelHook hook && json instanceof JsonObject object && object.has("stitch:loader")) {
            JsonElement element = object.get("stitch:loader");
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                ResourceLocation id = new ResourceLocation(GsonHelper.convertToString(element, "stitch:loader"));
                if (FactoryManagerImpl.hasType(id)) {
                    hook.stitch$setModelData(id, object);
                }
            }
        }
    }
}