package com.psicomanager.api.services;

import com.psicomanager.api.repositories.document.model.Document;
import com.psicomanager.api.domain.document.exception.DocumentNotFoundException;
import com.psicomanager.api.repositories.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentService {
    @Autowired
    private DocumentRepository docRepo;

    public Document getDocumentById(String id){
        log.info("Buscando informações do documento de id "+id);
        return docRepo.findById(id).orElseThrow(DocumentNotFoundException::new);
    }
}
