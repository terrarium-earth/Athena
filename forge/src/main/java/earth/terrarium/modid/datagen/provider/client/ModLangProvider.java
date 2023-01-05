package earth.terrarium.modid.datagen.provider.client;

import earth.terrarium.modid.ModId;
import earth.terrarium.modid.common.registry.ModBlocks;
import earth.terrarium.modid.common.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.common.data.LanguageProvider;
import org.codehaus.plexus.util.StringUtils;

import java.util.Objects;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(DataGenerator pGenerator) {
        super(pGenerator, ModId.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.modid.main", "ModId");

        ModBlocks.BLOCKS.stream().forEach(entry -> addBlock(entry, StringUtils.capitaliseAllWords(entry.getId().getPath().replace("_", " "))));
        ModItems.ITEMS.stream().filter(e -> !(e instanceof BlockItem)).forEach(entry -> addItem(entry, StringUtils.capitaliseAllWords(Objects.requireNonNull(entry.getId()).getPath().replace("_", " "))));
    }
}
