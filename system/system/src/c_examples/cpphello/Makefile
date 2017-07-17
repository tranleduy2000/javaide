###########################
# Simple Generic Makefile #
###########################

CC=terminal-g++

CFLAGS=-c -Wall
LDFLAGS=

#SOURCES=rect.cpp
SOURCES=$(shell ls *.cpp)

OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=rect

all: $(SOURCES) $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.cpp.o:
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -rf *o $(EXECUTABLE)

