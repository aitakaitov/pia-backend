package backend.controller.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendshipRequestResponse {
    private static final long serialVersionUID = 5926468583005150L;

    private String userEmail;
    private String userName;
    private FriendshipRequestType type;
}

public enum FriendshipRequestType {
    SENT,
    RECEIVED
}