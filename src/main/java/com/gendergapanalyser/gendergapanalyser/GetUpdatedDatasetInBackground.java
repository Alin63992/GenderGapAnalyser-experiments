package com.gendergapanalyser.gendergapanalyser;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class GetUpdatedDatasetInBackground implements Runnable {
    @Override
    public void run() {
        //Trying to download the Median annual earnings by sex, race and Hispanic ethnicity dataset file from the U.S. Department of Labor server (https://www.dol.gov/agencies/wb/data/earnings/median-annual-sex-race-hispanic-ethnicity)
        try {
            //Downloading the file
            FileUtils.copyURLToFile(URI.create("https://public.tableau.com/views/Earningsbysexraceethnicity/Table.csv?%3Adisplay_static_image=y&%3AbootstrapWhenNotified=true&%3Aembed=true&%3Alanguage=en-US&:embed=y&:showVizHome=n&:apiID=host0").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/0DownloadedDataset.csv"), 500, 10000);

            //Setting the downloaded dataset path
            Path downloadedDatasetPath = Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/0DownloadedDataset.csv");

            //Checking to see if this thread is interrupted and stopping it if it is
            if (Thread.currentThread().isInterrupted()) {
                Files.delete(downloadedDatasetPath);
                return;
            }

            //Setting the PDF report path
            Path PDFReportPath = Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf");

            //Booleans used to store the generation states of a PDF report and of predictions, used to determine whether to show the refresh information alert below or not
            boolean predictionsGenerated = Main.processData.predictionsGenerated;
            boolean PDFGenerated = Files.exists(PDFReportPath);

            //Deleting the old and outdated PDF report, if it exists
            try {
                Files.delete(PDFReportPath);
            } catch (IOException ignored) {}

            //Checking to see if this thread is interrupted and stopping it if it is
            if (Thread.currentThread().isInterrupted()) {
                Files.delete(downloadedDatasetPath);
                return;
            }

            //Reprocessing data after the new dataset has been downloaded
            Main.processData = new DataProcessing();
            Main.processData.prepareData();

            //Checking to see if this thread is interrupted and stopping it if it is
            if (Thread.currentThread().isInterrupted()) {
                Files.delete(downloadedDatasetPath);
                return;
            }

            if (Main.getCurrentStage().getTitle() != null && !Main.getCurrentStage().getTitle().equals(Main.language.equals("EN") ? "Main Menu" : Main.language.equals("FR") ? "Menu Principal" : "Meniu Principal") || predictionsGenerated || PDFGenerated) {
                Platform.runLater(() -> {
                    //Preparing the alert informing the user that new data has been found and applied
                    Alert refreshInfo = new Alert(Alert.AlertType.INFORMATION);
                    refreshInfo.setTitle(Main.language.equals("EN") ? "Got new data" : Main.language.equals("FR") ? "Nouveaux données obtenus" : "Date noi obținute");
                    refreshInfo.setHeaderText(Main.language.equals("EN") ? "Got new data" : Main.language.equals("FR") ? "Nouveaux données obtenus" : "Date noi obținute");
                    refreshInfo.setContentText(Main.language.equals("EN") ? "Updated statistics were found and the app has been refreshed with them applied. If you generated a PDF report or predictions, they were deleted as they're no longer current." : Main.language.equals("FR") ? "Des nouveaux statistiques ont été trouvés et l'application a été rafraîchie avec eux appliquées. Si vous avez généré un rapport PDF ou des prédictions, ils étaient effacés à cause du fait qu'ils ne sont plus actuelles." : "Noi statistici au fost găsite iar aplicația a fost reîmprospătată cu ele aplicate. Dacă ați generat un raport PDF sau predicții, acestea au fost șterse deoarece nu mai sunt de actualitate.");
                    //Replacing the current window with a new one containing the main menu screen
                    Stage mainMenu = new Stage();
                    mainMenu.initStyle(StageStyle.UNDECORATED);
                    //Setting the new window's title
                    mainMenu.setTitle(Main.language.equals("EN") ? "Main Menu" : Main.language.equals("FR") ? "Menu Principal" : "Meniu Principal");
                    try {
                        //Preparing the new window
                        mainMenu.setScene(new Scene(new FXMLLoader(getClass().getResource("MainMenu-" + Main.language + ".fxml")).load()));
                        mainMenu.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                        //Making the new window not resizeable so that the user doesn't change the size of the window and the elements of the page won't look out of place
                        mainMenu.setResizable(false);
                        mainMenu.centerOnScreen();
                        //Opening the new window
                        mainMenu.show();
                        //Closing the currently open window
                        Main.getCurrentStage().close();
                        //Setting the new main menu window as the currently open window
                        Main.setCurrentStage(mainMenu);
                        //Setting the app icon that's going to be shown on the title bar and taskbar to the Gender Fluid free icon created by Vitaly Gorbachev, published on the flaticon website (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
                        Main.getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/AppIcon.png")));
                    } catch (IOException ignored) {}
                    refreshInfo.show();
                });
            }
        } catch (IOException e) {
            Platform.runLater(() -> {
                Main.connectError = !e.getMessage().equals("Read timed out");
                Alert errorDownload = new Alert(Alert.AlertType.ERROR);
                errorDownload.setTitle("Error obtaining updated information");
                errorDownload.setHeaderText("We could not download updated information!");
                errorDownload.setContentText((e.getMessage().equals("Read timed out") ? "Updated information was found, but could not be downloaded quickly enough. " : "We could not connect to the server to obtain updated information quickly enough. ") + "Maybe the server is down at this moment, or there are internet connection problems on your end. Please check your internet connection, then restart the app to try the download again!");
                errorDownload.show();
            });
        }
    }
}
