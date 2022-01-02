package backend.controller;

import backend.auth.requests.EmailAvailableRequest;
import backend.auth.requests.RegistrationRequest;
import backend.model.UserFactory;
import backend.model.entity.RoleEntity;
import backend.model.repo.RoleRepository;
import backend.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

@RestController
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    private final double MINIMUM_STRING_ENTROPY = 20;

    // Email regex pattern compliant with RFC 5322
    private final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    @RequestMapping(value = "/api/auth/email/available", method = RequestMethod.POST)
    public ResponseEntity<?> checkEmailAvailable(@RequestBody EmailAvailableRequest emailAvailableRequest) throws Exception {
        boolean available = emailAvailable(emailAvailableRequest.getEmail());
        if (available) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }


    @RequestMapping(value = "/api/auth/registration", method = RequestMethod.POST)
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest) throws Exception {
        if (!isValidMail(registrationRequest.getEmail())) {
            // Return 400 on invalid mail
            log.info("Invalid email received: " + registrationRequest.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email");
        }

        if (!emailAvailable(registrationRequest.getEmail())) {
            // Return 409 on email already used
            log.info("Email already used: " + registrationRequest.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already used");
        }

        if (!passwordStrong(registrationRequest.getPassword())) {
            // Return 406 on low password strength
            log.info("Password not strong enough");
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Password not strong enough");
        }

        // Create new user
        var newUser = UserFactory.createUser(encoder.encode(registrationRequest.getPassword()), registrationRequest.getEmail(),
                registrationRequest.getName());

        // Try to find the user role
        var roleEntity = roleRepository.getRoleByName("USER");
        if (roleEntity.isEmpty()) {
            log.error("Could not find USER role after initialization");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching role from database");
        }

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleEntity.get());
        newUser.setRoles(roles);
        userRepository.save(newUser);

        // Return 200 on success
        log.info("Successfully added user " + registrationRequest.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean isValidMail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean emailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    private boolean passwordStrong(String password) {
        // Get the counts of chars
        var map = new HashMap<Character, Integer>();
        for (int i = 0; i < password.length(); i++) {
            if (!map.containsKey(password.charAt(i))) {
                map.put(password.charAt(i), 1);
            }
            else {
                map.put(password.charAt(i), map.get(password.charAt(i)) + 1);
            }
        }

        // calculate shannon's character entropy
        double res = 0;
        for (var e : map.entrySet()) {
            double freq = (double)e.getValue() / password.length();
            res -= freq * (Math.log(freq) / Math.log(2));
        }

        // get string entropy
        res *= password.length();

        return res >= MINIMUM_STRING_ENTROPY;
    }
}
