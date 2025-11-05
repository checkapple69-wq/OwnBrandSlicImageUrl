package org.example.service;

import org.example.entity.PdfFile;
import org.example.repository.PdfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    @Autowired
    private PdfRepository pdfRepository;

    // ✅ Upload PDF with username
    public void uploadPdf(MultipartFile file, String username) throws IOException {
        PdfFile pdf = new PdfFile();
        pdf.setFileName(file.getOriginalFilename());
        pdf.setContentType(file.getContentType());
        pdf.setUploadedBy(username);
        pdf.setData(file.getBytes());

        pdfRepository.save(pdf);
    }

    // ✅ Get all PDFs
    public List<PdfFile> getAllPdfs() {
        return pdfRepository.findAll();
    }

    // ✅ Get PDF by ID
    public PdfFile getPdfById(Long id) {
        return pdfRepository.findById(id).orElse(null);
    }

    // ✅ Get PDFs by username
    public List<PdfFile> getPdfsByUsername(String username) {
        return pdfRepository.findByUploadedBy(username);
    }


}
