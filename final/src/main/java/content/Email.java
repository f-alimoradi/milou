package content;

import jakarta.persistence.*;
import java.util.Arrays;
import java.time.LocalDate;
import java.util.ArrayList;
import java.security.SecureRandom;

@Entity
@Table(name = "Emails")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic(optional = false)
    private String code;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Transient
    private ArrayList<String> recipients;

    @Basic(optional = false)
    private String subject;

    @Basic(optional = false)
    private String body;

    @Basic(optional = false)
    @Column(name = "creation_time")
    private LocalDate creationTime;

    public Email() {}

    public Email(User sender, String to, String subject, String body) {
        setCode();
        setSender(sender);
        setRecipients(to);
        setSubject(subject);
        setBody(body);
        setCreationTime();
    }

    @Override
    public String toString() {
        return "+ " + sender.getEmail() + " - " + getSubject() + " (" + getCode() + ")";
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    private void setCode() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        this.code = sb.toString();
    }

    public String getCode() {
        return code;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getSender() {
        return sender;
    }

    public void setRecipients(String to) {
        recipients = new ArrayList<>();
        String[] parts = to.trim().split(", ");
        recipients.addAll(Arrays.asList(parts));
    }

    public ArrayList<String> getRecipients() {
        return recipients;
    }

    public void setSubject(String subject) {
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be empty or null!");
        }
        this.subject = subject.trim();
    }

    public String getSubject() {
        return subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public LocalDate getCreationTime() {
        return creationTime;
    }

    private void setCreationTime() {
        this.creationTime = LocalDate.now();
    }

}