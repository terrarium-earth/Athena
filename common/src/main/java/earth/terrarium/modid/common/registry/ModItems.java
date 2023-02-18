package earth.terrarium.modid.common.registry;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.modid.ModId;
import earth.terrarium.modid.common.util.PlatformUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModItems {
    public static final CreativeModeTab ITEM_GROUP = PlatformUtils.createTab(new ResourceLocation(ModId.MOD_ID, "main"), () -> new ItemStack(Items.GRASS_BLOCK));
    public static final ResourcefulRegistry<Item> ITEMS = ResourcefulRegistries.create(Registry.ITEM, ModId.MOD_ID);

//        public static final RegistryEntry<Item> TEST_ITEM = ITEMS.register("test_item", () -> new Item(new Item.Properties().tab(ITEM_GROUP)));

}
