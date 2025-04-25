import java.io.*;
import java.lang.*;

class BufferEmptyThread extends Thread {
   AudioBuffer audioBuffer;
   AudioDevice audioDevice;
   private boolean request_suspend=false;
   
   BufferEmptyThread (AudioDevice dev, AudioBuffer audioBuffer) {
      this.audioBuffer = audioBuffer;
      audioDevice=dev;
      audioBuffer.registerEmptyThread(this);
   }
   
   
   public void run () {
      if (Conf.DEBUG) System.out.println ("BufferEmptyThread running");
      byte[] buffer = new byte[AudioBuffer.SEGMENT_SIZE];
      byte[] aubuffer = new byte[Gsm.AU_FRAME];
      byte[] large_aubuffer = new byte[Gsm.AU_FRAME * AudioBuffer.GSM_FRAMES_PER_SEGMENT];
      //byte[] large_aubuffer;
      if (Conf.DEBUG) System.out.println ("BufferEmptyThread starting loop");

      File logfile=null;
      FileOutputStream logfile_stream=null;
      if (Conf.DEBUG) {
	 try {
	    logfile = new File ("jaudio.log");
	    logfile_stream = new FileOutputStream(logfile);
	 } catch (Exception e) {
	    System.out.println ("Error creating log file: " + e);
	 }
      }
      
      while ( (buffer = audioBuffer.getSegment()) != null) {
	 // Assume while number of GSM frames in each segment
	 // Decode all GSM frames and copy into large_aubuffer
	 
	 // Assign new buffer each time as an experiment... (no
	 // doesnt seem to fixing the problem
         //large_aubuffer = new byte[Gsm.AU_FRAME * AudioBuffer.GSM_FRAMES_PER_SEGMENT];
	 
	 for (int i=0; i<AudioBuffer.GSM_FRAMES_PER_SEGMENT; i++) {
	    Gsm.decompress (buffer,aubuffer,i*Gsm.GSM_FRAME);
	    System.arraycopy (aubuffer,0,large_aubuffer,i*Gsm.AU_FRAME,Gsm.AU_FRAME);
	 }
	 if (Conf.DEBUG && logfile_stream!=null) {
	    try {
	       logfile_stream.write(large_aubuffer);
	       logfile_stream.flush();
	    } catch (Exception e) {
	       System.out.println ("Error writing to log file: " + e);
	    }
	 }
							     
	 // Write to audio device
	  audioDevice.write(large_aubuffer);
	  
	 // Have we been asked to suspend?
	 if (request_suspend) {
	    if (Conf.DEBUG) System.out.println ("BufferEmptyThread: SUSPEND");
	    request_suspend=false;
	    suspend();
	 } else {
	    //yield();
	    try {
	       sleep (large_aubuffer.length/32);						     
	    } catch (Exception e) {
	    }
	 }
      }
   }
   void requestSuspend () {
      request_suspend=true;
   }
}

	 
