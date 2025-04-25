/**
 * Thread takes data from URL and feeds it to audio device
 *
 */

import sun.audio.*;
import java.net.*;

import java.io.*;
import java.lang.*;

class AudioThread extends Thread {
   
   static final int AU_PACKET_SIZE = 160;
   static final int GSM_PACKET_SIZE = 33;
   static final int PACKETS_PER_ASSBUF = 150;  
   
   // Buffers
   byte gsm_pkt[] = new byte [GSM_PACKET_SIZE];
   byte au_pkt[]  = new byte [AU_PACKET_SIZE];
   byte au_data[]  = new byte [AU_PACKET_SIZE * PACKETS_PER_ASSBUF];
   
   
   DataInputStream input_stream;
   AudioStreamSequence ass;
   AudioFiller af;
   int data_length;
   int current_value = 0;

   private int aserv_port=0;
   
   public AudioThread (URL audio_url) {

      // Determine length of audio file
      try {
	 input_stream =  new DataInputStream(audio_url.openStream());
	 data_length = Gsm.header(input_stream);
	 System.out.println("init: data_length =  " + data_length );
      } catch (IOException e) {
	 System.out.println("Error: Malformed audio file header: " + e );
	 System.exit(0);
      }
       
      try {
	 loadASSBuf();
	 af = new AudioFiller(au_data, PACKETS_PER_ASSBUF * AU_PACKET_SIZE);
	 loadASSBuf();
	 ass = af.getStream();
      } catch (Exception e){
	 System.out.println("AudioEngine Failed: "+e);
	 System.exit(0);
      }
       
    }
   
   void setAservPort (int p) {
      aserv_port = p;
   }
   
    public void start () {
      super.start();
      AudioPlayer.player.start(ass);
    }

    public void run () {
       System.out.println("AudioEngine thread running");

       while (current_value < data_length) {
	  if (af.needs_refill()) {
	     af.fill(au_data, au_data.length);  // Send next buffer to audioplayer
	     try {
		loadASSBuf();
		current_value = af.current();
		this.sleep(50); // was 1000
	     } catch (Exception e) {
	     }
          }
       }
    }
   
   /**
    * Read from input_stream enough packets to fill one AudioStreamSequence
    **/
   void  loadASSBuf () throws IOException {
      // experimental code added by JD 24 June 1998
	 InetAddress localhost=null;
	 DatagramPacket packet=null;
	 DatagramSocket socket=null;
      
      if (aserv_port > 0) {
	 try {
	    localhost = InetAddress.getByName("localhost");
	    socket = new DatagramSocket();
	 } catch (Exception e) {
	    System.out.println ("Error opening aserv port: " + e.getMessage());
	 }
      }
      
      for (int i = 0; i < PACKETS_PER_ASSBUF; i++) {
	 input_stream.readFully(gsm_pkt);  // decode first packet and write

	 if ( Gsm.decompress(gsm_pkt, au_pkt) == null){
	    System.out.println("init : bad gsm packet = " + i + " " + gsm_pkt[0]  );
	 }

	 if (aserv_port == 0) {
	    System.arraycopy(au_pkt, 0,  au_data , i * AU_PACKET_SIZE, AU_PACKET_SIZE);
	 } else {
	    try {
	       packet = new DatagramPacket(au_pkt,AU_PACKET_SIZE,localhost,aserv_port);
	       socket.send(packet);
	    }  catch (Exception e) {
	       System.out.println ("Error on packet send: " + e.getMessage());
	    }
	 } 
	 
      }
   }



}
