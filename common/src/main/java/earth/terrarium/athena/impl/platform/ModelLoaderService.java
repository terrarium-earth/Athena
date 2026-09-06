package earth.terrarium.athena.impl.platform;

import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.utils.AthenaUnbakedModelLoader;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@PlatformService
public interface ModelLoaderService {

    AthenaUnbakedModelLoader register(Identifier type, AthenaModelType factory);

    static ModelLoaderService create() {
        throw new AssertionError("Failed to create ModelLoaderService, no implementation found");
    }
}
