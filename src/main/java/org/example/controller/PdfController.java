package org.example.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.entity.PdfFile;
import org.example.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    // Ensure headless mode for Render
    @PostConstruct
    public void init() {
        System.setProperty("java.awt.headless", "true");
    }

    // Upload PDF
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("username") String username) {
        try {
            pdfService.uploadPdf(file, username);
            return ResponseEntity.ok("PDF uploaded by " + username + ": " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    // List all PDFs
    @GetMapping("/list")
    public List<String> getAllPdfNames() {
        return pdfService.getAllPdfs().stream()
                .map(pdf -> pdf.getFileName() + " (uploaded by " + pdf.getUploadedBy() + ")")
                .toList();
    }

    // Download PDF by ID
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getPdfById(@PathVariable Long id) {
        PdfFile pdf = pdfService.getPdfById(id);
        if (pdf == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(pdf.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdf.getFileName() + "\"")
                .body(pdf.getData());
    }

    // Convert PDF to image safely
    private byte[] convertPdfToImage(byte[] pdfBytes) {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bim, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // conversion failed
        }
    }

    // Get PDF as Image by ID
    @GetMapping("/file-image/{id}")
    public ResponseEntity<byte[]> getPdfAsImage(@PathVariable Long id) {
        PdfFile pdf = pdfService.getPdfById(id);
        if (pdf == null) return ResponseEntity.notFound().build();

        byte[] imageBytes = convertPdfToImage(pdf.getData());
        if (imageBytes == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.getFileName() + ".png\"")
                .body(imageBytes);
    }

    // Get PDFs for a user with image URLs (only if conversion succeeds)
    @GetMapping("/users/{username}")
    public ResponseEntity<List<Map<String, Object>>> getPdfsByUserBlob(@PathVariable String username) {
        List<PdfFile> pdfs = pdfService.getPdfsByUsername(username);
        if (pdfs.isEmpty()) return ResponseEntity.status(404).body(List.of());

        List<Map<String, Object>> result = new ArrayList<>();
        for (PdfFile pdf : pdfs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", pdf.getId());
            map.put("fileName", pdf.getFileName());
            map.put("uploadedBy", pdf.getUploadedBy());
            map.put("contentType", pdf.getContentType());

            byte[] imageBytes = convertPdfToImage(pdf.getData());
            if (imageBytes != null) {
                map.put("imageUrl", "https://ownbrandslicimageurl.onrender.com/api/pdf/file-image/" + pdf.getId());
            } else {
                map.put("imageUrl", null); // fallback
            }

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }
}
