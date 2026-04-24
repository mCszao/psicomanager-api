package com.psicomanager.api.document;

import com.psicomanager.api.document.exception.ContractWithoutArgsException;
import com.psicomanager.api.document.mapper.DocumentMapper;
import com.psicomanager.api.document.model.Document;
import com.psicomanager.api.patient.PatientRepository;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.utils.DateUtils;
import com.psicomanager.api.utils.FileUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

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

    @Autowired
    private DocumentMapper mapper;

    public byte[] generateContract(String patientId) throws IOException {
        log.info("Buscando informações paciente de id " + patientId);
        var patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new ContractWithoutArgsException("Paciente informado não possuí registro"));
        if (patient.getAddresses().isEmpty())
            throw new ContractWithoutArgsException("Paciente não possuí endereço cadastrado.");
        Context context = new Context();
        context.setVariable("patient", patient);
        context.setVariable("currentDate", DateUtils.getDateToContract());
        log.info("Adicionando paciente ao modelo de contrato.");
        String htmlContent = templateEngine.process("template", context);
        log.info("Obtendo bytes do arquivo gerado.");
        byte[] bytes = FileUtils.generactByteByContractHtml(htmlContent);
        log.info("Retornando bytes");
        return bytes;
    }

    @Transactional
    public void saveDoc(MultipartFile file, String patientId) {
        log.info("Montando objeto com o arquivo recebido.");
        Document doc;
        try {
            doc = mapper.fileToDocument(file);
        } catch (Exception exception) {
            String msg = "Não foi possível criar o arquivo";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        log.info("Buscando informações paciente de id " + patientId);
        if (patientId != null && !patientId.trim().isEmpty()) {
            var patient = patientRepo.findById(patientId).orElseThrow(PatientNotFoundException::new);
            doc.setPatient(patient);
        }
        log.info("Salvando novo documento");
        docRepo.save(doc);
    }
}
