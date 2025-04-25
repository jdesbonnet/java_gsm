
/**
 * Stores a reasonable amount of compressed audio data. To prevent problems due to 
 * blocking on stream reading our audio device writing, two threads are employed - 
 * one to read chunks from the imput stream and the other to feed chunks to the 
 * audio device.
 * Each segment is fed to the AudioDevice as one block
 */
class AudioBuffer extends Object {
   static final int GSM_FRAMES_PER_SEGMENT=25;
   static final int SEGMENT_SIZE=Gsm.GSM_FRAME*GSM_FRAMES_PER_SEGMENT;
   static final int NSEG=16;
   int head, tail, size, nseg, highwater_mark, lowwater_mark;
   BufferFillThread fillThread;
   BufferEmptyThread emptyThread;
   byte[][] bufferSegment;  // An array of byte arrays
   
   AudioBuffer (int nseg) {
      this.nseg = nseg;
      head=0;
      tail=0;
      size=0;
      highwater_mark = nseg - (nseg/4);
      lowwater_mark = nseg/4;
      bufferSegment = new byte[nseg][SEGMENT_SIZE];
      
      if (Conf.DEBUG) {
	 System.out.println ("AudioBuffer: nseg="+nseg);
      }
   }

   
   // these need to by synchronized I think
   
   /** 
    * Add a segment to the buffer
    */
   
   synchronized void addSegment (byte[] data) {
      bufferSegment[head++]=data;
      if (head==NSEG) head=0;
      size++;
      if (Conf.DEBUG) System.out.println ("AudioBuffer::addSegment, size=" + size);
      if (size==NSEG) {
	 if (Conf.DEBUG) System.out.println ("AudioBuffer: buffer full, suspending fill thread, resuming empty thread");
	 fillThread.requestSuspend();
	 emptyThread.resume();
      } 
   }
   
   /**
    * Remove a segment from the buffer
    */
   synchronized byte[] getSegment () {
      tail++;
      if (tail==NSEG) tail=0;
      size--;
      if (Conf.DEBUG) System.out.println ("AudioBuffer::getSegment, size=" + size);
      if (size<=0) {
	 if (Conf.DEBUG) System.out.println ("AudioBuffer: buffer empty, suspending empty thread, resuming fill thread" + size);
	 emptyThread.requestSuspend();
      }
      if (size<highwater_mark) {
	 fillThread.requestResume();
      }
      
      // Must return a copy of the segment - else corruption occurs
      // I believe what is happening is that even though this space is
      // declared as free for reuse - it is still needed until used
      // up by the audio device, which may be some time later.
      return (byte[])bufferSegment[tail].clone();
      //return bufferSegment[tail];
   }

   void flush() {
      if (Conf.DEBUG) System.out.println ("AudioBuffer: flush()");
      emptyThread.resume();
   }
   
   /**
    * Who is responsible for filling me?
    */
   void registerFillThread (BufferFillThread t) {
      fillThread=t;
   }
   
   /**
    * Who is responsible for emptying me?
    */
   void registerEmptyThread (BufferEmptyThread t) {
      emptyThread=t;
   }
   
   
   /**
    * Returns the number of segments currently left in the buffer
    */
   public int getSize() {
      return size;
   }
   
   /**
    * Returns the buffer size in segments
    */
   public int getBufferSize() {
      return nseg;
   }
   
   /**
    * Returns the size of a segment in bytes
    */
   public int getSegmentSize() {
      return SEGMENT_SIZE;
   }
}

