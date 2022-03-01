package com.wjr.spark.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spark.utils
 * @ClassName: FieldAndValue
 * @create 2022-02-04 16:08
 * @Description:
 */
public abstract class FieldAndValue {
    protected static String[] getFiledName(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    protected static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter, new Class[] {});
            Object value = method.invoke(o, new Object[] {});
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
