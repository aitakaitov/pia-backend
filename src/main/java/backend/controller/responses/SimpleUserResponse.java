package backend.controller.responses;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SimpleUserResponse {
    private static final long serialVersionUID = 5926468583005150L;

    private String userEmail;
    private String userName;
}