package com.servicio.reservas.pago.domain.services;

import com.servicio.reservas.pago.domain.entities.Payment;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.borders.SolidBorder;
import com.servicio.reservas.pago.infraestructure.exception.VoucherGenerationException;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class VoucherPdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm 'hs.'");

    private static final DeviceRgb BRAND_COLOR = new DeviceRgb(0, 158, 227);

    public byte[] generatePdf(Payment payment) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(byteArrayOutputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.setMargins(40, 40, 40, 40);

            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1f}));
            headerTable.setWidth(UnitValue.createPercentValue(100));

            Cell titleCell = new Cell()
                    .add(new Paragraph("StudioBarber")
                            .setFontSize(16)
                            .setBold()
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(BRAND_COLOR)
                    .setPadding(10)
                    .setBorder(null);

            headerTable.addCell(titleCell);
            document.add(headerTable);

            document.add(new Paragraph("Comprobante de Ingreso")
                    .setFontSize(14)
                    .setMarginTop(20)
                    .setMarginBottom(10)
                    .setBold());

            document.add(new Paragraph(payment.getCreatedAt().format(DATE_FORMATTER))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10)
                    .setMarginBottom(15));

            Double originalAmount = payment.getAmount();
            BigDecimal formattedAmount = new BigDecimal(String.valueOf(originalAmount))
                    .setScale(2, RoundingMode.HALF_UP);

            document.add(new Paragraph("$ " + formattedAmount + " COP")
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(BRAND_COLOR)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(25));

            document.add(new Paragraph("")
                    .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 1)));
            document.add(new Paragraph(" "));

            Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1f, 2.5f}));
            detailsTable.setWidth(UnitValue.createPercentValue(100));

            addDetailRow(detailsTable, "Estado de Pago:", payment.getStatus().name());
            addDetailRow(detailsTable, "Referencia Externa:", safeStringValue(payment.getExternalPaymentId()));
            addDetailRow(detailsTable, "ID de Operación:", safeStringValue(payment.getId().toString()));

            document.add(detailsTable);

            document.add(new Paragraph(" ")
                    .setMarginTop(20));
            document.add(new Paragraph("Desde:")
                    .setFontSize(10)
                    .setBold());
            document.add(new Paragraph("Reserva N° " + safeStringValue(payment.getReservationId().toString()))
                    .setFontSize(12)
                    .setMarginBottom(15));

            document.add(new Paragraph("Hacia:")
                    .setFontSize(10)
                    .setBold());
            document.add(new Paragraph("StudioBarber | Cuenta de Comercio")
                    .setFontSize(12));

            document.close();
            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            throw new VoucherGenerationException("Error al generar el PDF del Voucher: " + e.getMessage());
        }
    }

    private void addDetailRow(Table table, String key, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(key).setFontSize(10).setFontColor(ColorConstants.GRAY))
                .setBorder(null)
                .setPaddingLeft(0));

        table.addCell(new Cell()
                .add(new Paragraph(value).setFontSize(10))
                .setBorder(null)
                .setPaddingRight(0));
    }

    private String safeStringValue(Object value) {
        return value != null ? String.valueOf(value) : "N/A";
    }
}