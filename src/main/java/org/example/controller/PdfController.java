package org.example.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.entity.PdfFile;
import org.example.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

static {
    // Ensure headless mode for Render (PDFBox + AWT)
    System.setProperty("java.awt.headless", "true");
}

@Autowired
private PdfService pdfService;

// Upload PDF
@PostMapping("/upload")
public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file,
                                        @RequestParam("username") String username) {
    try {
        pdfService.uploadPdf(file, username);
        return ResponseEntity.ok("PDF uploaded by " + username + ": " + file.getOriginalFilename());
    } catch (IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Upload failed: " + e.getMessage());
    }
}

// List all PDFs
@GetMapping("/list")
public List<String> getAllPdfNames() {
    List<PdfFile> pdfs = pdfService.getAllPdfs();
    List<String> result = new ArrayList<>();
    for (PdfFile pdf : pdfs) {
        result.add(pdf.getFileName() + " (uploaded by " + pdf.getUploadedBy() + ")");
    }
    return result;
}

// Download PDF by ID
@GetMapping("/{id}")
public ResponseEntity<byte[]> getPdfById(@PathVariable Long id) {
    PdfFile pdf = pdfService.getPdfById(id);
    if (pdf == null) return ResponseEntity.notFound().build();

    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(pdf.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + pdf.getFileName() + "\"")
            .body(pdf.getData());
}

// Convert PDF to Image (first page)
private byte[] convertPdfToImage(byte[] pdfBytes) {
    if (pdfBytes == null || pdfBytes.length == 0) return null;
    try (PDDocument document = PDDocument.load(pdfBytes)) {
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage image = renderer.renderImageWithDPI(0, 150); // first page, 150 DPI
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    } catch (Exception e) {
        e.printStackTrace();
        return null;
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

// Get PDFs for a user with image URLs
@GetMapping("/users/{username}")
public ResponseEntity<List<Map<String, Object>>> getPdfsByUser(@PathVariable String username) {
    try {
        List<PdfFile> pdfs = pdfService.getPdfsByUsername(username);
        if (pdfs == null || pdfs.isEmpty()) return ResponseEntity.status(404).body(List.of());

        List<Map<String, Object>> result = new ArrayList<>();
        for (PdfFile pdf : pdfs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", pdf.getId());
            map.put("fileName", pdf.getFileName());
            map.put("uploadedBy", pdf.getUploadedBy());
            map.put("contentType", pdf.getContentType());

            byte[] imageBytes = convertPdfToImage(pdf.getData());
            map.put("imageUrl", imageBytes != null
                    ? "https://ownbrandslicimageurl.onrender.com/api/pdf/file-image/" + pdf.getId()
                    : null);

            result.add(map);
        }

        return ResponseEntity.ok(result);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(List.of(Map.of("error", e.getMessage())));
    }
}


}
