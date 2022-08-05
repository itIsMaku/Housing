package cz.maku.housing;

import com.google.common.reflect.TypeToken;
import cz.maku.mommons.Mommons;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AddonData extends HashMap<String, String> {

    public String serialize() {
        return Mommons.GSON.toJson(this);
    }

    public static AddonData deserialize(String data) {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return Mommons.GSON.fromJson(data, type);
    }

}
