/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package earthsubstrate;

import com.sun.net.httpserver.Headers;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import org.json.simple.JSONObject;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author Christopher Brett
 */
public class DatabaseControl {
    //GET

    //PUT
    protected ApiResponseData validateEmailVerificationKey(JSONObject request, Headers headers, Connection databaseConnection) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        JSONObject response = new JSONObject();
        final EncryptionManager encryptionManager = new EncryptionManager();
        final String sql = "select relatedemailverificationrequestid from cultures where ID = ?";
        final PreparedStatement statement = databaseConnection.prepareStatement(sql);
        statement.setInt(1, (ToolKit.getLongFromValue(request.get("CULTURE_ID"))).intValue());
        final ResultSet results = statement.executeQuery();
        if (results.next()) {
            final String sql2 = "select salt, hash, verified from emailverificationrequests where ID = ?";
            final PreparedStatement statement2 = databaseConnection.prepareStatement(sql2);
            statement2.setInt(1, results.getInt("relatedemailverificationrequestid"));
            final ResultSet results2 = statement2.executeQuery();
            if (results2.next()) {
                if (results2.getString("verified").equals("F")) {
                    if (encryptionManager.authenticate(((String) request.get("EMAIL_VERIFICATION_CODE")).toUpperCase(), results2.getBytes("hash"), results2.getBytes("salt"))) {
                        final String sql3 = "update emailverificationrequests set verified = ? where Id = ?";
                        final PreparedStatement statement3 = databaseConnection.prepareStatement(sql3);
                        statement3.setString(1, "T");
                        statement3.setInt(1, results.getInt("relatedemailverificationrequestid"));
                        statement3.execute();
                        response.put("RESULT", "success");
                        return new ApiResponseData(response, HttpConstants.STATUS_OK);
                    } else {
                        response.put("ERROR", "Incorrect Verification Key");
                        return new ApiResponseData(response, HttpConstants.STATUS_OK);
                    }
                } else {
                    response.put("ERROR", "Email Has Already Been Verified");
                    return new ApiResponseData(response, HttpConstants.STATUS_OK);
                }
            } else {
                response.put("ERROR", "Email Verification Request Not Found");
                return new ApiResponseData(response, HttpConstants.STATUS_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("ERROR", "Culture Not Found For Provided ID");
            return new ApiResponseData(response, HttpConstants.STATUS_NOT_FOUND);
        }
    }

    //POST
    protected ApiResponseData createCulture(JSONObject request, Headers headers, Connection databaseConnection) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        JSONObject response = new JSONObject();
        final String sql = "select relatedemailverificationrequestid from cultures where culturename = ?"; //check if it already exists
        final PreparedStatement statement = databaseConnection.prepareStatement(sql);
        statement.setString(1, (String) request.get("CULTURE_NAME"));
        final ResultSet results = statement.executeQuery();
        if (results.next()) { //match found, culture exists, check if verified alrerady
            final String sql2 = "select verified from emailverificationrequests where ID = ?";
            final PreparedStatement statement2 = databaseConnection.prepareStatement(sql2);
            statement2.setInt(1, results.getInt("relatedemailverificationrequestid"));
            final ResultSet results2 = statement2.executeQuery();
            if (results2.next()) {
                if (results2.getString("verified").equals("T")) {
                    response.put("ERROR", "Culture Already Exists And Is Verified");
                } else {
                    response.put("ERROR", "Culture Already Exists But Has *NOT* Been Verified");
                }
            }
            return new ApiResponseData(response, HttpConstants.STATUS_OK);
        } else { //Create a new culture and send email verification code
            //generate a random verification code
            final EncryptionManager encryptionManager = new EncryptionManager();
            final String charcterSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            final SecureRandom secureRandom = new SecureRandom();
            final StringBuilder stringBuilder = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                stringBuilder.append(charcterSet.charAt(secureRandom.nextInt(charcterSet.length())));
            }
            String verificationKey = stringBuilder.toString();
            byte[] salt = encryptionManager.generateSalt();
            byte[] verificationKeyHashBytes = encryptionManager.generateSaltedHash(verificationKey, salt);
            //create entry in email verififcation row 
            final String sql3 = "insert into emailverificationrequests values (?,?,?,?,?)";
            final PreparedStatement statement3 = databaseConnection.prepareStatement(sql3, Statement.RETURN_GENERATED_KEYS);
            statement3.setNull(1, 0);
            statement3.setString(2, (String) request.get("RECOVERY_EMAIL"));
            statement3.setBytes(3, salt);
            statement3.setBytes(4, verificationKeyHashBytes);
            statement3.setString(5, "F");
            statement3.execute();
            ResultSet getEmailVerificationRequestID = statement3.getGeneratedKeys();
            int emailVerificationRequestID = -1;
            if (getEmailVerificationRequestID.next()) {
                emailVerificationRequestID = getEmailVerificationRequestID.getInt(1);
            }
            final String sql4 = "insert into cultures values (?,?,?,?,?,?,?)";
            final PreparedStatement statement4 = databaseConnection.prepareStatement(sql4, Statement.RETURN_GENERATED_KEYS);
            statement4.setNull(1, 0);
            statement4.setTimestamp(2, new java.sql.Timestamp(TimeAndDateManager.getCurrentTime().getTime().getTime()));
            statement4.setString(3, (String) request.get("CULTURE_NAME"));
            statement4.setNull(4, 0);
            statement4.setNull(5, 0);
            statement4.setNull(6, 0);
            statement4.setInt(7, emailVerificationRequestID);
            statement4.execute();
            ResultSet getCultureID = statement4.getGeneratedKeys();
            int cultureID = -1;
            if (getCultureID.next()) {
                cultureID = getCultureID.getInt(1);
            }
            //load data into json object for email use
            request.put("subject", "Earth Substrate Email Verification");
            request.put("message", "Your Verification Code Is: \"" + verificationKey + "\"");
            request.put("email", (String) request.get("RECOVERY_EMAIL"));
            new Thread(new EmailText(request)).start();
            response.put("RESULT", "success");
            response.put("MESSAGE", "Please Check Your Email For Your Verification Code");
            response.put("CULTURE_ID", cultureID + "");
            return new ApiResponseData(response, HttpConstants.STATUS_CREATED);
        }
    }

    //DELETE
}
