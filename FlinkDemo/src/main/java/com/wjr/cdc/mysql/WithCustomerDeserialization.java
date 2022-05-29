package com.wjr.cdc.mysql;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc
 * @ClassName: WithCustomerDeserialization
 * @create 2022-05-13 14:14
 * @Description:
 */
public class WithCustomerDeserialization implements DebeziumDeserializationSchema<String> {

    /**
     * 封账数据 -> JSON格式
     *
     * @param record
     * @param out
     * @throws Exception {
     *                   "database":""
     *                   "tableName":""
     *                   "before":""
     *                   "after":""
     *                   "type":"c u d"
     *                   "ts":""
     *                   }
     */
    @Override
    public void deserialize(SourceRecord record, Collector<String> out) throws Exception {
        // 1. 创建JSON对象
        JSONObject jsonObject = new JSONObject();
        // 2. 获取库名&表名
        String[] fields = record.topic().split("\\.");
        String database = fields[1];
        String tableName = fields[2];
        // 3. 获取before字段
        Struct value = (Struct) record.value();
        Struct before = value.getStruct("before");
        JSONObject beforeJson = new JSONObject();
        if (before != null) {
            before.schema().fields().forEach(field -> {
                Object fieldValue = before.get(field);
                beforeJson.put(field.name(), fieldValue);
            });
        }
        // 4. 获取after字段
        Struct after = value.getStruct("after");
        JSONObject afterJson = new JSONObject();
        if (after != null) {
            after.schema().fields().forEach(field -> {
                Object fieldValue = after.get(field);
                afterJson.put(field.name(), fieldValue);
            });
        }
        // 5.获取操作类型
        Envelope.Operation operation = Envelope.operationFor(record);
        String type = operation.toString().toLowerCase();
        if (type.equals("create")){
            type = "insert";
        }
        System.out.println(value.get("ts_ms"));

        // 6. 将字段写入JSON对象
        jsonObject.put("database",database);
        jsonObject.put("tableName",tableName);
        jsonObject.put("before", beforeJson);
        jsonObject.put("after",afterJson);
        jsonObject.put("type",type);
        jsonObject.put("ts",value.get("ts_ms"));
        // 7. 输出数据
        out.collect(jsonObject.toJSONString());
    }

    @Override
    public TypeInformation<String> getProducedType() {
        return BasicTypeInfo.STRING_TYPE_INFO;
    }
}
