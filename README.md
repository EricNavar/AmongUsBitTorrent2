# Computer Network Fundamentals Semester Project

**video link:** ```https://uflorida-my.sharepoint.com/:v:/g/personal/ericnavar_ufl_edu/Eeu9FPaEGstAq-FwhDckZewBWHXLGnUZPyhgZKSlorLf7w?e=4wCKaZ```

# Members

- Eric Navar
- Greg Bolling
- Santosh Tirumala

# How to run

Our StartRemotePeers does not work. Running the program required manually SSHing.

For every process you want use, do the following:
- Open a terminal
- SSH into storm
    - ex: `ssh ericnavar@storm.cise.ufl.edu`
- SSH into the lin server that you want to use.
    - They are listed in PeerInfo.cfg.
    - ex: `ssh ericnavar@lin114-01.cise.ufl.edu`
    - only up to 5 sessions can be had at once with on the CISE servers with one account
- If the repository has not been cloned yet, then clone the repository.
    - ex: `git clone https://github.com/EricNavar/AmongUsBitTorrent2`
- cd into the repository
- compile
    - `./compile.sh`
    - this might take a while
- Run the process
    - run the process using the peer id number corresponding to the lin server being used from PeerInfo.cfg
    - ex: on lin114-01.cise.ufl.edu, run `java peerProcess 1001`
    - The port numbers in PeerInfo.cfg will need to be changed if there is a port conflict
    - Run the processes in the order that they appear in PeerInfo.cfg. For the best result, wait a second between running each process

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
lin114-11.cise.ufl.edu  10.242.94.45

# Project Structure

## peerProcess.java

- This contains the main method. Holds a reference for other objects like the Logger, Client, FileWriter, MessagesHandler
- reads both config files

## RemotePeerInfo.java
- A process uses a RemotePeerInfo object to hold data about other peers such as 
  - has file
  - bitfield
  - port
  - address
  - is choked
  - pieces transmitted (used for calculating preferred neighbor)

## Client.java

This is the client code. The starting point of this was the Client.java from Canvas. Client.java creates a Handler instance for each other peer that it handshakes with.

## Handler
The handler extends the thread class. When a handler detects that it has all the pieces, it sends a bitfield and safely closes the thread and the main peerProcess.

## Messages.java

This handles messages. It decodes the bytebuffers that are received from the inputstreams. It also encodes new messages to send

## MessageType.java

This just defines an enum for the message type  

## Logger.java

The Logger writes the logs. Can also be used for general debugging

## FileHandling.java

This is for writing to the destination files, ex: thefile. It writes pieces as soon as it gets them.

## remove.sh
- run `./remove.sh` to remove unnecessary files. .log files, .class files, and all the directories peer_1002/ through peer-1010/

## unused files/
- CFG.java
- StartRemotePeers.java

**All other files are unused or used for testing**

![](./peer_1001/tree.jpg)  
