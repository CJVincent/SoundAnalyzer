

package soundtest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.sound.sampled.*; 
import org.jtransforms.fft.*;
import java.util.Arrays;
import org.jtransforms.utils.CommonUtils;
public class SoundAnalyze 
{
    public static void main (String []args) throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
       String fileName = "test3.wav";
       //calculateMaxAmplitude(fileName);
       //calculateAvgAmplitude(fileName);
       calculateMaxFrequency(fileName);
    }
    private static void calculateMaxAmplitude(String fileName) throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
      File soundFile = new File(fileName);
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
          for (int j = 0; j < numChannels; j++)
          {
              sample += Math.abs(ampData[j][i]);
          }
          sample /= numChannels;
          if (sample > max)
          {        
                   max = sample;
                   maxIndex = i;
          }
      }
      System.out.println("Max amplitude: " + max + " at frame " + maxIndex + " (" + (maxIndex / frameRate) + " seconds in)");
    }
    private static void calculateAvgAmplitude(String fileName) throws UnsupportedAudioFileException, IOException 
    {
      File soundFile = new File(fileName);
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
      System.out.println("Average amplitude is " + ((double)totalAmp / frameLength));
    }
    private static int[][] getUnscaledAmplitude(byte[] eightBitByteArray, int nbChannels)
    {
        int[][] toReturn = new int[nbChannels][eightBitByteArray.length / (2 * nbChannels)];
        int index = 0;

        for (int audioByte = 0; audioByte < eightBitByteArray.length;)
        {
            for (int channel = 0; channel < nbChannels; channel++)
            {
                // Do the byte to sample conversion.
                int low = (int) eightBitByteArray[audioByte];
                audioByte++;
                int high = (int) eightBitByteArray[audioByte];
                audioByte++;
                int sample = (high << 8) + (low & 0x00ff);

                toReturn[channel][index] = sample;
            }
        index++;
        }
        return toReturn;
}

    
    private static void calculateMaxFrequency(String fileName) throws UnsupportedAudioFileException, IOException 
    {
      File soundFile = new File(fileName);
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
      int frameLength = (int)audioIn.getFrameLength();
      int frameSize = audioIn.getFormat().getFrameSize();
      int numChannels = audioIn.getFormat().getChannels();
      byte[] buffer = new byte[frameLength * frameSize];
      int readNums = audioIn.read(buffer);
      double[] fftData = new double[CommonUtils.nextPow2(buffer.length*2)];
      for(int i = 0; i < buffer.length; i++)
      {
          fftData[i] = (double)buffer[i];
      }
      System.out.println(fftData.length);
      int x=0;
      DoubleFFT_1D fft = new DoubleFFT_1D(buffer.length);
      fft.realForwardFull(fftData);
      System.out.println("done");
      double[] magnitude = new double[fftData.length / 2];
      for (int i = 0; i < magnitude.length;i++)
      {
          double real = fftData[2*i];
          double imag = fftData[2*i+1];
          magnitude[i] = Math.sqrt((real * real) + (imag * imag));
      }
      double maxMag = 0;
      int maxMagIndex = 0;
      for (int i = 0; i < magnitude.length;i++)
      {
          if (magnitude[i] > maxMag)
          {
              maxMag = magnitude[i];
              maxMagIndex = i;
          }
      }
      double freq = maxMagIndex * audioIn.getFormat().getFrameRate() / buffer.length;
      System.out.println(maxMag);
      System.out.println(maxMagIndex);
      System.out.println(freq);
    }




}
