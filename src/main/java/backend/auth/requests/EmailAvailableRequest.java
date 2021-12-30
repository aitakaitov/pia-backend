package backend.auth.requests;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EmailAvailableRequest {
    private static final long serialVersionUID = 5926468583005150707L;

    private String email;

    // default constructor for JSON Parsing
    public EmailAvailableRequest()
    {

    }

    public EmailAvailableRequest(String email) {
        this.setEmail(email);
    }
}
