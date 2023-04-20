package earth.terrarium.athena.impl.client;

import earth.terrarium.athena.api.client.models.FactoryManager;
import earth.terrarium.athena.impl.client.models.*;
import net.minecraft.resources.ResourceLocation;

public class DefaultModels {

    public static final String MODID = "athena";

    public static void init() {
        FactoryManager.register(new ResourceLocation(MODID, "ctm"), ConnectedBlockModel.FACTORY);
        FactoryManager.register(new ResourceLocation(MODID, "carpet_ctm"), ConnectedCarpetBlockModel.FACTORY);
        FactoryManager.register(new ResourceLocation(MODID, "pane_ctm"), PaneConnectedBlockModel.FACTORY);
        FactoryManager.register(new ResourceLocation(MODID, "giant"), GiantBlockModel.FACTORY);
        FactoryManager.register(new ResourceLocation(MODID, "pillar"), PillarBlockModel.FACTORY);
        FactoryManager.register(new ResourceLocation(MODID, "limited_pillar"), LimitedPillarBlockModel.FACTORY);
        FactoryManager.register(new ResourceLocation(MODID, "pane_pillar"), PanePillarBlockModel.FACTORY);
    }
}
