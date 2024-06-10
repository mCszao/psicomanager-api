package com.psicomanager.api.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HeadersUtils {
    public static HttpHeaders pdfHeaders(byte[] pdf){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated-contract.pdf");
        headers.setContentLength(pdf.length);
        return headers;
    }
}
