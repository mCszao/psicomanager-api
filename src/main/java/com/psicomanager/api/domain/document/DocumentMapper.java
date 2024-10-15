package com.psicomanager.api.domain.document;

import com.psicomanager.api.domain.document.dto.DocumentResponseDTO;
import com.psicomanager.api.repositories.document.model.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class DocumentMapper {
    public Document fileToDocument(MultipartFile file) throws IOException {
        var doc = new Document();
        doc.setName(file.getOriginalFilename());
        doc.setType(file.getContentType());
        doc.setContent(file.getBytes());
        return doc;
    }

    public static DocumentResponseDTO documentToDto(Document doc) {
        return new DocumentResponseDTO(doc.getId(), doc.getName(), doc.getType());
    }
}
