package org.example.service;

import org.example.entity.PdfFile;
import org.example.repository.PdfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PdfService {

    @Autowired
    private  PdfRepository pdfRepository;



    // ✅ Upload PDF with username (no Lombok builder)
    public void uploadPdf(MultipartFile file, String username) throws IOException {
        PdfFile pdf = new PdfFile();
        pdf.setFileName(file.getOriginalFilename());
        pdf.setContentType(file.getContentType());
        pdf.setUploadedBy(username);
        pdf.setData(file.getBytes());

        pdfRepository.save(pdf);
    }


    public List<PdfFile> getAllPdfs() {
        return pdfRepository.findAll();
    }

    public PdfFile getPdfById(Long id) {
        return pdfRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PDF not found with id: " + id));
    }

    // ✅ New method
    public List<PdfFile> getPdfsByUsername(String username) {
        return pdfRepository.findByUploadedBy(username);
    }

    // ✅ Get single PDF by ID
    public PdfFile getPdfById(String id) {
        Optional<PdfFile> pdf = pdfRepository.findById(Long.valueOf(id));
        return pdf.orElse(null);
    }
}
