package com.certificate.system.service;

import com.certificate.system.entity.Certificate;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.repository.CertificateRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class CertificateGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateGenerationService.class);

    @Value("${app.certificate.dir:uploads/certificates}")
    private String certificateDir;

    @Value("${app.baseUrl:http://localhost:8080}")
    private String baseUrl;

    private Path certPath;
    private final CertificateRepository certificateRepository;
    private final AuditLogService auditLogService;

    public CertificateGenerationService(CertificateRepository certificateRepository, AuditLogService auditLogService) {
        this.certificateRepository = certificateRepository;
        this.auditLogService = auditLogService;
    }

    @PostConstruct
    public void init() {
        try {
            certPath = Paths.get(certificateDir).toAbsolutePath().normalize();
            Files.createDirectories(certPath);
            logger.info("Certificate storage initialized: {}", certPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create cert directory", e);
        }
    }

    @Transactional
    public Certificate generateCertificate(CertificateRequest request) {
        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new InvalidRequestOperationException("Cannot generate certificate for a non-approved request.");
        }
        
        if (certificateRepository.findByCertificateRequest(request).isPresent()) {
            throw new InvalidRequestOperationException("Certificate already generated for this request.");
        }

        String uuid = UUID.randomUUID().toString();
        String certNumber = "CERT-" + LocalDateTime.now().getYear() + "-" + String.format("%06d", request.getId());
        String fileName = uuid + ".pdf";

        try {
            Path targetLocation = certPath.resolve(fileName);
            generatePdf(request, uuid, certNumber, targetLocation);

            Certificate certificate = new Certificate();
            certificate.setCertificateRequest(request);
            certificate.setUuid(uuid);
            certificate.setCertificateNumber(certNumber);
            certificate.setPdfUrl(fileName); // storing relative name
            // issuedAt is handled by @CreationTimestamp

            Certificate saved = certificateRepository.save(certificate);
            logger.info("Certificate generated successfully: {}", certNumber);
            auditLogService.log("CERTIFICATE_GENERATED", "SYSTEM", "Generated certificate " + certNumber + " for request " + request.getId());
            
            // Also notify user
            // We could inject NotificationRepository, but we can rely on AdminService notification for approval.
            return saved;
        } catch (Exception e) {
            logger.error("Failed to generate PDF for request {}", request.getId(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void generatePdf(CertificateRequest request, String uuid, String certNumber, Path targetLocation) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(targetLocation.toFile()));
        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32, BaseColor.DARK_GRAY);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 20, BaseColor.GRAY);
        Font nameFont = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 38, BaseColor.BLACK);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);

        // Border and decoration
        document.add(new Paragraph("\n\n")); // Top margin

        Paragraph header = new Paragraph("CERTIFICATE OF ACHIEVEMENT", titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        Paragraph subHeader = new Paragraph("PROUDLY PRESENTED TO", subTitleFont);
        subHeader.setAlignment(Element.ALIGN_CENTER);
        subHeader.setSpacingBefore(30);
        document.add(subHeader);

        String fullName = request.getUser().getFirstName() + " " + request.getUser().getLastName();
        Paragraph name = new Paragraph(fullName.toUpperCase(), nameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingBefore(20);
        document.add(name);

        Paragraph body = new Paragraph("For successfully completing the requirements of\n" + request.getCertificateType().getName(), bodyFont);
        body.setAlignment(Element.ALIGN_CENTER);
        body.setSpacingBefore(30);
        document.add(body);

        Paragraph authority = new Paragraph("Issued by: Digital Certificate Authority", bodyFont);
        authority.setAlignment(Element.ALIGN_CENTER);
        authority.setSpacingBefore(30);
        document.add(authority);

        // QR Code and Meta Data Table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(50);
        
        // Left Cell (Meta Data)
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        leftCell.addElement(new Paragraph("Issue Date: " + date, smallFont));
        leftCell.addElement(new Paragraph("Certificate No: " + certNumber, smallFont));
        leftCell.addElement(new Paragraph("Verification ID: " + uuid, smallFont));
        table.addCell(leftCell);

        // Right Cell (QR Code)
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        String verificationUrl = baseUrl + "/verify/" + uuid;
        Image qrImage = generateQrCode(verificationUrl);
        qrImage.scaleAbsolute(80, 80);
        qrImage.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(qrImage);
        table.addCell(rightCell);

        document.add(table);
        document.close();
    }

    private Image generateQrCode(String barcodeText) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return Image.getInstance(pngOutputStream.toByteArray());
    }
}

