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
    Socket nextSock4; // socket connect to the server
    Socket nextSock5; // socket connect to the server
    Socket nextSock6; // socket connect to the server
    Socket nextSock7; // socket connect to the server
    Socket nextSock8; // socket connect to the server
    Socket nextSock9; // socket connect to the server
    Socket nextSock10; // socket connect to the server

    ObjectOutputStream out; // stream write to the socket
    ObjectInputStream in; // stream read from the socket
    ObjectOutputStream out1; // stream write to the socket
    ObjectInputStream in1; // stream read from the socket
    ObjectOutputStream out2; // stream write to the socket
    ObjectInputStream in2; // stream read from the socket
    ObjectOutputStream out3; // stream write to the socket
    ObjectInputStream in3; // stream read from the socket
    ObjectOutputStream out4; // stream write to the socket
    ObjectInputStream in4; // stream read from the socket
    ObjectOutputStream out5; // stream write to the socket
    ObjectInputStream in5; // stream read from the socket
    ObjectOutputStream out6; // stream write to the socket
    ObjectInputStream in6; // stream read from the socket
    ObjectOutputStream out7; // stream write to the socket
    ObjectInputStream in7; // stream read from the socket
    ObjectOutputStream out8; // stream write to the socket
    ObjectInputStream in8; // stream read from the socket
    ObjectOutputStream out9; // stream write to the socket
    ObjectInputStream in9; // stream read from the socket
    ObjectOutputStream out10; // stream write to the socket
    ObjectInputStream in10; // stream read from the socket


    byte[] fromServer; // capitalized message read from the server
    byte[] fromServer1; // capitalized message read from the server
    byte[] fromServer2; // capitalized message read from the server
    byte[] fromServer3; // capitalized message read from the server

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

                if (!pp.hasFile()) {
                    sendMessageBB(Messages.createRequestMessage(pp.randomMissingPiece()));
                    sendMessageBB(Messages.createBitfieldMessage(pp.getCurrBitfield()));
                }
            }

        }, 0, 2000);
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
                        if (!pp.isNeighbor(rpi.getPeerId())) { 
                            // do not send choke message if it's already choked
                            pp.messagesToSend.add(Messages.createChokeMessage());
                            count++;
                            //1003, 1002
                            if((rpi.getPeerId() == pp.allPeers.get(2).getPeerId() && peerID ==pp.allPeers.get(1).getPeerId()) || (rpi.getPeerId() == pp.allPeers.get(1).getPeerId() && peerID ==pp.allPeers.get(2).getPeerId())) {
                                sendMessage1(pp.messagesToSend.get(count - 1));
                            }

                            //1003, 1004
                            if((rpi.getPeerId() == pp.allPeers.get(2).getPeerId() && peerID ==pp.allPeers.get(3).getPeerId()) ||(rpi.getPeerId() == pp.allPeers.get(3).getPeerId() && peerID ==pp.allPeers.get(2).getPeerId()) )
                                sendMessage2(pp.messagesToSend.get(count - 1));

                            //1004, 1002
                            if((rpi.getPeerId() == pp.allPeers.get(3).getPeerId() && peerID ==pp.allPeers.get(1).getPeerId()) || (rpi.getPeerId() == pp.allPeers.get(1).getPeerId() && peerID ==pp.allPeers.get(3).getPeerId()))
                                sendMessage3(pp.messagesToSend.get(count - 1));




                            if (connectedToPeerId == rpi.getPeerId()) {
                                // System.out.println("Choking peer " + rpi.getPeerId());
                                rpi.setChoked(true);
                                sendMessageBB(pp.messagesToSend.get(count - 1));
                            }
                        } else if (pp.isNeighbor(rpi.getPeerId())) {
                            pp.messagesToSend.add(Messages.createUnchokeMessage());
                            count++;
                            //1003, 1002
                            if((rpi.getPeerId() == 1003 && peerID ==1002) || (rpi.getPeerId() == 1002 && peerID ==1003)) {
                                sendMessage1(pp.messagesToSend.get(count - 1));
                            }

                            //1003, 1004
                            if((rpi.getPeerId() == 1003 && peerID ==1004) ||(rpi.getPeerId() == 1004 && peerID ==1003) )
                                sendMessage2(pp.messagesToSend.get(count - 1));

                            //1004, 1002
                            if((rpi.getPeerId() == 1004 && peerID ==1002) || (rpi.getPeerId() == 1002 && peerID ==1004))
                                sendMessage3(pp.messagesToSend.get(count - 1));


                            if (connectedToPeerId == rpi.getPeerId()) {
                                // System.out.println("Setting peer " + rpi.getPeerId() + " to be a preferred
                                // neighbor");
                                rpi.setChoked(false);
                                sendMessageBB(pp.messagesToSend.get(count - 1));
                            }

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
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
                        // System.out.println("Optimistically unchoking " + rpi.getPeerId());
                        rpi.setChoked(false);
                        sendMessageBB(pp.messagesToSend.get(0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, 0, pp.optimisticUnchokingInterval * 1000);
    }

    void run() {

        try {
            // create a socket to connect to the server

            requestSocket = new Socket("localhost", pp.getPortNumber());
            System.out.println("Connected to localhost " + pp.getPortNumber());
            nextSock = new Socket("localhost", pp.allPeers.get(1).getPeerId());
            System.out.println("Connected to localhost " + pp.allPeers.get(1).getPeerId());
            nextSock2 = new Socket("localhost", pp.allPeers.get(2).getPeerId());
            System.out.println("Connected to localhost " + pp.allPeers.get(2).getPeerId());
            nextSock3 = new Socket("localhost", pp.allPeers.get(3).getPeerId());
            System.out.println("Connected to localhost " + pp.allPeers.get(3).getPeerId());
            nextSock4 = new Socket("localhost", pp.allPeers.get(4).getPeerId());
            System.out.println("Connected to localhost " + pp.allPeers.get(4).getPeerId());
            nextSock5 = new Socket("localhost", pp.allPeers.get(5).getPeerId());
            System.out.println("Connected to localhost " + pp.allPeers.get(5).getPeerId());
            nextSock6 = new Socket("localhost", pp.allPeers.get(5).getPeerId()+1);
            System.out.println("Connected to localhost " + pp.allPeers.get(5).getPeerId()+1);
            nextSock7 = new Socket("localhost", pp.allPeers.get(5).getPeerId()+2);
            System.out.println("Connected to localhost " + pp.allPeers.get(1).getPeerId()+2);
            nextSock8 = new Socket("localhost", pp.allPeers.get(5).getPeerId()+3);
            System.out.println("Connected to localhost " + pp.allPeers.get(5).getPeerId()+3);
            nextSock9 = new Socket("localhost", pp.allPeers.get(5).getPeerId()+4);
            System.out.println("Connected to localhost " + pp.allPeers.get(5).getPeerId()+4);
            nextSock10 = new Socket("localhost", pp.allPeers.get(5).getPeerId()+5);
            System.out.println("Connected to localhost " + pp.allPeers.get(3).getPeerId()+5);

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

            out4 = new ObjectOutputStream(nextSock4.getOutputStream());
            out4.flush();
            in4 = new ObjectInputStream(nextSock4.getInputStream());

            out5 = new ObjectOutputStream(nextSock5.getOutputStream());
            out5.flush();
            in5 = new ObjectInputStream(nextSock5.getInputStream());

            out6 = new ObjectOutputStream(nextSock6.getOutputStream());
            out6.flush();
            in6 = new ObjectInputStream(nextSock6.getInputStream());

            out7 = new ObjectOutputStream(nextSock7.getOutputStream());
            out7.flush();
            in7 = new ObjectInputStream(nextSock7.getInputStream());

            out8 = new ObjectOutputStream(nextSock8.getOutputStream());
            out8.flush();
            in8 = new ObjectInputStream(nextSock8.getInputStream());

            out9 = new ObjectOutputStream(nextSock9.getOutputStream());
            out9.flush();
            in9 = new ObjectInputStream(nextSock9.getInputStream());

            out10 = new ObjectOutputStream(nextSock10.getOutputStream());
            out10.flush();
            in10 = new ObjectInputStream(nextSock10.getInputStream());


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
                // System.out.println("Receive message"); // debug message

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

                // send interested message to server, this messagesToSend is created in messsages.java
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
                int pieceMsg = 0;

                int messageLength = -1;
                int bytesReadSoFar = 0;

                int messageLength1 = -1;
                int bytesReadSoFar1 = 0;
                boolean in1handshaked = false;

                int messageLength2 = -1;
                int bytesReadSoFar2 = 0;
                boolean in2handshaked = false;

                int messageLength3 = -1;
                int bytesReadSoFar3 = 0;
                boolean in3handshaked = false;

                while (true) {
                    while (in.available() <= 0) {
                    }
                    try {

                        // the number of bytes that should be read from the buffer. This number is
                        // obtained by looking at the first 4 bytes of the message. If messageLength
                        // is -1, then it's unknown, and the next thing to read is the message length.
                        // If it's not negative one, then read messageLength + 1 bytes. +1 because of
                        // the message type.
                        while ((messageLength == -1 && in.available() >= 4) || (messageLength != -1 && in.available() > 0)) {
                            if (messageLength == -1 && in.available() >= 4) {
                                byte[] messageLengthBuff = new byte[4];
                                bytesReadSoFar = in.read(messageLengthBuff, 0, 4);// only read in 4 bytes
                                buff = ByteBuffer.wrap(messageLengthBuff);
                                messageLength = Messages.GetMessageLength(buff);
                                fromServer = new byte[messageLength + 5];
                                fromServer[0] = messageLengthBuff[0];
                                fromServer[1] = messageLengthBuff[1];
                                fromServer[2] = messageLengthBuff[2];
                                fromServer[3] = messageLengthBuff[3];

                            } else if (messageLength != -1 && in.available() > 0) {
                                // read to the end of the message, or until the end of the input stream buffer
                                int bytesToRead = Math.min(in.available(), messageLength + 5 - bytesReadSoFar);
                                connectedToPeerId = originalId;
                                int bytesReadNow = in.read(fromServer, bytesReadSoFar, bytesToRead);
                                pp.logger.log("Reading " + bytesReadNow + " bytes");
                                bytesReadSoFar += bytesReadNow;
                                buff = ByteBuffer.wrap(fromServer);

                                if (bytesReadSoFar < messageLength) {
                                    continue;
                                } else {
                                    pp.logger.log("Done reading entire message. messageLength: " + messageLength + " + 5. bytesReadSoFar: " + bytesReadSoFar);
                                }

                                pieceMsg = Messages.decodeMessage(buff, pp, connectedToPeerId);

                                for (int i = 0; i < pp.messagesToSend.size(); i++) {
                                    sendMessageBB(pp.messagesToSend.get(i));
                                }
                                pp.messagesToSend.clear();
                                for (int i = 0; i < pp.pieceMessages.size(); i++) {
                                    sendMessageBB(pp.pieceMessages.get(i));
                                }

                                pp.pieceMessages.clear();
                                messageLength = -1;
                                bytesReadSoFar = 0;
                            } else {
                                pp.logger.log("Waiting for more data from in. in.available() = " + in.available() + ". messageLength = " + messageLength);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // used for connections between clients
                    if (!in1handshaked) {

                        while (in1.available() > 0) {
                            fromServer1 = new byte[in1.available()];
                            in1.read(fromServer1);
                            buff = ByteBuffer.wrap(fromServer1);
                            if (buff.remaining() == 32) {
                                newId1 = Messages.decodeMessage(buff, pp, -1);
                                in1handshaked = true;
                                break;
                            }
                        }
                    }
                    while (in1handshaked && ((messageLength1 == -1 && in1.available() >= 4) || (messageLength1 != -1 && in1.available() > 0))) {
                        if (messageLength1 == -1 && in1.available() >= 4) {
                            byte[] messageLengthBuff = new byte[4];
                            bytesReadSoFar1 = in1.read(messageLengthBuff, 0, 4);// only read in 4 bytes

                            buff = ByteBuffer.wrap(messageLengthBuff);
                            messageLength1 = Messages.GetMessageLength(buff);
                            fromServer1 = new byte[messageLength1 + 5];
                            fromServer1[0] = messageLengthBuff[0];
                            fromServer1[1] = messageLengthBuff[1];
                            fromServer1[2] = messageLengthBuff[2];
                            fromServer1[3] = messageLengthBuff[3];

                        } else if (messageLength1 != -1 && in1.available() > 0) {
                            // read to the end of the message, or until the end of the input stream buffer
                            int bytesToRead = Math.min(in1.available(), messageLength1 + 5 - bytesReadSoFar1);
                            int bytesReadNow = in1.read(fromServer1, bytesReadSoFar1, bytesToRead);
                            pp.logger.log("Reading " + bytesReadNow + " bytes");
                            bytesReadSoFar1 += bytesReadNow;
                            buff = ByteBuffer.wrap(fromServer1);


                            if (bytesReadSoFar1 < messageLength1) {
                                continue;
                            } else {
                                pp.logger.log("Done reading entire message. messageLength1: " + messageLength1 + " + 5. bytesReadSoFar: " + bytesReadSoFar1);
                            }

                            pieceMsg = Messages.decodeMessage(buff, pp, newId1);

                            /*for (int i = 0; i < pp.messagesToSend.size(); i++) {
                                sendMessageBB(pp.messagesToSend.get(i));
                            }
                            pp.messagesToSend.clear();
                            for (int i = 0; i < pp.pieceMessages.size(); i++) {
                                sendMessageBB(pp.pieceMessages.get(i));
                            }*/

                            for (int i = 0; i < pp.pieceMessages.size(); i++) {
                                sendMessage1(pp.pieceMessages.get(i));
                            }
                            for (int i = 0; i < pp.messagesToSend.size(); i++) {
                                sendMessage1(pp.messagesToSend.get(i));
                            }

                            pp.messagesToSend.clear();
                            pp.pieceMessages.clear();

                            // send the bitfield message after receiving a message
                            /*pp.logger.log("Sending bitfield\n");
                            sendMessageBB(Messages.createBitfieldMessage(pp.getCurrBitfield()));*/
                            messageLength1 = -1;
                            bytesReadSoFar1 = 0;


                        }

                    }
                    if (!in2handshaked) {
                        while (in2.available() > 0) {

                            fromServer2 = new byte[in2.available()];
                            in2.read(fromServer2);
                            buff = ByteBuffer.wrap(fromServer2);


                            if (buff.remaining() == 32) {
                                in2handshaked = true;
                                newId2 = Messages.decodeMessage(buff, pp, -1);
                                break;
                            }
                        }
                    }
                    while (in2handshaked && ((messageLength2 == -1 && in2.available() >= 4) || (messageLength2 != -1 && in2.available() > 0))) {
                        if (messageLength2 == -1 && in2.available() >= 4) {
                            byte[] messageLengthBuff = new byte[4];
                            bytesReadSoFar2 = in2.read(messageLengthBuff, 0, 4);// only read in 4 bytes

                            buff = ByteBuffer.wrap(messageLengthBuff);
                            messageLength2 = Messages.GetMessageLength(buff);
                            fromServer2 = new byte[messageLength2 + 5];
                            fromServer2[0] = messageLengthBuff[0];
                            fromServer2[1] = messageLengthBuff[1];
                            fromServer2[2] = messageLengthBuff[2];
                            fromServer2[3] = messageLengthBuff[3];

                        } else if (messageLength2 != -1 && in2.available() > 0) {
                            // read to the end of the message, or until the end of the input stream buffer
                            int bytesToRead = Math.min(in2.available(), messageLength2 + 5 - bytesReadSoFar2);
                            connectedToPeerId = originalId;
                            int bytesReadNow = in2.read(fromServer2, bytesReadSoFar2, bytesToRead);
                            pp.logger.log("Reading " + bytesReadNow + " bytes");
                            bytesReadSoFar2 += bytesReadNow;
                            buff = ByteBuffer.wrap(fromServer2);

                            if (bytesReadSoFar2 < messageLength3) {
                                continue;
                            } else {
                                pp.logger.log("Done reading entire message. messageLength2: " + messageLength2 + " + 5. bytesReadSoFar: " + bytesReadSoFar2);
                            }

                            pieceMsg = Messages.decodeMessage(buff, pp, newId2);

                            /*for (int i = 0; i < pp.messagesToSend.size(); i++) {
                                sendMessageBB(pp.messagesToSend.get(i));
                            }
                            pp.messagesToSend.clear();
                            for (int i = 0; i < pp.pieceMessages.size(); i++) {
                                sendMessageBB(pp.pieceMessages.get(i));
                            }*/

                            for (int i = 0; i < pp.pieceMessages.size(); i++) {
                                sendMessage2(pp.pieceMessages.get(i));
                            }

                            for (int i = 0; i < pp.messagesToSend.size(); i++) {
                                sendMessage2(pp.messagesToSend.get(i));
                            }
                            pp.messagesToSend.clear();
                            pp.pieceMessages.clear();

                            // send the bitfield message after receiving a message
                           /* pp.logger.log("Sending bitfield\n");
                            sendMessageBB(Messages.createBitfieldMessage(pp.getCurrBitfield()));*/
                            messageLength2 = -1;
                            bytesReadSoFar2 = 0;
                        }


                    }
                    if (!in3handshaked) {
                        while (in3.available() > 0) {
                            fromServer3 = new byte[in3.available()];
                            in3.read(fromServer3);
                            buff = ByteBuffer.wrap(fromServer3);


                            if (buff.remaining() == 32) {
                                in3handshaked = true;
                                newId3 = Messages.decodeMessage(buff, pp, -1);
                                break;
                            }
                        }
                    }
                    while (in3handshaked && ((messageLength3 == -1 && in3.available() >= 4) || (messageLength3 != -1 && in3.available() > 0))) {
                        if (messageLength3 == -1 && in3.available() >= 4) {
                            byte[] messageLengthBuff = new byte[4];
                            bytesReadSoFar3 = in3.read(messageLengthBuff, 0, 4);// only read in 4 bytes

                            buff = ByteBuffer.wrap(messageLengthBuff);
                            messageLength3 = Messages.GetMessageLength(buff);
                            fromServer3 = new byte[messageLength3 + 5];
                            fromServer3[0] = messageLengthBuff[0];
                            fromServer3[1] = messageLengthBuff[1];
                            fromServer3[2] = messageLengthBuff[2];
                            fromServer3[3] = messageLengthBuff[3];

                        } else if (messageLength3 != -1 && in3.available() > 0) {
                            // read to the end of the message, or until the end of the input stream buffer
                            int bytesToRead = Math.min(in3.available(), messageLength3 + 5 - bytesReadSoFar3);
                            connectedToPeerId = originalId;
                            int bytesReadNow = in3.read(fromServer3, bytesReadSoFar3, bytesToRead);
                            pp.logger.log("Reading " + bytesReadNow + " bytes");
                            bytesReadSoFar3 += bytesReadNow;
                            buff = ByteBuffer.wrap(fromServer3);

                            if (bytesReadSoFar3 < messageLength3) {
                                continue;
                            } else {
                                pp.logger.log("Done reading entire message. messageLength3: " + messageLength3 + " + 5. bytesReadSoFar: " + bytesReadSoFar3);
                            }

                            pieceMsg = Messages.decodeMessage(buff, pp, newId3);

                           /* for (int i = 0; i < pp.messagesToSend.size(); i++) {
                                sendMessageBB(pp.messagesToSend.get(i));
                            }
                            pp.messagesToSend.clear();
                            for (int i = 0; i < pp.pieceMessages.size(); i++) {
                                sendMessageBB(pp.pieceMessages.get(i));
                            }*/


                            for (int i = 0; i < pp.pieceMessages.size(); i++) {
                                sendMessage3(pp.pieceMessages.get(i));
                            }
                            for (int i = 0; i < pp.messagesToSend.size(); i++) {
                                sendMessage3(pp.messagesToSend.get(i));
                            }
                            pp.messagesToSend.clear();
                            pp.pieceMessages.clear();

                            // send the bitfield message after receiving a message
                            /*pp.logger.log("Sending bitfield\n");
                            sendMessageBB(Messages.createBitfieldMessage(pp.getCurrBitfield()));*/
                            messageLength3 = -1;
                            bytesReadSoFar3 = 0;
                        }


                    }

                }
            
        }
        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
            unknownHost.printStackTrace();
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

                in4.close();
                out4.close();

                in5.close();
                out5.close();

                in6.close();
                out6.close();

                in7.close();
                out7.close();

                in8.close();
                out8.close();

                in9.close();
                out9.close();

                in10.close();
                out10.close();

                requestSocket.close();
                nextSock.close();
                nextSock2.close();
                nextSock3.close();
                nextSock4.close();
                nextSock5.close();
                nextSock6.close();
                nextSock7.close();
                nextSock8.close();
                nextSock9.close();
                nextSock10.close();


            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
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
  
    void sendMessage1(ByteBuffer msg) {
        try {
            // stream write the message
            out1.write(msg.array());
            out1.flush();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    void sendMessage2(ByteBuffer msg) {
        try {
            // stream write the message
            out2.write(msg.array());
            out2.flush();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    void sendMessage3(ByteBuffer msg) {
        try {
            // stream write the message
            out3.write(msg.array());
            out3.flush();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
