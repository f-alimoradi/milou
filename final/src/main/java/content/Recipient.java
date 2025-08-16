package content;

import jakarta.persistence.*;

@Entity
@Table(name = "Recipients")
public class Recipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id")
    private Email email;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Basic(optional = false)
    @Column(name = "is_read")
    private Boolean isRead;

    public Recipient() {}

    public Recipient(Email email, User recipient, Boolean isRead) {
        setEmail(email);
        setRecipient(recipient);
        setRead(isRead);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Email getEmail() {
        return email;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public Boolean getRead() {
        return isRead;
    }

}
