###########################
# Simple Generic Makefile #
###########################

CC=terminal-gcc

CFLAGS=-c -Wall
LDFLAGS=-shared

#SOURCES=myfunc.c
SOURCES=$(shell ls *.c)

OBJECTS=$(SOURCES:.c=.o)
EXECUTABLE=libmyfunc.so

all: $(SOURCES) $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.c.o:
	$(CC) $(CFLAGS) $< -o $@

install:
	install -m 0755 $(EXECUTABLE) $(HOME)/local/lib
	install -m 0644 myfunc.h $(HOME)/local/include

clean:
	rm -rf *o $(EXECUTABLE)

