package earth.terrarium.athena.fabric.client;

import earth.terrarium.athena.impl.loading.AthenaResourceLoader;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class AthenaModelLoadingPlugin implements ModelLoadingPlugin {

    @Override
    public void initialize(@NonNull Context context) {
        context.modifyBlockModelBeforeBake().register((model, ctx) ->
            Objects.requireNonNullElse(AthenaResourceLoader.tryLoadModel(ctx.state()), model)
        );
    }
}
