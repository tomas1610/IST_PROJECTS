package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;

import java.util.Scanner;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final UserService userService;

    public CommandParser(UserService userService) {
        this.userService = userService;
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        this.createAccount(line);
                        break;

                    case TRANSFER_TO:
                        this.transferTo(line);
                        break;

                    case BALANCE:
                        this.balance(line);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        this.userService.shutdown();
                        exit = true;
                        break;

                    default:
                        break;
                }
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void createAccount(String line) {
        String[] split = line.split(SPACE);

        if (split.length != 3 && split.length != 4) {
            this.printUsage();
            return;
        }

        String server = split[1];
        String username = split[2];

        if (!check_qualificator(server)){
            System.out.println("Qualificator not valid");
            return;
        }

        if (split.length == 4) {
            UserClientMain.debugParser(line, 1); 
        }
        
        this.userService.createAccount(username,server);
    }

    private void balance(String line) {
        String[] split = line.split(SPACE);

        if (split.length != 3 && split.length != 4) {
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        if (!check_qualificator(server)){
            System.out.println("Qualificator not valid");
            return;
        }

        if (split.length == 4) { 
            UserClientMain.debugParser(line, 3); 
        }

        this.userService.balance(username,server);
    }

    private void transferTo(String line) {
        String[] split = line.split(SPACE);

        if (split.length != 5 && split.length != 6) {
            this.printUsage();
            return;
        }

        String server = split[1];
        String from = split[2];
        String dest = split[3];
        Integer amount = Integer.valueOf(split[4]);

        if (!check_qualificator(server)){
            System.out.println("Qualificator not valid");
            return;
        }

        if (amount <= 0){
            System.out.println("Amount zero or negative , no transfer to make");
            return;
        }

        if (split.length == 6) { 
            UserClientMain.debugParser(line, 4); 
        }

        this.userService.transferTo(from,dest,amount,server);
    }

    private boolean check_qualificator(String qualificator) {
        return qualificator.equals("A") || qualificator.equals("B") || qualificator.equals("C");
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                        "- createAccount <server> <username>\n" +
                        "- balance <server> <username>\n" +
                        "- transferTo <server> <username_from> <username_to> <amount>\n" +
                        "- exit\n");
    }
}