
package soundtest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.sound.sampled.*; 
public class SoundAnalyze extends Application
{
    //initialize stage and scene
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("SoundAnalyzeGUI.fxml"));
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("WAV File Amplitude Analyzer");
        stage.show();
    }
    
    
    public static void main (String []args) throws UnsupportedAudioFileException, IOException, LineUnavailableException, Exception
    {
       launch(args);
    }
    
    //Finds the point in the audio file with the highest amplitude
    protected static String calculateMaxAmplitude(Path filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException, Exception
    {
      File soundFile = new File(filePath.toString());
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
      int frameRate = 44100;
      int numChannels = audioIn.getFormat().getChannels();
      //test  mp3 conversion
      byte [] buffer = getDataBytes(audioIn);
      if( audioIn.getFormat().getFrameRate() != 44100)
        buffer = convertMp3DataBytes(buffer, new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioIn.getFormat().getSampleRate(), 16, audioIn.getFormat().getChannels(), audioIn.getFormat().getChannels()*2, audioIn.getFormat().getSampleRate(), false));
      //test  mp3 conversion
      int[][] ampData = getUnscaledAmplitude(buffer,numChannels);
      int max = 0;
      int maxIndex = 0; // frame #
      for (int i = 0; i < ampData[0].length;i++)
      {
          int sample = 0;
          for (int channel = 0; channel < numChannels; channel++)
          {
              sample += Math.abs(ampData[channel][i]);
          }
          sample /= numChannels;
          if (sample > max)
          {        
                   max = sample;
                   maxIndex = i;
          }
      }
      return "Max amplitude of " + trimPath(filePath) + " is " + max + " at " + LocalTime.MIN.plusSeconds(maxIndex / frameRate).toString();
    }
    
    
    protected static String calculateAvgAmplitude(Path filePath) throws UnsupportedAudioFileException, IOException, Exception 
    {
      return "Average amplitude of " + trimPath(filePath) + " is " + getAvgAmplitude(filePath);
    }
    //returns average amplitude of the whole sound file
    protected static double getAvgAmplitude(Path filePath) throws UnsupportedAudioFileException, IOException, Exception
    {
      File soundFile = new File(filePath.toString());
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
      int numChannels = audioIn.getFormat().getChannels();
      byte [] buffer = getDataBytes(audioIn);
      if( audioIn.getFormat().getFrameRate() != 44100)
        buffer = convertMp3DataBytes(buffer, new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioIn.getFormat().getSampleRate(), 16, audioIn.getFormat().getChannels(), audioIn.getFormat().getChannels()*2, audioIn.getFormat().getSampleRate(), false));
      int[][] ampData = getUnscaledAmplitude(buffer,numChannels);
      long totalAmp = 0;
      for (int i = 0; i < ampData.length; i++)
          for (int j = 0; j < ampData[i].length;j++)
              totalAmp += Math.abs(ampData[i][j]);
      return ((double)totalAmp / ampData[0].length);
    }
    

    //finds loudest second in song by looking at average amplitude over the second.
    protected static String calculateLoudestSecond(Path filePath) throws UnsupportedAudioFileException, IOException, Exception
    {
      File soundFile = new File(filePath.toString());
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
      int frameRate = 44100;
      int numChannels = audioIn.getFormat().getChannels();
      byte [] buffer = getDataBytes(audioIn);
      if( audioIn.getFormat().getFrameRate() != 44100)
        buffer = convertMp3DataBytes(buffer, new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioIn.getFormat().getSampleRate(), 16, audioIn.getFormat().getChannels(), audioIn.getFormat().getChannels()*2, audioIn.getFormat().getSampleRate(), false));
      int[][] ampData = getUnscaledAmplitude(buffer,numChannels);
      int loudestSecond =0;
      int loudestSecondAmp = 0; //average amplitude for loudest second
      int x = 0;
      for (int i = 0; i < ampData[0].length;i+=frameRate)
      {
          int currentSecondAmplitude = 0;  // total amplitude for the second
          int numFrames = 0; // number of frames in the second, equal to frameRate except for the last second
          for (int j = 0; (j < frameRate) && (i+j<ampData[0].length);j++)
          {
              int sample = 0;
              for (int channel = 0; channel < numChannels; channel++)
              {
                sample += Math.abs(ampData[channel][i+j]);
              }
          sample /= numChannels;
          currentSecondAmplitude += sample;
          numFrames++;
          }
          if (currentSecondAmplitude / numFrames > loudestSecondAmp)
          {
              loudestSecondAmp = currentSecondAmplitude / numFrames;
              loudestSecond = i / frameRate;
          }
      }
      return "Loudest second of " + trimPath(filePath) + " is " + LocalTime.MIN.plusSeconds(loudestSecond).toString();
    }
    
    protected static String calculateLoudestSongInDirectory(ArrayList<Path> directory) throws UnsupportedAudioFileException, IOException, Exception
    {
        double loudestSongAmp = 0;
        Path loudestSong = null;
        for (Path p: directory)
        {
            double temp = getAvgAmplitude(p);
            if (temp > loudestSongAmp)
            {
                loudestSongAmp = temp;
                loudestSong = p;
            }
        }
        if (loudestSong == null)
            throw new Exception("no .wav files in directory");
        return trimPath(loudestSong);
    }
    
    //gets amplitude data by dividing each channel into its own array, and by combining the high and low bytes for each sample
    protected static int[][] getUnscaledAmplitude(byte[] buffer, int numChannels)
    {
        int[][] ampData = new int[numChannels][buffer.length / (2 * numChannels)];
        int index = 0;

        for (int audioByte = 0; audioByte < buffer.length;)
        {
            for (int channel = 0; channel < numChannels; channel++)
            {
                // Do the byte to sample conversion.
                int low = (int) buffer[audioByte];
                audioByte++;
                int high = (int) buffer[audioByte];
                audioByte++;
                int sample = (high << 8) + (low & 0x00ff);

                ampData[channel][index] = sample;
            }
        index++;
        }
        return ampData;
    }
    
    private static String trimPath(Path filePath)
    {
        if (filePath.getNameCount() > 0)
            filePath =filePath.subpath(filePath.getNameCount()-1,filePath.getNameCount()); //reduces filepath to just name of file
        return filePath.toString();
    }
    private static void mp3ToWav(InputStream mp3Data) throws UnsupportedAudioFileException, IOException
    {
        AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(mp3Data);
    }
    private static byte [] convertMp3DataBytes(byte [] sourceBytes, AudioFormat audioFormat) throws UnsupportedAudioFileException, IllegalArgumentException, Exception{
        if(sourceBytes == null || sourceBytes.length == 0 || audioFormat == null){
            throw new IllegalArgumentException("Illegal Argument passed to this method");
        }

        ByteArrayInputStream bais = null;
        ByteArrayOutputStream baos = null;
        AudioInputStream sourceAIS = null;
        AudioInputStream convert1AIS = null;
        AudioInputStream convert2AIS = null;

        bais = new ByteArrayInputStream(sourceBytes);
        sourceAIS = AudioSystem.getAudioInputStream(bais);
        AudioFormat sourceFormat = sourceAIS.getFormat(); //mp3
        AudioFormat convertFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16, sourceFormat.getChannels(), sourceFormat.getChannels()*2, sourceFormat.getSampleRate(), false); //wav
        convert1AIS = AudioSystem.getAudioInputStream(convertFormat, sourceAIS); // converts mp3 stream to wav stream
        convert2AIS = AudioSystem.getAudioInputStream(audioFormat, convert1AIS); // converts wav stream to provided format stream

        return getDataBytes(convert2AIS);
    }
    private static byte [] getDataBytes(AudioInputStream audioIn) throws IOException
    {
        byte [] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while(true){
            int readCount = audioIn.read(buffer, 0, buffer.length);
            if(readCount == -1){
                break;
            }
            baos.write(buffer, 0, readCount);
        }
        return baos.toByteArray();
    }




}