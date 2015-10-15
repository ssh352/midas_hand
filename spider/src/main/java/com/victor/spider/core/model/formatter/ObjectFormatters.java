package com.victor.spider.core.model.formatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ObjectFormatters {

    private static Map<Class, Class<? extends ObjectFormatter>> formatterMap = new ConcurrentHashMap<Class, Class<? extends ObjectFormatter>>();

    static {
        for (Class<? extends ObjectFormatter> basicTypeFormatter : BasicTypeFormatter.basicTypeFormatters) {
            put(basicTypeFormatter);
        }
        put(DateFormatter.class);
    }

    public static void put(Class<? extends ObjectFormatter> objectFormatter) {
        try {
            formatterMap.put(objectFormatter.newInstance().clazz(), objectFormatter);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Class<? extends ObjectFormatter> get(Class<?> clazz){
        return formatterMap.get(clazz);
    }
}
