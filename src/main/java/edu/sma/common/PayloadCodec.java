package edu.sma.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class PayloadCodec {

    private PayloadCodec() {
    }

    public static String encode(Map<String, String> data) {
        StringJoiner joiner = new StringJoiner(";");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }
        return joiner.toString();
    }

    public static Map<String, String> decode(String payload) {
        Map<String, String> map = new LinkedHashMap<>();
        if (payload == null || payload.isBlank()) {
            return map;
        }
        String[] parts = payload.split(";");
        for (String part : parts) {
            int index = part.indexOf('=');
            if (index > 0 && index < part.length() - 1) {
                String key = part.substring(0, index).trim();
                String value = part.substring(index + 1).trim();
                map.put(key, value);
            }
        }
        return map;
    }
}
