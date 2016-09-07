package safenote.client.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import safenote.client.services.AuthenticationService;
import safenote.client.persistence.InstanceRepository;

/**
 * This class serves as an endpoint for authentication
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
@RestController
@Transactional
@CrossOrigin(origins = "*")
@RequestMapping(value="/authentication", headers = "Accept=*/*", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private final InstanceRepository instanceRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(InstanceRepository instanceRepository, AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        this.instanceRepository = instanceRepository;
    }

    /**
     * Ensures a unique device ID is defined by initializing instance repository.
     * Passes the passphrase to the authenticationservice
     * @param passphrase passprashe entered by user in plaintext
     * @return returns response code 200 if authentication was successful, if any exceptions occurs returns 403
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity authenticate(@RequestBody String passphrase){
        try {
            instanceRepository.init();
            authenticationService.authenticate(passphrase);

            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }
}
