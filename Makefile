JFLAGS = -g -d $(BINDIR)/ -cp $(BINDIR)
CLASSPATH = $(BINDIR)
SRCDIR=src
BINDIR=bin
DOCDIR=doc

.SUFFIXES: .java .class


$(SRCDIR)/%.class:$(SRCDIR)/%.java
	javac $(JFLAGS) $<

CLASSES=ClientSide.class ServerSide.class

CLASS_FILES=$(CLASSES:%.class=$(SRCDIR)/%.class)

default: $(CLASS_FILES)

client: $(CLASS_FILES)
	java -cp $(CLASSPATH) ClientSide $(PORT)

server: $(CLASS_FILES)
	java -cp $(CLASSPATH) ServerSide $(PORT)

docs:
	javadoc -cp $(CLASSPATH) - $(DOCDIR) $(SRCDIR)/*.java

clean:
	rm $(BINDIR)/*.class

cleandocs:
	rm $(DOCDIR)/*

cleandata:
	rm  ChatService.db