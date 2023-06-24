package com.gendergapanalyser.gendergapanalyser;

import com.itextpdf.text.DocumentException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class SendEmailInBackground implements Runnable {
    @Override
    public void run() {
        if (!Files.exists(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf")) || Main.processData.changedLanguage || Main.processData.predictionsGenerated && !Main.processData.PDFGeneratedWithPredictions || !Main.processData.predictionsGenerated && Main.processData.PDFGeneratedWithPredictions) {
            try {
                Main.processData.createPDF();
            } catch (IOException | DocumentException ignored) {}
        }
        EmailAttachment pdfDocument = new EmailAttachment();
        pdfDocument.setPath("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf");
        pdfDocument.setDisposition(EmailAttachment.ATTACHMENT);
        pdfDocument.setDescription(Main.language.equals("EN") ? "Gender equality in the United States" : Main.language.equals("FR") ? "L'égalité entre les genres dans les États-Unis" : "Egalitatea între genuri în Statele Unite");
        MultiPartEmail mail = new MultiPartEmail();
        mail.setHostName("smtp-mail.outlook.com");
        try {
            mail.addTo(Main.email, Main.email.split("@")[0]);
            mail.setFrom("gender-gap-analyser@outlook.com");
            mail.setSmtpPort(587);
            mail.setAuthentication("gender-gap-analyser@outlook.com", "javagga2023");
            mail.setStartTLSEnabled(true);
            mail.setSubject(Main.language.equals("EN") ? "The PDF report regarding the gender equality situation in the United States" : Main.language.equals("FR") ? "Le rapport PDF regardant la situation de l'égalité entre les genres dans les États-Unis" : "Raportul PDF despre situația egalității între genuri în Statele Unite");
            mail.setMsg(Main.language.equals("EN") ? "Hello!\n\nAttached to this e-mail you'll find the PDF report regarding the gender equality situation in the United States.\nWhere did this email come from?\nUsername: " + System.getProperty("user.name") + "\nOperating System: " + System.getProperty("os.name") + "\nTimezone: " + System.getProperty("user.timezone") + "\n\nHave a nice day!" : Main.language.equals("FR") ? "Bonjour !\n\nAttaché a ce courriel électronique vous trouverez le rapport PDF regardant la situation de l'égalité entre les genres dans les États-Unis.\nD'où à arrivé ce courriel ?\nNom d'utilisateur: " + System.getProperty("user.name") + "\nSystème d'exploitation: " + System.getProperty("os.name") + "\nFuseau horaire: " + System.getProperty("user.timezone") + "\n\nBonne journée !" : "Bună ziua!\n\nAtașat acestui e-mail găsiți raportul PDF despre situația egalității între genuri în Statele Unite.\nDe unde a venit acest mail?\nNume de utilizator: " + System.getProperty("user.name") + "\nSistem de Operare: " + System.getProperty("os.name") + "\nFus orar: " + System.getProperty("user.timezone") + "\n\nO zi bună!");
            mail.attach(pdfDocument);
            mail.send();
            Platform.runLater(() -> {
                Alert emailSent = new Alert(Alert.AlertType.INFORMATION);
                emailSent.setTitle(Main.language.equals("EN") ? "Report sent" : Main.language.equals("FR") ? "Rapport envoyé" : "Raport trimis");
                emailSent.setHeaderText(Main.language.equals("EN") ? "The report was sent to the " + Main.email + " e-mail address!\nAlso check the junk folder, in case you don't see it in your inbox!" : Main.language.equals("FR") ? "Le rapport a été envoyé a l'adresse électronique " + Main.email + " !\nVérifiez aussi la boîte des courriels indésirables, si vous ne le voyez pas dans votre boîte de réception !" : "Raportul a fost trimis la adresa de e-mail " + Main.email + "!\nVă rugăm verificați si cutia de spam, în cazul în care nu găsiți mail-ul în inbox!");
                emailSent.getDialogPane().setMaxWidth(750);
                emailSent.initStyle(StageStyle.UNDECORATED);
                if (Main.displayMode.equals("Dark")) {
                    emailSent.getDialogPane().getStylesheets().clear();
                    emailSent.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/DarkMode.css")).toExternalForm());
                    emailSent.getDialogPane().getStyleClass().add("alerts");
                }
                try {
                    Main.getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("MainMenu-" + Main.language + ".fxml")).load()));
                    Main.getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                } catch (IOException ignored) {}
                emailSent.show();
            });
        } catch (EmailException e) {
            Platform.runLater(() -> {
                Alert errorSendingEmail = new Alert(Alert.AlertType.ERROR);
                errorSendingEmail.setTitle(Main.language.equals("EN") ? "Report not sent" : Main.language.equals("FR") ? "Rapport non envoyé" : "Raport netrimis");
                errorSendingEmail.setHeaderText(Main.language.equals("EN") ? "The report couldn't be sent!\nPlease check your internet connection, or wait for a bit then try again!" : Main.language.equals("FR") ? "Le rapport n'a pas pu être envoyé !\nVeuillez vérifier votre connexion internet, ou attendez un peu et réessayez !" : "Raportul nu a putut fi trimis!\nVă rugăm verificați conexiunea la internet, sau așteptați puțin si reîncercați!");
                errorSendingEmail.getDialogPane().setMaxWidth(750);
                errorSendingEmail.initStyle(StageStyle.UNDECORATED);
                if (Main.displayMode.equals("Dark")) {
                    errorSendingEmail.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/DarkMode.css")).toExternalForm());
                    errorSendingEmail.getDialogPane().getStyleClass().add("alerts");
                }
                try {
                    Main.getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("MainMenu-" + Main.language + ".fxml")).load()));
                    Main.getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                } catch (IOException ignored) {}
                errorSendingEmail.show();
            });
        }
    }
}
