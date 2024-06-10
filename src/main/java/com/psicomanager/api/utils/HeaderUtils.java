package com.psicomanager.api.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HeaderUtils {
    public static HttpHeaders pdfHeaders(byte[] pdf){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated-PDF.pdf");
        headers.setContentLength(pdf.length);
        return headers;
    }

    public static HttpHeaders fileWithNameHeader(String name){
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+name);
        return headers;
    }

    public static HttpHeaders fileWithNameAndTypeHeader(String name, String type){
        var headers = HeaderUtils.fileWithNameHeader(name);
        headers.setContentType(MediaType.valueOf(type));
        return headers;
    }
}
