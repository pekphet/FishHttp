package cc.fish.fishhttp.net.annotation;

import java.lang.reflect.Field;

import cc.fish.fishhttp.net.RequestHelper;


/**
 * Created by fish on 16-4-28.
 */
public class NetInject {

    public static void inject(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object o = field.get(obj);
                if (field.getType().equals(RequestHelper.class)) {
                    if (o == null) {
                        o = new RequestHelper<>();
                    }
                    NetMethod netMethod = field.getAnnotation(NetMethod.class);
                    NetUrl netUrl = field.getAnnotation(NetUrl.class);
                    Result result = field.getAnnotation(Result.class);
                    ((RequestHelper)o).Method(netMethod.value()).Url(netUrl.value()).Result(result.value());
                    field.set(obj, o);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
