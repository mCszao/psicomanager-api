package com.psicomanager.api.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.psicomanager.api.domain.document.model.Document;
import com.psicomanager.api.domain.document.exception.ContractWithoutArgsException;
import com.psicomanager.api.domain.document.exception.DocumentNotFoundException;
import com.psicomanager.api.domain.patient.exception.PatientNotFoundException;
import com.psicomanager.api.repositories.DocumentRepository;
import com.psicomanager.api.repositories.PatientRepository;
import com.psicomanager.api.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class DocumentService {
    private DocumentRepository docRepo;

    public Document getDocumentById(String id){
        log.info("Buscando informações do documento de id "+id);
        return docRepo.findById(id).orElseThrow(() -> new DocumentNotFoundException("Documento solicitado não existe"));
    }
}
