package control;

import content.*;
import service.*;
import java.util.Scanner;

public class Manage {

    private static Scanner scanner = new Scanner(System.in);
    private static User user;
    private static boolean loggedIn = false;
    private static boolean read = false;

    public void begin() {
        System.out.println();

        while (true) {

            if (!loggedIn) {
                System.out.println("[L]ogin, [S]ign up, [E]xit: ");
            }
            else if (!read) {
                System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [E]xit: ");
            }
            else {
                System.out.println("[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode, [B]ack, [E]xit: ");
            }

            System.out.print("insert instruction >>> ");
            String instruction = scanner.nextLine().trim();

            if (instruction.equals("E")) {
                System.out.println("Exiting...");
                break;
            }

            if (!loggedIn) {
                FirstInstruction(instruction);
            } else if (read) {
                viewInstruction(instruction);
            } else {
                LoggedInInstruction(instruction);
            }

            System.out.println();
        }

        System.out.println();
    }

    public void FirstInstruction(String instruction) {
        try {
            switch (instruction) {
                case "L" -> login();
                case "S" -> signUp();
                default -> System.out.println("Unknown instruction: " + instruction);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void login() {
        System.out.println("Enter your Email: ");
        String email = scanner.nextLine();

        System.out.println("Enter your Password: ");
        String password = scanner.nextLine();

        user = UserService.login(email, password);

        loggedIn = true;

        System.out.println("Welcome back, " + user.getName() + "!");

        System.out.println();

        unreadEmails();
    }

    private void signUp() {
        System.out.println("Enter your Name: ");
        String name = scanner.nextLine();

        System.out.println("Enter your Email: ");
        String email = scanner.nextLine();

        System.out.println("Enter your Password: ");
        String password = scanner.nextLine();

        UserService.signUp(name, email, password);

        System.out.println("Your new account is created");
        System.out.println("Go ahead and login!");
    }

    public void LoggedInInstruction(String instruction) {
        try {
            switch (instruction) {
                case "S" -> send();
                case "V" -> read = true;
                case "R" -> reply();
                case "F" -> forward();
                default -> System.out.println("Unknown instruction: " + instruction);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void send() {
        System.out.println("Enter Recipient(s): ");
        String recipients = scanner.nextLine();

        System.out.println("Enter Subject: ");
        String subject = scanner.nextLine();

        System.out.println("Enter Body: ");
        StringBuilder bodyBuilder = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).isEmpty()) {
            bodyBuilder.append(line).append("\n");
        }
        String body = bodyBuilder.toString().trim();

        Email email = EmailService.send(user, recipients, subject, body);

        System.out.println("Successfully sent your email.");
        System.out.println("Code: " + email.getCode());
    }

    public void viewInstruction(String instruction) {
        try {
            switch (instruction) {
                case "A" -> allEmails();
                case "U" -> unreadEmails();
                case "S" -> sentEmails();
                case "C" -> readByCode();
                case "B" -> read = false;
                default -> System.out.println("Unknown instruction: " + instruction);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void reply() {
        System.out.println("Code:");
        String code = scanner.nextLine();

        System.out.println("Enter Body: ");
        StringBuilder bodyBuilder = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).isEmpty()) {
            bodyBuilder.append(line).append("\n");
        }
        String body = bodyBuilder.toString().trim();

        EmailService.reply(user, code, body);
    }

    private void forward() {
        System.out.println("Code:");
        String code = scanner.nextLine();

        System.out.println("Enter Recipient(s): ");
        String recipients = scanner.nextLine();

        EmailService.forward(user, code, recipients);
    }

    private void allEmails() {
        EmailService.allEmails(user);
    }

    private void unreadEmails() {
        EmailService.unreadEmails(user);
    }

    private void sentEmails() {
        EmailService.sentEmails(user);
    }

    private void readByCode() {
        System.out.println("Code:");
        String code = scanner.nextLine();

        EmailService.readByCode(user, code);
    }

}
