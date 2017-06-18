
package soundtest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.sound.sampled.*;

public class SoundAnalyze 
{
    public static void main (String []args) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException
    {
        
      File soundFile = new File("spanishflea.wav");
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
      Clip clip = AudioSystem.getClip();
      clip.open(audioIn);
      int[][] data = getUnscaledAmplitude(buffer,numChannels);
      int max = 0;
      int maxIndex = 0;
      for (int i = 0; i < data[0].length;i++)
      {
          int sample = 0;
          for (int j = 0; j < numChannels; j++)
          {
              sample += Math.abs(data[j][i]);
          }
          sample /= numChannels;
          if (sample > max)
          {        
                   max = sample;
                   maxIndex = i;
          }
      }
      System.out.println("Max amplitude: " + max);
      System.out.println("Max amplitude frame: " + maxIndex); // frame #
      System.out.println("Max amplitude second: " + maxIndex / frameRate);
    }
    public static int[][] getUnscaledAmplitude(byte[] eightBitByteArray, int nbChannels)
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
}
