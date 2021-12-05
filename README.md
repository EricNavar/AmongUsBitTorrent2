The final project for computer network fundamentals.
The team name is Among Us Bit Torrent 2 because the project is to make Bit Torrent so we can distribute Among Us to the masses.

![](./peer_1001/tree.jpg)  

# SSH

ssh ericnavar@storm.cise.ufl.edu
ssh ericnavar@lin114-06.cise.ufl.edu

## IP Addesses of the CISE Servers

lin114-00.cise.ufl.edu  10.242.94.34 
lin114-01.cise.ufl.edu  10.242.94.35
lin114-02.cise.ufl.edu  10.242.94.36
lin114-03.cise.ufl.edu  10.242.94.37
lin114-04.cise.ufl.edu  10.242.94.38
lin114-05.cise.ufl.edu  10.242.94.39
lin114-06.cise.ufl.edu  10.242.94.40
lin114-07.cise.ufl.edu  10.242.94.41
lin114-08.cise.ufl.edu  10.242.94.42
lin114-09.cise.ufl.edu  10.242.94.43
lin114-10.cise.ufl.edu  10.242.94.44

# Members

- Eric Navar
- Greg Bolling
- Santosh Tirumala

# How to run
## Compile:
```
./compile.sh
```

## Run
```
java StartRemotePeers
```

# Files

## peerProcess.java

This contains the main method. Holds a reference for other objects like the logger and the file writer

## MessageType.java

This just defines an enum for the message type  

## Logger.java

The Logger writes the logs

## README.md

You are literally reading the ReadMe

## Client.java

This is the client code. The starting point of this was the Client.java from Canvas  

## FileHandler.java

This is for writing to the destination files
