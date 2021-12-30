package backend.auth.requests;

public class RegistrationRequest {
    private static final long serialVersionUID = 5926468583005150706L;

    private String name;

    private String password;

    private String email;

    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }

}
