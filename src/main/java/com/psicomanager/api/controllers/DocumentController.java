package com.psicomanager.api.controllers;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.psicomanager.api.services.DocumentService;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
public class DocumentController {
    @Autowired
    private DocumentService service;
    @GetMapping("/generate-contract")
    public ResponseEntity<byte[]> getContract(@RequestParam String patientId) throws IOException {
        byte[] pdfContract = service.generateContract(patientId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated-contract.pdf");
        headers.setContentLength(pdfContract.length);

        return ResponseEntity.ok().headers(headers).body(pdfContract);
    }
}
