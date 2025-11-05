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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {


    @Autowired
    private PdfService pdfService;

    // ✅ Upload PDF
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

    // ✅ List all PDFs
    @GetMapping("/list")
    public List<String> getAllPdfNames() {
        return pdfService.getAllPdfs()
                .stream()
                .map(pdf -> pdf.getFileName() + " (uploaded by " + pdf.getUploadedBy() + ")")
                .collect(Collectors.toList());
    }

    // ✅ Download PDF by ID
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

    // ✅ Convert PDF to Image (first page) helper
    private byte[] convertPdfToImage(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150); // first page, 150 DPI
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bim, "png", baos);
            return baos.toByteArray();
        }
    }

    // ✅ Get PDF as Image by ID
    @GetMapping("/file-image/{id}")
    public ResponseEntity<byte[]> getPdfAsImage(@PathVariable Long id) throws IOException {
        PdfFile pdf = pdfService.getPdfById(id);
        if (pdf == null) return ResponseEntity.notFound().build();

        byte[] imageBytes = convertPdfToImage(pdf.getData());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.getFileName() + ".png\"")
                .body(imageBytes);
    }

    // ✅ Get PDFs for a user with image URLs
    @GetMapping("/users/{username}")
    public ResponseEntity<List<Map<String, Object>>> getPdfsByUserBlob(@PathVariable String username) {
        List<PdfFile> pdfs = pdfService.getPdfsByUsername(username);
        if (pdfs.isEmpty()) return ResponseEntity.status(404).body(List.of());

        List<Map<String, Object>> result = pdfs.stream()
                .map(pdf -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", pdf.getId());
                    map.put("fileName", pdf.getFileName());
                    map.put("uploadedBy", pdf.getUploadedBy());
                    map.put("contentType", pdf.getContentType());
                    map.put("imageUrl", "https://ownbrandslicimageurl.onrender.com/api/pdf/file-image/" + pdf.getId());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(result);
    }


}
