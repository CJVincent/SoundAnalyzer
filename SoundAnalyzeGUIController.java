/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package soundtest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * FXML Controller class
 *
 * @author Chris
 */
public class SoundAnalyzeGUIController implements Initializable {


    @FXML
    private TextArea resultField; // displays results of analysis
    @FXML
    private ListView<String> chosenFilesList;// lists selected files
    @FXML
    private ListView<String> directoryList; // lists files in directory, allows for multiple selection
    @FXML
    private Button selectDirectoryButton;

    private File chosenFile; //used in select file option
    private File chosenDirectory; // used in select directory option
    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;
    private List<Path> chosenFiles; //internal list of selected files
    private Service thread;
    @FXML
    private MenuItem exportButton;
    @FXML
    private Button selectFileButton;
    @FXML
    private GridPane buttonGrid;
    @FXML
    private Button findLoudestSecondButton;
    @FXML
    private Button findAvgAmplitudeButton;
    @FXML
    private Button findLoudestSongButton;
    @FXML
    private Button findMaxAmplitudeButton;
    @FXML
    private Button cancelButton;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button clearButton;
    @FXML
    private ProgressBar progressBar;

    //setup
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chosenFiles = new ArrayList<Path>();
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Music"));
        directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Music"));
        directoryList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        chosenFilesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //add change listener for list view
        directoryList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() 
                {
                    public void changed(ObservableValue<? extends String> ov, 
                    String old_val, String new_val)
                    {
                       chosenFiles.clear();
                       chosenFilesList.getItems().clear();
                       for (String s : directoryList.getSelectionModel().getSelectedItems())
                        {
                            if (s!=null)
                            {
                            chosenFilesList.getItems().add(s);
                            chosenFiles.add(Paths.get(chosenDirectory.toString(),s).toAbsolutePath());

                            }
                        }

                    }
                });
    }    

    //lets user select a file to add to chosenFiles
    @FXML
    private void handleSelectFileButtonEvent(ActionEvent event) throws IOException, Exception {
        
        fileChooser.setTitle("Choose WAV File...");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV", "*.wav"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3", "*.mp3"));
        chosenFile = fileChooser.showOpenDialog((Stage)((Node)(event.getSource())).getScene().getWindow());

        if (chosenFile != null)
        {
            fileChooser.setInitialDirectory(chosenFile.getParentFile());
            if(!chosenFiles.contains(chosenFile.toPath().toAbsolutePath()))
            {
            chosenFilesList.getItems().add(trimPath(chosenFile.toPath()));
            chosenFiles.add(chosenFile.toPath().toAbsolutePath());
            }
        }
    }
    //lets user choose a directory to select WAV files from
    @FXML
    private void handleSelectDirectoryButtonEvent(ActionEvent event) throws IOException 
    {
        directoryChooser.setTitle("Choose Directory...");
        chosenDirectory = directoryChooser.showDialog((Stage)((Node)(event.getSource())).getScene().getWindow());

        if (chosenDirectory != null)
        {
            directoryChooser.setInitialDirectory(chosenDirectory);
            ObservableList<String> files = FXCollections.observableArrayList();
            DirectoryStream<Path> dStream = Files.newDirectoryStream(chosenDirectory.toPath(), "*.{wav,mp3}");
            for (Path p : dStream)
            {
                files.add(trimPath(p));
            }
            directoryList.setItems(files);
            selectDirectoryButton.setText("Change Directory...");
        }
    }
    private String trimPath(Path filePath)
    {
        if (filePath.getNameCount() > 0)
            filePath =filePath.subpath(filePath.getNameCount()-1,filePath.getNameCount()); //reduces filepath to just name of file
        return filePath.toString();
    }

    @FXML
    private void handleFindLoudestSecondButtonEvent(ActionEvent event) throws UnsupportedAudioFileException, IOException, Exception 
    {
        doFunction("loudestSec");
    }

    @FXML
    private void handleFindMaxAmplitudeButtonEvent(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException, Exception 
    {
        doFunction("max");
    }

    @FXML
    private void handleFindAvgAmplitudeButtonEvent(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException, Exception 
    {
        doFunction("avg");
    }


    @FXML
    private void handleFindLoudestSongButtonEvent(ActionEvent event) throws UnsupportedAudioFileException, IOException, Exception 
    {
       doFunction("loudestSong");
    }
    @FXML
    private void handleClearButtonEvent(ActionEvent event) 
    {
        chosenFilesList.getItems().clear();
        chosenFiles.clear();
        directoryList.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleExportButtonEvent(ActionEvent event) throws IOException 
    {
        System.out.println(resultField.getText());
        fileChooser.setTitle("Save results...");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT", "*.txt"));
        File file = fileChooser.showSaveDialog(exportButton.getParentPopup().getScene().getWindow());
        if(file != null)
        {
            try (PrintWriter writer = new PrintWriter(file)) 
            {
                writer.print(resultField.getText());
            }
        }

    }

    @FXML
    private void handleMouseExitEvent(MouseEvent event) {
        ((Node)event.getSource()).getScene().setCursor(javafx.scene.Cursor.DEFAULT);
    }

    @FXML
    private void handleMouseEnterEvent(MouseEvent event) {
        if (!((Node)event.getSource()).isDisable())
        ((Node)event.getSource()).getScene().setCursor(javafx.scene.Cursor.HAND);
        else
        ((Node)event.getSource()).getScene().setCursor(javafx.scene.Cursor.WAIT);
    }
    @FXML
    private void handleRemoveButtonEvent(ActionEvent event) 
    {
        //update internal list before removing from UI list
        for(String s:chosenFilesList.getSelectionModel().getSelectedItems())
        {   
            for (int i = 0; i < chosenFiles.size(); i++)
            {
                Path p = chosenFiles.get(i);
                if(s.equals(trimPath(p)))
                    chosenFiles.remove(p);
            }
        }
        chosenFilesList.getItems().removeAll(chosenFilesList.getSelectionModel().getSelectedItems());
    }
    //attempts to cancel current task, calculations already in execution may still finish
    @FXML
    private void handleCancelButtonEvent(ActionEvent event) 
    {
        thread.cancel();
    }
    private void disableButtons()
    {
        findMaxAmplitudeButton.setDisable(true);
        findAvgAmplitudeButton.setDisable(true);
        findLoudestSongButton.setDisable(true);
        findLoudestSecondButton.setDisable(true);
        for (MenuItem m : directoryList.getContextMenu().getItems())
            m.setDisable(true);
        exportButton.setDisable(true);
        cancelButton.setDisable(false);
        cancelButton.setVisible(true);
                
    }
    private void enableButtons()
    {
        findMaxAmplitudeButton.setDisable(false);
        findAvgAmplitudeButton.setDisable(false);
        findLoudestSongButton.setDisable(false);
        findLoudestSecondButton.setDisable(false);
        for (MenuItem m : directoryList.getContextMenu().getItems())
            m.setDisable(false);
        exportButton.setDisable(false);
        cancelButton.setDisable(true);
        cancelButton.setVisible(false);
    }
    private void doFunction(final String function)
    {
         if (chosenFiles.size() > 0)
        {
            resultField.clear();
            disableButtons();
            progressBar.setVisible(true);
            //Services and Tasks keep UI from freezing during calculation
            final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            thread = new Service() {
                @Override
                protected Task createTask() {
                    return new Task() {
                        @Override
                        protected Object call() throws Exception {
                            if (!function.equals("loudestSong"))
                            {
                                for (final Path p : chosenFiles)
                                {
                                    if (isCancelled())
                                    {
                                        return null;
                                    }
                                executor.execute(new Runnable(){
                                    @Override
                                    public void run() {
                                        Result r = new Result();
                                        try {
                                           if (function.equals("max"))
                                           {r = SoundAnalyze.calculateMaxAmplitude(p);}
                                           if (function.equals("avg"))
                                           {r = SoundAnalyze.calculateAvgAmplitude(p);}
                                           if (function.equals("loudestSec"))
                                           {r = SoundAnalyze.calculateLoudestSecond(p);}
                                           if (!isCancelled())
                                           {resultField.appendText(r.getOutput() + "\n");
                                           updateProgress(((ThreadPoolExecutor)executor).getCompletedTaskCount(), chosenFiles.size()-1);
                                           }
                                        } catch (Exception ex) {
                                            Logger.getLogger(SoundAnalyzeGUIController.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                });
                                }
                                executor.shutdown();
                                executor.awaitTermination(1, TimeUnit.MINUTES);
                            }
                            else
                            {
                                resultField.clear();
                                if (chosenFiles.size() == 1)
                                    resultField.setText("The loudest song in the list is " + trimPath(chosenFiles.get(0)));
                                else
                                    resultField.setText(SoundAnalyze.calculateLoudestSongInList((ArrayList<Path>) chosenFiles).getOutput());
                            }
                            return null;
                        }
                        @Override
                        protected void succeeded() {
                            super.succeeded();
                            enableButtons();
                            progressBar.setVisible(false);
                        }

                        @Override
                        protected void failed() {
                            super.failed();
                            enableButtons();
                            progressBar.setVisible(false);
                        }

                        @Override
                        protected void cancelled() {
                            super.cancelled();
                            enableButtons();
                            progressBar.setVisible(false);
                        }
                    };
                }
            };
            progressBar.progressProperty().bind(thread.progressProperty());
            thread.start();
        }
        else
        {
            resultField.setText("No files selected.");
            exportButton.setDisable(true);
        }
    }
}