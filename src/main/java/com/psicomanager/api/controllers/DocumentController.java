package com.psicomanager.api.controllers;
import com.psicomanager.api.dtos.BaseResponse;
import com.psicomanager.api.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/documents")
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

    @PostMapping("/add")
    public ResponseEntity<BaseResponse> upload(@RequestParam MultipartFile file ,@RequestParam(required = false) String patientId) throws IOException {
        service.saveDoc(file, patientId);
        return ResponseEntity.ok(new BaseResponse<>(true, file.getOriginalFilename() + " salvo com sucesso"));
    }

    @GetMapping("/download/{id}")
    private ResponseEntity<byte[]> download(@PathVariable String id){
        var doc = service.getDocumentById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+doc.getName());
        return ResponseEntity.ok().headers(headers).body(doc.getContent());
    }
}
