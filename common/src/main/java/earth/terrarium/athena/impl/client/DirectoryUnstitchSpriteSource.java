package earth.terrarium.athena.impl.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

@NullMarked
public record DirectoryUnstitchSpriteSource(
        String sourcePath,
        String prefix,
        Optional<Vec2> expectedSize,
        List<Region> regions
) implements SpriteSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<DirectoryUnstitchSpriteSource> MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            Codec.STRING.fieldOf("source").forGetter(DirectoryUnstitchSpriteSource::sourcePath),
            Codec.STRING.fieldOf("prefix").forGetter(DirectoryUnstitchSpriteSource::prefix),
            Vec2.CODEC.optionalFieldOf("expected_size").forGetter(DirectoryUnstitchSpriteSource::expectedSize),
            ExtraCodecs.nonEmptyList(Region.CODEC.listOf()).fieldOf("regions").forGetter(DirectoryUnstitchSpriteSource::regions)
    ).apply(i, DirectoryUnstitchSpriteSource::new));

    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        var converter = new FileToIdConverter("textures/" + this.sourcePath, ".png");
        converter.listMatchingResources(resourceManager).forEach((identifier, resource) -> {
            var image = new LazyLoadedImage(identifier, resource, this.regions.size() + 1);

            try {
                var nativeImage = image.get();
                var widthRatio = (double) nativeImage.getWidth() / nativeImage.getHeight();
                var heightRatio = (double) nativeImage.getHeight() / nativeImage.getWidth();
                var expectedWidthRatio = this.expectedSize.map(it -> it.x / it.y);
                var expectedHeightRatio = this.expectedSize.map(it -> it.y / it.x);

                if (expectedWidthRatio.isPresent() && Math.abs(widthRatio - expectedWidthRatio.get()) > 0.01) {
                    DirectoryUnstitchSpriteSource.LOGGER.warn("Expected width ratio of {} for {}, but got {}", expectedWidthRatio.get(), identifier, nativeImage.getWidth());
                }

                if (expectedHeightRatio.isPresent() && Math.abs(heightRatio - expectedHeightRatio.get()) > 0.01) {
                    DirectoryUnstitchSpriteSource.LOGGER.warn("Expected height ratio of {} for {}, but got {}", expectedHeightRatio.get(), identifier, nativeImage.getHeight());
                }
            } catch (Exception e) {
                DirectoryUnstitchSpriteSource.LOGGER.error("Failed to load image {}", identifier, e);
            } finally {
                image.release();
            }

            for(Region region : this.regions) {
                var id = converter.fileToId(identifier).withSuffix(region.suffix).withPrefix(this.prefix);
                output.add(id, new RegionInstance(image, id, region));
            }
        });
    }

    @Override
    public MapCodec<DirectoryUnstitchSpriteSource> codec() {
        return MAP_CODEC;
    }

    public record Region(String suffix, double x, double y, double width, double height) {
        public static final Codec<Region> CODEC = RecordCodecBuilder.create((i) -> i.group(
                Codec.STRING.fieldOf("suffix").forGetter(Region::suffix),
                Codec.DOUBLE.fieldOf("x").forGetter(Region::x),
                Codec.DOUBLE.fieldOf("y").forGetter(Region::y),
                Codec.DOUBLE.fieldOf("width").forGetter(Region::width),
                Codec.DOUBLE.fieldOf("height").forGetter(Region::height)
        ).apply(i, Region::new));
    }

    public record RegionInstance(LazyLoadedImage image, Identifier identifier, Region region) implements SpriteSource.DiscardableLoader {

        @Override
        public SpriteContents get(SpriteResourceLoader loader) {
            try {
                NativeImage image = this.image.get();
                int x = Mth.floor(this.region.x * image.getWidth());
                int y = Mth.floor(this.region.y * image.getHeight());
                int width = Mth.floor(this.region.width * image.getWidth());
                int height = Mth.floor(this.region.height * image.getHeight());

                NativeImage output = new NativeImage(NativeImage.Format.RGBA, width, height, false);
                image.copyRect(output, x, y, 0, 0, width, height, false, false);

                return new SpriteContents(this.identifier, new FrameSize(width, height), output);
            } catch (Exception e) {
                DirectoryUnstitchSpriteSource.LOGGER.error("Failed to unstitch region {}", this.identifier, e);
            } finally {
                this.image.release();
            }

            return MissingTextureAtlasSprite.create();
        }

        @Override
        public void discard() {
            this.image.release();
        }
    }
}