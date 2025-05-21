package io.github.han9912.zipin.common.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

public class ListMapStringHandler extends JsonTypeHandler<List<Map<String, String>>> {
    public ListMapStringHandler() {
        super(new TypeReference<>() {});
    }
}