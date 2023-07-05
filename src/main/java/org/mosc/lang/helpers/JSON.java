package org.mosc.lang.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JSON {
    static Logger LOGGER = Logger.getLogger(JSON.class.getSimpleName());
    public static Map<String, Object> toMap(String json) {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, typeRef);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            return new HashMap<>();
        }
    }
    public static List<Object> toList(String str) {
        TypeReference<List<Object>> typeRef = new TypeReference<List<Object>>() {
        };
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(str, typeRef);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            return new ArrayList<>();
        }
    }
}
