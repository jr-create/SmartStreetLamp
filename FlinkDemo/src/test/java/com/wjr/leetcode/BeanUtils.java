package com.wjr.leetcode;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wjr.cdc.bean.SmartLampSource;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.uitls
 * @ClassName: BeanUtils
 * @create 2022-06-06 20:10
 * @Description:
 */
public class BeanUtils {
    private static Object generateObject(Map properties) {
        BeanGenerator generator = new BeanGenerator();
        Set keySet = properties.keySet();
        for (Iterator i = keySet.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            generator.addProperty(key, (Class) properties.get(key));
        }
        return generator.create();
    }

    private static Object getValue(Object obj, String property) {
        BeanMap beanMap = BeanMap.create(obj);
        return beanMap.get(property);
    }

    private static void setValue(Object obj, String property, Object value) {
        BeanMap beanMap = BeanMap.create(obj);
        beanMap.put(property, value);
    }

    public static String json = "{\"reason\":\"success\",\"device_id\":\"my_00193\",\"type_id\":7,\"timestamp\":1654685474,\"error_code\":0,\"road_id\":14,\"longitude\":\"117.7820083778303\",\"latitude\":\"37.7242552469552\",\"values\":{\"voltage\":\"3\",\"temperature\":\"23\",\"humidity\":\"75\",\"lighting\":\"75\",\"PM2_5\":\"75\",\"CO_2\":\"75\",\"info\":\"yin阴\",\"direct\":\"xibeifeng西北风\",\"power\":\"7级\",\"key\":{\"kes\":\"v\",\"kes\":\"v\"}},\"test\":[\"a1\",2,{\"k1\":\"v\",\"k2\":\"v\"}]}\n";


    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println("Generate JavaBean");
        Map properties = new HashMap();
        properties.put("id", Class.forName("java.lang.Integer"));
        properties.put("name", Class.forName("java.lang.String"));
        properties.put("address", Class.forName("java.lang.String"));
        properties.put("array", Class.forName("java.util.ArrayList"));
        Object stu = generateObject(properties);

        System.out.println("Set values");
        setValue(stu, "id", 123);
        setValue(stu, "name", "454");
        setValue(stu, "address", "789");
        ArrayList<Object> list1 = new ArrayList<>();
        list1.add("123");
        list1.add(32);
        setValue(stu, "array", list1);

        System.out.println("Get values");
        System.out.println(">> " + getValue(stu, "id"));
        System.out.println(">> " + getValue(stu, "name"));
        System.out.println(">> " + getValue(stu, "address"));
        System.out.println(">> " + getValue(stu, "array"));

        System.out.println("Show all methods");
        Method[] methods = stu.getClass().getDeclaredMethods();
        for (Method method : methods) {
            System.out.println(">> " + method.getName());
        }

        System.out.println("Show all properties");
        Field[] fields = stu.getClass().getDeclaredFields();
        for (Field field : fields) {
            System.out.println(">> " + field.getName());
        }

        System.out.println("Analysis JSONStr");
        HashMap<String, Object> list = jsonToList(new HashMap<>(), json, "");
        System.out.println(createObject(list));
    }

    public static HashMap<String, Object> jsonToList(HashMap<String, Object> result, String json, String prefix) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        for (Map.Entry<String, Object> objectEntry : jsonObject.entrySet()) {
            String key = prefix + objectEntry.getKey();
            Object value = objectEntry.getValue();
            String typeName = value.getClass().getTypeName();
            // 解析JSONArray
            if (typeName.endsWith("JSONArray")) {
                // 缓存JSONArray的普通类型
                ArrayList<Object> array = new ArrayList<>();
                Object[] objects = ((JSONArray) value).toArray();
                for (Object object : objects) {
                    String typeName1 = object.getClass().getTypeName();
                    if (typeName1.endsWith("JSONObject")) {//解析JSONArray的JSONObject,并在其中去除
                        jsonToList(result, object.toString(), key + '_');
                    } else {
                        array.add(object);
                    }
                }
                result.put(key, array);
            } else if (typeName.endsWith("JSONObject")) {//解析JSONObject
                jsonToList(result, value.toString(), key + '_');
            } else {// 添加普通类型
                result.put(key, value);
            }
        }
        return result;
    }

    public static Object createObject(HashMap<String, Object> map) throws ClassNotFoundException {
        Map properties = new HashMap();
        Map typeMap = new HashMap();
        // 设置类型
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            properties.put(entry.getKey(), Class.forName(entry.getValue().getClass().getTypeName()));
            typeMap.put(entry.getKey(), entry.getValue().getClass().getTypeName());
        }
        // 创建对象
        Object object = generateObject(properties);
        // 插入数据
        Map fields = new HashMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            fields.put(entry.getKey(), entry.getValue());
            setValue(object, entry.getKey(), entry.getValue());
        }

        SmartLampSource smartLampSource = new SmartLampSource(object, fields, typeMap);
        return smartLampSource;
    }
}
