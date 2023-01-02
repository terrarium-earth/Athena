package earth.terrarium.modid.datagen.provider.client;

import earth.terrarium.modid.ModId;
import earth.terrarium.modid.common.registry.ModBlocks;
import earth.terrarium.modid.common.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.codehaus.plexus.util.StringUtils;

import java.util.function.Supplier;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(DataGenerator pGenerator) {
        super(pGenerator, ModId.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.modid.main", "ModId");

        for (Supplier<Block> block : ModBlocks.BLOCKS.getRegistries()) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block.get());
            addBlock(block, StringUtils.capitaliseAllWords(id.getPath().replace("_", " ")));
        }

        ModItems.ITEMS.getRegistries().forEach(reg -> {
            if (!(reg.get() instanceof BlockItem)) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(reg.get());
                addItem(reg, StringUtils.capitaliseAllWords(id.getPath().replace("_", " ")));
            }
        });
    }
}
