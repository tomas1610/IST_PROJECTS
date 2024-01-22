package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;

public class AdminClientMain {

    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != "true");

	private static void debug(String debugMessage) {
        if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}
    
    public static void main(String[] args) {
        System.out.println(AdminClientMain.class.getSimpleName());

        CommandParser parser = new CommandParser(new AdminService("localhost", 5001));
        parser.parseInput();
    }

    public static void debugParser(String line, int option) {
        //activate 
        if (option == 1) {  
            final String messageDebug = "Debug: This operation 'Activate' turns the state of server to active"; 
            debug(messageDebug);
        }
        //deactivate
        else if (option == 2) {
            final String messageDebug = "Debug: This operation 'Deactivate' turns the state of server to inactive";
            debug(messageDebug);
        }
        //getLedgerState
        else if (option == 3) {
            final String messageDebug = "Debug : This operation 'GetLedgerState' provides you the content of the ledger";
            debug(messageDebug);
        }
        //gossip
        else if (option == 4) {
            final String messageDebug = "Debug: This operation 'Balance' provides you to check the balance of an account by giving a server Qualificator and an UserID";
            debug(messageDebug);
        }
    }
}