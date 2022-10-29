/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package earthsubstrate;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Christopher Brett
 */
public class CultureRequestHandler implements HttpHandler {

    private static final DatabaseControl database = new DatabaseControl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        DebugTools.printHttpExchangeRequestInfo(exchange);
        try (exchange) { //closes all resources all by itself when done 
            try (exchange) {
                final Headers requestHeaders = exchange.getRequestHeaders();
                final JSONObject queryJSONObject = ToolKit.getJSONFromHttpExchangeQuery(exchange);
                final JSONObject bodyJSONObject = ToolKit.getJSONFromHttpExchangeBody(exchange);
                final String requestMethod = (queryJSONObject.containsKey("method") && ServerEnvironmentVariables.IN_TEST_MODE) ? ((String) queryJSONObject.get("method")).toUpperCase() : exchange.getRequestMethod().toUpperCase();
                final String apiIdentifier = exchange.getRequestURI().getPath().split("/")[2];
                switch (requestMethod) {
                    case HttpConstants.METHOD_GET -> { //Read-Only access to resources
                        processGetRequest(apiIdentifier, requestHeaders, exchange, queryJSONObject, bodyJSONObject);
                    }
                    case HttpConstants.METHOD_PUT -> { //Update a resource
                        processPutRequest(apiIdentifier, requestHeaders, exchange, queryJSONObject, bodyJSONObject);
                    }
                    case HttpConstants.METHOD_POST -> { //Create a new resource
                        processPostRequest(apiIdentifier, requestHeaders, exchange, queryJSONObject, bodyJSONObject);
                    }
                    case HttpConstants.METHOD_DELETE -> { //Remove a resource
                        processDeleteRequest(apiIdentifier, requestHeaders, exchange, queryJSONObject, bodyJSONObject);
                    }
                    case HttpConstants.METHOD_OPTIONS -> { //Return what methods are supported
                        ToolKit.sendMethodOptionsResponse(exchange);
                    }
                    default -> {
                        ToolKit.sendDefaultResponse(exchange);
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    private static void processGetRequest(String apiIdentifier, Headers requestHeaders, HttpExchange exchange, JSONObject queryJSONObject, JSONObject bodyJSONObject) throws IOException {
        switch (apiIdentifier) {

            default -> {
                ToolKit.sendApiJSONResponse(GenericErrorApiResponse.methodNotFoundError(), exchange);
            }
        }
    }

    private static void processPutRequest(String apiIdentifier, Headers requestHeaders, HttpExchange exchange, JSONObject queryJSONObject, JSONObject bodyJSONObject) throws IOException {
        switch (apiIdentifier) {
            case "ValidateEmailVerificationKey" -> {
                validateEmailVerificationKey(requestHeaders, exchange, queryJSONObject, bodyJSONObject);
            }
            default -> {
                ToolKit.sendApiJSONResponse(GenericErrorApiResponse.methodNotFoundError(), exchange);
            }
        }
    }

    private static void processPostRequest(String apiIdentifier, Headers requestHeaders, HttpExchange exchange, JSONObject queryJSONObject, JSONObject bodyJSONObject) throws IOException {
        switch (apiIdentifier) {
            case "CreateCulture" -> {
                createCulture(requestHeaders, exchange, queryJSONObject, bodyJSONObject);
            }
            default -> {
                ToolKit.sendApiJSONResponse(GenericErrorApiResponse.methodNotFoundError(), exchange);
            }
        }
    }

    private static void processDeleteRequest(String apiIdentifier, Headers requestHeaders, HttpExchange exchange, JSONObject queryJSONObject, JSONObject bodyJSONObject) throws IOException {
        switch (apiIdentifier) {

            default -> {
                ToolKit.sendApiJSONResponse(GenericErrorApiResponse.methodNotFoundError(), exchange);
            }
        }
    }

    private static void createCulture(Headers requestHeaders, HttpExchange exchange, JSONObject queryJSONObject, JSONObject bodyJSONObject) throws IOException {
        ApiRequestValidator apiRequestValidator = new ApiRequestValidator();
        ApiRequestValidationResult apiRequestValidationResult = apiRequestValidator.validateApiRequest(SupportedAPIs.CREATE_CULTURE, queryJSONObject, bodyJSONObject, requestHeaders);
        if (apiRequestValidationResult.passedApiValidation()) {
            JSONObject requestJSONObject = UserInputSecurity.sanitizeAgainstCrossScripting(ToolKit.mergeJSONObjects(queryJSONObject, bodyJSONObject));
            Connection databaseConnection = EarthSubstrate.databaseConnectionPool.checkOut();
            try {
                databaseConnection.setAutoCommit(false); //all or nothing, prevents partial commits if error occurs 
                ToolKit.sendApiJSONResponse(database.createCulture(requestJSONObject, requestHeaders, databaseConnection), exchange);
                databaseConnection.commit();
            } catch (SQLException e) {
                e.printStackTrace(System.out);
                try {
                    databaseConnection.rollback();
                } //undo database entries 
                catch (SQLException ex) {
                } //do nothing
                ToolKit.sendApiJSONResponse(GenericErrorApiResponse.databaseError(), exchange);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                e.printStackTrace(System.out);
                ToolKit.sendApiJSONResponse(GenericErrorApiResponse.generalError(), exchange);
            } finally {
                EarthSubstrate.databaseConnectionPool.checkIn(databaseConnection);
            }
        } else { //failed validation 
            ToolKit.sendApiJSONResponse(new ApiResponseData(apiRequestValidationResult.getReason(), HttpConstants.STATUS_BAD_REQUEST), exchange);
        }
    }

    private static void validateEmailVerificationKey(Headers requestHeaders, HttpExchange exchange, JSONObject queryJSONObject, JSONObject bodyJSONObject) throws IOException {
        ApiRequestValidator apiRequestValidator = new ApiRequestValidator();
        ApiRequestValidationResult apiRequestValidationResult = apiRequestValidator.validateApiRequest(SupportedAPIs.VALIDATE_EMAIL_VERIFICATION_KEY, queryJSONObject, bodyJSONObject, requestHeaders);
        if (apiRequestValidationResult.passedApiValidation()) {
            JSONObject requestJSONObject = UserInputSecurity.sanitizeAgainstCrossScripting(ToolKit.mergeJSONObjects(queryJSONObject, bodyJSONObject));
            Connection databaseConnection = EarthSubstrate.databaseConnectionPool.checkOut();
            try {
                databaseConnection.setAutoCommit(true);
                ToolKit.sendApiJSONResponse(database.validateEmailVerificationKey(requestJSONObject, requestHeaders, databaseConnection), exchange);
            } catch (SQLException e) {
                e.printStackTrace(System.out);
                ToolKit.sendApiJSONResponse(GenericErrorApiResponse.databaseError(), exchange);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                e.printStackTrace(System.out);
                ToolKit.sendApiJSONResponse(GenericErrorApiResponse.generalError(), exchange);
            } finally {
                EarthSubstrate.databaseConnectionPool.checkIn(databaseConnection);
            }
        } else { //failed validation 
            ToolKit.sendApiJSONResponse(new ApiResponseData(apiRequestValidationResult.getReason(), HttpConstants.STATUS_BAD_REQUEST), exchange);
        }
    }
}
