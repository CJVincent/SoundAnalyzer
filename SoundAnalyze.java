

package soundtest;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*; 
public class SoundAnalyze 
{
    public static void main (String []args) throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        try {
            // anaylze all the soundfiles in the current folder
            DirectoryStream<Path> directory = Files.newDirectoryStream(Paths.get(System.getProperty("user.home")), "*.wav");
            System.out.println(calculateLoudestSongInDirectory(directory));
        } catch (Exception ex) {
            Logger.getLogger(SoundAnalyze.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Finds the point in the audio file with the highest amplitude and returns that value
    private static int calculateMaxAmplitude(Path filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
      File soundFile = new File(filePath.toString());
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
      int frameLength = (int)audioIn.getFrameLength();
      int frameRate = (int) audioIn.getFormat().getFrameRate();
      int frameSize = audioIn.getFormat().getFrameSize();
      int numChannels = audioIn.getFormat().getChannels();
      byte[] buffer = new byte[frameLength * frameSize];
      System.out.println(audioIn.getFormat());
      System.out.println("Frame Length: " + audioIn.getFrameLength());
      int readNums = audioIn.read(buffer);
      System.out.println("Bytes read : " + readNums + "\n" + buffer.length);
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
      if (filePath.getNameCount() > 0)
        filePath =filePath.subpath(filePath.getNameCount()-1,filePath.getNameCount()); //reduces filepath to just name of file
      System.out.println("Max amplitude of " + filePath + ": " + max + " at frame " + maxIndex + " (" + (maxIndex / frameRate) + " seconds in)");
      return max;
    }
    
    //returns average amplitude of the whole sound file
    private static double calculateAvgAmplitude(Path filePath) throws UnsupportedAudioFileException, IOException 
    {
      File soundFile = new File(filePath.toString());
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
      int frameLength = (int)audioIn.getFrameLength();
      int frameSize = audioIn.getFormat().getFrameSize();
      int numChannels = audioIn.getFormat().getChannels();
      byte[] buffer = new byte[frameLength * frameSize];
      int readNums = audioIn.read(buffer);
      int[][] ampData = getUnscaledAmplitude(buffer,numChannels);
      long totalAmp = 0;
      for (int i = 0; i < ampData.length; i++)
          for (int j = 0; j < ampData[i].length;j++)
              totalAmp += Math.abs(ampData[i][j]);
      if (filePath.getNameCount() > 0)
        filePath =filePath.subpath(filePath.getNameCount()-1,filePath.getNameCount()); //reduces filepath to just name of file
      System.out.println("Average amplitude of " + filePath + ": " + ((double)totalAmp / frameLength));
      return ((double)totalAmp / frameLength);
    }
    
    //gets amplitude data by dividing each cahnnel into its own array, and by combining the high and low bytes for each sample
    private static int[][] getUnscaledAmplitude(byte[] buffer, int numChannels)
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

    //finds loudest second in song by looking at average amplitude over the second. Returns the second as int
    private static int calculateLoudestSecond(Path filePath) throws UnsupportedAudioFileException, IOException
    {
      File soundFile = new File(filePath.toString());
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
      int frameLength = (int)audioIn.getFrameLength();
      int frameSize = audioIn.getFormat().getFrameSize();
      int frameRate = (int) audioIn.getFormat().getFrameRate();
      int numChannels = audioIn.getFormat().getChannels();
      byte[] buffer = new byte[frameLength * frameSize];
      int readNums = audioIn.read(buffer);
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
      if (filePath.getNameCount() > 0)
        filePath =filePath.subpath(filePath.getNameCount()-1,filePath.getNameCount()); //reduces filepath to just name of file
      System.out.println("Loudest second of " + filePath + ": " + loudestSecond + " with an average amplitude of " + loudestSecondAmp);
      return loudestSecond;
    }
    
    private static Path calculateLoudestSongInDirectory(DirectoryStream<Path> directory) throws UnsupportedAudioFileException, IOException, Exception
    {
        double loudestSongAmp = 0;
        Path loudestSong = null;
        for (Path p: directory)
        {
            double temp =calculateAvgAmplitude(p);
            if (temp > loudestSongAmp)
            {
                loudestSongAmp = temp;
                loudestSong = p;
            }
        }
        if (loudestSong == null)
            throw new Exception("no .wav files in directory");
        return loudestSong;
    }
    
    
    




}
