package com.janajagoran.scms.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.janajagoran.scms.entity.Member;
import com.janajagoran.scms.entity.Payment;
import com.janajagoran.scms.repository.MemberRepository;
import com.janajagoran.scms.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Generates member, payment, and budget reports as PDF or Excel byte streams,
 * ready to be returned directly as a downloadable file from a controller.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public byte[] generateMemberReportPdf() throws IOException {
        List<Member> members = memberRepository.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new Paragraph("Jana Jagoran Club - Member Report").setBold().setFontSize(16));
        document.add(new Paragraph(" "));

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 3, 2, 2}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(new Cell().add(new Paragraph("Membership ID")));
        table.addHeaderCell(new Cell().add(new Paragraph("Name")));
        table.addHeaderCell(new Cell().add(new Paragraph("Email")));
        table.addHeaderCell(new Cell().add(new Paragraph("Status")));
        table.addHeaderCell(new Cell().add(new Paragraph("Joined")));

        for (Member m : members) {
            table.addCell(new Cell().add(new Paragraph(m.getMembershipId())));
            table.addCell(new Cell().add(new Paragraph(m.getUser().getName())));
            table.addCell(new Cell().add(new Paragraph(m.getUser().getEmail())));
            table.addCell(new Cell().add(new Paragraph(m.getStatus().name())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(m.getDateOfJoining()))));
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    public byte[] generateMemberReportExcel() throws IOException {
        List<Member> members = memberRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Members");

            Row header = sheet.createRow(0);
            String[] cols = {"Membership ID", "Name", "Email", "Phone", "Status", "Joined", "Monthly Fee"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            int rowIdx = 1;
            for (Member m : members) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(m.getMembershipId());
                row.createCell(1).setCellValue(m.getUser().getName());
                row.createCell(2).setCellValue(m.getUser().getEmail());
                row.createCell(3).setCellValue(m.getUser().getPhone() != null ? m.getUser().getPhone() : "");
                row.createCell(4).setCellValue(m.getStatus().name());
                row.createCell(5).setCellValue(String.valueOf(m.getDateOfJoining()));
                row.createCell(6).setCellValue(m.getMonthlyFee().doubleValue());
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] generatePaymentReportExcel(Integer month, Integer year) throws IOException {
        List<Payment> payments = (month != null && year != null)
                ? paymentRepository.findByPaymentMonthAndPaymentYear(month, year)
                : paymentRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payments");

            Row header = sheet.createRow(0);
            String[] cols = {"Membership ID", "Member Name", "Month", "Year", "Amount", "Status", "Paid On", "Mode"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            int rowIdx = 1;
            for (Payment p : payments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getMember().getMembershipId());
                row.createCell(1).setCellValue(p.getMember().getUser().getName());
                row.createCell(2).setCellValue(p.getPaymentMonth());
                row.createCell(3).setCellValue(p.getPaymentYear());
                row.createCell(4).setCellValue(p.getAmount().doubleValue());
                row.createCell(5).setCellValue(p.getStatus().name());
                row.createCell(6).setCellValue(p.getPaidOn() != null ? p.getPaidOn().toString() : "");
                row.createCell(7).setCellValue(p.getPaymentMode() != null ? p.getPaymentMode().name() : "");
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}
