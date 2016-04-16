package nl.jeroenhd.app.bcbreader.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An object to deserialize the chapter list
 */
public class ChapterListDeserializer implements JsonDeserializer<List<Chapter>> {
    @Override
    public List<Chapter> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ArrayList<Chapter> chapters = new ArrayList<>();

        // Loop over all members in the main object, and deserialize every one of them apart
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            Chapter chapter = context.deserialize(entry.getValue(), Chapter.class);
            chapter.number = Double.parseDouble(
                    entry.getKey()
            );

            for (Page p : chapter.getPageDescriptions()) {
                p.setChapter(chapter.number);
            }
            chapters.add(chapter);
        }

        return chapters;
    }
}
