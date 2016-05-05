package nl.jeroenhd.app.bcbreader.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * An object to serialize the telemetry
 */
class TelemetrySerializer implements JsonSerializer<Telemetry> {
    @Override
    public JsonElement serialize(Telemetry src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("AndroidVersion", new JsonPrimitive(src.getAndroidVersion()));
        result.add("Model", new JsonPrimitive(src.getModel()));
        result.add("RAM", new JsonPrimitive(src.getRAMSize()));
        result.add("InternalFree", new JsonPrimitive(src.getInternalFree()));
        result.add("InternalSize", new JsonPrimitive(src.getInternalSize()));
        result.add("SDCardFree", new JsonPrimitive(src.getSDCardFree()));
        result.add("SDCardSize", new JsonPrimitive(src.getSDCardSize()));
        result.add("SDCardEmulated", new JsonPrimitive(src.isSDCardEmulated()));
        result.add("UniqueID", new JsonPrimitive(src.getUniqueID()));

        return result;
    }
}
