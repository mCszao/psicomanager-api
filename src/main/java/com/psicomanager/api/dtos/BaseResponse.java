package com.psicomanager.api.dtos;

public record BaseResponse<T>(boolean success, T object) {
}
