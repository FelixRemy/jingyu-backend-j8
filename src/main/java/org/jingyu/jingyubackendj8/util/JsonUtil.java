package org.jingyu.jingyubackendj8.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Colin
 * 对象转json字符串工具，用于切面打印入参出参
 */
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 对象转为json字符串
     * @param obj 任意对象
     * @return json字符串，序列化失败返回toString
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }
}

