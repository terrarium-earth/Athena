package earth.terrarium.athena.api.client.data;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.impl.loading.AthenaResourceLoader;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.PropertyValueList;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;

public class AthenaBlockModelDefinitionGenerator implements BlockModelDefinitionGenerator {
    private final Block block;
    private final List<Entry> entries;

    private AthenaBlockModelDefinitionGenerator(Block block, List<Entry> entries) {
        this.block = block;
        this.entries = entries;
    }

    @Override
    public Block block() {
        return block;
    }

    @Override
    public BlockStateModelDispatcher create() {
        Map<String, BlockStateModel.Unbaked> variants = new HashMap<>();

        for(Entry entry : this.entries) {
            variants.put(entry.properties.getKey(), AthenaResourceLoader.SERVICE.createUnbaked(entry.model()));
        }

        return new BlockStateModelDispatcher(
            Optional.of(new BlockStateModelDispatcher.SimpleModelSelectors(variants)),
            Optional.empty()
        );
    }

    /**
     * Create builder for an Athena model, starting with a variant
     * @param block The block to generate a block state model for
     * @return A variant builder to add properties and specify a model with for the initial variant
     */
    public static VariantBuilder dispatchVariant(Block block) {
        AthenaBlockModelDefinitionGenerator generator = new AthenaBlockModelDefinitionGenerator(block, List.of());

        return generator.new VariantBuilder();
    }

    /**
     * Start adding new variant
     * @return A variant builder to add properties and specify a model with
     */
    public VariantBuilder variant() {
        return new VariantBuilder();
    }

    private record Entry(PropertyValueList properties, AthenaBlockModel model) {
    }

    public class VariantBuilder {
        private final List<Property.Value<?>> values = new ArrayList<>();

        private VariantBuilder() {}

        /**
         * Add new property for this variant dispatch
         * @param property Property
         * @param value Property value
         * @return The builder, to add more properties to this variant or to finalize with a model
         * @param <T> The property type
         */
        public <T extends Comparable<T>> VariantBuilder property(Property<T> property, T value) {
            values.add(new Property.Value<>(property, value));

            return this;
        }

        /**
         * Finalize variant with a model
         * @param model The model to use for this variant, given the properties specified
         * @return The generator, to add more variants or to create the model dispatch.
         */
        public AthenaBlockModelDefinitionGenerator build(AthenaBlockModel model) {
            entries.add(new Entry(new PropertyValueList(values), model));

            return AthenaBlockModelDefinitionGenerator.this;
        }
    }
}
