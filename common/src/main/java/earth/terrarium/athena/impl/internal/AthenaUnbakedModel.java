package earth.terrarium.athena.impl.internal;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public record AthenaUnbakedModel(
    AthenaBlockModel model,
    Function<Function<Material, Material.Baked>, BlockStateModel> baker
) implements BlockStateModel.UnbakedRoot {

    @Override
    public void resolveDependencies(@NonNull Resolver resolver) {

    }

    @Override
    public @NotNull BlockStateModel bake(@NonNull BlockState state, @NonNull ModelBaker baker) {
        return this.baker.apply(material -> baker.materials().get(material, () -> "Athena: ?"));
    }

    @Override
    public @NotNull Object visualEqualityGroup(@NonNull BlockState blockState) {
        return this;
    }
}
