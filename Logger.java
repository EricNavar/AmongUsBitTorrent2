import java.util.Vector;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

class Logger {
    int peerID;

    public Logger(int peerID) {
        this.peerID = peerID;
        emptyFile();
    }

    private void emptyFile() {
        String fileName = "log_peer_" + this.peerID + ".log";
        try {
            new FileOutputStream(fileName).close();
        } catch (IOException e) {
            System.err.println("IO Exception");
        }
    }

    private static String getDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public void onConnectingTo(int peerID2) {
        log(getDate() + ": Peer " + this.peerID + " makes a connection to Peer " + peerID2 + ".\n");
    }

    public void onConnectingFrom(int peerID2) {
        log(getDate() + ": Peer " + this.peerID + " is connected from Peer " + peerID2 + ".\n");
    }

    public void onChangeOfPreferredNeighbors(Vector<Integer> preferredNeighbors) {
        StringBuilder toPrint = new StringBuilder(
                getDate() + ": Peer " + this.peerID + " has the preferred neighbors ");
        for (int i = 0; i < preferredNeighbors.size(); i++) {
            if (i != 0) {
                toPrint.append(", ");
            }
            toPrint.append(preferredNeighbors.get(i));
        }
        toPrint.append(".\n");
        log(toPrint.toString());
    }

    public void onChangeOfOptimisticallyUnchokedNeighbor(int optimisticallyUnchockedNeighbor_ID) {
        log(getDate() + ": Peer " + this.peerID + " has the optimistically unchoked neighbor "
                + optimisticallyUnchockedNeighbor_ID + ".\n");
    }

    public void onUnchoking(int peerID2) {
        log(getDate() + ": Peer " + this.peerID + " is unchoked by " + peerID2 + ".\n");
    }

    // choke me plz, well... at least we didn't throw four interceptions this
    // week...
    public void onChoking(int peerID2) {
        log(getDate() + ": Peer " + this.peerID + " is choked by " + peerID2 + ".\n");
    }

    public void onReceiveHaveMessage(int peerID2, int pieceIndex) {
        log(getDate() + ": Peer " + this.peerID + " received the  ‘have’ message from " + peerID2 + " for the piece "
                + pieceIndex + ".\n");
    }

    public void onReceiveInterestedMessage(int peerID2) {
        log(getDate() + ": Peer " + this.peerID + " received the ‘interested’ message from " + peerID2 + ".\n");
    }

    public void onReceiveNotInterestedMessage(int peerID2) {
        log(getDate() + ": Peer " + this.peerID + " received the ‘not interested’ message from " + peerID2 + ".\n");
    }

    public void onDownloadingAPiece(int peerID2, int piece_index, int number_of_pieces) {
        log(getDate() + ": Peer " + this.peerID + " has downloaded the piece " + String.valueOf(piece_index) + " from "
                + peerID2 + ". Now the number of pieces it has is " + String.valueOf(number_of_pieces) + ".\n");
    }

    public void onCompletionOfDownload() {
        log(getDate() + ": Peer " + this.peerID + "has downloaded the complete file.\n");
    }

    public void log(String toPrint) {
        // file directory would change based on windows or linux
        // need to specify full directory path
        String fileName = "log_peer_" + this.peerID + ".log";
        try {
            // true in constructor means append
            FileOutputStream outputStream = new FileOutputStream(fileName, true);
            byte[] strToBytes = toPrint.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (IOException e) {
            System.err.println("IO Exception");
        }
    }

    public void logPiece(String toPrint) {
        // file directory would change based on windows or linux
        // need to specify full directory path
        String fileName = "piece" + this.peerID + ".txt";
        try {
            // true in constructor means append
            FileOutputStream outputStream = new FileOutputStream(fileName, true);
            byte[] strToBytes = toPrint.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (IOException e) {
            System.err.println("IO Exception");
        }
    }
}
