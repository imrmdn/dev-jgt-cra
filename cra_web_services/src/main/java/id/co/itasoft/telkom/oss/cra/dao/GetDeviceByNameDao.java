/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra.dao;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class GetDeviceByNameDao {

    private String loginUrl;
    private String deviceUrl;
    private String accessToken;

    // Constructor untuk inisialisasi URL login dan URL device
    public GetDeviceByNameDao(String loginUrl, String deviceUrl) {
        this.loginUrl = loginUrl;
        this.deviceUrl = deviceUrl;
    }

    // Method untuk login dan mendapatkan access token
    public boolean login(String username, String password) {
        try {
            JSONObject loginBody = new JSONObject();
            loginBody.put("username", username);
            loginBody.put("password", password);

            URL url = new URL(this.loginUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try ( OutputStream os = connection.getOutputStream()) {
                os.write(loginBody.toString().getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getString("status").equals("Success")) {
                    this.accessToken = jsonResponse.getString("accessToken");
                    return true;
                }
            } else {
                System.out.println("Login API response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method untuk memanggil API getDeviceLikeName dengan access token
    public String getDeviceLikeName(String hostName, int page, int limit) {
        try {
            if (this.accessToken == null) {
                System.out.println("Access token belum tersedia. Silakan login terlebih dahulu.");
                return null;
            }

            JSONObject deviceBody = new JSONObject();
            deviceBody.put("host_name", hostName);
            deviceBody.put("page", page);
            deviceBody.put("limit", limit);

            URL url = new URL(this.deviceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + this.accessToken);
            connection.setDoOutput(true);

            try ( OutputStream os = connection.getOutputStream()) {
                os.write(deviceBody.toString().getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                return response;
            } else {
                System.out.println("getDeviceLikeName API response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
