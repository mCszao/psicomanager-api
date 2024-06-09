package com.psicomanager.api.core.dto;

public record BaseResponse<T>(boolean success, T object) {
}
