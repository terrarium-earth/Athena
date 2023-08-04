package earth.terrarium.athena.api.client.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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



    public static BiPredicate<BlockState, BlockState> parseCondition(JsonObject json) {
        if (!json.has("connect_to")) {
            return (selfState, otherState) -> selfState == otherState;
        }
        JsonElement connectTo = json.get("connect_to");
        if (connectTo instanceof JsonObject jsonObject) {
            return parseConditionInternal(jsonObject);
        }
        return (selfState, otherState) -> false;
    }
    private static BiPredicate<BlockState, BlockState> parseConditionInternal(JsonObject json) {
        if (!json.has("type"))
            return (selfState, otherState) -> false;
        JsonElement type = json.get("type");
        if (!type.isJsonPrimitive())
            return (selfState, otherState) -> false;
        JsonPrimitive typePrimitive = type.getAsJsonPrimitive();
        if (!typePrimitive.isString())
            return (selfState, otherState) -> false;
        return switch (typePrimitive.getAsString()) {
            case "not" -> parseNotCondition(json);
            case "and" -> parseAndCondition(json);
            case "or" -> parseOrCondition(json);
            case "xor" -> parseXorCondition(json);
            case "state" -> parseStateCondition(json);
            case "tag" -> parseTagCondition(json);
            case "sameBlock" -> parseSelfBlockCondition(json);
            case "sameState" -> parseSelfStateCondition(json);
            default -> (selfState, otherState) -> false;
        };
    }

    private static BiPredicate<BlockState, BlockState> parseAndCondition(JsonObject json) {
        List<JsonObject> conditionsJson = unwrapConditions(json);
        if (conditionsJson == null)
            return (selfState, otherState) -> false;
        List<BiPredicate<BlockState, BlockState>> conditions = conditionsJson.stream().map(CtmUtils::parseConditionInternal).toList();
        if (conditions.isEmpty())
            return (selfState, otherState) -> false;
        if (conditions.size() == 1)
            return conditions.get(0);
        return conditions.stream().reduce((selfState, otherState) -> true, BiPredicate::and);
    }
    private static BiPredicate<BlockState, BlockState> parseOrCondition(JsonObject json) {
        List<JsonObject> conditionsJson = unwrapConditions(json);
        if (conditionsJson == null)
            return (selfState, otherState) -> false;
        List<BiPredicate<BlockState, BlockState>> conditions = conditionsJson.stream().map(CtmUtils::parseConditionInternal).toList();
        if (conditions.isEmpty())
            return (selfState, otherState) -> false;
        if (conditions.size() == 1)
            return conditions.get(0);
        return conditions.stream().reduce((selfState, otherState) -> false, BiPredicate::or);
    }

    private static BiPredicate<BlockState, BlockState> parseXorCondition(JsonObject json) {
        List<JsonObject> conditionsJson = unwrapConditions(json);
        if (conditionsJson == null || conditionsJson.size() != 2)
            return (selfState, otherState) -> false;
        List<BiPredicate<BlockState, BlockState>> conditions = conditionsJson.stream().map(CtmUtils::parseConditionInternal).toList();
        return (selfState, otherState) -> conditions.get(0).test(selfState, otherState) ^ conditions.get(1).test(selfState, otherState);
    }

    @Nullable
    private static List<JsonObject> unwrapConditions(JsonObject json) {
        if (!json.has("conditions"))
            return null;
        JsonElement conditions = json.get("conditions");
        List<JsonObject> conditionList = new ArrayList<>();
        if (conditions instanceof JsonArray array) {
            for (JsonElement jsonElement : array) {
                if (jsonElement instanceof JsonObject jsonObject)
                    conditionList.add(jsonObject);
            }
        }
        return conditionList;
    }
    private static BiPredicate<BlockState, BlockState> parseNotCondition(JsonObject json) {
        JsonElement condition = json.get("condition");
        if (condition instanceof JsonObject jsonObject)
            return parseConditionInternal(jsonObject).negate();
        return (selfState, otherState) -> false;
    }

    private static BiPredicate<BlockState, BlockState> parseStateCondition(JsonObject json) {
        JsonElement jsonBlock = json.get("block");
        if (!(jsonBlock instanceof JsonPrimitive primitive) || !primitive.isString()) {
            return (selfState, otherState) -> false;
        }
        @Nullable
        ResourceLocation blockRL = ResourceLocation.tryParse(jsonBlock.getAsString());
        if (blockRL == null)
            return (selfState, otherState) -> false;
        Block block = BuiltInRegistries.BLOCK.get(blockRL);
        //don't connect to defaulted fallback (air)
        if (!BuiltInRegistries.BLOCK.getKey(block).equals(blockRL))
            return (selfState, otherState) -> false;
        if (!json.has("properties")) {
            return (selfState, otherState) -> otherState.is(block);
        }
        JsonElement propertiesElem = json.get("properties");
        if (!propertiesElem.isJsonObject())
            return (selfState, otherState) -> false;
        Map<Property<?>, Object> properties = new HashMap<>();
        for (var jsonEntry : propertiesElem.getAsJsonObject().asMap().entrySet()) {
            @Nullable
            Property<?> property = block.getStateDefinition().getProperty(jsonEntry.getKey());
            if (property == null)
                continue;
            if (!jsonEntry.getValue().isJsonPrimitive())
                continue;
            JsonPrimitive propertyValueJson = jsonEntry.getValue().getAsJsonPrimitive();
            if (!propertyValueJson.isString())
                continue;
            property.getValue(propertyValueJson.getAsString()).ifPresent(value -> properties.put(property, value));
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
        return (selfState, otherState) -> otherState.is(TagKey.create(Registries.BLOCK, ResourceLocation.tryParse(json.get("tag").getAsString())));
    }

    private static BiPredicate<BlockState, BlockState> parseSelfStateCondition(JsonObject json) {
        return (selfState, otherState) -> selfState == otherState;
    }

    private static BiPredicate<BlockState, BlockState> parseSelfBlockCondition(JsonObject json) {
        return (selfState, otherState) -> selfState.getBlock() == otherState.getBlock();
    }
}
