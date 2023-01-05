package earth.terrarium.modid.common.registry;

import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.modid.ModId;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

public class ModBlocks {
    public static final ResourcefulRegistry<Block> BLOCKS = ResourcefulRegistries.create(Registry.BLOCK, ModId.MOD_ID);
}
