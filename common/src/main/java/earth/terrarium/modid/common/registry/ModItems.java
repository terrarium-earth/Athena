package earth.terrarium.modid.common.registry;

import earth.terrarium.botarium.api.registry.RegistryHolder;
import earth.terrarium.modid.ModId;
import earth.terrarium.modid.common.util.ModUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModItems {
    public static final CreativeModeTab ITEM_GROUP = ModUtils.createTab(new ResourceLocation(ModId.MOD_ID, "main"), () -> new ItemStack(Items.GRASS_BLOCK));
    public static final RegistryHolder<Item> ITEMS = new RegistryHolder<>(Registry.ITEM, ModId.MOD_ID);
}
