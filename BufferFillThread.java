/**
 * Thread takes data from URL and keeps buffers full(ish)
 *
 */

import java.io.*;
import java.lang.*;

class BufferFillThread extends Thread {
   DataInputStream input_stream;
   AudioBuffer audioBuffer;
   byte[] buffer;
   private boolean request_suspend=false;
   private Play listener;
   private int bytes_read;
   
   BufferFillThread (DataInputStream input_stream, AudioBuffer audioBuffer, Play listener) {
      this.input_stream=input_stream;
      this.audioBuffer = audioBuffer;
      this.listener = listener; // hack
      buffer = new byte[AudioBuffer.SEGMENT_SIZE];
      audioBuffer.registerFillThread(this);
   }

   public void run () {
      if (Conf.DEBUG) System.out.println("BufferFillThread running");
      bytes_read=0;
      
      while (true) {
	 try {
	    input_stream.readFully(buffer);  // decode first packet and write
	    bytes_read += buffer.length;
	 } catch (Exception e) {
	    System.out.println ("Got exception of stream read: " + e);
	    audioBuffer.flush();
	    suspend();
	    break;
	 }
	 audioBuffer.addSegment(buffer);
	 listener.updateByteCount(bytes_read);
	 System.out.println ("bytes_read: " + bytes_read);
	 
	 if (request_suspend) {
	    if (Conf.DEBUG) System.out.println ("BufferFillThread: SUSPEND");
	    request_suspend=false;
	    suspend();
	 } else {
	    yield();
	 }
      }
   }
   void requestSuspend () {
      request_suspend=true;
   }
   void requestResume () {
      if (Conf.DEBUG) System.out.println ("BufferFillThread: resume()");
      resume();
   }
}
