/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package earthsubstrate;

import java.io.File;

/**
 *
 * @author Christopher Brett
 */
public class SupportedAPIs {
    
    private static final String FILE_PATH = "API_REQUIREMENTS/";
    protected static final int CREATE_CULTURE = 1;
    protected static final int VALIDATE_EMAIL_VERIFICATION_KEY = 2;
    
    protected static File getApiRequirementsFile(int API_ID) {
        switch (API_ID) {
            case CREATE_CULTURE -> {
                return new File(FILE_PATH + "createCulture.txt");
            }
            case VALIDATE_EMAIL_VERIFICATION_KEY -> {
                return new File(FILE_PATH + "validateEmailVerificationKey.txt");
            }
            default -> {
                return null;
            }
        }
    }
}
