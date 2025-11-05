package org.example.repository;

import org.example.entity.PdfFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdfRepository extends JpaRepository<PdfFile, Long> {

    List<PdfFile> findByUploadedBy(String uploadedBy);



}

