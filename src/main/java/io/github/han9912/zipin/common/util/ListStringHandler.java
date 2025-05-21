package io.github.han9912.zipin.common.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class ListStringHandler extends JsonTypeHandler<List<String>> {
    public ListStringHandler() {
        super(new TypeReference<>() {});
    }
}