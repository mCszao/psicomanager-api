package com.psicomanager.api.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.psicomanager.api.exceptions.patient.PatientNotFoundException;
import com.psicomanager.api.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
}
