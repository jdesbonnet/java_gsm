import java.awt.*;

class ProgressBar extends Canvas {
   int w,h,t,current_value;
  ProgressBar () {
     current_value=0;
  }
  public void setValue (int v) {
     if (v<0) {
	v=0;
	return;
     }
     if (v>99) {
	v=99;
	return;
     }
     current_value=v;
     repaint();
  }
   
  public void paint (Graphics g) {

     // Note: size() is depreciated - so we sould be using getSize(), but Netscape 4 does not support
     // jdk1.1 properly
     w = size().width;
     h = size().height;

     
     // Progress bar sould be no more than ten pixels high
     if (h>10) {
	h=10;
     }
     
     t = (current_value*w)/100;
     g.setColor(Color.red);
     g.fillRect(0,0,t,h-1);
     g.setColor(Color.black);
     g.fillRect(t+1,0,w-t-1,h-1);
  }
  public Dimension getPreferredSize() {
     return new Dimension(150,20);
  }
}     
