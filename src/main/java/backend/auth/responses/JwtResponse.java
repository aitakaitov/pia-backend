package backend.auth.responses;

import backend.constants.Constants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;



@RequiredArgsConstructor
@Getter
@Setter
public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwtToken;
    private final UserRole role;
}

public enum UserRole {
    USER,
    ADMIN
}