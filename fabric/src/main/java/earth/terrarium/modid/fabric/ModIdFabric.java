package earth.terrarium.modid.fabric;

import earth.terrarium.modid.ModId;
import earth.terrarium.modid.common.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.network.chat.Component;

public class ModIdFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModId.init();
        registerCreativeTabs();
    }

    public static void registerCreativeTabs() {
        ModItems.onRegisterCreativeTabs((loc, item, items) -> FabricItemGroup.builder(loc)
                .title(Component.translatable("itemGroup." + loc.getNamespace() + "." + loc.getPath()))
                .icon(() -> item.get().getDefaultInstance())
                .displayItems((itemDisplayParameters, output) -> items.forEach(output::accept))
                .build());
    }
}
