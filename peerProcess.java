// Your First Program

class peerProcess {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Process number must be provided");
            return;
        }
        System.out.println("Process " + args[0]);
    }
}