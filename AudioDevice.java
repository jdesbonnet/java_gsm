import java.io.*;
import sun.audio.*;
import java.util.*; 
import java.net.*;
import java.lang.*;

/**
 * Takes raw AU format data and pipes it to speaker. This ties to behave as
 * /dev/dsp or /dev/audio as much as possible, although I may be wasting my
 * time trying to mould this stuff into the unix way of thinking.
 * Tries to block if buffers fill.
 */

class AudioDevice extends Object {

   public static final int API_SUNAUDIO=1;
   public static final int API_ASERV_UDP=2;
   public static final int API_ASERV_TCP=3;
   public static final int API_ASERV=API_ASERV_UDP;
   public static final int ASERV_PORT=3333;
   public static final int UDP_PACKET_SIZE=4096;
   
   private static final byte  au_header[] =  {
     (byte)0x2e,(byte)0x73,(byte)0x6e,(byte)0x64, // AU_MAGIC
     (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x18, // AU_HLEN
     (byte)0x00,(byte)0x00,(byte)0x1f,(byte)0x40, // AU_DLEN   1 sec
     (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01, // AU_ULAW
     (byte)0x00,(byte)0x00,(byte)0x1f,(byte)0x40, // AU_RATE
     (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01 }; // AU_MONO

   
   private static final int VECTOR_SIZE = 3;
   
   private static final int AU_HEADER_SIZE = 24;
   
   private static final int AU_MIN = 160; // Min bytes before we refill

   private static final int AU_MAGIC = 0; //
   private static final int AU_HLEN  = 1; // Header length
   private static final int AU_DLEN  = 2; // Data Length, 32968bytes
   private static final int AU_ULAW  = 3; // encoding
   private static final int AU_RATE  = 4; // sampling reate 8000Hz
   private static final int AU_MONO  = 5; // 
   
   private int audioAPI;
   private InetAddress localhost;
   private DatagramSocket socket;
   private OutputStream socket_stream;
   private int packet_number=0;
   private byte[] packet_buffer;
   private byte[] au_buffer;
   
   AudioDevice () {
      
      // If Linux then use aserv - else use sun audio
      if (System.getProperty("os.name").equals("Linux")) {
	 //audioAPI = API_ASERV;
	 audioAPI = API_SUNAUDIO; 
      } else {
	 audioAPI = API_SUNAUDIO;
      }
      
      switch (audioAPI) {
       case API_SUNAUDIO:
	 break;
       case API_ASERV_UDP:
	 try {
	    localhost = InetAddress.getByName("localhost");
	    socket = new DatagramSocket();
	 } catch (Exception e) {
	    System.out.println ("Error opening aserv port: " + e.getMessage());
	    System.exit(0);
	 }
	 packet_buffer = new byte[UDP_PACKET_SIZE];
	 break;
       case API_ASERV_TCP:
	 try {
	    localhost = InetAddress.getByName("localhost");
	    socket_stream = new Socket(localhost,ASERV_PORT).getOutputStream();
	    //socket_stream = socket.getOutputStream();
	 } catch (Exception e) {
	    System.out.println ("Error opening aserv port: " + e);
	    System.exit(0);
	 }
	 break;
	 
      }
   }
   
   void write (byte[] data) {
	 this.write (data,0,data.length);
   }
   
   void write (byte[] data, int offset, int length) {
      if (Conf.DEBUG) System.out.println ("Writing " + length + " data bytes to audio device");
      
      switch (audioAPI) {
       case API_SUNAUDIO:

	 // I wonder if it necessary to declare a new au_buffer each time?
	 au_buffer = new byte[length+24];
	 System.arraycopy(au_header,0,au_buffer,0,AU_HEADER_SIZE);
	 System.arraycopy(data,offset,au_buffer,AU_HEADER_SIZE,length);
	 
	 // Patch header to include correcdt length of audio data
	 au_buffer[8]  = (byte) 0;
	 au_buffer[9] = (byte)((length & 0xff0000)>>16);
	 au_buffer[10] = (byte)((length & 0xff00)>>8);
	 au_buffer[11] = (byte)(length & 0x00ff);               

	 try {
	    AudioPlayer.player.start(new AudioStream( new ByteArrayInputStream(au_buffer)));
	 } catch (Exception e) {
	    System.out.println ("Error on AudioPlayer.player.start(): " + e);
	 }
	 break;
	 
       case API_ASERV_UDP:
	 int npackets = length / UDP_PACKET_SIZE;
	 int residual = length - (npackets*UDP_PACKET_SIZE);
	 for (int i=0; i<npackets; i++) {
	    sendUDPPacket (data,offset + i*UDP_PACKET_SIZE,UDP_PACKET_SIZE);
	 }
	 if (residual > 0) {
	    sendUDPPacket (data, offset + npackets*UDP_PACKET_SIZE,residual);
	 }
	 break;
	 
       case API_ASERV_TCP:
	 if (Conf.DEBUG) System.out.println ("Writing data to TCP socket");
	 try {
	    socket_stream.write(data);
	 } catch (Exception e) {
	    System.out.println ("Error writing to socket: " + e);
	 }
	 break;
	 
      }
   }

   // Send a packet from array "data" starting at "offset" of "length"
   void sendUDPPacket (byte[] data, int offset, int length) {
      
      System.arraycopy(data,offset,packet_buffer,0,length);
      
      // Some experimental stuff here to make sure that packets were not
      // being dropped - there seemed to be absolutely no packet loss sending
      // to localhost
      if (Conf.PACKET_NUMBERING) {
	 packet_buffer[0] = (byte)(packet_number/256);
	 packet_buffer[1] = (byte)(packet_number%256);
      }
      
      if (Conf.DEBUG) System.out.println ("Sending packet number " + packet_number);
      packet_number++;
      try {
	 DatagramPacket packet = new DatagramPacket(packet_buffer, length, localhost, ASERV_PORT);
	 socket.send(packet);
      } catch (Exception e) {
	 System.out.println ("Error on packet send: " + e);
      }
   }
   
}
