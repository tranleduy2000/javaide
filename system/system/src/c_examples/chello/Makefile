###########################
# Simple Generic Makefile #
###########################

CC=terminal-gcc

CFLAGS=-c -Wall
LDFLAGS=

#SOURCES=hello.c
SOURCES=$(shell ls *.c)

OBJECTS=$(SOURCES:.c=.o)
EXECUTABLE=hello

all: $(SOURCES) $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.c.o:
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -rf *o $(EXECUTABLE)

