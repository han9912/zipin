package io.github.han9912.zipin.common.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Result<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.success = true;
        result.data = data;
        result.message = "success";
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.success = false;
        result.message = message;
        return result;
    }
}
