package earth.terrarium.stitch.mixins.fabric;

import com.google.gson.JsonObject;
import earth.terrarium.stitch.api.client.models.fabric.BlockModelHook;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockModel.class)
public class BlockModelMixin implements BlockModelHook {

    @Unique
    private ResourceLocation chipped$modelType;
    @Unique
    private JsonObject chipped$modelData;

    @Override
    public void stitch$setModelData(ResourceLocation modelType, JsonObject data) {
        this.chipped$modelType = modelType;
        this.chipped$modelData = data;
    }

    @Override
    public ResourceLocation stitch$getModelType() {
        return this.chipped$modelType;
    }

    @Override
    public JsonObject stitch$getModelData() {
        return this.chipped$modelData;
    }
}
