###########################
# Simple Generic Makefile #
###########################

CC=terminal-gcc

CFLAGS=-c -Wall -I$(HOME)/local/include
LDFLAGS=-lmyfunc -L$(HOME)/local/lib

#SOURCES=tester.c
SOURCES=$(shell ls *.c)

OBJECTS=$(SOURCES:.c=.o)
EXECUTABLE=tester

all: $(SOURCES) $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.c.o:
	$(CC) $(CFLAGS) $< -o $@

install:
	install -m 0755 $(EXECUTABLE) $(HOME)/local/bin

clean:
	rm -rf *o $(EXECUTABLE)

