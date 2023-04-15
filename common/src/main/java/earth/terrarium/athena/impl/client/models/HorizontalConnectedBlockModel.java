package earth.terrarium.athena.impl.client.models;

import com.google.gson.JsonObject;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.CtmUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

public class HorizontalConnectedBlockModel extends ConnectedBlockModel {

    public static final AthenaModelFactory FACTORY = new Factory();

    private static final List<AthenaQuad> TOP = List.of(AthenaQuad.withSprite(5));
    private static final List<AthenaQuad> BOTTOM = List.of(AthenaQuad.withSprite(6));

    public HorizontalConnectedBlockModel(Int2ObjectMap<Material> materials) {
        super(materials);
    }

    @Override
    public List<AthenaQuad> getQuads(BlockAndTintGetter level, BlockState blockState, BlockPos pos, Direction direction) {
        if (direction.getAxis().isVertical()) {
            return direction == Direction.UP ? TOP : BOTTOM;
        }
        return super.getQuads(level, blockState, pos, direction);
    }

    private static class Factory implements AthenaModelFactory {

        @Override
        public Supplier<AthenaBlockModel> create(JsonObject json) {
            var ctmTextures = GsonHelper.getAsJsonObject(json, "ctm_textures");
            final var materials = CtmUtils.parseCtmMaterials(ctmTextures);
            if (ctmTextures.has("end")) {
                materials.put(5, CtmUtils.blockMat(GsonHelper.getAsString(ctmTextures, "end")));
                materials.put(6, CtmUtils.blockMat(GsonHelper.getAsString(ctmTextures, "end")));
            } else {
                materials.put(5, CtmUtils.blockMat(GsonHelper.getAsString(ctmTextures, "top")));
                materials.put(6, CtmUtils.blockMat(GsonHelper.getAsString(ctmTextures, "bottom")));
            }
            return () -> new HorizontalConnectedBlockModel(materials);
        }
    }
}
