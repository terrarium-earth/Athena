package earth.terrarium.athena.impl.platform;

import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.neoforge.AthenaBakedModel;
import earth.terrarium.athena.api.client.utils.AthenaUnbakedModelLoader;
import net.minecraft.resources.Identifier;

public class ModelLoaderServiceNeoForgeImpl implements ModelLoaderService {

    @Override
    public AthenaUnbakedModelLoader register(Identifier type, AthenaModelType factory) {
        return new AthenaUnbakedModelLoader(type, factory, AthenaBakedModel::new);
    }
}
