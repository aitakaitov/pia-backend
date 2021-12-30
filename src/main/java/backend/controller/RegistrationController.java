package backend.controller;

import backend.auth.RegistrationRequest;
import backend.model.UserFactory;
import backend.model.entity.RoleEntity;
import backend.model.repo.RoleRepository;
import backend.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final EntityManager em;

    private final PasswordEncoder encoder;

    @RequestMapping(value = "/api/auth/registration", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody RegistrationRequest registrationRequest) throws Exception {
        if (!emailAvailable(registrationRequest.getEmail())) {
            // Return 409 on email already used
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if (!passwordStrong(registrationRequest.getPassword())) {
            // Return 406 on low password strength
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        // Create new user
        var newUser = UserFactory.createUser(encoder.encode(registrationRequest.getPassword()), registrationRequest.getEmail(),
                registrationRequest.getName());

        // Try to find the user role
        var roleEntity = roleRepository.getRoleByName("USER");
        if (roleEntity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleEntity.get());
        newUser.setRoles(roles);
        userRepository.save(newUser);

        // Return 200 on success
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean emailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    private boolean passwordStrong(String password) {
        return true;
    }
}
