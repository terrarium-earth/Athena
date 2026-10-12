package earth.terrarium.athena.impl.client;

import earth.terrarium.athena.api.client.models.FactoryManager;
import earth.terrarium.athena.api.client.utils.ModelConnectionCondition;
import earth.terrarium.athena.impl.client.models.*;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class DefaultModels {

    public static final String MODID = "athena";

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MODID, name);
    }

    public static void init() {
        FactoryManager.register(id("ctm"), ConnectedBlockModel.TYPE);
        FactoryManager.register(id("carpet_ctm"), ConnectedCarpetBlockModel.TYPE);
        FactoryManager.register(id("pane_ctm"), PaneConnectedBlockModel.TYPE);
        FactoryManager.register(id("giant"), GiantBlockModel.GIANT_TYPE);
        FactoryManager.register(id("mural"), GiantBlockModel.MURAL_TYPE);
        FactoryManager.register(id("pillar"), PillarBlockModel.TYPE);
        FactoryManager.register(id("limited_pillar"), LimitedPillarBlockModel.TYPE);
        FactoryManager.register(id("pane_pillar"), PanePillarBlockModel.TYPE);

        ModelConnectionCondition.CONDITION_TYPES.put("not", ModelConnectionCondition.Not.CODEC);
        ModelConnectionCondition.CONDITION_TYPES.put("and", ModelConnectionCondition.And.CODEC);
        ModelConnectionCondition.CONDITION_TYPES.put("or", ModelConnectionCondition.Or.CODEC);
        ModelConnectionCondition.CONDITION_TYPES.put("xor", ModelConnectionCondition.Xor.CODEC);
        ModelConnectionCondition.CONDITION_TYPES.put("state", ModelConnectionCondition.State.CODEC);
        ModelConnectionCondition.CONDITION_TYPES.put("tag", ModelConnectionCondition.Tag.CODEC);
        ModelConnectionCondition.CONDITION_TYPES.put("sameBlock", ModelConnectionCondition.SameBlock.CODEC);
        ModelConnectionCondition.CONDITION_TYPES.put("sameState", ModelConnectionCondition.SameState.CODEC);
    }
}
