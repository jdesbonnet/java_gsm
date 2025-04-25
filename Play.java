/**
 * Streaming Audio Player
 *
 * World Wide Web Marketing Ltd. (c) 1997
 * @author Micheal Colhoun
 * @author Eoin Burke
 * @author Joe Desbonnet
 *
 */
import java.io.*;
import java.net.*;
import java.lang.*;

public class Play extends Object {
   DataInputStream input_stream;
   AudioBuffer audioBuffer;
   AudioDevice audioDevice;
   BufferFillThread bufferFillThread;
   BufferEmptyThread bufferEmptyThread;
   
   private ProgressBar playProgressBar, bufferSizeBar;
   int data_length;
   
   Play (URL audio_url, int mode, ProgressBar playProgressBar, 
	 ProgressBar bufferSizeBar) {
      this.playProgressBar = playProgressBar;
      try {
	 data_length = audio_url.openConnection().getContentLength();
	 input_stream =  new DataInputStream(audio_url.openStream());
	 //data_length = Gsm.header(input_stream);
	 int blah = Gsm.header(input_stream);
      } catch (Exception e) {
	 System.out.println ("Error: " + e);
      }
	   
      System.out.println("play: data_length =  " + data_length );
	    
      
      // Create AudioBuffer & filler thread
      if (Conf.DEBUG) System.out.println ("Creating Audio Buffer");
      audioBuffer = new AudioBuffer(64);
      if (Conf.DEBUG) System.out.println ("Creating Audio Device");
      audioDevice = new AudioDevice();
      
      bufferFillThread = new BufferFillThread(input_stream, audioBuffer, this);
      bufferEmptyThread = new BufferEmptyThread(audioDevice, audioBuffer);
   }
   
   void start () {
      bufferFillThread.start();
      bufferEmptyThread.start();
      bufferEmptyThread.suspend();
   }
   void stop () {
      bufferEmptyThread.stop();
      bufferFillThread.stop();
   }
   void suspend () {
      bufferEmptyThread.suspend();
   }
   
   void updateByteCount (int i) {
      playProgressBar.setValue((i*100)/data_length);
   }
   void updateBufferSize (int i) {
   }
}
