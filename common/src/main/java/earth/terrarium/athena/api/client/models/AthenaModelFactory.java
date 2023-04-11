package earth.terrarium.athena.api.client.models;

import com.google.gson.JsonObject;

import java.util.function.Supplier;

public interface AthenaModelFactory {

    Supplier<AthenaBlockModel> create(JsonObject json);
}
