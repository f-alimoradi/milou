package service;

import content.*;
import framework.SingletonSessionFactory;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.concurrent.atomic.AtomicReference;

public class EmailService {

    private EmailService() {}

    public static Email send(User sender, String to, String subject, String body) {
        List<Recipient> recipients = new ArrayList<>();
        String[] parts = to.trim().split(", ");
        List<String> recipientEmails = new ArrayList<>(Arrays.asList(parts));

        for (String recipientEmail : recipientEmails) {
            User recipientUser = UserService.getByEmail(recipientEmail.trim());
            if (recipientUser == null) {
                System.out.println("No such recipient: " + recipientEmail);
                continue;
            }
            recipients.add(new Recipient(null, recipientUser, false));
        }

        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("No valid recipient found.");
        }

        Email email = new Email(sender, to, subject, body);

        SingletonSessionFactory.get().inTransaction(session -> {
            session.persist(email);

            for (Recipient recipient : recipients) {
                recipient.setEmail(email);
                session.persist(recipient);
            }
        });

        return email;
    }


    public static void allEmails(User user) {
        AtomicReference<List<Email>> emails = new AtomicReference<>(new ArrayList<>());
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "SELECT r.email FROM Recipient r WHERE r.recipient = :user";

            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("user", user);
            emails.set(query.getResultList());

            if (emails.get().isEmpty()) {
                System.out.println("You don't have any emails");
                return;
            }

            System.out.println("All Emails:");
            System.out.println("Count: " + emails.get().size());

            printEmails(emails.get());
        });
    }

    public static void unreadEmails(User user) {
        AtomicReference<List<Email>> emails = new AtomicReference<>(new ArrayList<>());
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "SELECT r.email FROM Recipient r WHERE r.recipient = :user AND r.isRead = false";

            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("user", user);
            emails.set(query.getResultList());

            if (emails.get().isEmpty()) {
                System.out.println("No emails found.");
                return;
            }

            System.out.println(emails.get().size() + " Unread Emails:");

            printEmails(emails.get());
        });
    }

    public static void sentEmails(User user) {
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM Email e WHERE e.sender = :user";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("user", user);
            List<Email> emails = query.getResultList();

            if (emails.isEmpty()) {
                System.out.println("You don't sent any emails");
                return;
            }

            System.out.println(emails.size() + " Sent Emails:");

            for (Email email : emails) {
                String hqlRecipients = "FROM Recipient r WHERE r.email = :email";
                Query<Recipient> recQuery = session.createQuery(hqlRecipients, Recipient.class);
                recQuery.setParameter("email", email);
                List<Recipient> recipients = recQuery.getResultList();

                List<String> recipientEmails = new ArrayList<>();

                for (Recipient recipient : recipients) {
                    String emailAddress = recipient.getRecipient().getEmail();
                    recipientEmails.add(emailAddress);
                }

                String joinedRecipients = String.join(", ", recipientEmails);

                System.out.println("+ " + joinedRecipients + " - " + email.getSubject() + " (" + email.getCode() + ")");
            }
        });
    }

    public static void readByCode(User user, String code) {
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM Email e WHERE e.code = :code";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("code", code);

            Email email;
            try {
                email = query.getSingleResult();
            } catch (Exception e) {
                throw new IllegalArgumentException("No such email with code: " + code);
            }

            List<Recipient> recipients = getRecipientsOfEmail(session, email);

            boolean allowed = false;
            List<String> recipientEmails = new ArrayList<>();

            for (Recipient recipient : recipients) {
                String emailAddress = recipient.getRecipient().getEmail();
                recipientEmails.add(emailAddress);

                if (emailAddress.equals(user.getEmail())) {
                    allowed = true;
                    recipient.setRead(true);
                    session.merge(recipient);
                }
            }

            String joinedRecipients = String.join(", ", recipientEmails);

            if (!allowed && !email.getSender().getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("You cannot read this email.");
            }

            System.out.println("Code: " + code);
            System.out.println("Recipient(s): " + joinedRecipients);
            System.out.println("Subject: " + email.getSubject());
            System.out.println("Date: " + email.getCreationTime());
            System.out.println();
            System.out.println(email.getBody());
        });
    }

    public static void reply(User user, String code, String replyBody) {
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM Email e WHERE e.code = :code";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("code", code);

            Email email;
            try {
                email = query.getSingleResult();
            } catch (Exception e) {
                throw new IllegalArgumentException("No such email with code: " + code);
            }

            List<Recipient> recipients = getRecipientsOfEmail(session, email);

            boolean isReply = false;
            List<String> recipientEmails = new ArrayList<>();

            for (Recipient recipient : recipients) {
                String emailAddress = recipient.getRecipient().getEmail();

                if (emailAddress.equals(user.getEmail())) {
                    emailAddress = email.getSender().getEmail();
                    isReply = true;
                    recipient.setRead(true);
                    session.merge(recipient);
                }

                recipientEmails.add(emailAddress);
            }

            String replyRecipients = String.join(", ", recipientEmails);

            if (!isReply && !email.getSender().getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("You cannot reply to this email.");
            }

            String replySubject = "[Re] " + email.getSubject();

            Email replyEmail = send(user, replyRecipients, replySubject, replyBody);

            System.out.println("Successfully sent your reply to email " + code + ".");
            System.out.println("Code: " + replyEmail.getCode());
        });
    }

    public static void forward(User user, String code, String forwardRecipients) {
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM Email e WHERE e.code = :code";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("code", code);

            Email email;
            try {
                email = query.getSingleResult();
            } catch (Exception e) {
                throw new IllegalArgumentException("No such email with code: " + code);
            }

            List<Recipient> recipients = getRecipientsOfEmail(session, email);

            boolean isForward = false;

            for (Recipient recipient : recipients) {
                String emailAddress = recipient.getRecipient().getEmail();

                if (emailAddress.equals(user.getEmail())) {
                    recipient.setRead(true);
                    session.merge(recipient);
                    isForward = true;
                    break;
                }
            }

            if (!isForward && !email.getSender().getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("You cannot forward this email.");
            }

            String forwardSubject = "[fw] " + email.getSubject();

            Email forwardEmail = send(user, forwardRecipients, forwardSubject, email.getBody());

            System.out.println("Successfully forwarded your email.");
            System.out.println("Code: " + forwardEmail.getCode());
        });
    }

    public static void printEmails(List<Email> emails) {
        for (Email email : emails) {
            System.out.println(email);
        }
    }

    private static List<Recipient> getRecipientsOfEmail(Session session, Email email) {
        String hqlRecipients = "FROM Recipient r WHERE r.email = :email";
        Query<Recipient> recipientQuery = session.createQuery(hqlRecipients, Recipient.class);
        recipientQuery.setParameter("email", email);
        return recipientQuery.getResultList();
    }

}
