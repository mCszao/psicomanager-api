package com.psicomanager.api.document;

import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.utils.HeaderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/documents")
@Slf4j
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentOfPatientService dopService;

    @GetMapping("/generate-contract")
    public ResponseEntity<byte[]> getContract(@RequestParam String patientId) throws IOException {
        log.info("GET: /documents/generate-contract?patientId=" + patientId);
        byte[] pdfContract = dopService.generateContract(patientId);
        var headers = HeaderUtils.pdfHeaders(pdfContract);
        return ResponseEntity.ok().headers(headers).body(pdfContract);
    }

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> upload(
            @RequestParam MultipartFile file,
            @RequestParam String patientId) throws IOException {
        log.info("POST: /documents/upload?patientId=" + patientId);
        log.info("Recebendo arquivo do tipo " + file.getContentType());
        dopService.saveDoc(file, patientId);
        return ResponseEntity.ok(new BaseResponse<>(true, file.getOriginalFilename() + " salvo com sucesso"));
    }

    @GetMapping("/download/{id}")
    private ResponseEntity<byte[]> download(@PathVariable String id) {
        log.info("GET: /documents/download/" + id);
        var doc = documentService.getDocumentById(id);
        HttpHeaders headers = HeaderUtils.fileWithNameAndTypeHeader(doc.getName(), doc.getType());
        return ResponseEntity.ok().headers(headers).body(doc.getContent());
    }
}
