package com.gendergapanalyser.gendergapanalyser;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;

public class ManageSplashScreen implements Initializable {
    public AnchorPane titleBar;
    public ImageView appIconImageView;
    public ImageView loadingCircleImageView;
    public Text recoveryText;
    public Text updateText;
    public Text noCloseWindowText;
    public Text cleanupText;
    public Text rollbackText;
    public static double dragX;
    public static double dragY;
    private final Runnable refreshSplashScreenMessages = () -> {
        while (!Main.interruptThreads) {
            switch (Main.appState) {
                case "StartupStage-Recovery" -> {
                    if (!recoveryText.isVisible()) {
                        toggleUpdateText(false);
                        toggleRollbackText(false);
                        toggleCleanupText(false);
                        toggleRecoveryText(true);
                    }
                }
                case "UpdateStage-Update" -> {
                    if (!updateText.isVisible()) {
                        toggleRecoveryText(false);
                        toggleRollbackText(false);
                        toggleCleanupText(false);
                        toggleUpdateText(true);
                    }
                }
                case "UpdateStage-Rollback" -> {
                    if (!rollbackText.isVisible()) {
                        toggleRecoveryText(false);
                        toggleUpdateText(false);
                        toggleCleanupText(false);
                        toggleRollbackText(true);
                    }
                }
                case "UpdateStage-Cleanup" -> {
                    if (!cleanupText.isVisible()) {
                        toggleRecoveryText(false);
                        toggleUpdateText(false);
                        toggleRollbackText(false);
                        toggleCleanupText(true);
                    }
                }
                case "Normal" -> {
                    if (recoveryText.isVisible()) {
                        toggleRecoveryText(false);
                        toggleUpdateText(false);
                        toggleRollbackText(false);
                        toggleCleanupText(false);
                    }
                }
            }
        }
    };
    private final Thread keepSplashScreenUpdated = new Thread(refreshSplashScreenMessages);

    @FXML
    private void exitApp() {
        Main.exitAppMain();
    }

    //Function ran when the minimisation button is clicked
    @FXML
    private void minimizeWindow() {
        Main.minimizeWindowMain();
    }

    public void toggleUpdateText(boolean isVisible) {
        updateText.setVisible(isVisible);
        noCloseWindowText.setVisible(isVisible);
    }

    public void toggleRecoveryText(boolean isVisible) {
        recoveryText.setVisible(isVisible);
        noCloseWindowText.setVisible(isVisible);
    }

    public void toggleRollbackText(boolean isVisible) {
        rollbackText.setVisible(isVisible);
        noCloseWindowText.setVisible(isVisible);
    }

    public void toggleCleanupText(boolean isVisible) {
        cleanupText.setVisible(isVisible);
        noCloseWindowText.setVisible(isVisible);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Making the window movable when dragging the embedded title bar
        titleBar.setOnMousePressed(event -> {
            dragX = Main.getCurrentStage().getX() - event.getScreenX();
            dragY = Main.getCurrentStage().getY() - event.getScreenY();
        });
        titleBar.setOnMouseDragged(event -> {
            Main.getCurrentStage().setX(event.getScreenX() + dragX);
            Main.getCurrentStage().setY(event.getScreenY() + dragY);
        });

        try {
            if (Main.displayMode.equals("Dark"))
                appIconImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png")));
            loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + Main.displayMode + ".gif")));
        } catch (FileNotFoundException ignored) {}

        keepSplashScreenUpdated.start();
    }
}
