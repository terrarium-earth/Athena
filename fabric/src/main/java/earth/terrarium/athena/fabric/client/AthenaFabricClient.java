package earth.terrarium.athena.fabric.client;

import earth.terrarium.athena.impl.client.DirectoryUnstitchSpriteSource;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import net.minecraft.resources.Identifier;

public class AthenaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new AthenaModelLoadingPlugin());
        CustomUnbakedBlockStateModel.register(
                Identifier.fromNamespaceAndPath("athena", "athena"),
                AthenaCustomUnbakedBlockStateModel.CODEC
        );
        SpriteSourceRegistry.register(
                Identifier.fromNamespaceAndPath("athena", "unstitch"),
                DirectoryUnstitchSpriteSource.MAP_CODEC
        );
    }
}
