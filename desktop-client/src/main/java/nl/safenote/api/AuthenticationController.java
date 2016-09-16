package nl.safenote.api;

import nl.safenote.utils.FileIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import nl.safenote.services.AuthenticationService;

/**
 * This class serves as an endpoint for authentication
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(headers = "Accept=*/*")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        assert authenticationService!=null;
        this.authenticationService = authenticationService;
    }

    /**
     * Ensures a unique device ID is defined by initializing instance repository.
     * Passes the passphrase to the authenticationservice
     * @param passphrase passprashe entered by user in plaintext
     * @return returns response code 200 if authentication was successful, if any exceptions occurs returns 403
     */
    @RequestMapping(value="/authentication", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity authenticate(@RequestBody String passphrase){
        try {
            authenticationService.authenticate(passphrase);

            return new ResponseEntity(HttpStatus.OK);
        } catch (SecurityException e) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(value="key", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] key(){
        return FileIO.getKeyAsImage();
    }
}
