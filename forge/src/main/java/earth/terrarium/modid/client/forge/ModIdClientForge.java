package earth.terrarium.modid.client.forge;

import earth.terrarium.modid.common.config.forge.ForgeMenuConfig;

public class ModIdClientForge {
    public static void init() {
        ForgeMenuConfig.register();
    }

    public static void postInit() {
    }
}
