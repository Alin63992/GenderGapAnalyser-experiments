package com.gendergapanalyser.gendergapanalyser;

import animatefx.animation.FadeOut;
import com.itextpdf.text.DocumentException;
import eu.iamgio.animated.transition.AnimatedThemeSwitcher;
import eu.iamgio.animated.transition.Animation;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class SendEmailInBackground implements Runnable {
    @Override
    public void run() {
        //If the PDF report doesn't exist,
        // it was generated in a different language than the current one of the application,
        // it was generated without including the generated predictions,
        // or it includes predictions that were deleted, the report is regenerated.
        Path PDFReportPath = Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf");
        if (!Files.exists(PDFReportPath) || Main.processData.changedLanguage || Main.processData.predictionsGenerated && !Main.processData.PDFGeneratedWithPredictions || !Main.processData.predictionsGenerated && Main.processData.PDFGeneratedWithPredictions) {
            try {
                Main.processData.createPDF();
            } catch (IOException | DocumentException ignored) {}
        }

        //Checking to see if this thread is interrupted and stopping it if it is
        if (Thread.currentThread().isInterrupted()) {
            try {
                Files.delete(PDFReportPath);
            } catch (IOException ignored) {}
            return;
        }

        //Building the email attachment containing the PDF report
        EmailAttachment pdfDocument = new EmailAttachment();
        pdfDocument.setPath("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf");
        pdfDocument.setDisposition(EmailAttachment.ATTACHMENT);
        pdfDocument.setDescription(Main.language.equals("EN") ? "Gender equality in the United States" : Main.language.equals("FR") ? "L'égalité entre les genres dans les États-Unis" : "Egalitatea între genuri în Statele Unite");

        //Checking to see if this thread is interrupted and stopping it if it is
        if (Thread.currentThread().isInterrupted()) {
            try {
                Files.delete(PDFReportPath);
            } catch (IOException ignored) {}
            return;
        }

        //Starting the email
        MultiPartEmail mail = new MultiPartEmail();
        //Setting the email server's address (using Outlook)
        mail.setHostName("smtp-mail.outlook.com");
        try {
            //Setting the name of the recipient to be the local part of the email address (before the '@' symbol)
            mail.addTo(Main.email, Main.email.split("@")[0]);
            //Setting the Outlook server's SMTP port number
            mail.setSmtpPort(587);
            //Enabling TLS as required by the Outlook server
            mail.setStartTLSEnabled(true);
            //Setting the email address and the password of the email account
            // that sends the email containing the PDF
            mail.setAuthentication(Main.outgoingAccountEmail, Main.outgoingAccountPassword);
            //Setting the sender email address
            mail.setFrom(Main.outgoingAccountEmail);

            //Checking to see if this thread is interrupted and stopping it if it is
            if (Thread.currentThread().isInterrupted()) {
                try {
                    Files.delete(PDFReportPath);
                } catch (IOException ignored) {}
                return;
            }

            //Setting the subject of the email
            mail.setSubject(Main.language.equals("EN") ? "The PDF report regarding the gender equality situation in the United States" : Main.language.equals("FR") ? "Le rapport PDF regardant la situation de l'égalité entre les genres dans les États-Unis" : "Raportul PDF despre situația egalității între genuri în Statele Unite");
            //Setting the body of the email
            // describing what the email is about and including the 3 characteristics specific to the computer the app is running on,
            // for which the user consented before starting this thread,
            // to verify the source of the data
            mail.setMsg(Main.language.equals("EN") ? "Hello!\n\nAttached to this e-mail you'll find the PDF report regarding the gender equality situation in the United States.\nWhere did this email come from?\nUsername: " + System.getProperty("user.name") + "\nOperating System: " + System.getProperty("os.name") + "\nTimezone: " + System.getProperty("user.timezone") + "\n\nHave a nice day!" : Main.language.equals("FR") ? "Bonjour !\n\nAttaché a ce courriel électronique vous trouverez le rapport PDF regardant la situation de l'égalité entre les genres dans les États-Unis.\nD'où à arrivé ce courriel ?\nNom d'utilisateur: " + System.getProperty("user.name") + "\nSystème d'exploitation: " + System.getProperty("os.name") + "\nFuseau horaire: " + System.getProperty("user.timezone") + "\n\nBonne journée !" : "Bună ziua!\n\nAtașat acestui e-mail găsiți raportul PDF despre situația egalității între genuri în Statele Unite.\nDe unde a venit acest mail?\nNume de utilizator: " + System.getProperty("user.name") + "\nSistem de Operare: " + System.getProperty("os.name") + "\nFus orar: " + System.getProperty("user.timezone") + "\n\nO zi bună!");
            //Attaching the attachment containing the report to the email message
            mail.attach(pdfDocument);
            //Lift off! (Sending the email)
            mail.send();

            //Checking to see if this thread is interrupted and stopping it if it is
            if (Thread.currentThread().isInterrupted()) {
                try {
                    Files.delete(PDFReportPath);
                } catch (IOException ignored) {}
                return;
            }

            //Running graphical instructions on the JavaFX thread
            Platform.runLater(() -> {
                //Building and showing an alert that tells the user the email was sent successfully
                Alert emailSent = new Alert(Alert.AlertType.INFORMATION);
                emailSent.setTitle(Main.language.equals("EN") ? "Report sent" : Main.language.equals("FR") ? "Rapport envoyé" : "Raport trimis");
                emailSent.setHeaderText(Main.language.equals("EN") ? "The report was sent to the " + Main.email + " e-mail address!\nAlso check the junk folder, in case you don't see it in your inbox!" : Main.language.equals("FR") ? "Le rapport a été envoyé a l'adresse électronique " + Main.email + " !\nVérifiez aussi la boîte des courriels indésirables, si vous ne le voyez pas dans votre boîte de réception !" : "Raportul a fost trimis la adresa de e-mail " + Main.email + "!\nVă rugăm verificați si cutia de spam, în cazul în care nu găsiți mail-ul în inbox!");
                emailSent.getDialogPane().setMaxWidth(750);
                emailSent.initStyle(StageStyle.UNDECORATED);
                //If the application is set to dark mode, the alert is dark too
                emailSent.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                emailSent.getDialogPane().getStyleClass().add("alerts");
                //Setting the alert icon
                // that's going to be shown on the taskbar to the Information free icon created by Anggara,
                // published on the flaticon website
                // (https://www.flaticon.com/free-icon/information_9195785)
                try {
                    ((Stage)emailSent.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/information.png")));
                } catch (FileNotFoundException ignored) {}

                //Checking to see if this thread is interrupted and stopping it if it is
                if (Thread.currentThread().isInterrupted()) {
                    try {
                        Files.delete(PDFReportPath);
                    } catch (IOException ignored) {}
                    return;
                }

                //Reloading the main menu screen so the wait screen is removed and the menu is usable again
                try {
                    Main.getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("MainMenu-" + Main.language + ".fxml")).load()));
                    Main.getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                    AnimatedThemeSwitcher switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                    switchTheme.init();
                } catch (IOException ignored) {}
                emailSent.show();
            });
        } catch (EmailException e) {
            e.printStackTrace();
            //Checking to see if this thread is interrupted and stopping it if it is
            if (Thread.currentThread().isInterrupted()) {
                try {
                    Files.delete(PDFReportPath);
                } catch (IOException ignored) {}
                return;
            }

            //If there was a problem sending the email, an alert is built and shown,
            // informing a user that there was a problem and the email was not sent
            Platform.runLater(() -> {
                Alert errorSendingEmail = new Alert(Alert.AlertType.ERROR);
                errorSendingEmail.setTitle(Main.language.equals("EN") ? "Report not sent" : Main.language.equals("FR") ? "Rapport non envoyé" : "Raport netrimis");
                errorSendingEmail.setHeaderText(Main.language.equals("EN") ? "The report couldn't be sent!\nPlease check your internet connection, or wait for a bit then try again!" : Main.language.equals("FR") ? "Le rapport n'a pas pu être envoyé !\nVeuillez vérifier votre connexion internet, ou attendez un peu et réessayez !" : "Raportul nu a putut fi trimis!\nVă rugăm verificați conexiunea la internet, sau așteptați puțin si reîncercați!");
                errorSendingEmail.getDialogPane().setMaxWidth(750);
                errorSendingEmail.initStyle(StageStyle.UNDECORATED);
                errorSendingEmail.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                errorSendingEmail.getDialogPane().getStyleClass().add("alerts");
                //Setting the alert icon
                // that's going to be shown on the taskbar to the Close free icon created by Alfredo Hernandez,
                // published on the flaticon website
                // (https://www.flaticon.com/free-icon/close_463612)
                try {
                    ((Stage)errorSendingEmail.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/close.png")));
                } catch (FileNotFoundException ignored) {}

                //Checking to see if this thread is interrupted and stopping it if it is
                if (Thread.currentThread().isInterrupted()) {
                    try {
                        Files.delete(PDFReportPath);
                    } catch (IOException ignored) {}
                    return;
                }

                try {
                    Main.getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("MainMenu-" + Main.language + ".fxml")).load()));
                    Main.getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                    AnimatedThemeSwitcher switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                    switchTheme.init();
                } catch (IOException ignored) {}
                errorSendingEmail.show();
            });
        }
        Main.outgoingAccountEmail = "";
        Main.outgoingAccountPassword = "";
    }
}
