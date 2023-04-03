package earth.terrarium.stitch.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod("stitch")
public class StitchForge {

    @SuppressWarnings("Convert2MethodRef")
    public StitchForge() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
            earth.terrarium.stitch.impl.client.DefaultModels.init()
        );
    }
}
