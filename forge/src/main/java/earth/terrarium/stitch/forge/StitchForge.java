package earth.terrarium.stitch.forge;

import earth.terrarium.stitch.Stitch;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Stitch.MOD_ID)
public class StitchForge {
    public StitchForge() {
        Stitch.init();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(StitchForge::commonSetup);
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        Stitch.postInit();
    }
}
