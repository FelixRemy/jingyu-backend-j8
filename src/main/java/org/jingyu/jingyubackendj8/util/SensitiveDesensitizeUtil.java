package org.jingyu.jingyubackendj8.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Colin
 * 敏感字段脱敏工具
 * 密码、手机号不能明文打印到日志，做*脱敏
 */
public class SensitiveDesensitizeUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 对json字符串做脱敏处理
     * @param jsonStr 原始json
     * @return 脱敏后的json
     */
    public static String desensitize(String jsonStr) {
        // Java8 兼容，去掉 isBlank()
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return jsonStr;
        }
        try {
            JsonNode root = MAPPER.readTree(jsonStr);
            // 递归脱敏，支持对象、数组、嵌套
            desensitizeNode(root);
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            // json解析异常，原样返回，不阻断业务
            return jsonStr;
        }
    }

    /**
     * 递归遍历JsonNode，处理对象、数组
     */
    private static void desensitizeNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            // 密码类字段
            if (objNode.has("password")) {
                objNode.put("password", "******");
            }
            if (objNode.has("userPassword")) {
                objNode.put("userPassword", "******");
            }
            // 手机号
            if (objNode.has("phone")) {
                String phone = objNode.get("phone").asText("");
                if (phone.length() == 11) {
                    phone = phone.substring(0, 3) + "****" + phone.substring(7);
                    objNode.put("phone", phone);
                }
            }
            // 递归遍历子字段
            objNode.fields().forEachRemaining(entry -> desensitizeNode(entry.getValue()));
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            // 遍历数组里面每一个元素
            for (JsonNode item : arrayNode) {
                desensitizeNode(item);
            }
        }
        // 普通值（字符串/数字/布尔）无需处理
    }
}
