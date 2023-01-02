package earth.terrarium.modid.common.registry;

import earth.terrarium.botarium.api.registry.RegistryHolder;
import earth.terrarium.modid.ModId;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

public class ModBlocks {
    public static final RegistryHolder<Block> BLOCKS = new RegistryHolder<>(Registry.BLOCK, ModId.MOD_ID);
}
