package earth.terrarium.athena.api.client.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.injectables.annotations.ExpectPlatform;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class CtmUtils {

    public static int getTexture(boolean first, boolean second, boolean firstSecond) {
        if (first && second) {
            return firstSecond ? 1 : 2;
        }
        return first ? 3 : second ? 4 : 0;
    }

    public static Int2ObjectMap<Material> parseCtmMaterials(JsonObject json) {
        Int2ObjectMap<Material> materials = new Int2ObjectArrayMap<>();
        materials.put(0, blockMat(GsonHelper.getAsString(json, "particle")));
        materials.put(2, blockMat(GsonHelper.getAsString(json, "center")));
        materials.put(3, blockMat(GsonHelper.getAsString(json, "vertical")));
        materials.put(4, blockMat(GsonHelper.getAsString(json, "horizontal")));
        materials.put(1, blockMat(GsonHelper.getAsString(json, "empty")));
        return materials;
    }

    public static Material blockMat(String id) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(id));
    }

    public static <I, O> O tryParse(I input, Function<I, O> parser) {
        try {
            return parser.apply(input);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This requires a method because forge has a different implementation.
     */
    @ExpectPlatform
    public static Rotation getPillarRotation(Direction.Axis axis, Direction direction) {
        throw new AssertionError();
    }

    private static final BiPredicate<BlockState, BlockState> FALSE = (selfState, otherState) -> false;
    private static final BiPredicate<BlockState, BlockState> STATE = (selfState, otherState) -> selfState == otherState;
    private static final BiPredicate<BlockState, BlockState> IS = (selfState, otherState) -> selfState.is(otherState.getBlock());

    public static BiPredicate<BlockState, BlockState> parseCondition(JsonObject json) {
        if (!json.has("connect_to")) {
            return STATE;
        }
        if (json.get("connect_to") instanceof JsonObject jsonObject) {
            return parseConditionInternal(jsonObject);
        }
        return FALSE;
    }

    private static BiPredicate<BlockState, BlockState> parseConditionInternal(JsonObject json) {
        return switch (GsonHelper.getAsString(json, "type", "")) {
            case "not" -> parseNotCondition(json);
            case "and" -> parseListCondition(json, BiPredicate::and);
            case "or" -> parseListCondition(json, BiPredicate::or);
            case "xor" -> parseXorCondition(json);
            case "state" -> parseStateCondition(json);
            case "tag" -> parseTagCondition(json);
            case "sameBlock" -> IS;
            case "sameState" -> STATE;
            default -> FALSE;
        };
    }

    private static BiPredicate<BlockState, BlockState> parseListCondition(JsonObject json, BinaryOperator<BiPredicate<BlockState, BlockState>> mapper) {
        List<BiPredicate<BlockState, BlockState>> conditions = unwrapConditions(json).stream()
                .map(CtmUtils::parseConditionInternal).toList();
        if (conditions.isEmpty()) return FALSE;
        if (conditions.size() == 1) return conditions.get(0);
        return conditions.stream().reduce(mapper).orElseThrow();
    }

    private static BiPredicate<BlockState, BlockState> parseXorCondition(JsonObject json) {
        List<JsonObject> conditionsJson = unwrapConditions(json);
        if (conditionsJson.size() != 2) return FALSE;
        BiPredicate<BlockState, BlockState> first = parseConditionInternal(conditionsJson.get(0));
        BiPredicate<BlockState, BlockState> second = parseConditionInternal(conditionsJson.get(1));
        return (selfState, otherState) -> first.test(selfState, otherState) ^ second.test(selfState, otherState);
    }

    private static List<JsonObject> unwrapConditions(JsonObject json) {
        List<JsonObject> conditionList = new ArrayList<>();
        if (json.get("conditions") instanceof JsonArray array) {
            for (JsonElement jsonElement : array) {
                if (jsonElement instanceof JsonObject jsonObject)
                    conditionList.add(jsonObject);
            }
        }
        return conditionList;
    }
    private static BiPredicate<BlockState, BlockState> parseNotCondition(JsonObject json) {
        if (json.get("condition") instanceof JsonObject jsonObject) {
            return parseConditionInternal(jsonObject).negate();
        }
        return (selfState, otherState) -> false;
    }

    private static BiPredicate<BlockState, BlockState> parseStateCondition(JsonObject json) {
        Optional<Block> blockOpt = Optional.ofNullable(GsonHelper.getAsString(json, "block", null))
                .map(ResourceLocation::tryParse)
                .flatMap(BuiltInRegistries.BLOCK::getOptional);
        //don't connect to defaulted fallback (air)
        if (blockOpt.isEmpty()) return FALSE;
        Block block = blockOpt.get();
        if (!json.has("properties")) {
            return (selfState, otherState) -> otherState.is(block);
        }
        JsonElement propertiesElem = json.get("properties");
        if (!propertiesElem.isJsonObject()) return FALSE;
        Map<Property<?>, Object> properties = new HashMap<>();
        for (var jsonEntry : propertiesElem.getAsJsonObject().asMap().entrySet()) {
            @Nullable
            Property<?> property = block.getStateDefinition().getProperty(jsonEntry.getKey());
            if (property == null)
                continue;
            if (!GsonHelper.isStringValue(jsonEntry.getValue()))
                continue;
            property.getValue(jsonEntry.getValue().getAsString()).ifPresent(value -> properties.put(property, value));
        }
        return (selfState, otherState) -> {
            if (!otherState.is(block))
                return false;
            for (var propertyTestValue : properties.entrySet()) {
                if (otherState.getValue(propertyTestValue.getKey()) != propertyTestValue.getValue())
                    return false;
            }
            return true;
        };
    }

    private static BiPredicate<BlockState, BlockState> parseTagCondition(JsonObject json) {
        TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.tryParse(json.get("tag").getAsString()));
        return (selfState, otherState) -> otherState.is(tag);
    }
}
