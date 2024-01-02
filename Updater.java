package com.gendergapanalyser.gendergapanalyser;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Updater {
    //Variable that holds the date this update will have been published
    protected static final GregorianCalendar appCurrentUpdateDate = new GregorianCalendar(2023, Calendar.NOVEMBER, 27);
    //Variable that holds the number of revisions that this update will have been published
    private static final int dailyRevision = 1;
    //Array that holds the update details
    private static final String[] updateDetails = {"", ""};

    public static String getCurrentAppVersion() {
        return appCurrentUpdateDate.get(GregorianCalendar.DAY_OF_MONTH) + "." + (appCurrentUpdateDate.get(GregorianCalendar.MONTH) + 1) + "." + appCurrentUpdateDate.get(GregorianCalendar.YEAR) + "." + dailyRevision;
    }

    public static String[] checkForUpdate() throws IOException, URISyntaxException {
        //Preparing to connect to the GitHub API to get new update info
        HttpURLConnection connection = (HttpURLConnection) new URI("https://api.github.com/repos/Alin63992/GenderGapAnalyser-experiments/releases/latest").toURL().openConnection();
        connection.setConnectTimeout(500);
        connection.setReadTimeout(1000);
        connection.addRequestProperty("User-Agent", "Mozilla/5.0");
        try {
            connection.connect();
            //Saving the JSON response
            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) (connection.getResponseCode() == 200 ? connection.getContent() : connection.getErrorStream())));
            String output;
            StringBuilder json = new StringBuilder();
            while ((output = br.readLine()) != null)
                json.append(output);
            JSONObject obj = (JSONObject) JSONValue.parse(json.toString());
            boolean gitVersionHigherThanLocalVersion = false;
            String formattedVersionNumber = obj.get("tag_name").toString();
            if (!formattedVersionNumber.equals(getCurrentAppVersion())) {
                if (Integer.parseInt(formattedVersionNumber.split("\\.")[0]) > appCurrentUpdateDate.get(GregorianCalendar.DAY_OF_MONTH) || Integer.parseInt(formattedVersionNumber.split("\\.")[1]) - 1 > appCurrentUpdateDate.get(GregorianCalendar.MONTH) || Integer.parseInt(formattedVersionNumber.split("\\.")[2]) > appCurrentUpdateDate.get(GregorianCalendar.YEAR) || Integer.parseInt(formattedVersionNumber.split("\\.")[3]) > dailyRevision) {
                    updateDetails[0] = formattedVersionNumber;
                    gitVersionHigherThanLocalVersion = true;
                }
            }
            if (gitVersionHigherThanLocalVersion) {
                updateDetails[1] = obj.get("body").toString();
            }
        }
        catch (IOException ignored) {}
        return updateDetails;
    }


    public static void performUpdate() throws IOException {
        Runnable applyUpdate = () -> {
            ArrayList<String> copiedFiles = new ArrayList<>();

            //Setting the app's current state as being in the updating stage, in the correct update stage,
            // to display the correct message on the splash screen.
            Main.appState = "UpdateStage-Update";

            //Backing up the currently installed app files into the GenderGapAnalyser-Backup folder
            String backupFolder = "GenderGapAnalyser-Backup";
            try {
                //Traversing all the app files in the app directory
                Files.walk(Path.of("")).forEach(path -> {
                    //Ignoring the .git, GenderGapAnalyser-Backup and target folders since they don't get modified during the update
                    if (!path.startsWith(".git") && !path.startsWith(backupFolder) && !path.startsWith("target") && !path.endsWith(".updateinprogress") && !path.endsWith(".rollbackinprogress") && !path.endsWith(".cleanupinprogress")) {
                        try {
                            //If the item we're on is a folder, we create it within the backup folder (ignoring the blank entry that's found first)
                            if (path.toFile().isDirectory()) {
                                Files.createDirectories(Path.of(backupFolder + (!path.toString().isEmpty() ? File.separator + path : "")));
                            }
                            //If it's a file, we copy it in its folder within the backup folder
                            else {
                                Files.copy(path, Path.of(backupFolder + File.separator + path));
                            }
                        } catch (IOException ignored) {}
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //Preparing the update
            File downloadedUpdateArchive = new File("GenderGapAnalyser-Update.zip");
            BufferedWriter writeUpdateLog = null;
            BufferedWriter writeRemainingFiles = null;
            ZipFile archivedUpdate = null;
            try {
                //Resuming update, if a crash happened during the update
                //If the update info log file exists
                if (new File("UpdateInfo.txt").exists()) {
                    BufferedReader br = new BufferedReader(new FileReader("UpdateInfo.txt"));
                    String line;
                    //If the partially copied update version is the same as the newest update available
                    if (br.readLine().split("=")[1].equals(updateDetails[0])) {
                        //Preparing the copiedFiles archive
                        while ((line = br.readLine()) != null) {
                            copiedFiles.add(line);
                        }
                        br.close();
                    }
                    //If it isn't
                    else {
                        //Deleting the downloaded update archive if it exists
                        if (downloadedUpdateArchive.exists())
                            downloadedUpdateArchive.delete();
                        //Recreating the update info log file to contain the newest version
                        br.close();
                        writeUpdateLog = new BufferedWriter(new FileWriter("UpdateInfo.txt"));
                        writeUpdateLog.write("Version=" + Main.updateDetails[0]);
                        writeUpdateLog.newLine();
                        writeUpdateLog.close();
                    }
                }
                else {
                    //Creating the update info log file
                    writeUpdateLog = new BufferedWriter(new FileWriter("UpdateInfo.txt"));
                    writeUpdateLog.write("Version=" + Main.updateDetails[0]);
                    writeUpdateLog.newLine();
                    writeUpdateLog.close();
                }
                if (!downloadedUpdateArchive.exists()) {
                    //Downloading the GitHub archive with the app files, if it doesn't exist
                    FileUtils.copyURLToFile(URI.create("https://codeload.github.com/Alin63992/GenderGapAnalyser-experiments/zip/refs/tags/" + updateDetails[0]).toURL(), downloadedUpdateArchive, 2000, 15000);
                }
                archivedUpdate = new ZipFile(downloadedUpdateArchive);

                //Setting the destination directory in which the update will be unzipped and the contents of the update are copied
                String destinationFolder = new File(".").getCanonicalPath();
                //Opening the update info log file
                writeUpdateLog = new BufferedWriter(new FileWriter("UpdateInfo.txt", new File("UpdateInfo.txt").exists()));

                //Unzipping the update archive to reveal the GenderGapAnalyser-main folder
                Enumeration<? extends ZipEntry> enu = archivedUpdate.entries();
                //Traversing the archive's contents
                while (enu.hasMoreElements()) {
                    boolean fileIsNew = false;
                    ZipEntry entry = enu.nextElement();
                    File zipEntryToBeExtracted = new File(destinationFolder, entry.getName().substring(("GenderGapAnalyser-experiments-" + updateDetails[0]).length() + 1)); //the substring excludes "GenderGapAnalyser-<new version number>/" from the path so that everything copies to the main app folders and not to the subfolder of the zip archive created by GitHub which contains all the new app files
                    //Checking if the zip entry isn't empty
                    // (so that the entry's path isn't the root folder of the app itself), or if the entry's path
                    // starts with the root folder of the app (to not be vulnerable to zip-slipping - learn more at
                    // https://snyk.io/research/zip-slip-vulnerability)
                    if (!entry.getName().startsWith(".idea") && !entry.getName().startsWith(".git") && !zipEntryToBeExtracted.getCanonicalPath().equals(destinationFolder) && zipEntryToBeExtracted.getCanonicalPath().startsWith(destinationFolder)) {
                        //Checking if the file wasn't already copied or the folder already created
                        if (!copiedFiles.contains(zipEntryToBeExtracted.getCanonicalPath())) {
                            //Checking if the zip entry is a folder or a file
                            if (entry.isDirectory()) {
                                //Creating the folder
                                Files.createDirectories(Path.of(zipEntryToBeExtracted.getCanonicalPath()));
                            } else {
                                //Checking if a file with the same name exists already in the app file
                                if (!new File(zipEntryToBeExtracted.getCanonicalPath()).exists())
                                    //Categorizing the file as new, if not
                                    fileIsNew = true;
                                //Copying the file to the app files
                                //TODO doesn't work for app icons, ERA logos and the loading gifs (FileSystemException: file in use). Maybe schedule them to be replaced before the update done alert is shown?
                                try {
                                    Files.copy(archivedUpdate.getInputStream(entry), Path.of(zipEntryToBeExtracted.getCanonicalPath()), StandardCopyOption.REPLACE_EXISTING);
                                }
                                catch (FileSystemException e){
                                    System.out.println(zipEntryToBeExtracted.getCanonicalPath());
                                    Files.copy(archivedUpdate.getInputStream(entry), Path.of(zipEntryToBeExtracted.getCanonicalPath() + ".copy"));
                                    fileIsNew = true;
                                    //Main.filesInUseToBeReplacedAtNextStart.createNewFile();
                                    if (writeRemainingFiles == null) {
                                        writeRemainingFiles = new BufferedWriter(new FileWriter(Main.filesInUseToBeReplacedAtNextStart.getName()));
                                    }
                                    writeRemainingFiles.write(Path.of(zipEntryToBeExtracted.getCanonicalPath()).toString());
                                    writeRemainingFiles.newLine();
                                }
                            }
                            //Adding the new folder or the newly copied file to the copiedFiles array and the update log
                            copiedFiles.add(fileIsNew ? "New:" + zipEntryToBeExtracted.getName() : zipEntryToBeExtracted.getCanonicalPath());
                            if (fileIsNew)
                                writeUpdateLog.write("New:");
                            writeUpdateLog.write(zipEntryToBeExtracted.getCanonicalPath());
                            writeUpdateLog.newLine();
                            //For every 10 operations made, we flush the writer's buffer to the file so that the update
                            // log is always up to date on the progress.
                            if (copiedFiles.size() % 10 == 0)
                                writeUpdateLog.flush();
                        }
                    }
                }
                archivedUpdate.close();
                writeUpdateLog.close();
                writeRemainingFiles.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                try {
                    //Setting the screen to inform the user that an error occurred and a rollback is in progress
                    Main.appState = "UpdateStage-Rollback";
                    /*Platform.runLater(() -> {
                        try {
                            Scene splash = new Scene(new FXMLLoader(Updater.class.getResource("AppScreens/SplashScreen-" + Main.language + ".fxml")).load());
                            splash.getStylesheets().setAll(Objects.requireNonNull(Updater.class.getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                            Main.getCurrentStage().setScene(splash);
                        }
                        catch (IOException ignored) {}
                    });*/

                    archivedUpdate.close();

                    //Closing the stream that writes to the update info log file, in case an error occurs
                    // before the file is closed for writing
                    writeUpdateLog.close();
                    //Closing the stream that writes to the file containing files in use that remain to be replaced,
                    // in case an error occurs before the file is closed for writing
                    writeRemainingFiles.close();

                    //Traversing the array which holds the new/modified files
                    for (String file: copiedFiles) {
                        //Deleting every file or directory newly added to the existing files, that's specific to the new update, to save storage
                        if (file.startsWith("New:")) {
                            File filePathWithoutNewPrefix = new File(file.substring(4));
                            if (filePathWithoutNewPrefix.isDirectory())
                                FileUtils.deleteDirectory(filePathWithoutNewPrefix);
                            else filePathWithoutNewPrefix.delete();
                        }
                        else {
                            //Copying any file (not folder) that was overwritten during the update from the backup folder back to its original location
                            if (!new File(file).isDirectory()) {
                                System.out.println(file);
                                String destinationFolder = new File(".").getCanonicalPath();
                                System.out.println(destinationFolder);
                                File filePathIncludingBackupFolder = new File(file.replace(destinationFolder, destinationFolder + File.separator + backupFolder));
                                System.out.println(filePathIncludingBackupFolder.getPath());
                                Files.copy(filePathIncludingBackupFolder.toPath(), new File(file).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }

                    //Deleting the temporary files that would replace the files in use at the next start
                    if (Main.filesInUseToBeReplacedAtNextStart.exists()) {
                        BufferedReader readRemainingFiles = new BufferedReader(new FileReader(Main.filesInUseToBeReplacedAtNextStart.getName()));
                        String line;
                        while ((line = readRemainingFiles.readLine()) != null) {
                            new File(line).delete();
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            //Cleaning up
            try {
                /*if (Main.updateInProgress.exists())
                    Main.updateInProgress.delete();*/
                Main.appState = "UpdateStage-Cleanup";
                /*Platform.runLater(() -> {
                    try {
                        Scene splash = new Scene(new FXMLLoader(Updater.class.getResource("AppScreens/SplashScreen-" + Main.language + ".fxml")).load());
                        splash.getStylesheets().setAll(Objects.requireNonNull(Updater.class.getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                        Main.getCurrentStage().setScene(splash);
                    }
                    catch (IOException ignored) {}
                });*/
                downloadedUpdateArchive.delete();
                FileUtils.deleteDirectory(new File(backupFolder));
                new File("UpdateInfo.txt").delete();
                Main.interruptThreads = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //Finishing the update
            Platform.runLater(() -> {
                Alert updateDone;
                if (!Main.appState.equals("UpdateState-Rollback")) {
                    updateDone = new Alert(Alert.AlertType.INFORMATION);
                    if (Main.language.equals("EN")) {
                        updateDone.setHeaderText("Done updating!");
                        updateDone.setContentText("The application now has the most recent updates installed!\nBefore you can use the new features and experience the new bug fixes and improvements, the app will close, and you'll need to run it again from your IDE.\nEnjoy the app!");
                    } else if (Main.language.equals("FR")) {
                        updateDone.setHeaderText("Actualisation finie !");
                        updateDone.setContentText("L'application maintenant à installé les plus récents mises à jour !\nAvant que vous pouvez utiliser les nouveaux fonctionnalités et découvrir les améliorations, l'application va se fermer, et vous nécessitez la rélancer depuis votre IDE.\nSavourez l'application !");
                    } else {
                        updateDone.setHeaderText("Actualizare finalizată!");
                        updateDone.setContentText("Aplicația are acum instalate cele mai recente actualizări!\nÎnainte să puteți utiliza noile funcționalități și descoperi îmbunătățirile, aplicația se va închide, și este necesar să o relansați din IDE-ul dumneavoastră.\nBucurați-vă de aplicație!");
                    }
                }
                else {
                    updateDone = new Alert(Alert.AlertType.ERROR);
                    if (Main.language.equals("EN")) {
                        updateDone.setHeaderText("Done rolling back changes!");
                        updateDone.setContentText("The application encountered an error while updating and it rolled back the changes, so you can still use the version you had installed before!\nIn order to use it again, the app will close, and you'll need to run it again from your IDE.\nEnjoy the app!");
                    } else if (Main.language.equals("FR")) {
                        updateDone.setHeaderText("Annulation des changements finie !");
                        updateDone.setContentText("L'application à rencontré une erreur pendant la mise à jour et a annulé les changements, et donc vous pouvez utiliser la version qui était installée avant !\nPour pouvoir l'utiliser du nouveau, l'application va se fermer, et vous nécessitez la rélancer depuis votre IDE.\nSavourez l'application !");
                    } else {
                        updateDone.setHeaderText("Anularea modificărilor finalizată!");
                        updateDone.setContentText("Aplicația a întâlnit o eroare în timpul actualizării și a anulat modificările, deci puteți folosi versiunea care era instalată înainte!\nPentru a o putea folosi din nou, aplicația se va închide, și este necesar să o relansați din IDE-ul dumneavoastră.\nBucurați-vă de aplicație!");
                    }
                }
                updateDone.setTitle(updateDone.getHeaderText());
                updateDone.getDialogPane().setMaxWidth(750);
                try {
                    ((Stage)updateDone.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-" + (updateDone.getAlertType().equals(Alert.AlertType.ERROR) ? "error.png" : "information.png"))));
                    FileUtils.deleteDirectory(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/ProgressFiles"));
                } catch (IOException ignored) {}
                Main.getCurrentStage().close();
                if (Main.updateInProgress.exists())
                    Main.updateInProgress.delete();
                updateDone.show();
            });
        };
        if (!Main.updateInProgress.exists()) {
            Main.cleanUp();
            Files.createDirectories(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/ProgressFiles"));
            Main.updateInProgress.createNewFile();
        }
        //Setting the screen to inform the user that an update is in progress
        Main.getCurrentStage().setTitle("Gender Gap Analyser");
        Scene splash = new Scene(new FXMLLoader(Updater.class.getResource("AppScreens/SplashScreen-" + Main.language + ".fxml")).load());
        splash.getStylesheets().setAll(Objects.requireNonNull(Updater.class.getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        Main.getCurrentStage().setScene(splash);
        new Thread(applyUpdate).start();
    }
}
