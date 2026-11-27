package earth.terrarium.athena.impl.internal;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelAttributes;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "26.4")
public class AthenaSimpleUnbakedPartCache {
    private static final Map<AthenaBlockModel, WeakReference<List<AthenaBlockModel.Part>>> cache = new WeakHashMap<>();

    private static List<AthenaBlockModel.Part> uncached(AthenaBlockModel model) {
        return List.of(
            new AthenaBlockModel.Part() {
                @Override
                public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
                    return model.getQuads(level, state, pos, direction);
                }

                @Override
                public Map<Direction, List<AthenaQuad>> getDefaultQuads(@Nullable Direction direction) {
                    return model.getDefaultQuads(direction);
                }

                @Override
                public Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter) {
                    return model.getTextures(getter);
                }

                @Override
                public AthenaModelAttributes getAttributes() {
                    return model.getAttributes();
                }
            }
        );
    }

    public static List<AthenaBlockModel.Part> simpleUnbakedPart(AthenaBlockModel model) {
        List<AthenaBlockModel.Part> parts = cache
                .computeIfAbsent(model, (m) -> new WeakReference<>(uncached(m)))
                .get();

        if (parts == null) {
            parts = uncached(model);
            cache.put(model, new WeakReference<>(parts));
        }

        return parts;
    }
}
