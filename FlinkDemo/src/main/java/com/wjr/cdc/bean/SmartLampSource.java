package com.wjr.cdc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.bean
 * @ClassName: SmartLampSource
 * @create 2022-06-08 18:46
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmartLampSource {
    private Object object;
    // Map<field,value>
    private Map<String, Object> fields;
    // Map<field,type>
    private Map<String, String> types;
    @Override
    public String toString() {
        return "SmartLampSource{" + object +
                ", fields=" + fields +
                ", types=" + types +
                '}';
    }

    public<T> T getValue(String field){
        T o = (T) fields.get(field);
        return o;
    }

    public String getType(String field) {
        String typeName = fields.get(field).getClass().getTypeName();
        return typeName;
    }
    public List<Method> getMethods(){
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            System.out.println(">> " + method.getName());
        }
        return Arrays.asList(methods);
    }

}
