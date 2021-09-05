// Your First Program

class peerProcess {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Process number must be provided.");
            return;
        }
        String peerID = args[0];
        // if peerID is not a positive integer
        if (!peerID.matches("\\d+")) {
            System.out.println("Process number must be a positive integer.");
            return;
        }

        System.out.println("Process " + args[0]);

        Logger logger = new Logger(peerID);
    }
}