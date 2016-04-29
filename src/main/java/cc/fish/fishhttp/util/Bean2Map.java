package cc.fish.fishhttp.util;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by fish on 16-4-27.
 */
public class Bean2Map {
    public static HashMap<String, Object> trans(Object obj) {
        HashMap<String, Object> result = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                result.put(f.getName(), f.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                result.clear();
            }

        }
        return result;
    }


}
