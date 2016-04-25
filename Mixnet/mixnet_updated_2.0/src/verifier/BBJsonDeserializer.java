package verifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;

/**
 * Created by Maor Elias on 25/02/16.
 */
public class BBJsonDeserializer<T> implements IJsonDeserializer<T> {

    private Class<T> type;

    public BBJsonDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public T deserialize(String json) {

        T deserializedObject = null;
        try {
            Gson gson = new GsonBuilder().create();
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.setLenient(true);
            deserializedObject = gson.fromJson(reader, type);
        } catch (Exception e) {
        }

        return deserializedObject;
    }
}