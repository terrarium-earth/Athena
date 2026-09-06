package earth.terrarium.athena.impl.client.models;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelAttributes;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.utils.AppearanceAndTintGetter;
import earth.terrarium.athena.api.client.utils.CtmState;
import earth.terrarium.athena.api.client.utils.CtmUtils;
import earth.terrarium.athena.api.client.utils.ModelConnectionCondition;
import earth.terrarium.athena.impl.client.models.ctm.ConnectedTextureMap;
import earth.terrarium.athena.impl.client.models.materials.MaterialStorage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConnectedBlockModel implements AthenaBlockModel {

    public static final MapCodec<ConnectedBlockModel> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
        ConnectedTextureMap.DirectionalCtmProviders.CODEC.fieldOf("ctm_textures").forGetter((model) -> model.textureProviders),
        ModelConnectionCondition.CONNECTS_TO_CODEC.forGetter((model) -> model.connectTo),
        AthenaModelAttributes.TINT_CODEC.forGetter((model) -> model.attributes)
    ).apply(instance, ConnectedBlockModel::new));

    public static final AthenaModelType TYPE = new AthenaModelType(CODEC);

    private final MaterialStorage materials;
    private final ConnectedTextureMap textures;
    private final ConnectedTextureMap.DirectionalCtmProviders textureProviders;
    private final ModelConnectionCondition connectTo;
    private final AthenaModelAttributes attributes;

    public ConnectedBlockModel(ConnectedTextureMap.DirectionalCtmProviders textureProviders, ModelConnectionCondition connectTo, AthenaModelAttributes attributes) {
        this.textureProviders = textureProviders;
        this.connectTo = connectTo;
        this.attributes = attributes;

        this.materials = new MaterialStorage();
        this.textures = textureProviders.resolve(this.materials);
    }

    @Override
    public AthenaModelType type() {
        return TYPE;
    }

    @Override
    public List<AthenaQuad> getQuads(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        if (CtmUtils.checkRelative(level, state, pos, direction)) {
            return List.of();
        }

        return this.textures.getQuads(
                direction,
                CtmState.from(level, state, pos, direction, CtmUtils.check(level, state, pos, direction, this.connectTo)),
                0f
        );
    }

    @Override
    public Map<Direction, List<AthenaQuad>> getDefaultQuads(Direction direction) {
        if (direction == null) return Object2ObjectMaps.emptyMap();
        return Object2ObjectMaps.singleton(direction, this.textures.getQuads(direction, CtmState.ALL_FALSE, 0f));
    }

    @Override
    public Int2ObjectMap<Material.Baked> getTextures(Function<Material, Material.Baked> getter) {
        return this.materials.resolve(getter);
    }

    @Override
    public @Nullable AthenaModelAttributes getAttributes() {
        return this.attributes;
    }
}
