package backend.controller;

import backend.auth.responses.JwtResponse;
import backend.constants.Constants;
import backend.controller.requests.SimpleUserRequest;
import backend.controller.responses.FriendshipRequestResponse;
import backend.controller.responses.FriendshipRequestType;
import backend.controller.responses.SimpleUserResponse;
import backend.model.entity.UserEntity;
import backend.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import java.util.*;

@RestController
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
public class UserController {

    final UserRepository userRepository;

    @Autowired
    private ServletContext servletContext;


    @RequestMapping(value = "/api/users/friends/requests", method = RequestMethod.POST)
    public ResponseEntity<?> createFriendRequest(@RequestBody SimpleUserRequest createFriendRequest) throws Exception {
        // TODO create friend request
        return null;
    }

    @RequestMapping(value = "/api/users/friends/requests", method = RequestMethod.DELETE)
    public ResponseEntity<?> rejectFriendRequest(@RequestBody SimpleUserRequest rejectFriendRequest) throws Exception {
        // TODO reject friend request
        return null;
    }

    // TODO api mapping + method
    @RequestMapping(value = "/api/users/friends/requests", method = RequestMethod.PATCH)
    public ResponseEntity<?> acceptFriendRequest(@RequestBody SimpleUserRequest acceptFriendRequest) throws Exception {
        // TODO accept friend request
        return null;
    }

    @RequestMapping(value = "/api/users/friends/requests", method = RequestMethod.GET)
    public ResponseEntity<?> getFriendRequests() throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        var userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not find user in database");
        }

        List<FriendshipRequestResponse> requests = new ArrayList<>();
        UserEntity userEntity = userOptional.get();

        // First handle requests sent by the user
        Set<UserEntity> requestsTo = userEntity.getRequests();
        for (var ue : requestsTo) {
            var frr = new FriendshipRequestResponse();
            frr.setType(FriendshipRequestType.SENT);
            frr.setUserName(ue.getName());
            frr.setUserEmail(ue.getEmail());
            requests.add(frr);
        }

        // Then handle requests sent by others to the user


        return ResponseEntity.ok(requests);
    }

    @RequestMapping(value = "/api/users/friends", method = RequestMethod.GET)
    public ResponseEntity<?> getFriends() throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        var userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not find user in database");
        }

        UserEntity userEntity = userOptional.get();
        var userFriends = userEntity.getFriends();
        return ResponseEntity.ok(userFriends);
    }

    @RequestMapping(value = "/api/users/blocked", method = RequestMethod.GET)
    public ResponseEntity<?> getBlockedUsers() throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        var userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not find user in database");
        }

        UserEntity userEntity = userOptional.get();
        var usersBlocked = userEntity.getBlocked();
        return ResponseEntity.ok(usersBlocked);
    }

    @RequestMapping(value = "/api/users/blocked", method = RequestMethod.POST)
    public ResponseEntity<?> blockUser(@RequestBody SimpleUserRequest blockUserRequest) throws Exception {
        // TODO block a user
        return null;
    }

    @RequestMapping(value = "/api/users/blocked", method = RequestMethod.DELETE)
    public ResponseEntity<?> unblockUser(@RequestBody SimpleUserRequest unblockUserRequest) throws Exception {
        // TODO unblock a user
        return null;
    }
}
























