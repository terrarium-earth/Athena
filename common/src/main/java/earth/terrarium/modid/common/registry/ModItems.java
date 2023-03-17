package earth.terrarium.modid.common.registry;

import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.modid.ModId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.util.Supplier;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;

public class ModItems {
    public static final ResourcefulRegistry<Item> ITEMS = ResourcefulRegistries.create(BuiltInRegistries.ITEM, ModId.MOD_ID);

//    public static final RegistryEntry<Item> TEST_ITEM = ITEMS.register("test_item", () -> new Item(new Item.Properties()));

    public static void onRegisterCreativeTabs(TriConsumer<ResourceLocation, Supplier<Item>, List<Item>> consumer) {
        consumer.accept(new ResourceLocation(ModId.MOD_ID, "main"), () -> Items.GRASS_BLOCK, BuiltInRegistries.ITEM.stream().filter(i -> BuiltInRegistries.ITEM.getKey(i).getNamespace().equals(ModId.MOD_ID)).toList());
    }
}
