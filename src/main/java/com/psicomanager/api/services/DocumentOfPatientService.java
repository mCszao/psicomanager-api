package com.psicomanager.api.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.psicomanager.api.domain.document.exception.ContractWithoutArgsException;
import com.psicomanager.api.domain.document.model.Document;
import com.psicomanager.api.domain.patient.exception.PatientNotFoundException;
import com.psicomanager.api.repositories.DocumentRepository;
import com.psicomanager.api.repositories.PatientRepository;
import com.psicomanager.api.utils.DateUtils;
import jakarta.transaction.Transactional;
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
public class DocumentOfPatientService {
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DocumentRepository docRepo;

    public byte[] generateContract(String patientId) throws IOException {
        log.info("Buscando informações paciente de id "+ patientId);
        var patient = patientRepo.findById(patientId).orElseThrow(()-> new ContractWithoutArgsException("Paciente informado não possuí registro"));
        if(patient.getAddresses().isEmpty()) throw new ContractWithoutArgsException("Paciente não possuí endereço cadastrado.");
        Context context = new Context();
        context.setVariable("patient", patient);
        context.setVariable("currentDate", DateUtils.getDateToContract());
        log.info("Adicionando paciente ao modelo de contrato.");
        String htmlContent = templateEngine.process("template", context);
        log.info("Obtendo bytes do arquivo gerado.");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(outputStream);
        builder.run();
        log.info("Retornando bytes");
        return outputStream.toByteArray();
    }

    @Transactional
    public void saveDoc(MultipartFile file, String patientId) throws IOException {
        var doc = new Document();
        log.info("Montando objeto com o arquivo recebido.");
        doc.setName(file.getOriginalFilename());
        doc.setType(file.getContentType());
        doc.setContent(file.getBytes());
        log.info("Buscando informações paciente de id "+ patientId);
        if(patientId != null && !(patientId.trim().equals(""))){
            var patient = patientRepo.findById(patientId).orElseThrow(() -> new PatientNotFoundException("Paciente informado não possuí registro"));
            doc.setPatient(patient);
        }
        log.info("Salvando novo documento");
        docRepo.save(doc);
    }
}
