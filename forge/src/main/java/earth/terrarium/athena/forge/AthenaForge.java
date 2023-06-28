package earth.terrarium.athena.forge;

import earth.terrarium.athena.forge.client.AthenaForgeClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod("athena")
public class AthenaForge {

    public AthenaForge() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            earth.terrarium.athena.impl.client.DefaultModels.init();
            AthenaForgeClient.init();
        });
    }
}
