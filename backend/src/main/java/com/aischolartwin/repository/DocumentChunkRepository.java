package com.aischolartwin.repository;

import com.aischolartwin.entity.Document;
import com.aischolartwin.entity.DocumentChunk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentOrderByChunkIndexAsc(Document document);

    Page<DocumentChunk> findByDocumentOrderByChunkIndexAsc(Document document, Pageable pageable);

    long countByDocument(Document document);

    void deleteByDocument(Document document);
}

