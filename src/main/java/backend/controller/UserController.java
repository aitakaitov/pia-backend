package backend.controller;

import backend.constants.Constants;
import backend.controller.requests.SimpleUserRequest;
import backend.controller.responses.FriendshipRequestResponse;
import backend.controller.responses.FriendshipRequestType;
import backend.controller.responses.SimpleUserResponse;
import backend.model.entity.RoleEntity;
import backend.model.entity.UserEntity;
import backend.model.entity.utils.UserEntityUtils;
import backend.model.repo.RoleRepository;
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
    final RoleRepository roleRepository;

    @Autowired
    private ServletContext servletContext;

    /**
     * Given email address of the user, sends a friend request to the user
     * If the addressed user has already sent a friend request to the client,
     * the two users are befriended and both requests are removed
     * @param createFriendRequest
     * @return OK if the request is completed successfully, 500 if the database errors,
     * 409 if the friend request already exists and BAD REQUEST if the user does not exist or has the requester blocked
     * @throws Exception
     */
    @RequestMapping(value = "/api/users/friends/requests", method = RequestMethod.POST)
    public ResponseEntity<?> createFriendRequest(@RequestBody SimpleUserRequest createFriendRequest) throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        // Find both users - addressed user and the requester
        var userToBefriend = userRepository.findByEmail(createFriendRequest.getEmail());
        if (userToBefriend.isEmpty()) {
            log.info("Request to befriend a non-existent user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }

        var clientUser = userRepository.findByEmail(userEmail);
        if (clientUser.isEmpty()) {
            log.error("Could not find calling user in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error on server side");
        }

        if (clientUser.get().equals(userToBefriend.get())) {
            log.info("Attempting to befriend oneself");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot befriend oneself");
        }

        // Disallow duplicate requests
        if (UserEntityUtils.hasRequested(clientUser.get(), userToBefriend.get())) {
            log.info("Duplicate friend request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Duplicate friend request");
        }

        // Check if the client has blocked the user
        if (UserEntityUtils.hasBlocked(clientUser.get(), userToBefriend.get())) {
            log.info("Attempting to send request to blocked user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot befriend a blocked user");
        }

        // Check if the addressed user has blocked the requester
        if (UserEntityUtils.hasBlocked(userToBefriend.get(), clientUser.get())) {
            log.info("Attempting to send a friend request to a user who has the client blocked");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot befriend a user who has you blocked");
        }


        // Now we know the users can send friend request to each other


        // Check if the addressed user has already sent a friend request to the client
        // we make them friends
        if (UserEntityUtils.hasRequested(userToBefriend.get(), clientUser.get())) {
            userToBefriend.get().getRequests().remove(clientUser.get());
            clientUser.get().getFriends().add(userToBefriend.get());
            userToBefriend.get().getFriends().add(clientUser.get());

            userRepository.save(clientUser.get());
            userRepository.save(userToBefriend.get());

            log.info("Making users friend since they sent friend requests to each other");
            return ResponseEntity.ok().body("");
        }

        // otherwise just register the friend request
        clientUser.get().getRequests().add(userToBefriend.get());
        userRepository.save(clientUser.get());
        return ResponseEntity.ok().body("");
    }

    /**
     * Given email address of the user, rejects a friend request from that user
     * @param rejectFriendEmail
     * @return OK if the request is completed successfully, 500 if the database errors,
     * and BAD REQUEST if the user does not exist or no request to reject exists
     * @throws Exception
     */
    @RequestMapping(value = "/api/users/friends/requests", method = RequestMethod.DELETE)
    public ResponseEntity<?> rejectFriendRequest(@RequestParam(name = "email") String rejectFriendEmail) throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        // Find both users - addressed user and the requester
        var userToReject = userRepository.findByEmail(rejectFriendEmail);
        if (userToReject.isEmpty()) {
            log.info("Request to reject a non-existent user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }

        var clientUser = userRepository.findByEmail(userEmail);
        if (clientUser.isEmpty()) {
            log.error("Could not find calling user in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error on server side");
        }

        // Check if the user is the same
        if (clientUser.get().equals(userToReject.get())) {
            log.info("Attempting to reject friend request from oneself");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot reject oneself");
        }

        // Check if there is a request to reject
        if (!UserEntityUtils.hasRequested(userToReject.get(), clientUser.get())) {
            log.info("Attempting to reject a non-existent request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No request to reject");
        }

        // remove the request - the request is in the rejectee's requests since it's from him
        userToReject.get().getRequests().remove(clientUser.get());
        userRepository.save(clientUser.get());
        return ResponseEntity.ok().body("");
    }


    /**
     * Given email address of the user, accepts a friend request from that user
     * @param acceptFriendRequest
     * @return OK if the request is completed successfully, 500 if the database errors,
     * BAD REQUEST if the user does not exist or no request to accept exists
     * @throws Exception
     */
    @RequestMapping(value = "/api/users/friends/requests", method = RequestMethod.PATCH)
    public ResponseEntity<?> acceptFriendRequest(@RequestBody SimpleUserRequest acceptFriendRequest) throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        // Find both users - addressed user and the requester
        var userToAccept = userRepository.findByEmail(acceptFriendRequest.getEmail());
        if (userToAccept.isEmpty()) {
            log.info("Request to accept a non-existent user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }

        var clientUser = userRepository.findByEmail(userEmail);
        if (clientUser.isEmpty()) {
            log.error("Could not find calling user in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error on server side");
        }

        // Check if the user is the same
        if (clientUser.get().equals(userToAccept.get())) {
            log.info("Attempting to accept friend request from oneself");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot accept oneself");
        }

        // Check if there is a request to accept
        if (!UserEntityUtils.hasRequested(userToAccept.get(), clientUser.get())) {
            log.info("Attempting to accept a non-existent request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No request to accept");
        }

        // remove the request
        userToAccept.get().getRequests().remove(clientUser.get());

        // add them as friends
        clientUser.get().getFriends().add(userToAccept.get());
        userToAccept.get().getFriends().add(clientUser.get());

        // save to database
        userRepository.save(userToAccept.get());
        userRepository.save(clientUser.get());
        return ResponseEntity.ok().body("");
    }

    /**
     * Returns the friend requests addressed to the client and sent by the client
     * @return friend requests
     * @throws Exception
     */
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
        Set<UserEntity> requestsFrom = userRepository.getRequestsToUser(userEmail);
        for (var ue : requestsFrom) {
            var frr = new FriendshipRequestResponse();
            frr.setType(FriendshipRequestType.RECEIVED);
            frr.setUserName(ue.getName());
            frr.setUserEmail(ue.getEmail());
            requests.add(frr);
        }

        return ResponseEntity.ok(requests);
    }

    /**
     * Returns all the friends the client has
     * @return friends
     * @throws Exception
     */
    @RequestMapping(value = "/api/users/friends", method = RequestMethod.GET)
    public ResponseEntity<?> getFriends() throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        var userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not find user in database");
        }

        UserEntity userEntity = userOptional.get();
        var userFriends = userEntity.getFriends();
        return ResponseEntity.ok(userEntitiesToSimpleUserResponses(userFriends));
    }

    /**
     * Find the users the client has blocked
     * @return blocked users
     * @throws Exception
     */
    @RequestMapping(value = "/api/users/blocked", method = RequestMethod.GET)
    public ResponseEntity<?> getBlockedUsers() throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        var userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not find user in database");
        }

        UserEntity userEntity = userOptional.get();
        var usersBlocked = userEntity.getBlocked();
        return ResponseEntity.ok(userEntitiesToSimpleUserResponses(usersBlocked));
    }

    /**
     * Given email address of the user, blocks the user
     * Friend request from the blocked user is removed - if no friend request has been sent by the user
     * the block attempt fails
     * @param blockUserRequest
     * @return OK if the request is completed successfully, 500 if the database errors,
     * 409 if the user is already blocked and BAD REQUEST if the user does not exist and if the user
     * has not sent a friend request and thus cannot be blocked
     * @throws Exception
     */
    @RequestMapping(value = "/api/users/blocked", method = RequestMethod.POST)
    public ResponseEntity<?> blockUser(@RequestBody SimpleUserRequest blockUserRequest) throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        // Find both users - the blocking one and the one to be blocked
        var userToBlock = userRepository.findByEmail(blockUserRequest.getEmail());
        if (userToBlock.isEmpty()) {
            log.info("Request to block a non-existent user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }

        var clientUser = userRepository.findByEmail(userEmail);
        if (clientUser.isEmpty()) {
            log.error("Could not find calling user in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error on server side");
        }

        if (clientUser.get().equals(userToBlock.get())) {
            log.info("Cannot block oneself");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot block oneself");
        }

        // Test if the user is already blocked
        if (UserEntityUtils.hasBlocked(clientUser.get(), userToBlock.get())) {
            log.info("Attempt to block an already blocked user");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already blocked");
        }

        // Test if the user to be blocked has sent a friend request
        if (UserEntityUtils.hasRequested(userToBlock.get(), clientUser.get())) {
            // Remove the request
            userToBlock.get().getRequests().remove(clientUser.get());
        }
        else {
            // Or let the blocking fail
            log.info("The user to be blocked has not sent a friend request to the blocker");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The user has not sent you a friend request");
        }

        // Do the same the other way - check for request to the user to block
        if (UserEntityUtils.hasRequested(clientUser.get(), userToBlock.get())) {
            clientUser.get().getRequests().remove(userToBlock.get());
        }

        // Now we know that the user to be blocked has sent a friend request, all possible requests are
        // removed and that the request is valid

        // block the user
        // we will save both users, since the blocked one has sent a friend request which was removed
        clientUser.get().getBlocked().add(userToBlock.get());
        userRepository.save(clientUser.get());
        userRepository.save(userToBlock.get());
        return ResponseEntity.ok().body("");
    }

    /**
     * Given email address of the user, unblocks the user
     * @param unblockUserEmail
     * @return OK if the request is completed successfully, 500 if the database errors,
     * 409 if the user is not blocked and BAD REQUEST if the user does not exist
     * @throws Exception
     */
    @RequestMapping(value = "/api/users/blocked", method = RequestMethod.DELETE)
    public ResponseEntity<?> unblockUser(@RequestParam(name = "email") String unblockUserEmail) throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        // Find both users - the one to unblock and the client
        var userToUnblock = userRepository.findByEmail(unblockUserEmail);
        if (userToUnblock.isEmpty()) {
            log.info("Request to block a non-existent user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }

        var clientUser = userRepository.findByEmail(userEmail);
        if (clientUser.isEmpty()) {
            log.error("Could not find calling user in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error on server side");
        }

        // Check if the user is trying to unblock himself
        if (clientUser.get().equals(userToUnblock.get())) {
            log.info("Attempt to unblock oneself");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot unblock oneself");
        }

        // Check if the target is actually blocked
        if (!UserEntityUtils.hasBlocked(clientUser.get(), userToUnblock.get())) {
            log.error("Attempting to unblock a non-blocked user");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is not blocked");
        }

        clientUser.get().getBlocked().remove(userToUnblock.get());
        userRepository.save(clientUser.get());
        return ResponseEntity.ok().body("");
    }

    @RequestMapping(value = "/api/users/admin", method = RequestMethod.POST)
    public ResponseEntity<?> grantAdmin(@RequestBody SimpleUserRequest grantAdminRequest) throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        // Find both users - the one to unblock and the client
        var userToAdmin = userRepository.findByEmail(grantAdminRequest.getEmail());
        if (userToAdmin.isEmpty()) {
            log.info("Request to block a non-existent user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }

        var clientUser = userRepository.findByEmail(userEmail);
        if (clientUser.isEmpty()) {
            log.error("Could not find calling user in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error on server side");
        }

        // Check if the client is admin
        var adminRole = roleRepository.getRoleByName(Constants.ADMIN_ROLE_NAME);
        if (adminRole.isEmpty()) {
            log.error("Could not find admin role in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error on server side");
        }

        if (!clientUser.get().getRoles().contains(adminRole.get())) {
            log.info("Attempt to access /users/admin endpoint without admin role");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Admin role required");
        }

        // client and target same
        if (userToAdmin.equals(clientUser)) {
            log.info("Cannot admin oneself");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot grant admin to oneself");
        }


        // Check if the user is friend with the target
        if (!UserEntityUtils.areFriends(clientUser.get(), userToAdmin.get())) {
            log.info("Attempted to admin non-friend");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Target user must be a friend");
        }

        // Check if the target is not an admin already
        if (userToAdmin.get().getRoles().contains(adminRole.get())) {
            log.info("User already admin");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already admin");
        }

        // Admin the user
        var set = new HashSet<RoleEntity>();
        set.add(adminRole.get());
        userToAdmin.get().setRoles(set);
        userRepository.save(userToAdmin.get());

        return ResponseEntity.ok("");
    }

    @RequestMapping(value = "/api/users/admin", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeAdmin(@RequestParam(name = "email") String targetEmail) throws Exception {
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        // Find both users - the one to unblock and the client
        var userToRemoveAdmin = userRepository.findByEmail(targetEmail);
        if (userToRemoveAdmin.isEmpty()) {
            log.info("Request to block a non-existent user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }

        var clientUser = userRepository.findByEmail(userEmail);
        if (clientUser.isEmpty()) {
            log.error("Could not find calling user in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error on server side");
        }

        // Check if the client is admin
        var adminRole = roleRepository.getRoleByName(Constants.ADMIN_ROLE_NAME);
        if (adminRole.isEmpty()) {
            log.error("Could not find admin role in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error on server side");
        }

        if (!clientUser.get().getRoles().contains(adminRole.get())) {
            log.info("Attempt to access /users/admin endpoint without admin role");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Admin role required");
        }

        // client and target same
        if (userToRemoveAdmin.equals(clientUser)) {
            log.info("Cannot un-admin oneself");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot remove admin from oneself");
        }

        // Check if the user is friend with the target
        if (!UserEntityUtils.areFriends(clientUser.get(), userToRemoveAdmin.get())) {
            log.info("Attempted to remove admin from non-friend");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Target user must be a friend");
        }

        // Check if the target is an admin
        if (!userToRemoveAdmin.get().getRoles().contains(adminRole.get())) {
            log.info("User not admin");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is not admin");
        }

        var userRole = roleRepository.getRoleByName(Constants.USER_ROLE_NAME);
        if (userRole.isEmpty()) {
            log.info("Could not find user role in database");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Server-side database error");
        }

        // Un-admin the user
        var set = new HashSet<RoleEntity>();
        set.add(userRole.get());
        userToRemoveAdmin.get().setRoles(set);
        userRepository.save(userToRemoveAdmin.get());

        return ResponseEntity.ok("");
    }

    /**
     * Convert a set of user entities to a list of simple user responses
     * @param userEntities
     * @return simple user responses
     */
    private List<SimpleUserResponse> userEntitiesToSimpleUserResponses(Set<UserEntity> userEntities) {
        List<SimpleUserResponse> responses = new ArrayList<>();
        for (var userEntity : userEntities) {
            SimpleUserResponse userResponse = new SimpleUserResponse();
            userResponse.setUserName(userEntity.getName());
            userResponse.setUserEmail(userEntity.getEmail());
            responses.add(userResponse);
        }
        return responses;
    }
}
























