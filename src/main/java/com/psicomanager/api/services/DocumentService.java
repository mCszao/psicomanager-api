package com.psicomanager.api.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.psicomanager.api.domain.document.Document;
import com.psicomanager.api.exceptions.document.DocumentNotFoundException;
import com.psicomanager.api.exceptions.patient.PatientNotFoundException;
import com.psicomanager.api.repositories.DocumentRepository;
import com.psicomanager.api.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class DocumentService {
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DocumentRepository docRepo;

    public byte[] generateContract(String patientId) throws IOException {
        var patient = patientRepo.findById(patientId).orElseThrow(()-> new PatientNotFoundException("Paciente informado não possuí registro"));
        Context context = new Context();
        context.setVariable("patient", patient);
        String htmlContent = templateEngine.process("template", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    }

    public void saveDoc(MultipartFile file, String patientId) throws IOException {
        var doc = new Document();
        doc.setName(file.getOriginalFilename());
        doc.setType(file.getContentType());
        doc.setContent(file.getBytes());
        if(patientId != null && !(patientId.trim().equals(""))){
            var patient = patientRepo.findById(patientId).orElseThrow(() -> new PatientNotFoundException("Paciente informado não possuí registro"));
            doc.setPatient(patient);
        }
        docRepo.save(doc);
    }

    public Document getDocumentById(String id){
        return docRepo.findById(id).orElseThrow(() -> new DocumentNotFoundException("Documento solicitado não existe"));
    }
}
