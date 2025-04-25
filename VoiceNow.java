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
import java.util.*; // Vector
import java.net.*;
import java.lang.*;

import java.applet.*;
import java.awt.*;

public class VoiceNow extends Applet  {
   boolean playing = false;
   URL audio_url = null;
   URL table_url = null;
   Play player;
   int audioSystem;
   int fetchMode;

   public ProgressBar playProgressBar, bufferSizeBar;

   public void init() {
      playProgressBar = new ProgressBar();
      bufferSizeBar = new ProgressBar();
      
      setLayout(new BorderLayout());
      Label banner = new Label ("Java Audio");
      Panel controlPanel = new Panel ();
      Panel progressBarPanel = new Panel ();
      progressBarPanel.setLayout(new BorderLayout());
      
      //progressBarPanel.add("North",controlPanel);
      //progressBarPanel.add("South",bufferSizeBar);
      progressBarPanel.add("North",new Button("North"));
      progressBarPanel.add("South",new Button("South"));
      progressBarPanel.add("West",new Button("West"));
      progressBarPanel.add("East",new Button("East"));
      
      controlPanel.add (new Button ("Start"));
      controlPanel.add (new Button ("Stop"));
      
      add ("North", banner);
      add ("Center", progressBarPanel);
      add ("South", controlPanel);
	
      try {
         audio_url = new URL (getDocumentBase(), getParameter("audio_url"));
         table_url = new URL (getDocumentBase(), getParameter("table_url"));
	 
	 // Determine method of playing audio to speaker. sun/0 = built in sound, but does not work with Linux
	 // Netscape V4, aserv/1 means use the aserv program to transfer audio data via network socket
	 audioSystem=0;
	 if (getParameter("audio_system")!=null && getParameter("audio_system").equals("sun")) {
	    audioSystem=0;
	 }
	 
	 if (getParameter("audio_system")!=null && getParameter("audio_system").equals("aserv")) {
	    audioSystem=1;
	 } 
	 
	 System.out.println ("Audio system is: " + audioSystem);
	 
	 // Determine fetch mode: preload or stream
	 fetchMode=0;
	 if (getParameter("fetch_mode")!=null && getParameter("fetch_mode").equals("stream")) {
	    fetchMode=0;
	 }
	 if (getParameter("fetch_mode")!=null && getParameter("fetch_mode").equals("preload")) {
	    fetchMode=1;
	 }
	 System.out.println ("Fetch mode is: " + fetchMode);
	 
	 
         System.out.println("init: table is "+ table_url);
	 try {
	    Gsm.readTable(table_url.openStream());
	 } catch (IOException ioe){
	    System.out.println("init: Couldn't open table"+ioe);
	 }

      } catch ( Exception e) {
         System.out.println("init: " +e);
      }
   }

   //public void run () {
   //   int i=0;
   //   for (i=0; i<100; i++) {
	// progress_bar.setValue(i);
	 //sleep (1000);
      //}
  // }
   
   public boolean action (Event e, Object arg) {
      if ( "Start".equals(arg)){
	 System.out.println ("START: ");
	 if (!playing) {
	    player = new Play (audio_url,0, playProgressBar, bufferSizeBar);
	    player.start();
	    playing = true;
	 }
      } else
	if ( "Stop".equals(arg)) {
	   System.out.println ("STOP: ");
	   if (playing) {
	      player.stop();
	      playing = false;
	      System.exit(0);
	   }
	}
      return true;
   }


}

