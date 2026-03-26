package utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import models.BilletDisplay;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PdfGenerator {
    public static void generateBilletPdf(BilletDisplay billet, String destPath) throws FileNotFoundException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(destPath));
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
        Paragraph title = new Paragraph("Billet d'Entrée", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // empty line

        document.add(new Paragraph("Evenement : " + billet.getNom_evenement()));
        document.add(new Paragraph("Date : " + billet.getDate_seance()));
        document.add(new Paragraph("Heure : " + billet.getHeure_seance()));
        document.add(new Paragraph("Lieu : " + billet.getNom_lieu()));
        document.add(new Paragraph("Salle : " + billet.getNom_salle()));
        document.add(new Paragraph("Siege : " + billet.getSiegeComplet()));
        document.add(new Paragraph("Prix : " + billet.getPrix() + " EUR"));
        document.add(new Paragraph(" "));

        // Génération du QR Code
        try {
            String qrData = "Billet N°" + billet.getId_billet() + "\nEvénement: " + billet.getNom_evenement() + "\nDate: " + billet.getDate_seance() + "\nHeure: " + billet.getHeure_seance() + "\nPlace: " + billet.getSiegeComplet();
            com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 250, 250);
            
            java.io.ByteArrayOutputStream pngOutputStream = new java.io.ByteArrayOutputStream();
            com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            
            com.itextpdf.text.Image qrImage = com.itextpdf.text.Image.getInstance(pngData);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du QR Code : " + e.getMessage());
            e.printStackTrace();
        }

        document.close();
    }
}
