/**
 * Created by Maor Elias on 25/02/16.
 */
public interface IJsonDeserializer<T> {

    T deserialize(String json);
}
