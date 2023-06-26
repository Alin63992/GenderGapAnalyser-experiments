package com.gendergapanalyser.gendergapanalyser;

import animatefx.animation.FadeOut;
import eu.iamgio.animated.transition.AnimatedSwitcher;
import eu.iamgio.animated.transition.AnimatedThemeSwitcher;
import eu.iamgio.animated.transition.Animation;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.validator.routines.EmailValidator;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class Main extends Application implements Initializable {
    private static Stage currentStage;
    @FXML
    private AnchorPane emptyAnchor;
    @FXML
    private AnimatedSwitcher animatedSwitcher;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private ChoiceBox<String> languagePicker;
    @FXML
    private Button lightModeButton;
    @FXML
    private Button darkModeButton;
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
    private TextField emailField;
    @FXML
    private Text invalidEmailWarning;
    @FXML
    private Button graphsButton;
    @FXML
    private Button closeDiscardConfirmation;
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
    protected static DataProcessing processData;
    protected static String displayMode;
    protected static String language;
    protected static final String[] languages = {"English", "Français", "Română"};
    protected static final String[] languagesShort = {"EN", "FR", "RO"};
    public static double dragX;
    public static double dragY;
    private static final Thread downloadDataset = new Thread(new GetUpdatedDatasetInBackground());
    protected static int predictionValue = 0;
    protected static String email = "";
    protected static boolean connectError = false;

    //Function used to set the currently open window to be used in the future
    public static void setCurrentStage(Stage s) {
        Main.currentStage = s;
    }

    //Function used by the DisplayEvolutionGraph class to get the currently open window
    public static Stage getCurrentStage() {
        return Main.currentStage;
    }

    //Function used to delete the generated graphs and quit the app
    public static void exitAppMain() {
        //Killing the thread that downloads the updated dataset, if it's still running
        if (downloadDataset.isAlive()) {
            downloadDataset.interrupt();
        }

        //Locating the Graphs folder
        File graphsFolder = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs");
        //Deleting every graph in the Graphs folder
        for (File graph : Objects.requireNonNull(graphsFolder.listFiles())) {
            graph.delete();
        }

        //Deleting the downloaded dataset file (if it exists)
        try {
            Files.delete(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/0DownloadedDataset.csv"));
        } catch (IOException ignored) {}

        //Deleting the generated PDF report (if it exists)
        try {
            Files.delete(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf"));
        } catch (IOException ignored) {}
        //Closing the window
        getCurrentStage().close();
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
        BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/UserSettings.txt"));
        buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language);
        buildUserSettings.close();
        if (processData.predictionsGenerated) processData.createSalaryGraphWithPredictionsForEverybody();
        processData.createSalaryGraphForEverybody();
        AnimatedThemeSwitcher switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
        switchTheme.init();
        Main.getCurrentStage().getScene().getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        switchTheme.pause();
    }

    //Function used to close the main menu screen and open the graph screen in a new window
    @FXML
    private void goToGraphPage() throws IOException {
        //Preparing a new non-resizable window with a title, that displays the graph screen
        Stage graphStage = new Stage();
        graphStage.initStyle(StageStyle.UNDECORATED);
        graphStage.setTitle(language.equals("EN") ? "Evolution Graph" : language.equals("FR") ? "Graphe d'Évolution" : "Grafic de Evoluție");
        graphStage.setScene(new Scene(new FXMLLoader(getClass().getResource("DisplayEvolutionGraph-" + Main.language + ".fxml")).load()));
        graphStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        graphStage.setResizable(false);
        graphStage.centerOnScreen();
        //Opening the new graphs window
        graphStage.show();
        //Closing the current window (in this case, the main menu window)
        getCurrentStage().close();
        //Setting the graph window as the currently open window
        setCurrentStage(graphStage);
        //Setting the app icon that's going to be shown on the title bar and taskbar to the Gender Fluid free icon created by Vitaly Gorbachev, published on the flaticon website (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
        getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/AppIcon.png")));
    }

    @FXML
    private void goToAnalysisPage() throws IOException {
        //Preparing a new non-resizable window with a title, that displays the analysis screen
        Stage analysisStage = new Stage();
        analysisStage.initStyle(StageStyle.UNDECORATED);
        analysisStage.setTitle(language.equals("EN") ? "Interpretations" : language.equals("FR") ? "Interprétations" : "Interpretări");
        analysisStage.setScene(new Scene(new FXMLLoader(getClass().getResource("Analysis-" + Main.language + ".fxml")).load()));
        analysisStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        analysisStage.setResizable(false);
        analysisStage.centerOnScreen();
        //Opening the new analysis window
        analysisStage.show();
        //Closing the current window (in this case, the main menu window)
        getCurrentStage().close();
        //Setting the analysis window as the currently open window
        setCurrentStage(analysisStage);
        //Setting the app icon that's going to be shown on the title bar and taskbar to the Gender Fluid free icon created by Vitaly Gorbachev, published on the flaticon website (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
        getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/AppIcon.png")));
    }

    //Function used to start the routine that creates the PDF that contains the interpretations, graph with all women's salaries, men's salaries and wage gap evolutions and the dataset
    @FXML
    private void generatePDF() throws IOException {
        FileInputStream image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/loading-" + displayMode + ".gif");
        loadingCircleImageView.setImage(new Image(image));
        image.close();
        backgroundOperations.setVisible(true);
        //Trying to open an already generated report PDF.
        //If there isn't one, it was generated in a different language than the current language of the application,
        // or if it was generated without including predictions when predictions exist or vice versa,
        // the report is regenerated.
        try {
            if (processData.predictionsGenerated && processData.PDFGeneratedWithPredictions && !processData.changedLanguage || !processData.predictionsGenerated && !processData.PDFGeneratedWithPredictions && !processData.changedLanguage) {
                //Locating an existing generated report PDF
                File existingPDF = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf");
                //Opening it
                Desktop.getDesktop().open(existingPDF);
                backgroundOperations.setVisible(false);
            }
            else throw new IllegalArgumentException();
        }
        catch (IllegalArgumentException e) {
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

    //Function used to hide or show the prompt which asks the user for the period of time they need wage prediction for
    @FXML
    private void togglePredictionPrompt() {
        //If the prompt is visible
        if (predictionPrompt.isVisible()) {
            //We set the text in the prediction input field back to the default
            predictionField.setText("1");
            //We hide the invalid number warning
            invalidNumberWarning.setVisible(false);
            //We set the 4 buttons on the main menu, the language picker and the display mode toggle on the menu bar to be selectable using the tab/arrow keys
            predictButton.setFocusTraversable(true);
            graphsButton.setFocusTraversable(true);
            analysisButton.setFocusTraversable(true);
            PDFButton.setFocusTraversable(true);
            mailButton.setFocusTraversable(true);
            languagePicker.setFocusTraversable(true);
            lightModeButton.setFocusTraversable(true);
            darkModeButton.setFocusTraversable(true);
            //We hide the prompt
            predictionPrompt.setVisible(false);
        }
        //If the prompt is hidden
        else {
            //Running the prediction function when the Enter key is pressed
            predictionPrompt.setOnKeyPressed(a -> attemptPrediction());
            //Setting the 4 main menu buttons, the language picker and the display mode toggle on the menu bar to not be accessible with tab/arrow keys
            predictButton.setFocusTraversable(false);
            graphsButton.setFocusTraversable(false);
            analysisButton.setFocusTraversable(false);
            PDFButton.setFocusTraversable(false);
            mailButton.setFocusTraversable(false);
            languagePicker.setFocusTraversable(false);
            lightModeButton.setFocusTraversable(false);
            darkModeButton.setFocusTraversable(false);
            //Showing the prompt
            predictionPrompt.setVisible(true);
            //Setting the focus on the prediction input field
            predictionField.requestFocus();
        }
    }

    //Function used to check whether the value of the prompt input and, if valid, trigger the prediction function in the DataProcessing class
    @FXML
    private void attemptPrediction() {
        try {
            int value = Integer.parseInt(predictionField.getText());
            if (value >= 1 && value <= 100) {
                predictionValue = Integer.parseInt(predictionField.getText());
                FileInputStream image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/loading-" + displayMode + ".gif");
                loadingCircleImageView.setImage(new Image(image));
                image.close();
                predictionPrompt.setVisible(false);
                predictButton.setFocusTraversable(false);
                graphsButton.setFocusTraversable(false);
                analysisButton.setFocusTraversable(false);
                PDFButton.setFocusTraversable(false);
                mailButton.setFocusTraversable(false);
                languagePicker.setFocusTraversable(false);
                lightModeButton.setFocusTraversable(false);
                darkModeButton.setFocusTraversable(false);
                backgroundOperations.setVisible(true);
                new Thread(new PredictInBackground()).start();
            } else invalidNumberWarning.setVisible(true);
        } catch (NumberFormatException e) {
            invalidNumberWarning.setVisible(true);
        } catch (IOException ignored) {}
    }

    //Function that executes the routine that deletes the generated predictions and then shows a confirmation screen with a button which reloads the main menu page
    @FXML
    private void discardPredictions() {
        processData.discardPredictions();
        predictButton.setFocusTraversable(false);
        graphsButton.setFocusTraversable(false);
        analysisButton.setFocusTraversable(false);
        PDFButton.setFocusTraversable(false);
        mailButton.setFocusTraversable(false);
        languagePicker.setFocusTraversable(false);
        lightModeButton.setFocusTraversable(false);
        darkModeButton.setFocusTraversable(false);
        discardPredictionsButton.setVisible(false);
        discardConfirmation.setVisible(true);
    }

    //Function used to hide or show the prompt which asks the user for the period of time they need wage prediction for
    @FXML
    private void toggleEmailPrompt() {
        //If the prompt is visible
        if (emailPrompt.isVisible()) {
            //We hide the invalid number warning
            invalidEmailWarning.setVisible(false);
            //We set the 4 buttons on the main menu, the language picker and the display mode toggle on the menu bar to be selectable using the tab/arrow keys
            predictButton.setFocusTraversable(true);
            graphsButton.setFocusTraversable(true);
            analysisButton.setFocusTraversable(true);
            PDFButton.setFocusTraversable(true);
            mailButton.setFocusTraversable(true);
            languagePicker.setFocusTraversable(true);
            lightModeButton.setFocusTraversable(true);
            darkModeButton.setFocusTraversable(true);
            //We hide the prompt
            emailPrompt.setVisible(false);
        }
        //If the prompt is hidden
        else {
            if (!connectError) {
                //Setting the 4 main menu buttons, the language picker and the display mode toggle on the menu bar to not be accessible with tab/arrow keys
                predictButton.setFocusTraversable(false);
                graphsButton.setFocusTraversable(false);
                analysisButton.setFocusTraversable(false);
                PDFButton.setFocusTraversable(false);
                mailButton.setFocusTraversable(false);
                languagePicker.setFocusTraversable(false);
                lightModeButton.setFocusTraversable(false);
                darkModeButton.setFocusTraversable(false);
                //Showing the prompt
                emailPrompt.setVisible(true);
                //Setting the focus on the prediction input field
                if (!email.equals("")) emailField.setText(email);
                emailField.requestFocus();
            }
            else {
                Alert errorSendingEmail = new Alert(Alert.AlertType.ERROR);
                errorSendingEmail.setTitle(Main.language.equals("EN") ? "No internet connection" : Main.language.equals("FR") ? "Pas de connexion internet" : "Fără conexiune internet");
                errorSendingEmail.setHeaderText(Main.language.equals("EN") ? "The report couldn't be sent!\nPlease check your internet connection, or wait for a bit then try again!" : Main.language.equals("FR") ? "Le rapport n'a pas pu être envoyé !\nVeuillez vérifier votre connexion internet, ou attendez un peu et réessayez !" : "Raportul nu a putut fi trimis!\nVă rugăm verificați conexiunea la internet, sau așteptați puțin si reîncercați!");
                errorSendingEmail.getDialogPane().setMaxWidth(750);
                errorSendingEmail.initStyle(StageStyle.UNDECORATED);
                if (displayMode.equals("Dark")) {
                    errorSendingEmail.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/DarkMode.css")).toExternalForm());
                    errorSendingEmail.getDialogPane().getStyleClass().add("alerts");
                }
                errorSendingEmail.show();
            }
        }
    }

    @FXML
    private void attemptSendMail() throws IOException {
        EmailValidator validator = EmailValidator.getInstance();
        if (validator.isValid(emailField.getText())) {
            invalidEmailWarning.setVisible(false);
            email = emailField.getText();
            emailPrompt.setVisible(false);
            Alert confirmInclusionOfUserData = new Alert(Alert.AlertType.CONFIRMATION);
            confirmInclusionOfUserData.setTitle(language.equals("EN") ? "Inclusion of user data" : language.equals("FR") ? "Inclusion des détails d'utilisateur" : "Includerea datelor utilizatorului");
            confirmInclusionOfUserData.setHeaderText(language.equals("EN") ? "The data below will be attached to the email containing the report. They are data taken from this computer and are used to help you match the data in the email and ensure they don't come from another computer, and they won't be stored anywhere other than in the email.\nDo you agree with the inclusion of said data and have the report sent?" : language.equals("FR") ? "Les données ci-dessous vont être attachées au courriel contenant le rapport. Ils sont des données provenant de cet ordinateur, et sont utilisées pour vous aider vérifier que le courriel vient d'ici et pas d'un autre ordinateur.\nÊtes-vous d'accord avec l'inclusion de ces données et avec l'envoi du rapport?" : "Detaliile de mai jos vor fi incluse în mail. Ele sunt folosite pentru a vă ajuta să vă asigurați că mail-ul vine de aici și nu de pe alt calculator.\nSunteți de acord cu includerea acestor date și trimiterea raportului?");
            switch (language) {
                case "EN" -> confirmInclusionOfUserData.setContentText("Username: " + System.getProperty("user.name") + "\nOperating System: " + System.getProperty("os.name") + "\nTimezone: " + System.getProperty("user.timezone"));
                case "FR" -> confirmInclusionOfUserData.setContentText("Nom d'utilisateur: " + System.getProperty("user.name") + "\nSystème d'exploitation: " + System.getProperty("os.name") + "\nFuseau horaire: " + System.getProperty("user.timezone"));
                case "RO" -> confirmInclusionOfUserData.setContentText("Nume de utilizator: " + System.getProperty("user.name") + "\nSistem de Operare: " + System.getProperty("os.name") + "\nFus orar: " + System.getProperty("user.timezone"));
            }
            ButtonType yesButton = new ButtonType(language.equals("EN") ? "Yes" : language.equals("FR") ? "Oui" : "Da", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType(language.equals("EN") ? "No" : language.equals("FR") ? "Non" : "Nu", ButtonBar.ButtonData.NO);
            confirmInclusionOfUserData.getButtonTypes().clear();
            confirmInclusionOfUserData.getButtonTypes().add(yesButton);
            confirmInclusionOfUserData.getButtonTypes().add(noButton);
            confirmInclusionOfUserData.getDialogPane().setMaxWidth(750);
            confirmInclusionOfUserData.initStyle(StageStyle.UNDECORATED);
            if (displayMode.equals("Dark")) {
                confirmInclusionOfUserData.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/DarkMode.css")).toExternalForm());
                confirmInclusionOfUserData.getDialogPane().getStyleClass().add("alerts");
            }
            Optional<ButtonType> confirmationResult = confirmInclusionOfUserData.showAndWait();
            if (confirmationResult.isPresent() && confirmationResult.get() == yesButton) {
                FileInputStream image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/loading-" + displayMode + ".gif");
                loadingCircleImageView.setImage(new Image(image));
                image.close();
                backgroundOperations.setVisible(true);
                new Thread(new SendEmailInBackground()).start();
            }
        }
        else invalidEmailWarning.setVisible(true);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            //Trying to download the dataset file from the U.S. Department of Labor server
            downloadDataset.start();

            //Checking to see if the user settings were already loaded by the GetUpdatedDatasetInBackground thread,
            // and loading them if not
            if (language == null && displayMode == null) {
                //Loading user settings (display mode and app language) from the UserSettings.txt file
                try {
                    BufferedReader loadUserSettings = new BufferedReader(new FileReader("src/main/resources/com/gendergapanalyser/gendergapanalyser/UserSettings.txt"));
                    String setting;
                    while ((setting = loadUserSettings.readLine()) != null) {
                        String[] settingParts = setting.split("=");
                        if (settingParts[0].equals("DisplayMode")) displayMode = settingParts[1];
                        else language = settingParts[1];
                    }
                    loadUserSettings.close();
                } catch (IOException ignored) {}
            }

            //Preparing the dataset and creating the plots
            processData = new DataProcessing();
            processData.prepareData();

            //Setting the primary stage so that other controllers can use it to display what they need displayed
            setCurrentStage(primaryStage);
            getCurrentStage().initStyle(StageStyle.UNDECORATED);

            //Setting the main menu to be shown on the application window
            getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("MainMenu-" + language + ".fxml")).load()));
            getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());

            //Setting the app icon that's going to be shown on the title bar and taskbar to the Gender Fluid free icon created by Vitaly Gorbachev, published on the flaticon website (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
            getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/AppIcon.png")));

            //Setting the window title
            getCurrentStage().setTitle(language.equals("EN") ? "Main Menu" : language.equals("FR") ? "Menu Principal" : "Meniu Principal");
            getCurrentStage().centerOnScreen();

            //Setting the window to be not resizable
            getCurrentStage().setResizable(false);

            //Opening the window
            getCurrentStage().show();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Making the window movable when dragging the embedded title bar
        titleBar.setOnMousePressed(event -> {
            dragX = getCurrentStage().getX() - event.getScreenX();
            dragY = getCurrentStage().getY() - event.getScreenY();
        });
        titleBar.setOnMouseDragged(event -> {
            getCurrentStage().setX(event.getScreenX() + dragX);
            getCurrentStage().setY(event.getScreenY() + dragY);
        });

        //Setting up the language picker
        languagePicker.setItems(FXCollections.observableArrayList(languages));
        switch (language) {
            case "EN" -> languagePicker.setValue(languages[0]);
            case "FR" -> languagePicker.setValue(languages[1]);
            case "RO" -> languagePicker.setValue(languages[2]);
        }
        //When selecting another language from the language picker...
        languagePicker.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                //Recreating the settings save file with the new current language and the display mode
                language = languagesShort[newValue.intValue()];
                BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/UserSettings.txt"));
                buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language);
                buildUserSettings.close();
                //Recreating graphs so that they use the newly set language
                if (processData.predictionsGenerated) processData.createSalaryGraphWithPredictionsForEverybody();
                processData.createSalaryGraphForEverybody();
                //Recreating analyses in the new language
                processData.performAnalysis();
                //Setting the boolean variable used by DataProcessing.createPDF method to true so that the method generates a new PDF document in a new Language
                Main.processData.changedLanguage = true;
                //Reloading the main menu screen so that it uses the new language
                getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("MainMenu-" + languagesShort[newValue.intValue()] + ".fxml")).load()));
                getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());
            } catch (IOException ignored) {}
        }));

        //Displaying the discard predictions button if the user generated predictions
        discardPredictionsButton.setVisible(processData.predictionsGenerated);

        //Setting the button that closes the predictions discarded confirmation box to reload the main menu on click
        closeDiscardConfirmation.setOnAction(action -> {
            predictButton.setFocusTraversable(true);
            graphsButton.setFocusTraversable(true);
            analysisButton.setFocusTraversable(true);
            PDFButton.setFocusTraversable(true);
            languagePicker.setFocusTraversable(true);
            lightModeButton.setFocusTraversable(true);
            darkModeButton.setFocusTraversable(true);
            discardConfirmation.setVisible(false);
        });
    }

    //Launch time!
    public static void main(String[] args) {
        launch();
    }
}