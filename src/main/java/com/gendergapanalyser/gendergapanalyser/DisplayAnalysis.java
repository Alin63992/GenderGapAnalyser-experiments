package com.gendergapanalyser.gendergapanalyser;

import animatefx.animation.*;
import eu.iamgio.animated.transition.AnimatedSwitcher;
import eu.iamgio.animated.transition.AnimatedThemeSwitcher;
import eu.iamgio.animated.transition.Animation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.ResourceBundle;

public class DisplayAnalysis implements Initializable {
    @FXML
    private Hyperlink voidLink;
    @FXML
    private AnimatedSwitcher promptAnimator;
    @FXML
    private AnimatedSwitcher darkOverlayAnimator;
    @FXML
    private ImageView loadingCircleImageView;
    @FXML
    private AnchorPane backgroundOperations;
    @FXML
    private Rectangle darkOverlay;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private ChoiceBox<String> languagePicker;
    @FXML
    private ChoiceBox<String> currencyPicker;
    @FXML
    private ImageView lightModeButtonGlyph;
    @FXML
    private ImageView darkModeButtonGlyph;
    @FXML
    private ImageView wageGapIcon;
    @FXML
    private HBox predictionControls;
    @FXML
    private ToggleButton includePredictionsToggle;
    @FXML
    private ScrollPane womenAnalysisPane;
    @FXML
    private ScrollPane menAnalysisPane;
    @FXML
    private ScrollPane wageGapAnalysisPane;
    private AnimatedThemeSwitcher switchTheme;

    //Function triggered when the user wants to go back to the main menu
    @FXML
    private void backToMainMenu() throws IOException {
        //Creating a new window for the main menu, since the main menu is a different-sized page than the current page and the switching should be smooth and pleasant for the user experience
        Stage mainMenu = new Stage();
        mainMenu.initStyle(StageStyle.UNDECORATED);
        //Setting the new window's title
        mainMenu.setTitle(Main.language.equals("EN") ? "Main Menu" : Main.language.equals("FR") ? "Menu Principal" : "Meniu Principal");
        //Setting the main menu page to be shown on the new window
        mainMenu.setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/MainMenu-" + Main.language + ".fxml")).load()));
        mainMenu.getScene().getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        //Making the new window not resizeable so that the user doesn't change the size of the window and the elements of the page won't look out of place
        mainMenu.setResizable(false);
        mainMenu.centerOnScreen();
        //Opening the new window
        mainMenu.show();
        //Closing the interpretations window, which at this point is still considered as the currently open window
        Main.getCurrentStage().close();
        //Setting the new main menu window as the currently open window
        Main.setCurrentStage(mainMenu);
        switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
        switchTheme.init();
        //Setting the app icon
        // that's going to be shown on the taskbar to the Gender Fluid free icon created by Vitaly Gorbachev,
        // published on the flaticon website
        // (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
        Main.getCurrentStage().getIcons().setAll(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
    }

    //Function triggered when the user wants to display the graph page
    @FXML
    private void goToGraphPage() throws IOException {
        AnimatedSwitcher as = new AnimatedSwitcher(new Animation(new SlideInRight()).setSpeed(3), new Animation(new FadeOutLeft()));
        Scene scene = new Scene(new Pane(as));
        as.of(Main.getCurrentStage().getScene().getRoot());
        as.setChild(new FXMLLoader(getClass().getResource("AppScreens/DisplayEvolutionGraph-" + Main.language + ".fxml")).load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        //Changing the title of the current stage
        Main.getCurrentStage().setTitle(Main.language.equals("EN") ? "Evolution Graph" : Main.language.equals("FR") ? "Graphe d'Évolution" : "Grafic de Evoluție");
        //Displaying the graph page on the current stage
        Main.getCurrentStage().setScene(scene);
        switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
        switchTheme.init();
    }

    //Function that executes the app closing routine
    @FXML
    private void exitApp() {
        Main.exitAppMain();
    }

    //Function ran when the minimize button is clicked
    @FXML
    private void minimizeWindow() {
        Main.minimizeWindowMain();
    }

    //Function that toggles the app display mode between light mode and dark mode
    @FXML
    private void toggleDisplayMode() throws IOException {
        Main.displayMode = Main.displayMode.equals("Light") ? "Dark" : "Light";
        BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
        buildUserSettings.write("DisplayMode=" + Main.displayMode + "\nLanguage=" + Main.language + "\nCurrency=" + Main.currency + "\nExchangeRateLastUpdated=" + Main.exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + Main.exchangeRateEUR + "\nExchangeRateToRON=" + Main.exchangeRateRON);
        buildUserSettings.close();
        if (Main.processData.predictionsGenerated) Main.processData.createSalaryGraphWithPredictionsForEverybody();
        Main.processData.createSalaryGraphForEverybody();
        Main.getCurrentStage().getScene().getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        if (Main.displayMode.equals("Dark")) {
            darkModeButtonGlyph.setFitHeight(50);
            lightModeButtonGlyph.setFitHeight(35);
            //Also changing the wage gap icon
            wageGapIcon.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png")));
        }
        else {
            lightModeButtonGlyph.setFitHeight(50);
            darkModeButtonGlyph.setFitHeight(35);
            //Also changing the wage gap icon
            wageGapIcon.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
        }
        voidLink.requestFocus();
    }

    //Function used when initializing the screen by populating the scroll panes with the interpretations
    @FXML
    private void togglePredictions() {
        //If the user opts to include the generated predictions in the interpretations
        if (includePredictionsToggle.isSelected()) {
            //Populating the analysis panes
            //Creating a label with the women's salary evolution interpretation that includes the generated predictions
            Label womenAnalysis = new Label(Main.processData.womenAnalysisWithPredictions);
            //Making it big enough to be comfortable to read
            womenAnalysis.setStyle("-fx-font-family: Calibri");
            womenAnalysis.setStyle("-fx-font-size: 18");
            //Making it span across the entire width of the women analysis scroll pane, not including the vertical scroll bar
            womenAnalysis.setPrefWidth(womenAnalysisPane.getPrefWidth() - 35);
            //Making it wrap so that the analysis doesn't get truncated and shown on a single line
            womenAnalysis.setWrapText(true);
            //Setting it in the women's salaries evolution interpretation scroll pane
            womenAnalysisPane.setContent(womenAnalysis);
            //Creating a different label for the men's salaries and wage gap evolutions interpretations that include the generated predictions, doing the same as above but setting each label in its respective scroll pane
            Label menAnalysis = new Label(Main.processData.menAnalysisWithPredictions);
            menAnalysis.setStyle("-fx-font-family: Calibri");
            menAnalysis.setStyle("-fx-font-size: 18");
            menAnalysis.setPrefWidth(menAnalysisPane.getPrefWidth() - 35);
            menAnalysis.setWrapText(true);
            menAnalysisPane.setContent(menAnalysis);
            Label gapAnalysis = new Label(Main.processData.wageGapAnalysisWithPredictions);
            gapAnalysis.setStyle("-fx-font-family: Calibri");
            gapAnalysis.setStyle("-fx-font-size: 18");
            gapAnalysis.setPrefWidth(wageGapAnalysisPane.getPrefWidth() - 35);
            gapAnalysis.setWrapText(true);
            wageGapAnalysisPane.setContent(gapAnalysis);

            includePredictionsToggle.setText(Main.language.equals("EN") ? "Include predictions" : Main.language.equals("FR") ? "Inclure les prédictions" : "Include predicțiile");
        }
        //If not
        else {
            //Populating the analysis panes
            //Creating a label with the women's salaries evolution interpretation
            Label womenAnalysis = new Label(Main.processData.womenAnalysis);
            //Making it big enough to be comfortable to read
            womenAnalysis.setStyle("-fx-font-size: 18");
            //Making it span across the entire width of the women analysis scroll pane, not including the vertical scroll bar
            womenAnalysis.setPrefWidth(womenAnalysisPane.getPrefWidth() - 35);
            //Making it wrap so that the analysis doesn't get truncated and shown on a single line
            womenAnalysis.setWrapText(true);
            //Setting it in the women's salaries evolution interpretation scroll pane
            womenAnalysisPane.setContent(womenAnalysis);
            //Creating a different label for the men's salaries and wage gap evolution interpretations, doing the same as above but setting each label in its respective scroll pane
            Label menAnalysis = new Label(Main.processData.menAnalysis);
            menAnalysis.setStyle("-fx-font-size: 18");
            menAnalysis.setPrefWidth(menAnalysisPane.getPrefWidth() - 35);
            menAnalysis.setWrapText(true);
            menAnalysisPane.setContent(menAnalysis);
            Label gapAnalysis = new Label(Main.processData.wageGapAnalysis);
            gapAnalysis.setStyle("-fx-font-size: 18");
            gapAnalysis.setPrefWidth(wageGapAnalysisPane.getPrefWidth() - 35);
            gapAnalysis.setWrapText(true);
            wageGapAnalysisPane.setContent(gapAnalysis);

            includePredictionsToggle.setText(Main.language.equals("EN") ? "Exclude predictions" : Main.language.equals("FR") ? "Exclure les prédictions" : "Exclude predicțiile");
        }
    }

    //Function that executes the routine that deletes the generated predictions and then reloads the analysis page with the data from the dataset
    @FXML
    private void discardPredictions() throws IOException {
        Main.processData.discardPredictions();
        Main.getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/Analysis-" + Main.language + ".fxml")).load()));
        Main.getCurrentStage().getScene().getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        promptAnimator.setIn(new Animation(new ZoomIn()).setSpeed(3));
        promptAnimator.setOut(new Animation(new ZoomOut()).setSpeed(10));
        darkOverlayAnimator.setIn(new Animation(new FadeIn()).setSpeed(3));
        darkOverlayAnimator.setOut(new Animation(new ZoomOut()).setSpeed(10));
        titleBar.setOnMousePressed(event -> {
            Main.dragX = Main.getCurrentStage().getX() - event.getScreenX();
            Main.dragY = Main.getCurrentStage().getY() - event.getScreenY();
        });
        titleBar.setOnMouseDragged(event -> {
            Main.getCurrentStage().setX(event.getScreenX() + Main.dragX);
            Main.getCurrentStage().setY(event.getScreenY() + Main.dragY);
        });
        //Setting up the language picker
        languagePicker.setItems(FXCollections.observableArrayList(Main.languages));
        switch (Main.language) {
            case "EN" -> languagePicker.setValue(Main.languages[0]);
            case "FR" -> languagePicker.setValue(Main.languages[1]);
            case "RO" -> languagePicker.setValue(Main.languages[2]);
        }
        //When selecting another language from the language picker...
        languagePicker.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
            //Updating the language with the newly picked one
            Main.language = Main.languagesShort[newValue.intValue()];
            //Setting the boolean variable used by DataProcessing.createPDF method to true
            // so that the method generates a new PDF document in a new language
            Main.changedLanguage = true;
            //Checking if the currency has been updated (default is -1.0)
            if (Main.exchangeRateEUR != -1.0 && Main.exchangeRateRON != -1.0) {
                //Updating the currency with the one associated with the language
                Main.currency = Main.currencies[newValue.intValue()];
                //Setting the boolean variable used by DataProcessing.createPDF method to true
                // so that the method generates a new PDF document with the new currency
                Main.changedCurrency = true;
            }
            else
                currencyPicker.setValue(Main.currencies[0]);
            Runnable rebuildResources = () -> {
                try {
                    BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                    buildUserSettings.write("DisplayMode=" + Main.displayMode + "\nLanguage=" + Main.language + "\nCurrency=" + Main.currency + "\nExchangeRateLastUpdated=" + Main.exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + Main.exchangeRateEUR + "\nExchangeRateToRON=" + Main.exchangeRateRON);
                    buildUserSettings.close();
                    //Checking if the currencies have ever been updated
                    if (Main.exchangeRateEUR != -1.0 && Main.exchangeRateRON != -1.0) {
                        //Creating the usable dataset,
                        // graphs and interpretations again so that it uses the new language and currency
                        Main.processData.prepareData();
                    }
                    else {
                        //Just regenerating the graphs and interpretations to use the new language
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.performAnalysis();
                    }
                    //Recreating predictions and prediction graphs so that they use the newly set language
                    if (Main.processData.predictionsGenerated) {
                        Main.processData.predictEvolutions(Main.predictionValue);
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                    }
                } catch (IOException ignored) {}

                Platform.runLater(() -> {
                    try {
                        Scene analysisScene = new Scene(new FXMLLoader(getClass().getResource("AppScreens/Analysis-" + Main.language + ".fxml")).load());
                        analysisScene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                        Main.getCurrentStage().setScene(analysisScene);
                    } catch (IOException ignored) {}
                    //Changing the title of the current stage
                    Main.getCurrentStage().setTitle(Main.language.equals("EN") ? "Interpretations" : Main.language.equals("FR") ? "Interprétations" : "Interpretări");
                    switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                    switchTheme.init();
                });
            };
            promptAnimator.setChild(new Pane(backgroundOperations));
            darkOverlayAnimator.setChild(new Pane(darkOverlay));
            try {
                loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + Main.displayMode + ".gif")));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            backgroundOperations.setVisible(true);
            darkOverlay.setVisible(true);
            new Thread(rebuildResources).start();
        }));

        //Setting up the currency picker
        currencyPicker.setItems(FXCollections.observableArrayList(Main.currencies));
        switch (Main.currency) {
            case "USD" -> currencyPicker.setValue(Main.currencies[0]);
            case "EUR" -> currencyPicker.setValue(Main.currencies[1]);
            case "RON" -> currencyPicker.setValue(Main.currencies[2]);
        }
        //Checking if the currencies were ever updated or if they are set to their default -1.0 value
        if (Main.exchangeRateEUR != -1.0 && Main.exchangeRateRON != -1.0) {
            //When selecting another language from the currency picker...
            currencyPicker.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
                //Updating the currency with the newly picked one
                Main.currency = Main.currencies[newValue.intValue()];
                //Setting the boolean variable used by DataProcessing.createPDF method to true so that the method generates a new PDF document with the new currency
                Main.changedCurrency = true;
                Runnable rebuildResources = () -> {
                    try {
                        BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                        buildUserSettings.write("DisplayMode=" + Main.displayMode + "\nLanguage=" + Main.language + "\nCurrency=" + Main.currency + "\nExchangeRateLastUpdated=" + Main.exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + Main.exchangeRateEUR + "\nExchangeRateToRON=" + Main.exchangeRateRON);
                        buildUserSettings.close();
                        //Creating the usable dataset, graphs and interpretations again so that it uses the new currency
                        Main.processData.prepareData();
                        //Recreating predictions and prediction graphs so that they use the newly set currency
                        if (Main.processData.predictionsGenerated) {
                            Main.processData.predictEvolutions(Main.predictionValue);
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        }
                    } catch (IOException ignored) {
                    }

                    Platform.runLater(() -> {
                        try {
                            Scene analysisScene = new Scene(new FXMLLoader(getClass().getResource("AppScreens/Analysis-" + Main.language + ".fxml")).load());
                            analysisScene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                            Main.getCurrentStage().setScene(analysisScene);
                        } catch (IOException ignored) {
                        }
                        switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                        switchTheme.init();
                    });
                };
                promptAnimator.setChild(new Pane(backgroundOperations));
                darkOverlayAnimator.setChild(new Pane(darkOverlay));
                try {
                    loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + Main.displayMode + ".gif")));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                backgroundOperations.setVisible(true);
                darkOverlay.setVisible(true);
                new Thread(rebuildResources).start();
            }));
        }
        else currencyPicker.setDisable(true);

        //Setting up the theme toggle
        if (Main.displayMode.equals("Dark")) {
            darkModeButtonGlyph.setFitHeight(50);
            lightModeButtonGlyph.setFitHeight(35);
            //Also displaying the wage gap icon
            try {
                wageGapIcon.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png")));
            } catch (FileNotFoundException ignored) {}
        }
        else {
            lightModeButtonGlyph.setFitHeight(50);
            darkModeButtonGlyph.setFitHeight(35);
            //Also displaying the wage gap icon
            try {
                wageGapIcon.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
            } catch (FileNotFoundException ignored) {}
        }

        if (Main.processData.predictionsGenerated) {
            //Disabling the prediction controls
            predictionControls.setDisable(false);

            //Selecting the include predictions checkbox
            includePredictionsToggle.setSelected(true);
            togglePredictions();
        }
        else {
            //Disabling the prediction controls
            predictionControls.setDisable(true);

            //Deselecting the include predictions checkbox
            includePredictionsToggle.setSelected(false);
            togglePredictions();
        }
    }
}
