package nl.safenote.controllers;

import nl.safenote.utils.FileIO;
import org.springframework.beans.factory.annotation.Autowired;
import nl.safenote.services.AuthenticationService;
import org.springframework.stereotype.Service;


@Service
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

    public boolean authenticate(String passphrase){
        try {
            authenticationService.authenticate(passphrase);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public byte[] key(){
        return FileIO.getKeyAsImage();
    }
}
