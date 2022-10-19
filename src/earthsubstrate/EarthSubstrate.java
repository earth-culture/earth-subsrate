/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package earthsubstrate;

/**
 *
 * @author Christopher Brett
 */
public class EarthSubstrate {

    protected static ApiRequestValidator apiValidator;
    protected static DatabaseConnectionPool databaseConnectionPool;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerEnvironmentVariables.loadVariablesFromFile(); //Load in environment variables before loading anything else 
        apiValidator = new ApiRequestValidator();
        new SecureApiRequestListener().start();
    }
    
}
