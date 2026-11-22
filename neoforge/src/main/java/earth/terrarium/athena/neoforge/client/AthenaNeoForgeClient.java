package earth.terrarium.athena.neoforge.client;

import earth.terrarium.athena.impl.client.DefaultModels;
import earth.terrarium.athena.impl.client.DirectoryUnstitchSpriteSource;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;

@Mod(value = "athena", dist = Dist.CLIENT)
public class AthenaNeoForgeClient {

    public AthenaNeoForgeClient(IEventBus bus) {
        DefaultModels.init();
        bus.addListener(AthenaNeoForgeClient::onRegisterBlockStateModels);
        bus.addListener(AthenaNeoForgeClient::onRegisterSpriteSources);
    }

    public static void onRegisterBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(Identifier.fromNamespaceAndPath("athena", "athena"), AthenaCustomUnbakedBlockStateModel.CODEC);
    }

    public static void onRegisterSpriteSources(RegisterSpriteSourcesEvent event) {
        event.register(Identifier.fromNamespaceAndPath("athena", "unstitch"), DirectoryUnstitchSpriteSource.MAP_CODEC);
    }
}