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
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    private TextField chosenFileField; // lists selected files
    @FXML
    private ListView<String> directoryList; // lists files in directory, allows for multiple selection
    @FXML
    private Button selectDirectoryButton;
    
    private File chosenFile;
    private File chosenDirectory;
    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;
    private List<Path> chosenFiles; //internal list of selected files
    @FXML
    private MenuItem exportButton;
    //setup
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        chosenFiles = new ArrayList<Path>();
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Music"));
        directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Music"));
        directoryList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //add change listener for list view
        directoryList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() 
                {
                    public void changed(ObservableValue<? extends String> ov, 
                    String old_val, String new_val)
                    {
                       chosenFileField.clear();
                       chosenFiles.clear();
                       for (String s : directoryList.getSelectionModel().getSelectedItems())
                        {
                            if (s!=null)
                            {
                            chosenFileField.appendText(s + " ");
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
        chosenFile = fileChooser.showOpenDialog((Stage)((Node)(event.getSource())).getScene().getWindow());

        if (chosenFile != null)
        {
            fileChooser.setInitialDirectory(chosenFile.getParentFile());
            if(!chosenFiles.contains(chosenFile.toPath().toAbsolutePath()))
            {
            chosenFileField.appendText(trimPath(chosenFile.toPath())+ " ");
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
            DirectoryStream<Path> dStream = Files.newDirectoryStream(chosenDirectory.toPath(), "*.wav");
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
    private void handleFindLoudestSecondButtonEvent(ActionEvent event) throws UnsupportedAudioFileException, IOException {
        if (chosenFiles.size() > 0)
        {
            resultField.clear();
            for (Path p : chosenFiles)
            {
            String s = SoundAnalyze.calculateLoudestSecond(p);
            resultField.appendText(s + "\n");
            }
            exportButton.setDisable(false);
        }
        else
        {
            resultField.setText("No files selected.");
            exportButton.setDisable(true);
        }
    }

    @FXML
    private void handleFindMaxAmplitudeButtonEvent(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (chosenFiles.size() > 0)
        {
            resultField.clear();
            for (Path p : chosenFiles)
            {
            String s = SoundAnalyze.calculateMaxAmplitude(p);
            resultField.appendText(s + "\n");
            }
            exportButton.setDisable(false);
        }
        else
        {
            resultField.setText("No files selected.");
            exportButton.setDisable(true);
        }
    }

    @FXML
    private void handleFindAvgAmplitudeButtonEvent(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (chosenFiles.size() > 0)
        {
            resultField.clear();
            for (Path p : chosenFiles)
            {
            String s = SoundAnalyze.calculateAvgAmplitude(p);
            resultField.appendText(s + "\n");
            }
            exportButton.setDisable(false);
        }
        else
        {
            resultField.setText("No files selected.");
            exportButton.setDisable(true);
        }
    }

    @FXML
    private void handleFindLoudestSongButtonEvent(ActionEvent event) throws UnsupportedAudioFileException, IOException, Exception {
        if (chosenFiles.size() > 1)
        {
            resultField.clear();
            resultField.setText("Loudest song is " + SoundAnalyze.calculateLoudestSongInDirectory((ArrayList<Path>) chosenFiles));
            exportButton.setDisable(false);
        }
        else
        {
            resultField.setText("Not enough files selected.");
            exportButton.setDisable(true);
        }
    }

    @FXML
    private void handleClearButtonEvent(ActionEvent event) 
    {
        chosenFileField.clear();
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
            PrintWriter writer = new PrintWriter(file);
            writer.print(resultField.getText());
            writer.close();
        }

    }


}