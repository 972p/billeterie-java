package utils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;

public class EmailSender {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "votre.email@gmail.com"; // Remplacer par un vrai email
    private static final String PASSWORD = "votre_mot_de_passe_app"; // Remplacer par le mot de passe d'application généré

    public static void sendWelcomeEmail(String toEmail, String nom) {
        String subject = "Bienvenue sur Billeterie Java !";
        String body = "Bonjour " + nom + ",\n\nMerci de vous être inscrit sur notre plateforme. Vous pouvez désormais réserver vos billets pour les meilleurs évènements !";
        sendEmailWithAttachment(toEmail, subject, body, null);
    }

    public static void sendTicketEmail(String toEmail, String nom, String eventTitle, String pdfPath) {
        String subject = "Votre billet pour : " + eventTitle;
        String body = "Bonjour " + nom + ",\n\nMerci pour votre réservation ! Veuillez trouver ci-joint votre billet d'entrée au format PDF.\n\nÀ très bientôt !";
        sendEmailWithAttachment(toEmail, subject, body, pdfPath);
    }

    private static void sendEmailWithAttachment(String toEmail, String subject, String bodyText, String attachmentPath) {
        System.out.println("--- DÉBUT DE LA SIMULATION D'ENVOI D'E-MAIL ---");
        System.out.println("Destinataire : " + toEmail);
        System.out.println("Sujet : " + subject);
        System.out.println("Pièce jointe : " + (attachmentPath != null ? attachmentPath : "Aucune"));
        System.out.println("Contenu :\n" + bodyText);
        System.out.println("----------------------------------------------");
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // Partie texte
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(bodyText);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);

            // Pièce jointe si fournie
            if (attachmentPath != null) {
                File f = new File(attachmentPath);
                if (f.exists()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(f);
                    multipart.addBodyPart(attachmentPart);
                }
            }

            message.setContent(multipart);

            // Pour envoyer l'e-mail pour de vrai, décommentez la ligne ci-dessous 
            // après avoir mis de vrais identifiants SMTP (ex: Mot de passe d'application Google)
            // Transport.send(message);
            // System.out.println("Email réellement envoyé !");

        } catch (Exception e) {
            System.err.println("Erreur de préparation de l'e-mail : " + e.getMessage());
        }
    }
}
