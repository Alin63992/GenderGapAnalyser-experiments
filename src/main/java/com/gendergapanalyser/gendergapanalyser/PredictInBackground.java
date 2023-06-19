package com.gendergapanalyser.gendergapanalyser;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class PredictInBackground implements Runnable {
    @Override
    public void run() {
        try {
            //Generating predictions
            Main.processData.predictEvolutions(Main.predictionValue);

            //If this thread is interrupted from the outside, we stop it
            if (Thread.currentThread().isInterrupted()) return;

            Platform.runLater(() -> {
                //Preparing a new non-resizable window with a title, that displays the graph screen
                Stage graphStage = new Stage();
                graphStage.initStyle(StageStyle.UNDECORATED);
                graphStage.setTitle(Main.language.equals("EN") ? "Evolution Graph" : Main.language.equals("FR") ? "Graphe d'Évolution" : "Grafic de Evoluție");
                try {
                    graphStage.setScene(new Scene(new FXMLLoader(getClass().getResource("DisplayEvolutionGraph-" + Main.language + ".fxml")).load()));
                    graphStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                    graphStage.setResizable(false);
                    graphStage.centerOnScreen();
                    //Opening the new graphs window
                    graphStage.show();
                    //Closing the current window (in this case, the main menu window)
                    Main.getCurrentStage().close();
                    //Setting the graph window as the currently open window
                    Main.setCurrentStage(graphStage);
                    //Setting the app icon that's going to be shown on the title bar and taskbar to the Gender Fluid free icon created by Vitaly Gorbachev, published on the flaticon website (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
                    Main.getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/AppIcon.png")));
                } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }
}
