package content;

import jakarta.persistence.*;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic(optional = false)
    private String name;

    @Basic(optional = false)
    private String email;

    @Basic(optional = false)
    private String password;

    public User() {
    }

    public User(String name, String email, String password) {
        setName(name);
        setEmail(email);
        setPassword(password);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty or null!");
        }
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty or null!");
        }
        email = email.trim();
        if (email.contains(" ")) {
            throw new IllegalArgumentException("Email cannot include spaces");
        }
        if (!email.contains("@milou.com")) {
            email += "@milou.com";
        }
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty or null!");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be 8 characters minimum!");
        }
        this.password = password;
    }

}
