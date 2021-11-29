// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

import java.nio.*;
import java.io.IOException;
// idea of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/

public class Client {
    Socket requestSocket; // socket connect to the server
    Socket nextSock; // socket connect to the server
    Socket nextSock2; // socket connect to the server
    Socket nextSock3; // socket connect to the server

    ObjectOutputStream out; // stream write to the socket
    ObjectInputStream in; // stream read from the socket
    ObjectOutputStream out1; // stream write to the socket
    ObjectInputStream in1; // stream read from the socket
    ObjectOutputStream out2; // stream write to the socket
    ObjectInputStream in2; // stream read from the socket
    ObjectOutputStream out3; // stream write to the socket
    ObjectInputStream in3; // stream read from the socket

    byte[] fromServer; // capitalized message read from the server
    int peerID;
    int connectedToPeerId;
    int originalId;
    int newId1;
    int newId2;
    int newId3;

    // String bitfieldHandshake;
    // FileHandling handler;

    // int socket;
    peerProcess pp;

    void setPeerID(int t_peerID) {
        peerID = t_peerID;
    }

    public Client(peerProcess pp) {
        this.pp = pp;
    }

    // Sometimes the program just stops, so I'm making this timer to see if 
    // requesting another piece every second will keep things running
    private void runRequestTimer() {
        // Every 5 seconds, recalculate the preferred neighbors
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                sendMessageBB(Messages.createRequestMessage(pp.randomMissingPiece()));
            }

        }, 0, pp.unchokingInterval * 1000);
    }

    // Timer for unchoking the neighbors who send the most data. Optimistically
    // unchoked neighbors is unchoked
    // in the runOptimisticallyUnchokedTimer()
    private void runUnchokingTimer() {
        // Every 5 seconds, recalculate the preferred neighbors
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    pp.calculatePreferredNeighbors();
                    pp.messagesToSend.clear();
                    // choke unchosen peers, unchoke chosen peers
                    int count = 0;
                    for (int i = 0; i < pp.peerInfoVector.size(); i++) {
                        RemotePeerInfo rpi = pp.peerInfoVector.get(i);
                        if (!pp.isNeighbor(rpi.getPeerId()) && !rpi.isChoked()) { // do not send choke message if it's
                                                                                  // already choked
                            pp.messagesToSend.add(Messages.createChokeMessage());
                            count++;
                            if (connectedToPeerId == rpi.getPeerId()) {
                                //System.out.println("Choking peer " + rpi.getPeerId());
                                rpi.setChoked(true);
                                sendMessageBB(pp.messagesToSend.get(count - 1));
                            }
                        } else if (pp.isNeighbor(rpi.getPeerId()) && rpi.isChoked()) {
                            pp.messagesToSend.add(Messages.createUnchokeMessage());
                            count++;
                            if (connectedToPeerId == rpi.getPeerId()) {
                                //System.out.println("Setting peer " + rpi.getPeerId() + " to be a preferred neighbor");
                                rpi.setChoked(false);
                                sendMessageBB(pp.messagesToSend.get(count - 1));
                            }
                        }
                    }

                } catch (Exception e) {
                }
            }

        }, 0, pp.unchokingInterval * 1000);
    }

    private void runOptimisticallyUnchokedTimer() {
        // Every 10 seconds, recalculate the preferred neighbors
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    pp.chooseOptimisticallyUnchokedPeer();
                    if (pp.optimisticallyUnchokedPeer == -1) {
                        return;
                    }
                    RemotePeerInfo rpi = pp.getRemotePeerInfo(pp.optimisticallyUnchokedPeer);
                    pp.messagesToSend.clear();
                    pp.messagesToSend.add(Messages.createUnchokeMessage());
                    if (connectedToPeerId == rpi.getPeerId()) {
                        //System.out.println("Optimistically unchoking " + rpi.getPeerId());
                        rpi.setChoked(false);
                        sendMessageBB(pp.messagesToSend.get(0));
                    }
                } catch (Exception e) {
                }
            }

        }, 0, pp.optimisticUnchokingInterval * 1000);
    }

    void run() {

        try {
            // create a socket to connect to the server

            requestSocket = new Socket("localhost", pp.getPortNumber());
            System.out.println("Connected to localhost " + pp.getPortNumber());
            nextSock = new Socket("localhost", 1002);
            System.out.println("Connected to localhost " + 1002);
            nextSock2 = new Socket("localhost", 1003);
            System.out.println("Connected to localhost " + 1003);
            nextSock3 = new Socket("localhost", 1004);
            System.out.println("Connected to localhost " + 1004);


            // initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            out1 = new ObjectOutputStream(nextSock.getOutputStream());
            out1.flush();
            in1 = new ObjectInputStream(nextSock.getInputStream());

            out2 = new ObjectOutputStream(nextSock2.getOutputStream());
            out2.flush();
            in2 = new ObjectInputStream(nextSock2.getInputStream());

            out3 = new ObjectOutputStream(nextSock3.getOutputStream());
            out3.flush();
            in3 = new ObjectInputStream(nextSock3.getInputStream());


            // create handshake message and send to server
            ByteBuffer messageToSend = Messages.createHandshakeMessage(pp.getPeerId());
            sendMessageBB(messageToSend);

            while (true) {
                // busy wait for input
                while (in.available() <= 0) {
                }

                fromServer = new byte[in.available()];
                in.read(fromServer);
                ByteBuffer buff = ByteBuffer.wrap(fromServer);
                //System.out.println("Receive message"); // debug message

                // receive handshake message from server
                connectedToPeerId = Messages.decodeMessage(buff, pp, -1);
                pp.logger.onConnectingTo(connectedToPeerId);
                System.out.println("I am peer " + pp.getPeerId() + " and I am connected to " + connectedToPeerId);
                originalId = connectedToPeerId;

                // send bitfield to server
                messageToSend = Messages.createBitfieldMessage(pp.getCurrBitfield());
                sendMessageBB(messageToSend);

                // expect a bitfield back
                while (in.available() <= 0) {
                }
                fromServer = new byte[in.available()];
                in.read(fromServer);
                buff = ByteBuffer.wrap(fromServer);
                int bitfieldMsg = Messages.decodeMessage(pp, buff, connectedToPeerId);

                // send interested message to server, this messagesToSend is created in
                // messsages.java
                for (int i = 0; i < pp.messagesToSend.size(); i++) {
                    sendMessageBB(pp.messagesToSend.get(i));
                }

                while (in.available() <= 0) {
                }
                fromServer = new byte[in.available()];
                in.read(fromServer);
                buff = ByteBuffer.wrap(fromServer);
                int interestMsg = Messages.decodeMessage(buff, pp, connectedToPeerId);

              
                pp.messagesToSend.clear();
                runUnchokingTimer();
                runOptimisticallyUnchokedTimer();
                runRequestTimer();
                int pieceMsg=0;
              
                while (true) {
                    //used for connections between clients
                    while(in1.available() > 0)
                    {
                        fromServer = new byte[in1.available()];
                        in1.read(fromServer);
                        buff = ByteBuffer.wrap(fromServer);
                        if (buff.remaining() >= 32) {
                            newId1 =Messages.decodeMessage(buff, pp, -1);
                        }
                        pieceMsg = Messages.decodeMessage(buff, pp, newId1);
                        // send the bitfield message after receiving a message
                        sendMessageBB(Messages.createBitfieldMessage(pp.getCurrBitfield()));
                    }
                    while(in2.available() > 0)
                    {
                        fromServer = new byte[in2.available()];
                        in2.read(fromServer);
                        buff = ByteBuffer.wrap(fromServer);
                        if (buff.remaining() >= 32) {
                            newId2 =Messages.decodeMessage(buff, pp, -1);
                        }
                        pieceMsg = Messages.decodeMessage(buff, pp, newId2);
                        sendMessageBB(Messages.createBitfieldMessage(pp.getCurrBitfield()));
                    }
                    while(in3.available() > 0)
                    {
                        fromServer = new byte[in3.available()];
                        in3.read(fromServer);
                        buff = ByteBuffer.wrap(fromServer);
                        if (buff.remaining() >= 32) {
                            newId3 =Messages.decodeMessage(buff, pp, -1);
                        }
                        pieceMsg = Messages.decodeMessage(buff, pp, newId3);
                        sendMessageBB(Messages.createBitfieldMessage(pp.getCurrBitfield()));
                    }
                    while (in.available() <= 0) {
                    }
                    try {
                        while (in.available() > 0) {

                            connectedToPeerId = originalId;
                            fromServer = new byte[in.available()];
                            in.read(fromServer);
                            buff = ByteBuffer.wrap(fromServer);

                            pieceMsg = Messages.decodeMessage(buff, pp, connectedToPeerId);

                            for (int i = 0; i < pp.messagesToSend.size(); i++) {
                                sendMessageBB(pp.messagesToSend.get(i));
                            }
                            pp.messagesToSend.clear();
                            for (int i = 0; i < pp.pieceMessages.size(); i++)
                                sendMessageBB(pp.pieceMessages.get(i));

                            pp.pieceMessages.clear();


                        }

                /*while(in1.available() > 0)
                    {
                        in.read();
                    }*/


                    } catch (Exception e) {
                    }
                }

            }

        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            // Close connections
            try {
                in.close();
                out.close();
                in1.close();
                out1.close();
                in2.close();
                out2.close();
                in3.close();
                out3.close();
                requestSocket.close();
                nextSock.close();
                nextSock2.close();
                nextSock3.close();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // send a message to the output stream
    void sendMessage(String msg) {
        try {
            // stream write the message
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // send a message to the output stream
    void sendMessageBB(ByteBuffer msg) {
        try {
            // stream write the message
            out.write(msg.array());
            out.flush();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    void sendMessageOther(ByteBuffer msg) {
        try {
            // stream write the message
            out1.write(msg.array());
            out1.flush();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

}
