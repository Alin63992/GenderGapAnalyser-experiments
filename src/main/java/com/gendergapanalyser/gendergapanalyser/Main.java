package com.gendergapanalyser.gendergapanalyser;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import animatefx.animation.ZoomIn;
import animatefx.animation.ZoomOut;
import eu.iamgio.animated.transition.AnimatedSwitcher;
import eu.iamgio.animated.transition.AnimatedThemeSwitcher;
import eu.iamgio.animated.transition.Animation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import one.jpro.platform.mdfx.MarkdownView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public class Main extends Application implements Initializable {
    private static Stage currentStage;
    public ToggleButton sourcesToggle;
    public ToggleButton creditsToggle;
    public AnchorPane contentCredits;
    public Hyperlink USDeptOfLaborHyperlink;
    public Hyperlink ERAHyperlink;
    public ImageView appIconCreditsImageView;
    public ImageView appIconImageView;
    public Text updateText;
    protected static double exchangeRateEUR = -1.0;
    protected static double exchangeRateRON = -1.0;
    protected static String[] updateDetails = null;
    public Hyperlink updateLink;
    public AnchorPane updatePrompt;
    public Text noCloseWindowText;
    public Text cleanupText;
    public Text rollbackText;
    @FXML
    private AnchorPane contentSources;
    @FXML
    private Hyperlink voidLink;
    @FXML
    private Hyperlink dataSourcesInfo;
    @FXML
    private AnchorPane missingOutgoingCredentialsPrompt;
    @FXML
    private TextField outgoingEmailField;
    @FXML
    private PasswordField outgoingPasswordField;
    @FXML
    private Text invalidOutgoingEmailWarning;
    @FXML
    private Rectangle darkOverlay;
    @FXML
    private AnimatedSwitcher darkOverlayAnimator;
    @FXML
    private AnimatedSwitcher promptAnimator;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private ChoiceBox<String> languagePicker;
    @FXML
    private ChoiceBox<String> currencyPicker;
    @FXML
    private Button lightModeButton;
    @FXML
    private Button darkModeButton;
    @FXML
    private ImageView lightModeButtonGlyph;
    @FXML
    private ImageView darkModeButtonGlyph;
    @FXML
    private AnchorPane predictionPrompt;
    @FXML
    private AnchorPane discardConfirmation;
    @FXML
    private AnchorPane backgroundOperations;
    @FXML
    private TextField predictionField;
    @FXML
    private Text invalidNumberWarning;
    @FXML
    private AnchorPane emailPrompt;
    @FXML
    private AnchorPane dataSources;
    @FXML
    private Label usDeptOfLaborYearRangeLabel;
    @FXML
    private ImageView ERALogoImageView;
    @FXML
    private Label ERALastUpdatedLabel;
    @FXML
    private TextField emailField;
    @FXML
    private Text invalidEmailWarning;
    @FXML
    private Button graphsButton;
    @FXML
    private Button analysisButton;
    @FXML
    private Button predictButton;
    @FXML
    private Button discardPredictionsButton;
    @FXML
    private Button PDFButton;
    @FXML
    private Button mailButton;
    @FXML
    private ImageView loadingCircleImageView;
    protected FXMLLoader splashScreen;
    protected static ManageSplashScreen splashScreenController;
    MarkdownView mdview = new MarkdownView(){
        @Override
        public void setLink(Node node, String link, String description) {
            node.setOnMouseClicked(e -> {
                try {
                    Desktop.getDesktop().browse(new URI(link));
                } catch (IOException | URISyntaxException ignored) {

                }
            });
        }

        @Override
        public Node generateImage(String url) {
            if(url.equals("node://colorpicker")) {
                return new ColorPicker();
            } else {
                return super.generateImage(url);
            }
        }
    };
    protected static DataProcessing processData;
    protected static String displayMode = "Dark";
    protected static String language = "EN";
    protected static boolean changedLanguage = false;
    protected static boolean changedCurrency = false;
    protected static GregorianCalendar exchangeRateLastUpdated = (GregorianCalendar) GregorianCalendar.getInstance();
    public Text updateReleasedOn;
    public ScrollPane changelogPane;
    protected static String currency = "USD";
    protected static final String[] languages = {"English", "Français", "Română"};
    protected static final String[] languagesShort = {"EN", "FR", "RO"};
    protected static final String[] currencies = {"USD", "EUR", "RON"};
    public static double dragX;
    public static double dragY;
    private static final Thread downloadDataset = new Thread(new GetUpdatedDatasetInBackground());
    protected static int predictionValue = 0;
    protected static String email = "";
    protected static String outgoingAccountEmail = "";
    protected static String outgoingAccountPassword = "";
    private boolean fileNotFoundOnGit = false;
    public SplitMenuButton performUpdateButton;
    public Text versionText;
    @FXML
    private Text recoveryText;
    private boolean updateRouteSelectedOrRecoveryAborted = false;
    private boolean allStartFilesExist = false;

    //String variable where the root of the GitHub link is stored for easier download
    String githubRoot = "https://raw.githubusercontent.com/Alin63992/GenderGapAnalyser/master/src/main/resources/com/gendergapanalyser/gendergapanalyser/";
    private final String appScreensFolder = "src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens";
    private final String emojisFolder = "src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis";
    private final String miscellaneousFolder = "src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous";
    private final String stylesheetsFolder = "src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets";
    public static final File recoveryInProgress = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/.recoveryinprogress");
    protected static final File updateInProgress = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/.updateinprogress");
    protected static final File cleanupInProgress = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/.cleanupinprogress");
    protected static final File rollbackInProgress = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/.rollbackinprogress");
    private AnimatedThemeSwitcher switchTheme;


    //Function used to set the currently open window to be used in the future
    public static void setCurrentStage(Stage s) {
        currentStage = s;
        currentStage.setOnCloseRequest(action -> exitAppMain());
    }

    //Function used by the DisplayEvolutionGraph class to get the currently open window
    public static Stage getCurrentStage() {
        return currentStage;
    }

    //Function used to delete the files the app created on disk while it was running
    public static void cleanUp() {
        //Deleting the Graphs folder
        try {
            FileUtils.deleteDirectory(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs"));
        } catch (IOException ignored) {
        }

        //Deleting the downloaded dataset file (if it exists)
        try {
            Files.delete(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/0DownloadedDataset.csv"));
        } catch (IOException ignored) {
        }

        //Deleting the generated PDF report (if it exists)
        try {
            Files.delete(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf"));
        } catch (IOException ignored) {
        }
    }

    //Function used to delete the generated graphs and quit the app
    public static void exitAppMain() {
        //Killing the thread that downloads the updated dataset if it's still running
        if (downloadDataset.isAlive()) {
            downloadDataset.interrupt();
        }

        if (!updateInProgress.exists()) {
            //Cleaning up the files created during app run
            cleanUp();

            //Closing the window
            getCurrentStage().close();
        }
    }

    //Function that gets run when the close button is pressed
    @FXML
    public void exitApp() {
        Main.exitAppMain();
    }

    //Function used to minimize the app
    public static void minimizeWindowMain() {
        getCurrentStage().setIconified(true);
    }

    //Function ran when the minimization button is clicked
    @FXML
    public void minimizeWindow() {
        Main.minimizeWindowMain();
    }

    //Function that toggles the app display mode between light mode and dark mode
    @FXML
    public void toggleDisplayMode() throws IOException {
        displayMode = displayMode.equals("Light") ? "Dark" : "Light";
        BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
        buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
        buildUserSettings.close();
        if (processData.predictionsGenerated) processData.createSalaryGraphWithPredictionsForEverybody();
        processData.createSalaryGraphForEverybody();
        getCurrentStage().getScene().getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());
        mdview.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/MarkdownFX-" + displayMode + ".css")).toExternalForm());
        ERALogoImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-" + displayMode + ".png")));
        if (displayMode.equals("Dark")) {
            appIconCreditsImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png")));
            darkModeButtonGlyph.setFitHeight(50);
            lightModeButtonGlyph.setFitHeight(35);
        } else {
            appIconCreditsImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
            lightModeButtonGlyph.setFitHeight(50);
            darkModeButtonGlyph.setFitHeight(35);
        }
        voidLink.requestFocus();
    }

    //Function used to close the main menu screen and open the graph screen in a new window
    @FXML
    private void goToGraphPage() throws IOException {
        //Preparing a new non-resizable window with a title, that displays the graph screen
        Stage graphStage = new Stage();
        Scene graphScene = new Scene(new FXMLLoader(getClass().getResource("AppScreens/DisplayEvolutionGraph-" + Main.language + ".fxml")).load());
        graphScene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        graphStage.initStyle(StageStyle.UNDECORATED);
        graphStage.setTitle(language.equals("EN") ? "Evolution Graph" : language.equals("FR") ? "Graphe d'Évolution" : "Grafic de Evoluție");
        graphStage.setScene(graphScene);
        graphStage.setResizable(false);
        graphStage.centerOnScreen();
        //Opening the new graphs window
        graphStage.show();
        //Closing the current window (in this case, the main menu window)
        getCurrentStage().close();
        //Setting the graph window as the currently open window
        setCurrentStage(graphStage);
        switchTheme = new AnimatedThemeSwitcher(getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
        switchTheme.init();
        //Setting the app icon
        // that's going to be shown on the taskbar to the Gender Fluid free icon created by Vitaly Gorbachev,
        // published on the flaticon website
        // (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
        getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
    }

    @FXML
    private void goToAnalysisPage() throws IOException {
        //Preparing a new non-resizable window with a title, that displays the analysis screen
        Stage analysisStage = new Stage();
        Scene analysisScene = new Scene(new FXMLLoader(getClass().getResource("AppScreens/Analysis-" + Main.language + ".fxml")).load());
        analysisScene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        analysisStage.initStyle(StageStyle.UNDECORATED);
        analysisStage.setTitle(language.equals("EN") ? "Interpretations" : language.equals("FR") ? "Interprétations" : "Interpretări");
        analysisStage.setScene(analysisScene);
        analysisStage.setResizable(false);
        analysisStage.centerOnScreen();
        //Opening the new analysis window
        analysisStage.show();
        //Closing the current window (in this case, the main menu window)
        getCurrentStage().close();
        //Setting the analysis window as the currently open window
        setCurrentStage(analysisStage);
        switchTheme = new AnimatedThemeSwitcher(getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
        switchTheme.init();
        //Setting the app icon
        // that's going to be shown on the taskbar to the Gender Fluid free icon created by Vitaly Gorbachev,
        // published on the flaticon website
        // (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
        getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
    }

    //Function used to start the routine that creates the PDF that contains the interpretations, graph with all women's salaries, men's salaries and wage gap evolutions and the dataset
    @FXML
    private void generatePDF() throws IOException {
        loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
        promptAnimator.setChild(new Pane(backgroundOperations));
        darkOverlayAnimator.setChild(new Pane(darkOverlay));
        backgroundOperations.setVisible(true);
        darkOverlay.setVisible(true);
        //Trying to open an already generated report PDF.
        //If there isn't one, it was generated in a different language than the current language of the application,
        // or if it was generated without including predictions when predictions exist or vice versa,
        // the report is regenerated.
        try {
            if (processData.predictionsGenerated && processData.PDFGeneratedWithPredictions && !changedLanguage && !changedCurrency || !processData.predictionsGenerated && !processData.PDFGeneratedWithPredictions && !changedLanguage && !changedCurrency) {
                //Locating an existing generated report PDF
                File existingPDF = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf");
                //Opening it
                Desktop.getDesktop().open(existingPDF);
                promptAnimator.setChild(null);
                darkOverlayAnimator.setChild(null);
                backgroundOperations.setVisible(false);
                darkOverlay.setVisible(false);
            } else throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            new Thread(new GeneratePDFInBackground()).start();
        }
    }

    //Function used to increase (increment) the value of the prediction field, if it is a number and if it is in the [1, 100] range
    @FXML
    private void increaseValue() {
        try {
            //Getting the value of the input field. If it's not a number, the function stops here and goes to the catch block below
            int value = Integer.parseInt(predictionField.getText());
            //If the incremented number is smaller than or equal to 100
            if (value + 1 <= 100) {
                //Setting the input field's value to the incremented number
                predictionField.setText(String.valueOf(value + 1));
                //Hiding the error, if shown
                invalidNumberWarning.setVisible(false);
            }
            //If it isn't, we show the error
            else invalidNumberWarning.setVisible(true);
        } catch (NumberFormatException e) {
            //If the value in the input prompt is not a number, we display an error
            invalidNumberWarning.setVisible(true);
        }
    }

    //Function used to decrease (decrement) the value of the prediction field, if it is a number and if it is in the [1, 100] range
    @FXML
    private void decreaseValue() {
        try {
            //Getting the value of the input field. If it's not a number, the function stops here and goes to the catch block below
            int value = Integer.parseInt(predictionField.getText());
            //If the decremented number is bigger than or equal to 0
            if (value - 1 >= 1) {
                //Setting the input field's value to the incremented number
                predictionField.setText(String.valueOf(value - 1));
                //Hiding the error, if shown
                invalidNumberWarning.setVisible(false);
            }
            //If it isn't, we show the error
            else invalidNumberWarning.setVisible(true);
        } catch (NumberFormatException e) {
            //If the value in the input prompt is not a number, we display an error
            invalidNumberWarning.setVisible(true);
        }
    }

    //Function that changes the focus-traversable property of the main menu items (except the light and the dark mode buttons) when a prompt is showing or not
    private void setFocusTraversableForMainMenuItems(boolean canBeFocused) {
        if (updateLink.isVisible())
            updateLink.setFocusTraversable(canBeFocused);
        dataSourcesInfo.setFocusTraversable(canBeFocused);
        languagePicker.setFocusTraversable(canBeFocused);
        currencyPicker.setFocusTraversable(canBeFocused);
        graphsButton.setFocusTraversable(canBeFocused);
        analysisButton.setFocusTraversable(canBeFocused);
        predictButton.setFocusTraversable(canBeFocused);
        PDFButton.setFocusTraversable(canBeFocused);
        mailButton.setFocusTraversable(canBeFocused);
    }

    //Function used to hide or show the prompt which asks the user for the period of time they need wage prediction for
    @FXML
    private void togglePredictionPrompt() {
        //If the prompt is visible
        if (predictionPrompt.isVisible()) {
            promptAnimator.setChild(null);
            darkOverlayAnimator.setChild(null);
            //We hide the prompt
            predictionPrompt.setVisible(false);
            darkOverlay.setVisible(false);
            //We set the text in the prediction input field back to the default
            predictionField.setText("1");
            //We hide the invalid number warning
            invalidNumberWarning.setVisible(false);
            //Setting everything but the prompt fields and buttons to be accessible by keyboard
            setFocusTraversableForMainMenuItems(true);
        }
        //If the prompt is hidden
        else {
            promptAnimator.setChild(new Pane(predictionPrompt));
            darkOverlayAnimator.setChild(new Pane(darkOverlay));
            //Running the prediction function when the Enter key is pressed
            predictionPrompt.setOnKeyPressed(a -> attemptPrediction());
            //Setting everything but the prompt fields and buttons to not be accessible by keyboard
            setFocusTraversableForMainMenuItems(false);
            //Showing the prompt
            predictionPrompt.setVisible(true);
            darkOverlay.setVisible(true);
        }
    }

    //Function used to check whether the value of the prompt input and, if valid, trigger the prediction function in the DataProcessing class
    @FXML
    private void attemptPrediction() {
        try {
            int value = Integer.parseInt(predictionField.getText());
            if (value >= 1 && value <= 100) {
                predictionValue = Integer.parseInt(predictionField.getText());
                loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                predictionPrompt.setVisible(false);
                promptAnimator.setChild(new Pane(backgroundOperations));
                backgroundOperations.setVisible(true);
                new Thread(new PredictInBackground()).start();
            } else invalidNumberWarning.setVisible(true);
        } catch (NumberFormatException e) {
            invalidNumberWarning.setVisible(true);
        } catch (IOException ignored) {
        }
    }

    //Function that executes the routine that deletes the generated predictions and then shows a confirmation screen with a button which reloads the main menu page
    @FXML
    private void discardPredictions() {
        processData.discardPredictions();
        //Setting everything but the prompt fields and buttons to not be accessible by keyboard
        setFocusTraversableForMainMenuItems(false);
        //Hiding the discard predictions button from the main menu
        discardPredictionsButton.setVisible(false);
        promptAnimator.setChild(new Pane(discardConfirmation));
        darkOverlayAnimator.setChild(new Pane(darkOverlay));
        //Showing the discard confirmation
        discardConfirmation.setVisible(true);
        darkOverlay.setVisible(true);
    }

    @FXML
    private void closeDiscardConfirmation() {
        setFocusTraversableForMainMenuItems(true);
        promptAnimator.setChild(null);
        darkOverlayAnimator.setChild(null);
        discardConfirmation.setVisible(false);
        darkOverlay.setVisible(false);
    }

    //Function used to hide or show the prompt which asks the user for the period of time they need wage prediction for
    @FXML
    private void toggleEmailPrompt() {
        //If the email prompt or the missing outgoing credentials prompt is visible
        if (emailPrompt.isVisible() || missingOutgoingCredentialsPrompt.isVisible()) {
            promptAnimator.setChild(null);
            darkOverlayAnimator.setChild(null);
            //We hide the prompt
            if (emailPrompt.isVisible()) {
                emailPrompt.setVisible(false);
            } else {
                missingOutgoingCredentialsPrompt.setVisible(false);
            }
            darkOverlay.setVisible(false);
            //We hide the invalid number warning
            invalidEmailWarning.setVisible(false);
            invalidOutgoingEmailWarning.setVisible(false);
            //Setting everything but the prompt fields and buttons to be accessible by keyboard
            setFocusTraversableForMainMenuItems(true);
        }
        //If the prompts are hidden
        else {
            if (outgoingAccountEmail.isEmpty() || outgoingAccountPassword.isEmpty()) {
                promptAnimator.setChild(new Pane(missingOutgoingCredentialsPrompt));
                darkOverlayAnimator.setChild(new Pane(darkOverlay));
                //Setting everything but the prompt fields and buttons to not be accessible by keyboard
                setFocusTraversableForMainMenuItems(false);
                //Showing the prompt
                missingOutgoingCredentialsPrompt.setVisible(true);
                darkOverlay.setVisible(true);
            } else {
                promptAnimator.setChild(new Pane(emailPrompt));
                darkOverlayAnimator.setChild(new Pane(darkOverlay));
                //Setting everything but the prompt fields and buttons to be accessible by keyboard
                setFocusTraversableForMainMenuItems(true);
                //Showing the prompt
                emailPrompt.setVisible(true);
                darkOverlay.setVisible(true);
                //Setting the value of the email field to the email address saved from a previous attempt
                if (!email.isEmpty()) emailField.setText(email);
            }
        }
    }

    @FXML
    private void attemptSendMail() throws IOException {
        EmailValidator validator = EmailValidator.getInstance();
        if (emailPrompt.isVisible()) {
            if (validator.isValid(emailField.getText())) {
                invalidEmailWarning.setVisible(false);
                email = emailField.getText();
                promptAnimator.setChild(null);
                darkOverlayAnimator.setChild(null);
                emailPrompt.setVisible(false);
                darkOverlay.setVisible(false);
                Alert confirmInclusionOfUserData = new Alert(Alert.AlertType.CONFIRMATION);
                confirmInclusionOfUserData.setTitle(language.equals("EN") ? "Inclusion of validation data" : language.equals("FR") ? "Inclusion des détails de validation" : "Includerea datelor de validare");
                confirmInclusionOfUserData.setHeaderText(language.equals("EN") ? "The data below will be attached to the email containing the report. They are data about this computer and are used to help you match the data in the email and ensure they don't come from another computer, and they won't be stored anywhere other than in the email.\nDo you agree with the inclusion of said data and have the report sent?" : language.equals("FR") ? "Les données ci-dessous vont être attachées au courriel contenant le rapport. Ils sont des données à propos de cet ordinateur, et sont utilisées pour vous aider vérifier que le courriel vient d'ici et pas d'un autre ordinateur.\nÊtes-vous d'accord avec l'inclusion de ces données et avec l'envoi du rapport?" : "Detaliile de mai jos vor fi incluse în mail. Ele sunt folosite pentru a vă ajuta să vă asigurați că mail-ul vine de aici și nu de pe alt calculator.\nSunteți de acord cu includerea acestor date și trimiterea raportului?");
                switch (language) {
                    case "EN" ->
                            confirmInclusionOfUserData.setContentText("Username: " + System.getProperty("user.name") + "\nOperating System: " + System.getProperty("os.name") + "\nTimezone: " + System.getProperty("user.timezone"));
                    case "FR" ->
                            confirmInclusionOfUserData.setContentText("Nom d'utilisateur: " + System.getProperty("user.name") + "\nSystème d'exploitation: " + System.getProperty("os.name") + "\nFuseau horaire: " + System.getProperty("user.timezone"));
                    case "RO" ->
                            confirmInclusionOfUserData.setContentText("Nume de utilizator: " + System.getProperty("user.name") + "\nSistem de Operare: " + System.getProperty("os.name") + "\nFus orar: " + System.getProperty("user.timezone"));
                }
                ButtonType yesButton = new ButtonType(language.equals("EN") ? "Yes" : language.equals("FR") ? "Oui" : "Da", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType(language.equals("EN") ? "No" : language.equals("FR") ? "Non" : "Nu", ButtonBar.ButtonData.NO);
                confirmInclusionOfUserData.getButtonTypes().clear();
                confirmInclusionOfUserData.getButtonTypes().add(yesButton);
                confirmInclusionOfUserData.getButtonTypes().add(noButton);
                confirmInclusionOfUserData.getDialogPane().setMaxWidth(750);
                confirmInclusionOfUserData.initStyle(StageStyle.UNDECORATED);
                confirmInclusionOfUserData.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                confirmInclusionOfUserData.getDialogPane().getStyleClass().add("alerts");
                //Setting the alert icon
                // that's going to be shown on the taskbar to the Question free icon created by Roundicons,
                // published on the flaticon website
                // (https://www.flaticon.com/free-icon/question_189665?term=question&page=1&position=11&origin=search&related_id=189665)
                try {
                    ((Stage) confirmInclusionOfUserData.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png")));
                } catch (FileNotFoundException ignored) {
                }
                Optional<ButtonType> confirmationResult = confirmInclusionOfUserData.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == yesButton) {
                    loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                    promptAnimator.setChild(new Pane(backgroundOperations));
                    darkOverlayAnimator.setChild(new Pane(darkOverlay));
                    backgroundOperations.setVisible(true);
                    darkOverlay.setVisible(true);
                    new Thread(new SendEmailInBackground()).start();
                }
            } else invalidEmailWarning.setVisible(true);
        } else if (missingOutgoingCredentialsPrompt.isVisible()) {
            if (validator.isValid(outgoingEmailField.getText())) {
                invalidOutgoingEmailWarning.setVisible(false);
                email = outgoingAccountEmail = outgoingEmailField.getText();
                outgoingAccountPassword = outgoingPasswordField.getText();
                promptAnimator.setChild(null);
                darkOverlayAnimator.setChild(null);
                missingOutgoingCredentialsPrompt.setVisible(false);
                darkOverlay.setVisible(false);
                Alert confirmInclusionOfUserData = new Alert(Alert.AlertType.CONFIRMATION);
                confirmInclusionOfUserData.setTitle(language.equals("EN") ? "Inclusion of validation data" : language.equals("FR") ? "Inclusion des détails de validation" : "Includerea datelor de validare");
                confirmInclusionOfUserData.setHeaderText(language.equals("EN") ? "The data below will be attached to the email containing the report. They are data about this computer and are used to help you match the data in the email and ensure they don't come from another computer, and they won't be stored anywhere other than in the email.\nDo you agree with the inclusion of said data and have the report sent?" : language.equals("FR") ? "Les données ci-dessous vont être attachées au courriel contenant le rapport. Ils sont des données à propos de cet ordinateur, et sont utilisées pour vous aider vérifier que le courriel vient d'ici et pas d'un autre ordinateur.\nÊtes-vous d'accord avec l'inclusion de ces données et avec l'envoi du rapport?" : "Detaliile de mai jos vor fi incluse în mail. Ele sunt folosite pentru a vă ajuta să vă asigurați că mail-ul vine de aici și nu de pe alt calculator.\nSunteți de acord cu includerea acestor date și trimiterea raportului?");
                switch (language) {
                    case "EN" ->
                            confirmInclusionOfUserData.setContentText("Username: " + System.getProperty("user.name") + "\nOperating System: " + System.getProperty("os.name") + "\nTimezone: " + System.getProperty("user.timezone"));
                    case "FR" ->
                            confirmInclusionOfUserData.setContentText("Nom d'utilisateur: " + System.getProperty("user.name") + "\nSystème d'exploitation: " + System.getProperty("os.name") + "\nFuseau horaire: " + System.getProperty("user.timezone"));
                    case "RO" ->
                            confirmInclusionOfUserData.setContentText("Nume de utilizator: " + System.getProperty("user.name") + "\nSistem de Operare: " + System.getProperty("os.name") + "\nFus orar: " + System.getProperty("user.timezone"));
                }
                ButtonType yesButton = new ButtonType(language.equals("EN") ? "Yes" : language.equals("FR") ? "Oui" : "Da", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType(language.equals("EN") ? "No" : language.equals("FR") ? "Non" : "Nu", ButtonBar.ButtonData.NO);
                confirmInclusionOfUserData.getButtonTypes().clear();
                confirmInclusionOfUserData.getButtonTypes().add(yesButton);
                confirmInclusionOfUserData.getButtonTypes().add(noButton);
                confirmInclusionOfUserData.getDialogPane().setMaxWidth(750);
                confirmInclusionOfUserData.initStyle(StageStyle.UNDECORATED);
                confirmInclusionOfUserData.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                confirmInclusionOfUserData.getDialogPane().getStyleClass().add("alerts");
                //Setting the alert icon
                // that's going to be shown on the taskbar to the Question free icon created by Roundicons,
                // published on the flaticon website
                // (https://www.flaticon.com/free-icon/question_189665?term=question&page=1&position=11&origin=search&related_id=189665)
                try {
                    ((Stage) confirmInclusionOfUserData.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png")));
                } catch (FileNotFoundException ignored) {
                }
                Optional<ButtonType> confirmationResult = confirmInclusionOfUserData.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == yesButton) {
                    loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                    promptAnimator.setChild(new Pane(backgroundOperations));
                    darkOverlayAnimator.setChild(new Pane(darkOverlay));
                    backgroundOperations.setVisible(true);
                    darkOverlay.setVisible(true);
                    new Thread(new SendEmailInBackground()).start();
                }
            } else invalidOutgoingEmailWarning.setVisible(true);
        }
    }

    @FXML
    private void toggleDataSources() {
        if (dataSources.isVisible()) {
            //Setting everything but the prompt fields and buttons to be accessible by keyboard
            setFocusTraversableForMainMenuItems(true);
            //Hiding the prompt
            dataSources.setVisible(false);
            darkOverlay.setVisible(false);
            promptAnimator.setChild(null);
            darkOverlayAnimator.setChild(null);
        } else {
            promptAnimator.setChild(new Pane(dataSources));
            darkOverlayAnimator.setChild(new Pane(darkOverlay));
            //Setting everything but the prompt fields and buttons to not be accessible by keyboard
            setFocusTraversableForMainMenuItems(false);
            //Showing the prompt
            dataSources.setVisible(true);
            darkOverlay.setVisible(true);
        }
    }

    @FXML
    private void toggleUpdatePrompt() {
        if (updatePrompt.isVisible()) {
            //Setting everything but the prompt fields and buttons to be accessible by keyboard
            setFocusTraversableForMainMenuItems(true);
            //Hiding the prompt
            updatePrompt.setVisible(false);
            darkOverlay.setVisible(false);
            promptAnimator.setChild(null);
            darkOverlayAnimator.setChild(null);
        } else {
            promptAnimator.setChild(new Pane(updatePrompt));
            darkOverlayAnimator.setChild(new Pane(darkOverlay));
            //Setting everything but the prompt fields and buttons to not be accessible by keyboard
            setFocusTraversableForMainMenuItems(false);
            //Setting the action for the update button
            performUpdateButton.setOnAction(a -> {
                try {
                    Updater.performUpdate();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            //Showing the prompt
            updatePrompt.setVisible(true);
            darkOverlay.setVisible(true);
            //Setting the link to not be visited after being clicked so it retains the red colour.
            updateLink.setVisited(false);
        }
    }

    @FXML
    private void openAppIconPage() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089"));
    }

    @FXML
    private void openMSEmojipediaPage() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://emojipedia.org/microsoft"));
    }

    @FXML
    private void openProjectGitHubPage() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://github.com/Alin63992/GenderGapAnalyser"));
        if (getCurrentStage() != null && !getCurrentStage().getTitle().equals("Gender Gap Analyser") && updatePrompt.isVisible()) {
            darkOverlayAnimator.setChild(null);
            promptAnimator.setChild(null);
            darkOverlay.setVisible(false);
            updatePrompt.setVisible(false);
        }
    }

    @FXML
    private void openPreloadersWebsite() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://icons8.com/preloaders/"));
    }

    private boolean performRecoveryByUpdating() {
        if (!updateRouteSelectedOrRecoveryAborted && !updateDetails[0].isEmpty()) {
            Alert updateFound = new Alert(Alert.AlertType.CONFIRMATION);
            ButtonType update = new ButtonType(language.equals("EN") ? "Update" : "Actuali" + (language.equals("FR") ? "ser" : "zare"), ButtonBar.ButtonData.OK_DONE);
            ButtonType goToGitHub = new ButtonType((language.equals("EN") ? "Open" : language.equals("FR") ? "Ouvrir" : "Deschidere") + " GitHub", ButtonBar.ButtonData.LEFT);
            ButtonType quit = new ButtonType(language.equals("EN") ? "Cancel & Quit" : language.equals("FR") ? "Annuler & Quitter" : "Anulare & Închidere", ButtonBar.ButtonData.CANCEL_CLOSE);
            if (language.equals("EN")) {
                updateFound.setTitle("Update required to recover");
                updateFound.setContentText("The app is missing essential files and needs to recover them.\nRecovering downloads the missing required files from the project's GitHub repository, but the app now has an update waiting with files that contain new features, fixes and improvements which, when combined with the files the version of the app currently installed has, can cause conflicts and errors." + /*" Unfortunately, the currently installed version of the app is no longer available to download, so the new version needs to be downloaded for recovery." + */"\nTo avoid conflicts, you can either update right now to the newest version, open this project's GitHub page to download the source code yourself, or abort the recovery and quit the app.\nTip: click the \"Show Details\" button below to read the changelog of this update!");
            } else if (language.equals("FR")) {
                updateFound.setTitle("Actualisation nécessaire pour récupérer");
                updateFound.setContentText("L'application manque des fichiers essentiels et necesite les récupérer.\nLa récuperation télécharge les fichiers essentiels manquants depuis le dépot GitHub de ce projet, mais l'application a maintenant une actualisation disponible contenant des fichiers avec nouveaux fonctions et améliorations qui, combinées avec les fichiers contenus dans la version d'application actuellement installée, peuvent causer des conflits et erreurs." + /*" Malheureusement, la version actuellement installée n'est plus disponible à télécharger, donc la nouvelle version doit être téléchargée pour la récuperation." + */"\nPour éviter des conflits, vous pouvez actualiser maintenant a la plus nouvelle version, ouvrir la page GitHub de cet projet pour télécharger le code source vous-même, ou annuler la récuperation et quitter l'application.\nConseil: Cliquez sur le bouton \"Show Details\" en dessous pour afficher les nouveautés de cette actualisation !");
            } else {
                updateFound.setTitle("Actualizare necesară pentru recuperare");
                updateFound.setContentText("Aplicației îi lipsesc fișiere esențiale și recuperarea lor este necesară.\nRecuperarea descarcă fișierele esențiale care lipsesc de pe depozitul GitHub al acestui proiect, dar aplicația are acum o actualizare disponibilă ce conține fișiere cu noi funcții și îmbunătățiri care, combinate cu fișierele conținute în versiunea instalată acum a aplicației, pot cauza conflicte și erori." + /*" Din păcate, versiunea instalată acum nu mai este disponibilă pentru descărcare, deci va trebui descărcată noua versiune pentru recuperare." + */"\nPentru a evita conflictele, puteți actualiza aplicația acum la cea mai nouă versiune, deschide pagina GitHub a proiectului pentru a descărca codul sursă, sau anula recuperarea și închide aplicația.\nSfat: faceți click pe butonul \"Show Details\" de dedesubt pentru a citi ce este nou în această actualizare!");
            }
            updateFound.setHeaderText(updateFound.getTitle());
            updateFound.getButtonTypes().clear();
            updateFound.getButtonTypes().add(goToGitHub);
            updateFound.getButtonTypes().add(quit);
            updateFound.getButtonTypes().add(update);
            updateFound.getDialogPane().setPrefWidth(750);
            ScrollPane changelogArea = new ScrollPane();
            changelogArea.setPrefWidth(updateFound.getDialogPane().getPrefWidth() - 50);
            changelogArea.setPrefHeight(250);
            String changelog = updateDetails[1];
            changelog = changelog.replaceAll("~", "");
            changelog = changelog.replaceAll("\\*", "");
            changelog = changelog.replaceAll("_", "");
            changelog = changelog.replaceAll("\\[", "");
            changelog = changelog.replaceAll("]\\(", " (");
            Label changelogLabel = new Label(changelog);
            changelogLabel.setWrapText(true);
            changelogLabel.setPrefWidth(changelogArea.getPrefWidth() - 35);
            changelogLabel.setStyle("-fx-font-family: Calibri");
            changelogLabel.setStyle("-fx-font-size: 15");
            changelogArea.setContent(changelogLabel);
            updateFound.getDialogPane().setExpandableContent(changelogArea);
            Optional<ButtonType> choice = updateFound.showAndWait();
            if (choice.isPresent() && choice.get() == goToGitHub) {
                try {
                    openProjectGitHubPage();
                } catch (IOException | URISyntaxException ignored) {
                }
            } else if (choice.isPresent() && choice.get() == update) {
                try {
                    Updater.performUpdate();
                } catch (IOException ignored) {
                }
            }
            updateRouteSelectedOrRecoveryAborted = true;
        }
        return updateRouteSelectedOrRecoveryAborted;
    }

    private void reloadSplashScreen(boolean createRecoveryInProgressFile) throws IOException {
        if (createRecoveryInProgressFile)
            recoveryInProgress.createNewFile();
        else
            recoveryInProgress.delete();
        //Reloading the splash screen to indicate that recovery has started or ended
        if (getCurrentStage() != null) {
            Platform.runLater(() -> {
                try {
                    Scene splash = new Scene(new FXMLLoader(getClass().getResource("AppScreens/SplashScreen-" + Main.language + ".fxml")).load());
                    splash.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                    getCurrentStage().setScene(splash);
                } catch (IOException ignored) {
                }
            });
        }
    }

    @FXML
    private boolean checkAndRecover() {
        System.out.println("Beginning application integrity check...");

        //Continuing to check and recover the AppScreens folder
        if (!new File(appScreensFolder + "/MainMenu-EN.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/MainMenu-EN.fxml").toURL(), new File(appScreensFolder + "/MainMenu-EN.fxml"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml").exists())
                    Files.copy(Path.of(appScreensFolder + "/MainMenu-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(appScreensFolder + "/MainMenu-FR.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/MainMenu-FR.fxml").toURL(), new File(appScreensFolder + "/MainMenu-FR.fxml"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml").exists())
                    Files.copy(Path.of(appScreensFolder + "/MainMenu-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(appScreensFolder + "/MainMenu-RO.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/MainMenu-RO.fxml").toURL(), new File(appScreensFolder + "/MainMenu-RO.fxml"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml").exists())
                    Files.copy(Path.of(appScreensFolder + "/MainMenu-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(appScreensFolder + "/DisplayEvolutionGraph-EN.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/DisplayEvolutionGraph-EN.fxml").toURL(), new File(appScreensFolder + "/DisplayEvolutionGraph-EN.fxml"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml").exists())
                    Files.copy(Path.of(appScreensFolder + "/DisplayEvolutionGraph-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(appScreensFolder + "/DisplayEvolutionGraph-FR.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/DisplayEvolutionGraph-FR.fxml").toURL(), new File(appScreensFolder + "/DisplayEvolutionGraph-FR.fxml"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml").exists())
                    Files.copy(Path.of(appScreensFolder + "/DisplayEvolutionGraph-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(appScreensFolder + "/DisplayEvolutionGraph-RO.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/DisplayEvolutionGraph-RO.fxml").toURL(), new File(appScreensFolder + "/DisplayEvolutionGraph-RO.fxml"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml").exists())
                    Files.copy(Path.of(appScreensFolder + "/DisplayEvolutionGraph-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(appScreensFolder + "/Analysis-EN.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/Analysis-EN.fxml").toURL(), new File(appScreensFolder + "/Analysis-EN.fxml"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml").exists())
                    Files.copy(Path.of(appScreensFolder + "/Analysis-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(appScreensFolder + "/Analysis-FR.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/Analysis-FR.fxml").toURL(), new File(appScreensFolder + "/Analysis-FR.fxml"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml").exists())
                    Files.copy(Path.of(appScreensFolder + "/Analysis-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(appScreensFolder + "/Analysis-RO.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/Analysis-RO.fxml").toURL(), new File(appScreensFolder + "/Analysis-RO.fxml"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml").exists())
                    Files.copy(Path.of(appScreensFolder + "/Analysis-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }

        //Checking and recovering the Glyphs/Emojis folder
        if (!new File(emojisFolder + "/Black_Rightwards_Arrow.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Rightwards_Arrow.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Black_Rightwards_Arrow.png").toURL(), new File(emojisFolder + "/Black_Rightwards_Arrow.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Rightwards_Arrow.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Black_Rightwards_Arrow.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Rightwards_Arrow.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Black_Sun_with_Rays.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Sun_with_Rays.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Black_Sun_with_Rays.png").toURL(), new File(emojisFolder + "/Black_Sun_with_Rays.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Sun_with_Rays.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Black_Sun_with_Rays.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Sun_with_Rays.png"));
            } catch (IOException e) {
                e.printStackTrace();
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Calendar.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Calendar.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Calendar.png").toURL(), new File(emojisFolder + "/Calendar.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Calendar.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Calendar.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Calendar.png"));
            } catch (IOException e) {
                e.printStackTrace();
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Chart_with_Upwards_Trend.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Chart_with_Upwards_Trend.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Chart_with_Upwards_Trend.png").toURL(), new File(emojisFolder + "/Chart_with_Upwards_Trend.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Chart_with_Upwards_Trend.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Chart_with_Upwards_Trend.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Chart_with_Upwards_Trend.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Crescent_Moon.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crescent_Moon.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Crescent_Moon.png").toURL(), new File(emojisFolder + "/Crescent_Moon.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crescent_Moon.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Crescent_Moon.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crescent_Moon.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Crystal_Ball.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crystal_Ball.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Crystal_Ball.png").toURL(), new File(emojisFolder + "/Crystal_Ball.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crystal_Ball.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Crystal_Ball.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crystal_Ball.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/E-Mail_Symbol.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/E-Mail_Symbol.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/E-Mail_Symbol.png").toURL(), new File(emojisFolder + "/E-Mail_Symbol.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/E-Mail_Symbol.png").exists())
                    Files.copy(Path.of(emojisFolder + "/E-Mail_Symbol.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/E-Mail_Symbol.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Female_Sign.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Female_Sign.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Female_Sign.png").toURL(), new File(emojisFolder + "/Female_Sign.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Female_Sign.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Female_Sign.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Female_Sign.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Globe_with_Meridians.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Globe_with_Meridians.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Globe_with_Meridians.png").toURL(), new File(emojisFolder + "/Globe_with_Meridians.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Globe_with_Meridians.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Globe_with_Meridians.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Globe_with_Meridians.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Information_Source.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Information_Source.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Information_Source.png").toURL(), new File(emojisFolder + "/Information_Source.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Information_Source.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Information_Source.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Information_Source.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Leftwards_Black_Arrow.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Leftwards_Black_Arrow.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Leftwards_Black_Arrow.png").toURL(), new File(emojisFolder + "/Leftwards_Black_Arrow.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Leftwards_Black_Arrow.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Leftwards_Black_Arrow.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Leftwards_Black_Arrow.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Male_Sign.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Male_Sign.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Male_Sign.png").toURL(), new File(emojisFolder + "/Male_Sign.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Male_Sign.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Male_Sign.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Male_Sign.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Memo.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Memo.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Memo.png").toURL(), new File(emojisFolder + "/Memo.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Memo.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Memo.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Memo.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(emojisFolder + "/Page_Facing_Up.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Page_Facing_Up.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Page_Facing_Up.png").toURL(), new File(emojisFolder + "/Page_Facing_Up.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Page_Facing_Up.png").exists())
                    Files.copy(Path.of(emojisFolder + "/Page_Facing_Up.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Page_Facing_Up.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }

        //Continuing to check and recover the Glyphs/Miscellaneous folder
        if (!new File(miscellaneousFolder + "/alert-confirmation.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-confirmation.png").toURL(), new File(miscellaneousFolder + "/alert-confirmation.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png").exists())
                    Files.copy(Path.of(miscellaneousFolder + "/alert-confirmation.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(miscellaneousFolder + "/alert-error.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-error.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-error.png").toURL(), new File(miscellaneousFolder + "/alert-error.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-error.png").exists())
                    Files.copy(Path.of(miscellaneousFolder + "/alert-error.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-error.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(miscellaneousFolder + "/alert-information.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-information.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-information.png").toURL(), new File(miscellaneousFolder + "/alert-information.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-information.png").exists())
                    Files.copy(Path.of(miscellaneousFolder + "/alert-information.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-information.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(miscellaneousFolder + "/ExchangeRate-API-Logo-Dark.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png").toURL(), new File(miscellaneousFolder + "/ExchangeRate-API-Logo-Dark.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png").exists())
                    Files.copy(Path.of(miscellaneousFolder + "/ExchangeRate-API-Logo-Dark.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(miscellaneousFolder + "/ExchangeRate-API-Logo-Light.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png").toURL(), new File(miscellaneousFolder + "/ExchangeRate-API-Logo-Light.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png").exists())
                    Files.copy(Path.of(miscellaneousFolder + "/ExchangeRate-API-Logo-Light.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }
        if (!new File(miscellaneousFolder + "/US-Dept-of-Labor-Logo.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/US-Dept-of-Labor-Logo.png").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/US-Dept-of-Labor-Logo.png").toURL(), new File(miscellaneousFolder + "/US-Dept-of-Labor-Logo.png"), 500, 2000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/US-Dept-of-Labor-Logo.png").exists())
                    Files.copy(Path.of(miscellaneousFolder + "/US-Dept-of-Labor-Logo.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/US-Dept-of-Labor-Logo.png"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }

        //Checking if the MarkdownFX stylesheets exist, and if not, trying to download it
        if (!new File(stylesheetsFolder + "/MarkdownFX-Dark.css").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/MarkdownFX-Dark.css").exists()) {
            if (!performRecoveryByUpdating()) {
                try {
                    if (!recoveryInProgress.exists())
                        recoveryInProgress.createNewFile();
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/MarkdownFX-Dark.css").toURL(), new File(stylesheetsFolder + "/MarkdownFX-Dark.css"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/MarkdownFX-Dark.css").exists())
                        Files.copy(Path.of(stylesheetsFolder + "/MarkdownFX-Dark.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/MarkdownFX-Dark.css"));
                } catch (IOException e) {
                    if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                    allStartFilesExist = false;
                }
            }
        }
        if (!new File(stylesheetsFolder + "/MarkdownFX-Light.css").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/MarkdownFX-Light.css").exists()) {
            if (!performRecoveryByUpdating()) {
                try {
                    if (!recoveryInProgress.exists())
                        recoveryInProgress.createNewFile();
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/MarkdownFX-Light.css").toURL(), new File(stylesheetsFolder + "/MarkdownFX-Light.css"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/MarkdownFX-Light.css").exists())
                        Files.copy(Path.of(stylesheetsFolder + "/MarkdownFX-Light.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/MarkdownFX-Light.css"));
                } catch (IOException e) {
                    if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                    allStartFilesExist = false;
                }
            }
        }


        //Checking if the fallback dataset CSV file exists, and if not, trying to download it
        if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/FallbackDataset.csv").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/FallbackDataset.csv").exists()) {
            if (performRecoveryByUpdating())
                return false;
            try {
                if (!recoveryInProgress.exists()) {
                    reloadSplashScreen(true);
                }
                FileUtils.copyURLToFile(URI.create(githubRoot + "FallbackDataset.csv").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/FallbackDataset.csv"), 500, 10000);
                if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/FallbackDataset.csv").exists())
                    Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/FallbackDataset.csv"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/FallbackDataset.csv"));
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                return false;
            }
        }

        //Finishing recovery
        try {
            if (recoveryInProgress.exists())
                reloadSplashScreen(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void start(Stage primaryStage) {
        Runnable appLoad = () -> {
            try {
                //Checking the app integrity
                if (checkAndRecover()) {
                    System.out.println("Application integrity check complete. Application is healthy, and it can start.");
                    //Trying to download the dataset file from the U.S. Department of Labor server
                    downloadDataset.start();

                    //Checking if a day has passed since last downloading exchange rates
                    exchangeRateLastUpdated.add(GregorianCalendar.DAY_OF_MONTH, 1);
                    //If a day did pass
                    if (exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) < LocalDate.now().getDayOfMonth() || exchangeRateLastUpdated.get(GregorianCalendar.MONTH) <= LocalDate.now().getMonthValue() || exchangeRateLastUpdated.get(GregorianCalendar.YEAR) <= LocalDate.now().getYear()) {
                        //Preparing to connect to the ExchangeRate-API to obtain new exchange rates
                        HttpURLConnection connection = (HttpURLConnection) new URI("https://v6.exchangerate-api.com/v6/9a9fc15f7944c0cb9bf532a8/latest/USD").toURL().openConnection();
                        connection.setConnectTimeout(500);
                        connection.setReadTimeout(1000);
                        connection.addRequestProperty("User-Agent", "Mozilla/5.0");
                        //Attempting to connect (hoping that the computer is connected to the internet)
                        try {
                            connection.connect();
                            if (connection.getResponseCode() == 200) {
                                //Saving the JSON response
                                BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
                                StringBuilder json = new StringBuilder();
                                String output;
                                while ((output = br.readLine()) != null)
                                    json.append(output);
                                //Parsing the JSON response
                                JSONObject obj = (JSONObject) JSONValue.parse(json.toString());
                                if (obj.get("result").equals("success")) {
                                    Map<String, Double> currencies = (Map<String, Double>) obj.get("conversion_rates");
                                    exchangeRateEUR = currencies.get("EUR");
                                    exchangeRateRON = currencies.get("RON");

                                    //Setting the current date as the date of last update
                                    exchangeRateLastUpdated.set(GregorianCalendar.DAY_OF_MONTH, LocalDate.now().getDayOfMonth());
                                    exchangeRateLastUpdated.set(GregorianCalendar.MONTH, LocalDate.now().getMonthValue());
                                    exchangeRateLastUpdated.set(GregorianCalendar.YEAR, LocalDate.now().getYear());

                                    //Rebuilding the user settings file with the new currency values
                                    BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                                    buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                                    buildUserSettings.close();
                                }
                            } else {
                                exchangeRateLastUpdated.add(GregorianCalendar.DAY_OF_MONTH, -1);
                                currency = "USD";
                                BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                                buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                                buildUserSettings.close();
                                Platform.runLater(() -> {
                                    Alert errorUpdatingExchangeRates = new Alert(Alert.AlertType.ERROR);
                                    errorUpdatingExchangeRates.setTitle(Main.language.equals("EN") ? "Exchange rates not updated" : Main.language.equals("FR") ? "Taux d'échange non actualisées" : "Rate de conversie neactualizate");
                                    errorUpdatingExchangeRates.setHeaderText(Main.language.equals("EN") ? "The exchange rates couldn't be updated because the monthly usage quota has been reached!\nThe usage quota resets on the 17th of every month. Please check back on the 17th or after to update with the new exchange rates." : Main.language.equals("FR") ? "Les taux d'échange n'ont pas pu être actualisées car la quota d'utilisation a était consommée !\nLa quota d'utilisation se réinitialise le 17 de chaque mois. Veuillez vérifiez sur le 17 ou aprés pour actualiser les taux d'échange !" : "Ratele de conversie nu au putut fi actualizate pentru că limita de consum a fost atinsă!\nLimita de consum se reinițializează pe data de 17 a fiecărei luni. Vă rugăm verificați pe data de 17 sau după pentru a actualiza ratele de schimb!");
                                    errorUpdatingExchangeRates.getDialogPane().setMaxWidth(370);
                                    errorUpdatingExchangeRates.initStyle(StageStyle.UNDECORATED);
                                    errorUpdatingExchangeRates.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                                    errorUpdatingExchangeRates.getDialogPane().getStyleClass().add("alerts");
                                    //Setting the alert icon
                                    // that's going to be shown on the taskbar to the Close free icon created by Alfredo Hernandez,
                                    // published on the flaticon website
                                    // (https://www.flaticon.com/free-icon/close_463612)
                                    try {
                                        ((Stage) errorUpdatingExchangeRates.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-error.png")));
                                    } catch (FileNotFoundException ignored) {
                                    }
                                    errorUpdatingExchangeRates.show();
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            exchangeRateLastUpdated.add(GregorianCalendar.DAY_OF_MONTH, -1);
                            currency = "USD";
                            BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                            buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                            buildUserSettings.close();
                            Platform.runLater(() -> {
                                Alert errorUpdatingExchangeRates = new Alert(Alert.AlertType.ERROR);
                                errorUpdatingExchangeRates.setTitle(Main.language.equals("EN") ? "Exchange rates not updated" : Main.language.equals("FR") ? "Taux d'échange non actualisées" : "Rate de conversie neactualizate");
                                errorUpdatingExchangeRates.setHeaderText(errorUpdatingExchangeRates.getTitle());
                                errorUpdatingExchangeRates.setContentText(Main.language.equals("EN") ? "The exchange rates couldn't be updated!\nPlease check your internet connection, or wait for a bit then restart the app to try again!" : Main.language.equals("FR") ? "Les taux d'échange n'ont pas pu être actualisées !\nVeuillez vérifier votre connexion internet, ou attendez un peu et redémarrez l'application pour réessayer !" : "Ratele de conversie nu au putut fi actualizate!\nVă rugăm verificați conexiunea la internet, sau așteptați puțin și relansați aplicația pentru a reîncerca!");
                                errorUpdatingExchangeRates.getDialogPane().setMaxWidth(370);
                                errorUpdatingExchangeRates.initStyle(StageStyle.UNDECORATED);
                                errorUpdatingExchangeRates.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                                errorUpdatingExchangeRates.getDialogPane().getStyleClass().add("alerts");
                                //Setting the alert icon
                                // that's going to be shown on the taskbar to the Close free icon created by Alfredo Hernandez,
                                // published on the flaticon website
                                // (https://www.flaticon.com/free-icon/close_463612)
                                try {
                                    ((Stage) errorUpdatingExchangeRates.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-error.png")));
                                } catch (FileNotFoundException ignored) {
                                }
                                errorUpdatingExchangeRates.show();
                            });
                        }
                    } else {
                        //Reverting the change made to the date so that the application does not use the wrong date
                        exchangeRateLastUpdated.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    }

                    //Preparing the dataset and creating the plots
                    processData = new DataProcessing();
                    processData.prepareData();

                    //Switching to the main menu page because the app loading is done
                    Platform.runLater(() -> {
                        //Setting the window title
                        getCurrentStage().setTitle(language.equals("EN") ? "Main Menu" : language.equals("FR") ? "Menu Principal" : "Meniu Principal");
                        AnimatedSwitcher as = new AnimatedSwitcher();
                        as.setIn(new Animation(new ZoomIn()).setSpeed(1.3));
                        Scene scene = new Scene(new Pane(as));
                        scene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());
                        as.of(getCurrentStage().getScene().getRoot());
                        try {
                            as.setChild(new FXMLLoader(getClass().getResource("AppScreens/MainMenu-" + language + ".fxml")).load());
                        } catch (IOException ignored) {}
                        getCurrentStage().setScene(scene);
                        switchTheme = new AnimatedThemeSwitcher(getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                        switchTheme.init();
                        getCurrentStage().centerOnScreen();
                    });
                } else {
                    if (!updateRouteSelectedOrRecoveryAborted) {
                        recoveryInProgress.delete();
                        System.out.println("Application integrity check complete. Application is missing essential files! Startup aborted!");
                        Platform.runLater(() -> {
                            Alert applicationError = new Alert(Alert.AlertType.ERROR);
                            applicationError.setTitle("Application Error");
                            applicationError.setHeaderText("Severe Application Error!");
                            getCurrentStage().close();
                            if (fileNotFoundOnGit) {
                                ButtonType goToGitHub = new ButtonType("Go to GitHub", ButtonBar.ButtonData.LEFT);
                                applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and online recovery is impossible because the missing files were not found on GitHub.\nPlease click the \"Go to GitHub\" button below to download the app again and get back on track.");
                                applicationError.getButtonTypes().add(goToGitHub);
                                applicationError.getDialogPane().setMaxWidth(750);
                                Optional<ButtonType> choice = applicationError.showAndWait();
                                if (choice.isPresent() && choice.get() == goToGitHub) {
                                    try {
                                        openProjectGitHubPage();
                                    } catch (IOException | URISyntaxException ignored) {
                                    }
                                }
                            } else {
                                applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and they couldn't be downloaded right now.\nPlease check that you're connected to the internet or wait for a few minutes and start the application again.");
                                applicationError.show();
                            }
                        });
                    }
                }
            } catch (IOException | URISyntaxException ignored) {
            }
        };
        try {
            //Loading user settings (display mode and app language) from the Properties.txt file
            try {
                BufferedReader loadUserSettings = new BufferedReader(new FileReader("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                String setting;
                while ((setting = loadUserSettings.readLine()) != null) {
                    String[] settingParts = setting.split("=");
                    switch (settingParts[0]) {
                        case "DisplayMode" -> displayMode = settingParts[1];
                        case "Language" -> language = settingParts[1];
                        case "Currency" -> currency = settingParts[1];
                        case "ExchangeRateLastUpdated" -> {
                            exchangeRateLastUpdated.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(settingParts[1].split("\\.")[0]));
                            exchangeRateLastUpdated.set(GregorianCalendar.MONTH, Integer.parseInt(settingParts[1].split("\\.")[1]));
                            exchangeRateLastUpdated.set(GregorianCalendar.YEAR, Integer.parseInt(settingParts[1].split("\\.")[2]));
                        }
                        case "ExchangeRateToEUR" -> exchangeRateEUR = Double.parseDouble(settingParts[1]);
                        case "ExchangeRateToRON" -> exchangeRateRON = Double.parseDouble(settingParts[1]);
                    }
                }
                loadUserSettings.close();
            } catch (IOException e) {
                //Setting the exchange rate last updated date 2 days back of the current date because, before updating,
                // a day is added to the last updated day,
                // so that way,
                // the date is still behind the current day and the exchange rates are downloaded
                exchangeRateLastUpdated.set(GregorianCalendar.DAY_OF_MONTH, LocalDate.now().getDayOfMonth());
                exchangeRateLastUpdated.add(GregorianCalendar.DAY_OF_MONTH, -2);
                exchangeRateLastUpdated.set(GregorianCalendar.MONTH, LocalDate.now().getMonthValue());
                exchangeRateLastUpdated.set(GregorianCalendar.YEAR, LocalDate.now().getYear());
                BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                buildUserSettings.close();
            }

            //Checking for new updates
            updateDetails = Updater.checkForUpdate();

            //Checking if the required resource folders exist and creating them if they don't
            try {
                if (!new File(appScreensFolder).exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists()) {
                    Files.createDirectories(Path.of(appScreensFolder));
                    Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                }
                if (!new File(emojisFolder).exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis").exists()) {
                    Files.createDirectories(Path.of(emojisFolder));
                    Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis"));
                }
                if (!new File(miscellaneousFolder).exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous").exists()) {
                    Files.createDirectories(Path.of(miscellaneousFolder));
                    Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous"));
                }
                if (!new File(stylesheetsFolder).exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets").exists()) {
                    Files.createDirectories(Path.of(stylesheetsFolder));
                    Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //First, the app checks if the dark + light mode app icons and loading wheels exist in the Miscellaneous folder.
            //Next, the app checks if the dark + light mode app CSS files exist in the Stylesheets folder.
            //Then, the app checks if the English, French and Romanian splash screens exist in the AppScreens folder.
            //If any or all of these files are missing, they are downloaded and saved in their own respective folders.
            //This is done to make sure that every resource needed by the splash screen exists so it can be displayed to
            // the user, so they know what is happening with the app.
            //After these files were recovered or were confirmed to be present, the splash screen is displayed, and the
            // checking & recovery continues for the rest of the app screens and icons.

            //Checking and recovering the app icons and loading wheels
            if (!new File(miscellaneousFolder + "/AppIcon.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png").exists()) {
                if (!performRecoveryByUpdating()) {
                    try {
                        if (!recoveryInProgress.exists())
                            recoveryInProgress.createNewFile();
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/AppIcon.png").toURL(), new File(miscellaneousFolder + "/AppIcon.png"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png").exists())
                            Files.copy(Path.of(miscellaneousFolder + "/AppIcon.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png"));
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        allStartFilesExist = false;
                    }
                }
            }
            else if (!new File(miscellaneousFolder + "/AppIcon-Dark.png").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png").exists()) {
                if (!performRecoveryByUpdating()) {
                    try {
                        if (!recoveryInProgress.exists())
                            recoveryInProgress.createNewFile();
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/AppIcon-Dark.png").toURL(), new File(miscellaneousFolder + "/AppIcon-Dark.png"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png").exists())
                            Files.copy(Path.of(miscellaneousFolder + "/AppIcon-Dark.png"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png"));
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        allStartFilesExist = false;
                    }
                }
            }
            else if (!new File(miscellaneousFolder + "/loading-Dark.gif").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Dark.gif").exists()) {
                if (!performRecoveryByUpdating()) {
                    try {
                        if (!recoveryInProgress.exists())
                            recoveryInProgress.createNewFile();
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/loading-Dark.gif").toURL(), new File(miscellaneousFolder + "/loading-Dark.gif"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Dark.gif").exists())
                            Files.copy(Path.of(miscellaneousFolder + "/loading-Dark.gif"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Dark.gif"));
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        allStartFilesExist = false;
                    }
                }
            }
            else if (!new File(miscellaneousFolder + "/loading-Light.gif").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Light.gif").exists()) {
                if (!performRecoveryByUpdating()) {
                    try {
                        if (!recoveryInProgress.exists())
                            recoveryInProgress.createNewFile();
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/loading-Light.gif").toURL(), new File(miscellaneousFolder + "/loading-Light.gif"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Light.gif").exists())
                            Files.copy(Path.of(miscellaneousFolder + "/loading-Light.gif"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Light.gif"));
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        allStartFilesExist = false;
                    }
                }
            }

            //Checking and recovering the Stylesheets folder
            else if (!new File(stylesheetsFolder + "/DarkMode.css").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css").exists()) {
                if (!performRecoveryByUpdating()) {
                    try {
                        if (!recoveryInProgress.exists())
                            recoveryInProgress.createNewFile();
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/DarkMode.css").toURL(), new File(stylesheetsFolder + "/DarkMode.css"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css").exists())
                            Files.copy(Path.of(stylesheetsFolder + "/DarkMode.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"));
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        allStartFilesExist = false;
                    }
                }
            }
            else if (!new File(stylesheetsFolder + "/LightMode.css").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css").exists()) {
                if (!performRecoveryByUpdating()) {
                    try {
                        if (!recoveryInProgress.exists())
                            recoveryInProgress.createNewFile();
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/LightMode.css").toURL(), new File(stylesheetsFolder + "/LightMode.css"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css").exists())
                            Files.copy(Path.of(stylesheetsFolder + "/LightMode.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css"));
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        allStartFilesExist = false;
                    }
                }
            }

            //Checking and recovering the splash screen FXML files
            else if (!new File(appScreensFolder + "/SplashScreen-EN.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml").exists()) {
                if (!performRecoveryByUpdating()) {
                    try {
                        if (!recoveryInProgress.exists())
                            recoveryInProgress.createNewFile();
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-EN.fxml").toURL(), new File(appScreensFolder + "/SplashScreen-EN.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml").exists())
                            Files.copy(Path.of(appScreensFolder + "/SplashScreen-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"));
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        allStartFilesExist = false;
                    }
                }
            }
            else if (!new File(appScreensFolder + "/SplashScreen-FR.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml").exists()) {
                if (!performRecoveryByUpdating()) {
                    try {
                        if (!recoveryInProgress.exists())
                            recoveryInProgress.createNewFile();
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-FR.fxml").toURL(), new File(appScreensFolder + "/SplashScreen-FR.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml").exists())
                            Files.copy(Path.of(appScreensFolder + "/SplashScreen-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"));
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        allStartFilesExist = false;
                    }
                }
            }
            else if (!new File(appScreensFolder + "/SplashScreen-RO.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml").exists()) {
                if (!performRecoveryByUpdating()) {
                    try {
                        if (!recoveryInProgress.exists())
                            recoveryInProgress.createNewFile();
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-RO.fxml").toURL(), new File(appScreensFolder + "/SplashScreen-RO.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml").exists())
                            Files.copy(Path.of(appScreensFolder + "/SplashScreen-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"));
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        allStartFilesExist = false;
                    }
                }
            }
            else
                allStartFilesExist = true;

            //The app now has the AppScreens, Glyphs/Emojis, Glyphs/Miscellaneous, and the Stylesheets folders in the
            // resources and target folders. The app also has the app icons and loading wheels, the dark and light mode
            // stylesheets, and the splash screen FXML files, needed for the good display of the splash screen.
            //The app window with the splash screen can now be opened.

            if (allStartFilesExist) {
                //Setting the primary stage so that other controllers can use it to display what they need displayed
                setCurrentStage(primaryStage);
                getCurrentStage().initStyle(StageStyle.UNDECORATED);

                //Setting the window title
                getCurrentStage().setTitle("Gender Gap Analyser");

                //Setting the window to be not resizable and centred on screen
                getCurrentStage().setResizable(false);
                getCurrentStage().centerOnScreen();

                //Setting the window's icon
                getCurrentStage().getIcons().setAll(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));

                //Opening the app window containing the splash screen
                try {
                    splashScreen = new FXMLLoader(getClass().getResource("AppScreens/SplashScreen-" + language + ".fxml")).load();
                    Scene splash = new Scene(splashScreen.load());
                    splashScreenController = splashScreen.getController();
                    splash.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());
                    getCurrentStage().setScene(splash);
                    getCurrentStage().show();
                } catch (IOException ignored) {
                }

                //Checking if the app crashed during an update (the .updateinprogress file exists),
                // to resume updating if it did
                if (updateInProgress.exists()) {
                    Updater.performUpdate();
                }
                else {
                    //Starting the app normally
                    new Thread(appLoad).start();
                }
            }
        } catch (IllegalStateException | URISyntaxException ignored) {
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //TODO: make GenderGapAnalyser-private public for testing
        //TODO: try to translate the changelog too (maybe google translate/deepl has an api? file on git with translated changelogs for better control over them?)
        //TODO: implement the updating mechanism: replacing classes and files? redirecting the user to github to download? downloading directly and asking the user for destination?
        //TODO: fix focusing to the update split button elements
        //TODO: group the variables better and change them to private
        //TODO: test the update system once finished
        //TODO: test recovery through updating once the update system is finished
        //TODO: remember to save the working app with the file replace update mechanism before testing out the mechanism, to not end up with a broken app and lost work

        //Making the window movable when dragging the embedded title bar
        titleBar.setOnMousePressed(event -> {
            dragX = getCurrentStage().getX() - event.getScreenX();
            dragY = getCurrentStage().getY() - event.getScreenY();
        });
        titleBar.setOnMouseDragged(event -> {
            getCurrentStage().setX(event.getScreenX() + dragX);
            getCurrentStage().setY(event.getScreenY() + dragY);
        });

        if (getCurrentStage().getTitle().equals("Gender Gap Analyser")) {
            try {
                if (displayMode.equals("Dark"))
                    appIconImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png")));
                loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + Main.displayMode + ".gif")));
            } catch (FileNotFoundException ignored) {}
            if (updateInProgress.exists()) {
                updateText.setVisible(true);
            }
            recoveryText.setVisible(recoveryInProgress.exists());
            rollbackText.setVisible(rollbackInProgress.exists());
            noCloseWindowText.setVisible(updateInProgress.exists() || recoveryInProgress.exists() || rollbackInProgress.exists());
            cleanupText.setVisible(cleanupInProgress.exists());
        } else {
            promptAnimator.setIn(new Animation(new ZoomIn()).setSpeed(3));
            darkOverlayAnimator.setIn(new Animation(new FadeIn()).setSpeed(3));
            promptAnimator.setOut(new Animation(new ZoomOut()).setSpeed(3));
            darkOverlayAnimator.setOut(new Animation(new FadeOut()).setSpeed(3));

            if (!updateDetails[0].isEmpty()) {
                updateLink.setVisible(true);
                String[] updateReleaseDate = updateDetails[0].split("\\.");
                updateReleasedOn.setText(updateReleasedOn.getText() + updateReleaseDate[0] + "." + updateReleaseDate[1] + "." + updateReleaseDate[2]);
                if (Integer.parseInt(updateReleaseDate[3]) > 1)
                    updateReleasedOn.setText(updateReleasedOn.getText() + ", Revision " + updateReleaseDate[3]);
                mdview.setMdString(updateDetails[1]);
                //TODO check if the MarkdownFX stylesheets exist and download them too, if they don't
                //TODO show the recovery by update alert only if the zip of the current version tag can't be downloaded (check checkAndRecovery())
                mdview.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/MarkdownFX-" + displayMode + ".css")).toExternalForm());
                mdview.setPrefWidth(changelogPane.getPrefWidth() - 35);
                changelogPane.setContent(mdview);
            }

            //Setting up the language picker
            languagePicker.setItems(FXCollections.observableArrayList(languages));
            switch (language) {
                case "EN" -> languagePicker.setValue(languages[0]);
                case "FR" -> languagePicker.setValue(languages[1]);
                case "RO" -> languagePicker.setValue(languages[2]);
            }
            //When selecting another language from the language picker...
            languagePicker.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
                //Updating the language with the newly picked one
                language = languagesShort[newValue.intValue()];
                //Setting the boolean variable used by DataProcessing.createPDF method to true
                // so that the method generates a new PDF document in a new language
                changedLanguage = true;
                //Checking if the currency has been updated (default is -1.0)
                if (exchangeRateEUR != -1.0 && exchangeRateRON != -1.0) {
                    //Updating the currency with the one associated with the language
                    currency = currencies[newValue.intValue()];
                    //Setting the boolean variable used by DataProcessing.createPDF method to true
                    // so that the method generates a new PDF document with the new currency
                    changedCurrency = true;
                } else
                    currencyPicker.setValue(currencies[0]);
                Runnable rebuildResources = () -> {
                    try {
                        BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                        buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                        buildUserSettings.close();
                        //Checking if the currencies have ever been updated
                        if (exchangeRateEUR != -1.0 && exchangeRateRON != -1.0) {
                            //Creating the usable dataset,
                            // graphs and interpretations again so that it uses the new language and currency
                            processData.prepareData();
                        } else {
                            //Just regenerating the graphs and interpretations to use the new language
                            processData.createSalaryGraphForEverybody();
                            processData.performAnalysis();
                        }
                        //Recreating predictions and prediction graphs so that they use the newly set language
                        if (processData.predictionsGenerated) {
                            processData.predictEvolutions(predictionValue);
                            processData.createSalaryGraphWithPredictionsForEverybody();
                        }
                    } catch (IOException ignored) {
                    }

                    Platform.runLater(() -> {
                        try {
                            Scene mainScene = new Scene(new FXMLLoader(getClass().getResource("AppScreens/MainMenu-" + language + ".fxml")).load());
                            mainScene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());
                            getCurrentStage().setScene(mainScene);
                        } catch (IOException ignored) {
                        }
                        //Changing the title of the current stage
                        getCurrentStage().setTitle(language.equals("EN") ? "Main Menu" : language.equals("FR") ? "Menu Principal" : "Meniu Principal");
                        switchTheme = new AnimatedThemeSwitcher(getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                        switchTheme.init();
                    });
                };
                promptAnimator.setChild(new Pane(backgroundOperations));
                darkOverlayAnimator.setChild(new Pane(darkOverlay));
                try {
                    loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                backgroundOperations.setVisible(true);
                darkOverlay.setVisible(true);
                new Thread(rebuildResources).start();
            }));

            //Setting up the currency picker
            currencyPicker.setItems(FXCollections.observableArrayList(currencies));
            switch (currency) {
                case "USD" -> currencyPicker.setValue(currencies[0]);
                case "EUR" -> currencyPicker.setValue(currencies[1]);
                case "RON" -> currencyPicker.setValue(currencies[2]);
            }
            //Checking if the currencies were ever updated or if they are set to their default -1.0 value
            if (exchangeRateEUR != -1.0 && exchangeRateRON != -1.0) {
                //When selecting another currency from the currency picker...
                currencyPicker.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
                    //Updating the currency with the newly picked one
                    currency = currencies[newValue.intValue()];
                    Runnable rebuildResources = () -> {
                        try {
                            //Setting the boolean variable used by DataProcessing.createPDF method to true
                            // so that the method generates a new PDF document with the new currency
                            changedCurrency = true;
                            //Rebuilding our resources
                            BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                            buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                            buildUserSettings.close();
                            //Creating the usable dataset, graphs and interpretations again so that it uses the new currency
                            processData.prepareData();
                            //Recreating predictions and prediction graphs so that they use the newly set currency
                            if (processData.predictionsGenerated) {
                                processData.predictEvolutions(predictionValue);
                                processData.createSalaryGraphWithPredictionsForEverybody();
                            }
                        } catch (IOException ignored) {
                        }

                        Platform.runLater(() -> {
                            promptAnimator.setChild(null);
                            darkOverlayAnimator.setChild(null);
                            backgroundOperations.setVisible(false);
                            darkOverlay.setVisible(false);
                        });
                    };
                    promptAnimator.setChild(new Pane(backgroundOperations));
                    darkOverlayAnimator.setChild(new Pane(darkOverlay));
                    try {
                        loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    backgroundOperations.setVisible(true);
                    darkOverlay.setVisible(true);
                    new Thread(rebuildResources).start();
                }));
            } else currencyPicker.setDisable(true);

            //Setting up the theme toggle
            if (displayMode.equals("Dark")) {
                darkModeButtonGlyph.setFitHeight(50);
                lightModeButtonGlyph.setFitHeight(35);
            } else {
                lightModeButtonGlyph.setFitHeight(50);
                darkModeButtonGlyph.setFitHeight(35);
            }

            //Setting up the information prompt
            sourcesToggle.setOnAction(e -> {
                if (contentCredits.isVisible()) {
                    contentCredits.setVisible(false);
                    contentSources.setVisible(true);
                } else sourcesToggle.setSelected(true);
            });
            creditsToggle.setOnAction(e -> {
                if (contentSources.isVisible()) {
                    contentSources.setVisible(false);
                    contentCredits.setVisible(true);
                } else creditsToggle.setSelected(true);
            });
            Tooltip USDeptOfLaborHyperlinkDescription = new Tooltip(language.equals("EN") ? "Opens the United States Department of Labor website in your default browser." : language.equals("FR") ? "Ouvre le site web du Département du Travail des États Unis dans votre navigateur." : "Deschide site-ul web al Departamentului de Muncă al Statelor Unite în navigatorul dumneavoastră.");
            USDeptOfLaborHyperlinkDescription.setFont(new Font("Calibri", 13));
            USDeptOfLaborHyperlinkDescription.setShowDelay(Duration.millis(200));
            USDeptOfLaborHyperlink.setOnAction(a -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.dol.gov/agencies/wb/data/earnings/median-annual-sex-race-hispanic-ethnicity"));
                } catch (IOException | URISyntaxException ignored) {
                }
            });
            USDeptOfLaborHyperlink.setTooltip(USDeptOfLaborHyperlinkDescription);
            usDeptOfLaborYearRangeLabel.setText(usDeptOfLaborYearRangeLabel.getText() + processData.dataset[0][1] + " - " + processData.dataset[processData.dataset.length - 1][1] + ".");
            try {
                ERALogoImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-" + displayMode + ".png")));
            } catch (FileNotFoundException ignored) {
            }
            Tooltip ERAHyperlinkDescription = new Tooltip(language.equals("EN") ? "Opens the ExchangeRates-API website in your default browser." : language.equals("FR") ? "Ouvre le site web d'ExchangeRates-API dans votre navigateur." : "Deschide site-ul web al ExchangeRates-API în navigatorul dumneavoastră.");
            ERAHyperlinkDescription.setFont(new Font("Calibri", 13));
            ERAHyperlinkDescription.setShowDelay(Duration.millis(200));
            ERAHyperlink.setOnAction(a -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.exchangerate-api.com/"));
                } catch (IOException | URISyntaxException ignored) {
                }
            });
            ERAHyperlink.setTooltip(ERAHyperlinkDescription);
            if (exchangeRateEUR != -1.0 && exchangeRateRON != -1.0)
                ERALastUpdatedLabel.setText(ERALastUpdatedLabel.getText() + (exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) < 10 ? "0" + exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) : exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH)) + (exchangeRateLastUpdated.get(GregorianCalendar.MONTH) < 10 ? ".0" : ".") + exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.YEAR));
            else
                ERALastUpdatedLabel.setText(language.equals("EN") ? "Exchange rates were never updated." : language.equals("FR") ? "Les taux d'échange n'étaient jamais actualisés." : "Ratele de schimb nu au fost niciodată actualizate.");
            if (displayMode.equals("Light")) {
                try {
                    appIconCreditsImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
                } catch (FileNotFoundException ignored) {
                }
            } else {
                try {
                    appIconCreditsImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png")));
                } catch (FileNotFoundException ignored) {
                }
            }
            versionText.setText(versionText.getText() + Updater.getCurrentAppVersion());

            //Displaying the discard predictions button if the user generated predictions
            discardPredictionsButton.setVisible(processData.predictionsGenerated);
        }
    }

    //Launch time!
    public static void main(String[] args) throws IOException {
        //TODO cleanup main()
        //FileUtils.copyURLToFile(URI.create("https://codeload.github.com/Alin63992/GenderGapAnalyser-experiments/zip/refs/heads/main").toURL(), new File("update"), 2000, 15000);
        //System.out.println("GenderGapAnalyser-main".length());
        //System.out.println("GenderGapAnalyser-experiments-main".length());
        String destinationFolder = new File(".").getCanonicalPath();
        String s = "C:\\Users\\alin1\\OneDrive\\Desktop\\Uni stuff\\Licenta\\GenderGapAnalyser\\UpdateInfo.txt";
        launch();
    }
}