package com.janajagoran.scms.controller;

import com.janajagoran.scms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/** Report generation & export endpoints (PDF / Excel), available to Secretary, President, Admin, Super Admin. */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/members/pdf")
    public ResponseEntity<byte[]> memberReportPdf() throws IOException {
        byte[] pdf = reportService.generateMemberReportPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=member-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/members/excel")
    public ResponseEntity<byte[]> memberReportExcel() throws IOException {
        byte[] excel = reportService.generateMemberReportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=member-report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/payments/excel")
    public ResponseEntity<byte[]> paymentReportExcel(
            @RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year) throws IOException {
        byte[] excel = reportService.generatePaymentReportExcel(month, year);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payment-report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
