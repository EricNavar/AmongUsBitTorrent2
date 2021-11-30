// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
//import java.math.BigInteger;
import java.nio.*;
//import java.util.*;
// idean of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/
import java.io.IOException;
//import java.nio.channels.FileChannel;
//import java.io.FileOutputStream;

public class Server {
    static private Vector<Integer> haveFile;
    static private ArrayList<Handler> handlers = new ArrayList<Handler>();

    private static peerProcess pp;

    public Server(peerProcess pp_) {
        pp = pp_;

    }

    public void startServer() throws Exception {
        ServerSocket listener = new ServerSocket(pp.getPortNumber());
        ServerSocket second = new ServerSocket(pp.allPeers.get(1).getPeerId());
        ServerSocket third = new ServerSocket(pp.allPeers.get(2).getPeerId());
        ServerSocket fourth = new ServerSocket(pp.allPeers.get(3).getPeerId());
        ServerSocket fifth = new ServerSocket(pp.allPeers.get(4).getPeerId());
        ServerSocket sixth = new ServerSocket(pp.allPeers.get(5).getPeerId());
        ServerSocket seventh = new ServerSocket(pp.allPeers.get(5).getPeerId()+1);
        ServerSocket eighth = new ServerSocket(pp.allPeers.get(5).getPeerId()+2);
        ServerSocket ninth = new ServerSocket(pp.allPeers.get(5).getPeerId()+3);
        ServerSocket tenth = new ServerSocket(pp.allPeers.get(5).getPeerId()+4);
        ServerSocket eleventh = new ServerSocket(pp.allPeers.get(5).getPeerId()+5);

        // System.out.println("The server is running.");
        int clientNum = 1;

        // make list of peerIds that have the file
        haveFile = new Vector<Integer>();
        for (RemotePeerInfo rpi : pp.peerInfoVector) {
            if (rpi.hasFile()) {
                haveFile.addElement(rpi.getPeerId());
            }
        }

        try {
            while (true) {

                Handler h = new Handler(listener.accept(), second.accept(), third.accept(), fourth.accept(), fifth.accept(), sixth.accept(), seventh.accept(), eighth.accept(), ninth.accept(), tenth.accept(),  eleventh.accept(), clientNum);
                h.start();
                handlers.add(h);
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        } finally {
            listener.close();
            second.close();
            third.close();
            fourth.close();
            fifth.close();
            sixth.close();
            seventh.close();
            eighth.close();
            ninth.close();
            tenth.close();
            eleventh.close();

        }
    }

    /**
     * A handler thread class. Handlers are spawned from the listening loop and are
     * responsible for dealing with a single client's requests.
     */
    private static class Handler extends Thread {
        private byte[] message = new byte[50]; // message received from the client
        private Socket connection;
        private Socket connection1;
        private Socket connection2;
        private Socket connection3;
        private Socket connection4;
        private Socket connection5;
        private Socket connection6;
        private Socket connection7;
        private Socket connection8;
        private Socket connection9;
        private Socket connection10;

        Vector<ByteBuffer> receivedMessages = new Vector<ByteBuffer>(0);

        private ObjectInputStream in; // stream read from the socket
        private ObjectOutputStream out; // stream write to the socket

        private ObjectInputStream in1; // stream read from the socket
        private ObjectOutputStream out1; // stream write to the socket

        private ObjectInputStream in2; // stream read from the socket
        private ObjectOutputStream out2; // stream write to the socket

        private ObjectInputStream in3; // stream read from the socket
        private ObjectOutputStream out3; // stream write to the socket

        private ObjectInputStream in4; // stream read from the socket
        private ObjectOutputStream out4; // stream write to the socket

        private ObjectInputStream in5; // stream read from the socket
        private ObjectOutputStream out5; // stream write to the socket

        private ObjectInputStream in6; // stream read from the socket
        private ObjectOutputStream out6; // stream write to the socket

        private ObjectInputStream in7; // stream read from the socket
        private ObjectOutputStream out7; // stream write to the socket

        private ObjectInputStream in8; // stream read from the socket
        private ObjectOutputStream out8; // stream write to the socket

        private ObjectInputStream in9; // stream read from the socket
        private ObjectOutputStream out9; // stream write to the socket

        private ObjectInputStream in10; // stream read from the socket
        private ObjectOutputStream out10; // stream write to the socket

        private int no; // The index number of the client
        int connectedFrom;
        boolean firstTime = true;
        boolean secondTurn = true;

        boolean turn1 = true;


        public Handler(Socket connection, Socket connection1, Socket connection2, Socket connection3, Socket connection4, Socket connection5,  Socket connection6, Socket connection7, Socket connection8, Socket connection9, Socket connection10, int no) {
            this.connection = connection;
            this.connection1 = connection1;
            this.connection2 = connection2;
            this.connection3 = connection3;
            this.connection4 = connection4;
            this.connection5 = connection5;
            this.connection6 = connection6;
            this.connection7 = connection7;
            this.connection8 = connection8;
            this.connection9 = connection9;
            this.connection10 = connection10;
            this.no = no;

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
                        // the last element in the vector is the optimistically unchoked neighbor, so
                        // don't change that
                        for (int i = 0; i < pp.peerInfoVector.size(); i++) {
                            RemotePeerInfo rpi = pp.peerInfoVector.get(i);
                            if (!pp.isNeighbor(rpi.getPeerId())) {
                                // do not send choke messages to processes that are already choked
                                // pp.logger.log(rpi.getPeerId() + " is not a neighbor");
                                if (connectedFrom == rpi.getPeerId() && !rpi.isChoked()) {
                                    pp.messagesToSend.add(Messages.createChokeMessage());
                                    count++;
                                    // System.out.println("Choking peer " + rpi.getPeerId());
                                    rpi.setChoked(true);
                                    sendMessageBB(pp.messagesToSend.get(count - 1));
                                }
                            } else {
                                // do not send unchoke messages to processes that are already unchoked
                                // pp.logger.log(rpi.getPeerId() + " is a neighbor");
                                if (connectedFrom == rpi.getPeerId() && rpi.isChoked()) {
                                    pp.messagesToSend.add(Messages.createUnchokeMessage());
                                    count++;
                                    // System.out.println("Unchoking peer " + rpi.getPeerId());
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
                        if (connectedFrom == rpi.getPeerId()) {
                            // System.out.println("Optimistically unchoking peer " + rpi.getPeerId());
                            rpi.setChoked(false);
                            sendMessageBB(Messages.createUnchokeMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }, 0, pp.optimisticUnchokingInterval * 1000);
        }

        private void serverLoop() throws ClassNotFoundException, IOException {
            // https://stackoverflow.com/ques1tions/2702980/java-loop-every-minute

            while (true) {
                while (in.available() <= 0) {
                }

                message = new byte[in.available()];
                in.read(message);

                ByteBuffer buff = ByteBuffer.wrap(message);

                connectedFrom = Messages.decodeMessage(buff, pp, -1);
                pp.logger.onConnectingFrom(connectedFrom);
                ByteBuffer messageToSend = Messages.createHandshakeMessage(pp.peerId);
                sendMessageBB(messageToSend);

                // System.out.println("I am peer " + pp.getPeerId() + " (server) and I am
                // connected to " + connectedFrom);

                // receive bitfield message

                while (in.available() <= 0) {
                }
                byte[] message2 = new byte[in.available()];

                in.read(message2);

                ByteBuffer buff2 = ByteBuffer.wrap(message2);

                int bitfieldRes = Messages.decodeMessage(buff2, pp, connectedFrom);

                ByteBuffer bitfieldMessage = Messages.createBitfieldMessage(pp.bitfield);
                sendMessageBB(bitfieldMessage);

                while (in.available() <= 0) {
                }
                message = new byte[in.available()];

                in.read(message);

                buff = ByteBuffer.wrap(message);

                int interestedRes = Messages.decodeMessage(buff, pp, connectedFrom);

                // send interested/not interested
                for (int i = 0; i < pp.messagesToSend.size(); i++) {
                    sendMessageBB(pp.messagesToSend.get(i));
                }
                pp.messagesToSend.clear();
                runUnchokingTimer();
                runOptimisticallyUnchokedTimer();

                while (true) {
                   while (in1.available() > 0) {
                        message = new byte[in1.available()];

                        in1.read(message);

                        buff = ByteBuffer.wrap(message);
                        receivedMessages.add(buff);

                    }
                    while (in2.available() > 0) {
                        message = new byte[in2.available()];

                        in2.read(message);

                        buff = ByteBuffer.wrap(message);
                        receivedMessages.add(buff);

                    }
                    while (in3.available() > 0) {
                        message = new byte[in3.available()];

                        in3.read(message);

                        buff = ByteBuffer.wrap(message);
                        receivedMessages.add(buff);

                    }

                    if (handlers.size() >= 2) {
                        for (int i = 0; i < handlers.size(); i++) {
                            // start sending piece messages here
                            // request piece from client
                            // exclude server
                            // coordinate piece distributuion between clients

                            if (firstTime) {

                                if (handlers.get(i).connectedFrom == connectedFrom)
                                    continue;



                                if (handlers.get(i).connectedFrom == pp.allPeers.get(1).getPeerId()) {

                                  if(connectedFrom == 1004) {

                                      
                                        messageToSend = Messages.createHandshakeMessage(connectedFrom);
                                        handlers.get(i).sendMessage3(messageToSend);
                                        messageToSend = Messages.createHandshakeMessage(handlers.get(i).connectedFrom);
                                        sendMessage3(messageToSend);

                                        messageToSend = Messages.createBitfieldMessage(pp.getRemotePeerInfo(connectedFrom).getBitfield());
                                        handlers.get(i).sendMessage3(messageToSend);
                                        messageToSend = Messages.createBitfieldMessage(pp.getRemotePeerInfo(handlers.get(i).connectedFrom).getBitfield());
                                        sendMessage3(messageToSend);
                                       while (in3.available() <= 0) {

                                       }

                                       while (in3.available() > 0) {
                                            message = new byte[in3.available()];

                                            in3.read(message);

                                            buff = ByteBuffer.wrap(message);
                                            receivedMessages.add(buff);

                                        }

                                       while (receivedMessages.size() == 0) {
                                       }
                                       for (int j = 0; j < receivedMessages.size(); j++) {
                                            handlers.get(i).sendMessage3(receivedMessages.get(j));

                                        }

                                        while (handlers.get(i).receivedMessages.size() == 0) {
                                        }
                                        for (int j = 0; j < handlers.get(i).receivedMessages.size(); j++) {
                                            sendMessage3(handlers.get(i).receivedMessages.get(j));
                                        }
                                        handlers.get(i).receivedMessages.clear();

                                        receivedMessages.clear();
                                    }
                                    if(connectedFrom == 1003) {
                                        messageToSend = Messages.createHandshakeMessage(connectedFrom);
                                        handlers.get(i).sendMessage1(messageToSend);
                                        messageToSend = Messages.createHandshakeMessage(handlers.get(i).connectedFrom);
                                        sendMessage1(messageToSend);

                                        messageToSend = Messages.createBitfieldMessage(pp.getRemotePeerInfo(connectedFrom).getBitfield());
                                        handlers.get(i).sendMessage1(messageToSend);
                                        messageToSend = Messages.createBitfieldMessage(pp.getRemotePeerInfo(handlers.get(i).connectedFrom).getBitfield());
                                        sendMessage1(messageToSend);
                                        while (in1.available() <= 0) {

                                        }
                                        while (in1.available() > 0) {
                                            message = new byte[in1.available()];

                                        in1.read(message);

                                            buff = ByteBuffer.wrap(message);
                                            receivedMessages.add(buff);

                                        }
                                        while (receivedMessages.size() == 0) {
                                        }

                                        for (int j = 0; j < receivedMessages.size(); j++) {
                                            handlers.get(i).sendMessage1(receivedMessages.get(j));
                                        }

                                        while (handlers.get(i).receivedMessages.size() == 0) {
                                        }
                                        for (int j = 0; j < handlers.get(i).receivedMessages.size(); j++) {
                                            sendMessage1(handlers.get(i).receivedMessages.get(j));
                                        }
                                        handlers.get(i).receivedMessages.clear();


                                        receivedMessages.clear();
                                    }

                                }




                            }
                            else {
                                    if(secondTurn) {
                                        if (handlers.get(i).connectedFrom == pp.allPeers.get(2).getPeerId()) {

                                            if (connectedFrom == 1004) {
                                                if(in3.available() > 0)
                                                {
                                                    continue;
                                                }
                                                else
                                                    secondTurn = false;



                                                messageToSend = Messages.createHandshakeMessage(connectedFrom);
                                                handlers.get(i).sendMessage2(messageToSend);
                                                messageToSend = Messages.createHandshakeMessage(handlers.get(i).connectedFrom);
                                                sendMessage2(messageToSend);

                                                messageToSend = Messages.createBitfieldMessage(pp.getRemotePeerInfo(connectedFrom).getBitfield());
                                                handlers.get(i).sendMessage2(messageToSend);
                                                messageToSend = Messages.createBitfieldMessage(pp.getRemotePeerInfo(handlers.get(i).connectedFrom).getBitfield());
                                                sendMessage2(messageToSend);
                                                while (in2.available() <= 0) {

                                                }

                                                while (in2.available() > 0) {
                                                    message = new byte[in2.available()];

                                                    in2.read(message);

                                                    buff = ByteBuffer.wrap(message);
                                                    receivedMessages.add(buff);

                                                }

                                                while (receivedMessages.size() == 0) {
                                                }
                                                for (int j = 0; j < receivedMessages.size(); j++) {
                                                    handlers.get(i).sendMessage2(receivedMessages.get(j));
                                                }

                                                while (handlers.get(i).receivedMessages.size() == 0) {
                                                }
                                                for (int j = 0; j < handlers.get(i).receivedMessages.size(); j++) {
                                                    sendMessage2(handlers.get(i).receivedMessages.get(j));
                                                }
                                                handlers.get(i).receivedMessages.clear();

                                                receivedMessages.clear();
                                            }
                                        }
                                    }
                              if (handlers.get(i).connectedFrom == pp.allPeers.get(2).getPeerId()) {
                                    if (connectedFrom == 1002) {
                                        boolean continueOn = false;
                                        // receive either choke or unchoke

                                        while (in1.available() > 0) {


                                            message = new byte[in1.available()];

                                            in1.read(message);

                                            buff = ByteBuffer.wrap(message);
                                            if (Messages.GetMessageType(buff) == 1)
                                                continueOn = true;
                                            receivedMessages.add(buff);

                                        }
                                        // if an unchoke message is received
                                        if (continueOn) {
                                            // send unchoke message to peer
                                            for (int j = 0; j < receivedMessages.size(); j++) {
                                                handlers.get(i).sendMessage1(receivedMessages.get(j));
                                            }

                                            while (handlers.get(i).receivedMessages.size() == 0) {
                                            }

                                            // send request message to origin
                                            for (int j = 0; j < handlers.get(i).receivedMessages.size(); j++) {
                                                sendMessage1(handlers.get(i).receivedMessages.get(j));
                                            }


                                            // receive piece message from origin

                                            while (in1.available() <= 0) {
                                            }
                                            while (in1.available() > 0) {
                                                message = new byte[in1.available()];

                                                in1.read(message);

                                                buff = ByteBuffer.wrap(message);
                                                receivedMessages.add(buff);

                                            }

                                            //send piece message to peer
                                            for (int j = 0; j < receivedMessages.size(); j++) {
                                                handlers.get(i).sendMessage1(receivedMessages.get(j));
                                            }
                                            handlers.get(i).receivedMessages.clear();


                                            receivedMessages.clear();

                                        }
                                    }
                                    if (connectedFrom == 1004) {
                                        boolean continueOn = false;
                                        // receive either choke or unchoke
                                        while (in2.available() > 0) {
                                            message = new byte[in2.available()];

                                            in2.read(message);

                                            buff = ByteBuffer.wrap(message);
                                            if (Messages.GetMessageType(buff) == 1)
                                                continueOn = true;
                                            receivedMessages.add(buff);

                                        }
                                        // if an unchoke message is received
                                        if (continueOn) {
                                            // send unchoke message to peer
                                            for (int j = 0; j < receivedMessages.size(); j++) {
                                                handlers.get(i).sendMessage2(receivedMessages.get(j));
                                            }

                                            while (handlers.get(i).receivedMessages.size() == 0) {
                                            }

                                            // send request message to origin
                                            for (int j = 0; j < handlers.get(i).receivedMessages.size(); j++) {
                                                sendMessage2(handlers.get(i).receivedMessages.get(j));
                                            }

                                            // receive piece message from origin

                                            while (in2.available() <= 0) {
                                            }
                                            while (in2.available() > 0) {
                                                message = new byte[in1.available()];

                                                in2.read(message);

                                                buff = ByteBuffer.wrap(message);
                                                receivedMessages.add(buff);

                                            }

                                            //send piece message to peer
                                            for (int j = 0; j < receivedMessages.size(); j++) {
                                                handlers.get(i).sendMessage2(receivedMessages.get(j));
                                            }
                                            handlers.get(i).receivedMessages.clear();


                                            receivedMessages.clear();

                                        }
                                    }

                                }
                            }



                            
                        }
                        firstTime = false;

                    }
                    while (in.available() <= 0) {
                    }

                    while (in.available() > 0) {
                        message = new byte[in.available()];

                        in.read(message);

                        buff = ByteBuffer.wrap(message);

                        int chokeRes = Messages.decodeMessage(buff, pp, connectedFrom);
                        for (int i = 0; i < pp.messagesToSend.size(); i++) {
                            sendMessageBB(pp.messagesToSend.get(i));
                        }
                        for (int i = 0; i < pp.pieceMessages.size(); i++) {
                            pp.logger.log("Sending piece message");
                            sendMessageBB(pp.pieceMessages.get(i));
                        }
                        pp.pieceMessages.clear();

                    }

                }
            }
        }

        public void run() {
            System.out.println("bitfield size: " + pp.bitfield.size());
            try {
                // initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());

                out1 = new ObjectOutputStream(connection1.getOutputStream());
                out1.flush();
                in1 = new ObjectInputStream(connection1.getInputStream());

                out2 = new ObjectOutputStream(connection2.getOutputStream());
                out2.flush();
                in2 = new ObjectInputStream(connection2.getInputStream());

                out3 = new ObjectOutputStream(connection3.getOutputStream());
                out3.flush();
                in3 = new ObjectInputStream(connection3.getInputStream());

                out4 = new ObjectOutputStream(connection4.getOutputStream());
                out4.flush();
                in4 = new ObjectInputStream(connection4.getInputStream());

                out5 = new ObjectOutputStream(connection5.getOutputStream());
                out5.flush();
                in5 = new ObjectInputStream(connection5.getInputStream());

                out6 = new ObjectOutputStream(connection6.getOutputStream());
                out6.flush();
                in6 = new ObjectInputStream(connection6.getInputStream());

                out7 = new ObjectOutputStream(connection7.getOutputStream());
                out7.flush();
                in7= new ObjectInputStream(connection7.getInputStream());

                out8 = new ObjectOutputStream(connection8.getOutputStream());
                out8.flush();
                in8 = new ObjectInputStream(connection8.getInputStream());

                out9 = new ObjectOutputStream(connection9.getOutputStream());
                out9.flush();
                in9 = new ObjectInputStream(connection9.getInputStream());

                out10 = new ObjectOutputStream(connection10.getOutputStream());
                out10.flush();
                in10 = new ObjectInputStream(connection10.getInputStream());

                try {
                    serverLoop();
                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            } catch (IOException ioException) {
                System.err.println("Disconnect with Client " + no);
            } finally {
                // Close connections
                try {
                    in.close();
                    out.close();
                    connection.close();

                    in1.close();
                    out1.close();
                    connection1.close();

                    in2.close();
                    out2.close();
                    connection2.close();

                    in3.close();
                    out3.close();
                    connection3.close();

                    in4.close();
                    out4.close();
                    connection4.close();

                    in5.close();
                    out5.close();
                    connection5.close();

                    in6.close();
                    out6.close();
                    connection6.close();

                    in7.close();
                    out7.close();
                    connection7.close();

                    in8.close();
                    out8.close();
                    connection8.close();

                    in9.close();
                    out9.close();
                    connection9.close();

                    in10.close();
                    out10.close();
                    connection10.close();

                } catch (IOException ioException) {
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        // send a message to the output stream
        public void sendMessageBB(ByteBuffer msg) {
            try {
                out.write(msg.array());
                out.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void sendMessage1(ByteBuffer msg) {
            try {
                out1.write(msg.array());
                out1.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void sendMessage2(ByteBuffer msg) {
            try {
                out2.write(msg.array());
                out2.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void sendMessage3(ByteBuffer msg) {
            try {
                out3.write(msg.array());
                out3.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        /*
         * public void sendMessage4(ByteBuffer msg) {
         * try {
         * out4.write(msg.array());
         * out4.flush();
         * } catch (IOException ioException) {
         * ioException.printStackTrace();
         * }
         * }
         * public void sendMessage5(ByteBuffer msg) {
         * try {
         * out5.write(msg.array());
         * out5.flush();
         * } catch (IOException ioException) {
         * ioException.printStackTrace();
         * }
         * }
         */

    }
}
