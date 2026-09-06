package earth.terrarium.athena.api.client.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AthenaModelProvider implements DataProvider {
    private final PackOutput output;
    private final Map<Block, AthenaBlockModel> models = new HashMap<>();

    protected AthenaModelProvider(PackOutput output) {
        this.output = output;
    }

    public abstract void gather();

    public void add(Block block, AthenaBlockModel model) {
        models.put(block, model);
    }

    @SuppressWarnings("unchecked")
    private <T extends AthenaBlockModel> CompletableFuture<?> save(CachedOutput cache, T model, Path path) {
        return DataProvider.saveStable(cache, ((Codec<T>) model.type().codec()), model, path);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        gather();

        ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();
        Path outputFolder = output.getOutputFolder(PackOutput.Target.DATA_PACK);

        for (var entry : models.entrySet()) {
            var block = entry.getKey();
            var model = entry.getValue();
            var id = block.builtInRegistryHolder().key().identifier();

            Path path = outputFolder
                .resolve(id.getNamespace())
                .resolve("athena")
                .resolve(id.getPath() + ".json");

            futuresBuilder.add(save(cache, model, path));
        }

        return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Athena Models";
    }
}
