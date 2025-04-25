import java.lang.*;

class audiotest {
   public static void main (String argc[]) {

      System.out.println ("os.name: " + System.getProperty("os.name"));
      
      byte[] signal = new byte[8192];
      int j=0;
      for (int i=0; i<8192; i++) {
	 signal[i]= (byte)(j%200);
	 j++;
      }
      
      AudioDevice dev = new AudioDevice();
      FeedAudioDevice f = new FeedAudioDevice(dev,signal);
      f.start();
   }
}

class FeedAudioDevice extends Thread {
   
   AudioDevice dev;
   byte[] signal;
   int delay;
   
   FeedAudioDevice (AudioDevice dev, byte[] signal) {
      this.dev = dev;
      this.signal = signal;
      delay = (signal.length*1000)/20000;
      System.out.println ("delay="+delay);
   }
   
   public void run () {
      while (true) {
	 dev.write(signal);
	 try {
	    sleep(delay);
	 } catch (Exception e) {
	    System.out.println ("Error: " + e);
	    System.exit(0);
	 }
      }
   }
}
