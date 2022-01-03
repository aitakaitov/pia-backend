package backend.controller.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleUserRequest {
    private static final long serialVersionUID = 5926468583005150L;

    private String email;
}