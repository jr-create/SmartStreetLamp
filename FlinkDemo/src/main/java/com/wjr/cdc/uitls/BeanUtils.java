package com.wjr.cdc.uitls;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wjr.cdc.bean.SmartLampSource;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;

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

    private static Object getValue(Object obj, String field) {
        BeanMap beanMap = BeanMap.create(obj);
        return beanMap.get(field);
    }

    private static void setValue(Object obj, String field, Object value) {
        BeanMap beanMap = BeanMap.create(obj);
        beanMap.put(field, value);
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

    public static SmartLampSource createObject(HashMap<String, Object> map) throws ClassNotFoundException {
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

    public static SmartLampSource jsonToSmart(String json) throws ClassNotFoundException {
        return createObject(jsonToList(new HashMap<>(), json, ""));
    }

    public static String json = "{\"reason\":\"success\",\"device_id\":\"my_00193\",\"type_id\":7,\"timestamp\":1654685474,\"error_code\":0,\"road_id\":14,\"longitude\":\"117.7820083778303\",\"latitude\":\"37.7242552469552\",\"values\":{\"voltage\":\"3\",\"temperature\":\"23\",\"humidity\":\"75\",\"lighting\":\"75\",\"PM2_5\":\"75\",\"CO_2\":\"75\",\"info\":\"yin阴\",\"direct\":\"xibeifeng西北风\",\"power\":\"7级\",\"key\":{\"kes\":\"v\",\"kes\":\"v\"}},\"test\":[\"a1\",2,{\"k1\":\"v\",\"k2\":\"v\"}]}\n";


    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println("Analysis JSONStr");
        HashMap<String, Object> list = jsonToList(new HashMap<>(), json, "");
        SmartLampSource object = createObject(list);
        System.out.println(object.getFields().get("test").getClass().getTypeName());
        System.out.println(object);
    }

}
