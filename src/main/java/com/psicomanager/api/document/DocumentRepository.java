package com.psicomanager.api.document;

import com.psicomanager.api.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, String> {
}
