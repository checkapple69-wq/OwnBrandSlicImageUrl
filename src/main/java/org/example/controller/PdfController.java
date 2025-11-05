package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.PdfFile;
import org.example.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
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
    private  PdfService pdfService;

    // ✅ Upload PDF with username
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

    // ✅ List all PDFs with usernames
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
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(pdf.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pdf.getFileName() + "\"")
                .body(pdf.getData());
    }

//    @GetMapping("/user/{username}")
//    public ResponseEntity<byte[]> getUserPdf(@PathVariable String username) {
//        List<PdfFile> pdfs = pdfService.getPdfsByUsername(username);
//
//        if (pdfs.isEmpty()) {
//            return ResponseEntity.status(404).body(null);
//        }
//
//        // Pick the most recent one (or the first)
//        PdfFile pdf = pdfs.get(pdfs.size() - 1);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(pdf.getContentType()))
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + pdf.getFileName() + "\"")
//                .body(pdf.getData());
//    }


    //get pdf file
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Map<String, Object>>> getPdfsByUser(@PathVariable String username) {
        List<PdfFile> pdfs = pdfService.getPdfsByUsername(username);

        if (pdfs.isEmpty()) {
            return ResponseEntity.status(404).body(List.of());
        }

        List<Map<String, Object>> result = pdfs.stream()
                .map(pdf -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", pdf.getId());
                    map.put("fileName", pdf.getFileName());
                    map.put("uploadedBy", pdf.getUploadedBy());
                    map.put("contentType", pdf.getContentType());
                    map.put("data", Base64.getEncoder().encodeToString(pdf.getData()));
                    return map;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<List<Map<String, Object>>> getPdfsByUserBlob(@PathVariable String username) {
        List<PdfFile> pdfs = pdfService.getPdfsByUsername(username);

        if (pdfs.isEmpty()) {
            return ResponseEntity.status(404).body(List.of());
        }

        List<Map<String, Object>> result = pdfs.stream()
                .map(pdf -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", pdf.getId());
                    map.put("fileName", pdf.getFileName());
                    map.put("uploadedBy", pdf.getUploadedBy());
                    map.put("contentType", pdf.getContentType());
                    // 👇 Instead of Base64, return a Blob URL endpoint
                    map.put("blobUrl", "http://localhost:8080/api/pdf/file/" + pdf.getId());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<byte[]> getPdfFile(@PathVariable String id) {
        PdfFile pdf = pdfService.getPdfById(id);

        if (pdf == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(pdf.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.getFileName() + "\"")
                .body(pdf.getData());
    }
}


