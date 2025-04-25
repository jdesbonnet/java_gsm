JAVAC = javac
#JFLAGS = -deprecation
JFLAGS = 
CFLAGS = -Wall

all: clean aserv VoiceNow.class

aserv.o: aserv.c

aserv: aserv.o
	$(CC) -o aserv aserv.o

VoiceNow.class: VoiceNow.java
	$(JAVAC) $(JFLAGS) VoiceNow.java
	
AudioEngine.class: VoiceNow.java
	$(JAVAC) $(JFLAGS) VoiceNow.java

clean:
	rm -f *.class *.o
	
