import java.io.*;
import sun.audio.*;
import java.util.*; // Vector
import java.net.*;
import java.lang.*;

class AudioFiller {

   private static final int VECTOR_SIZE = 3;
   
   private static final int AU_HEADER_SIZE = 24;
   
   private static final int AU_MIN = 160; // Min bytes before we refill

   private static final int AU_MAGIC = 0; //
   private static final int AU_HLEN  = 1; // Header length
   private static final int AU_DLEN  = 2; // Data Length, 32968bytes
   private static final int AU_ULAW  = 3; // encoding
   private static final int AU_RATE  = 4; // sampling reate 8000Hz
   private static final int AU_MONO  = 5; // 
   
   private static final byte  au_header[] =  {
     (byte)0x2e,(byte)0x73,(byte)0x6e,(byte)0x64, // AU_MAGIC
     (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x18, // AU_HLEN
     (byte)0x00,(byte)0x00,(byte)0x1f,(byte)0x40, // AU_DLEN   1 sec
     (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01, // AU_ULAW
     (byte)0x00,(byte)0x00,(byte)0x1f,(byte)0x40, // AU_RATE
     (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01 }; // AU_MONO

   private boolean refill = true; 
   private int was_available = 0;  // for refilling

   int flip = 0;        
   byte[][] au_data;
   AudioStreamSequence audiostreamsequence;
   private Vector v;

   /* 
    * @param au_din[][] are the first two ByteArrays
    * First two  buffers are filled
    * when player comes to end of first buffer, fill 
    * third buffer, etc.    
    */
   public AudioFiller (byte[] au_din, int length){
      
      au_data = new byte [VECTOR_SIZE][AU_HEADER_SIZE + length];
      
      for ( int i=0 ; i < VECTOR_SIZE; i++){
	 System.arraycopy(au_header, 0, au_data[i], 0, AU_HEADER_SIZE);
	 au_data[i][10] = (byte)((length & 0xff00)>>8); // header size
	 au_data[i][11] = (byte)(length & 0x00ff);               
	 
      }
      v = new Vector();
      v.ensureCapacity(VECTOR_SIZE);
      try {
	 fill(au_din, length);
      } catch (Exception e) {
	 System.out.println("AudioFiller:" +e);
      }
      audiostreamsequence = new  AudioStreamSequence(v.elements());
  }
 

  public AudioStreamSequence getStream() {
      return audiostreamsequence;
  }       

   /** 
    * @return true if we need to fill buffer
    */
   public boolean  needs_refill () {
     
      int left=0;
      try {
        left  = available();  // save local before it changes
      } catch (Exception e) {
      }
      
      //System.out.println( audiostreamsequence.available()  + " left:" );            
      if (refill){
	 refill = false;
         was_available = left;  // problem if left == MAX 
         //System.out.println("refilling..." );
	 return true;
      } else if (left == 0) {  // wrapped, so new stream
           //System.out.println("Time to refill: "+left+" bytes left");
	   refill = true;
        }  else if ( left > was_available ) {
           //System.out.println("Time to refill: "+left+" bytes left");
	   refill = true;
        }
      was_available = left;
      return false;
  }
   
  public  void fill (byte [] au_din, int length)  {
     System.arraycopy(au_din, 0, au_data[flip], AU_HEADER_SIZE, length);
     // InputStream in = new ByteArrayInputStream(au_data[flip]);
     v.addElement(new AudioStream( new ByteArrayInputStream(au_data[flip])  ));
     flip =  (++flip) % VECTOR_SIZE ;
  }
   
   /**
    * @return The current number of buffers  played or waiting to play
    */ 
   public  int current () throws IOException {
      return v.size();
   }
   
   /**
    * @return the number of bytes to play in the current buffer
    */
   public int available () throws IOException {
      return audiostreamsequence.available();
   }
   
}


