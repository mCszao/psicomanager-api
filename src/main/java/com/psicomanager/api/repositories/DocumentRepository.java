package com.psicomanager.api.repositories;

import com.psicomanager.api.repositories.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, String> {
}
