package earth.terrarium.modid.datagen;

import earth.terrarium.modid.ModId;
import earth.terrarium.modid.datagen.provider.client.ModLangProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModId.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModIdDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Client
        generator.addProvider(event.includeClient(), new ModLangProvider(generator));

        // Server
    }
}
