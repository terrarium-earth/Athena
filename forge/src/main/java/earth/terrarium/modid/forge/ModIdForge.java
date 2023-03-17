package earth.terrarium.modid.forge;

import earth.terrarium.modid.ModId;
import earth.terrarium.modid.client.ModIdClient;
import earth.terrarium.modid.client.forge.ModIdClientForge;
import earth.terrarium.modid.common.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModId.MOD_ID)
public class ModIdForge {
    public ModIdForge() {
        ModId.init();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ModIdForge::commonSetup);
        bus.addListener(ModIdForge::onRegisterCreativeTabs);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ModIdClientForge::init);
        bus.addListener(ModIdForge::onClientSetup);
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        ModId.postInit();
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        ModIdClient.init();
        ModIdClientForge.postInit();
    }

    public static void onRegisterCreativeTabs(CreativeModeTabEvent.Register event) {
        ModItems.onRegisterCreativeTabs((loc, item, items) -> event.registerCreativeModeTab(loc, b -> b
                .title(Component.translatable("itemGroup." + loc.getNamespace() + "." + loc.getPath()))
                .icon(() -> item.get().getDefaultInstance())
                .displayItems((itemDisplayParameters, output) -> items.forEach(output::accept))
                .build()));
    }
}
