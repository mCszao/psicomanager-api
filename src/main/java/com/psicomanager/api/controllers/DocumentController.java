package com.psicomanager.api.controllers;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.psicomanager.api.services.DocumentService;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void getContract(@RequestParam String patientId, HttpServletResponse response) throws IOException {
        byte[] pdfContract = service.generateContract(patientId);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=generated-contract.pdf");
        response.setContentLength(pdfContract.length);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfContract);
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
        response.setStatus(200);
    }
}
