package com.procurement.helper;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.procurement.entity.Contract;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Year;

@Slf4j
public class GeneratePBGPDF {

    /**
     * Generate complete Work Order PDF for a contract
     * @param contract Contract entity with all details
     * @return byte array of PDF
     */
    public static byte[] generateWorkOrderPDF(Contract contract) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, com.itextpdf.kernel.geom.PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            // ========== HEADER SECTION ==========
            Paragraph header = new Paragraph("WORK ORDER")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE);
            document.add(header);

            document.add(new Paragraph(" "));

            // Company Details
            Paragraph company = new Paragraph("E-PROCUREMENT DEPARTMENT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setBold();
            document.add(company);

            Paragraph address = new Paragraph("Government of India, Procurement Division")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10);
            document.add(address);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Work Order Number
            Paragraph woNumber = new Paragraph("Work Order No: WO/" + contract.getContractNo() + "/" + Year.now().getValue())
                    .setFontSize(12)
                    .setBold();
            document.add(woNumber);

            document.add(new Paragraph("Date: " + LocalDate.now().toString()));
            document.add(new Paragraph(" "));

            // ========== VENDOR DETAILS ==========
            document.add(new Paragraph("To,").setBold());
            document.add(new Paragraph(contract.getVendorName()));
            document.add(new Paragraph("Vendor Code: " + contract.getContractNo()));
            document.add(new Paragraph(" "));

            // Subject
            document.add(new Paragraph("Subject: Work Order for " + contract.getTenderTitle()).setBold());
            document.add(new Paragraph(" "));

            // ========== CONTRACT DETAILS TABLE ==========
            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            table.setWidth(UnitValue.createPercentValue(100));

            addTableRow(table, "Contract No:", contract.getContractNo());
            addTableRow(table, "Tender No:", contract.getTenderNo());
            addTableRow(table, "Tender Title:", contract.getTenderTitle());
            addTableRow(table, "Vendor Name:", contract.getVendorName());
            addTableRow(table, "Award Date:", contract.getAwardDate() != null ? contract.getAwardDate().toString() : "-");
            addTableRow(table, "Start Date:", contract.getStartDate() != null ? contract.getStartDate().toString() : "-");
            addTableRow(table, "End Date:", contract.getEndDate() != null ? contract.getEndDate().toString() : "-");
            addTableRow(table, "Contract Amount:", "₹ " + contract.getAmount());
            addTableRow(table, "PBG Status:", contract.getPbgPath() != null ? "✓ Uploaded" : "Pending");
            addTableRow(table, "Status:", contract.getStatus());

            document.add(table);

            document.add(new Paragraph(" "));

            // ========== TERMS AND CONDITIONS ==========
            Paragraph termsTitle = new Paragraph("TERMS AND CONDITIONS")
                    .setBold()
                    .setFontSize(12);
            document.add(termsTitle);
            document.add(new Paragraph(" "));

            String[] terms = {
                    "1. The vendor shall supply the goods/services as per the specifications mentioned in the tender.",
                    "2. The delivery must be completed within the stipulated timeline.",
                    "3. Performance Bank Guarantee (PBG) of 5% of contract value must be submitted before commencement.",
                    "4. Payment will be released within 30 days of successful delivery and invoice submission.",
                    "5. Any delay in delivery will attract penalty as per contract terms.",
                    "6. The vendor must maintain quality standards as per ISO specifications.",
                    "7. Disputes if any shall be resolved through arbitration.",
                    "8. This work order is valid only after both parties sign."
            };

            for (String term : terms) {
                document.add(new Paragraph(term));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // ========== SIGNATURE SECTION ==========
            Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            sigTable.setWidth(UnitValue.createPercentValue(100));

            sigTable.addCell(createSignatureCell("For: " + contract.getVendorName()));
            sigTable.addCell(createSignatureCell("For: Procurement Department"));
            sigTable.addCell(createSignatureCell("(Authorized Signatory)"));
            sigTable.addCell(createSignatureCell("(Authorized Signatory)"));
            sigTable.addCell(createSignatureCell(" "));
            sigTable.addCell(createSignatureCell(" "));
            sigTable.addCell(createSignatureCell("Date: ______________"));
            sigTable.addCell(createSignatureCell("Date: ______________"));

            document.add(sigTable);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // ========== FOOTER ==========
            Paragraph footer = new Paragraph("This is a system generated work order. No signature required for digital copy.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage());
        }
    }

    /**
     * Add a row to the table with label and value
     */
    private static void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label)).setBold();
        labelCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        table.addCell(labelCell);

        Cell valueCell = new Cell().add(new Paragraph(value != null ? value : "-"));
        table.addCell(valueCell);
    }

    /**
     * Create a cell for signature table without borders
     */
    private static Cell createSignatureCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setBorder(null);
    }
}