/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class SeverityImpactedServiceDaoNew {

    private final String pluginClassName = this.getClass().getName();
    private final String authentication;
    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");

    public SeverityImpactedServiceDaoNew() {
        String clientId = "4bca358d-7030-4ca0-8827-0b6e49356478";
        String clientSecret = "c6efd4a8-2560-4942-be76-5ebac2cab90d";
        this.authentication = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
    }

    public String getClientCredentials() {
        String returnValue = "";

        HttpURLConnection connection = null;
        try {
            String accessTokenUrl = "https://apigwsit.telkom.co.id:7777/invoke/pub.apigateway.oauth2/getAccessToken?grant_type=client_credentials&client_id=735235b2-7aa1-4264-a054-83109bc1d67f&client_secret=d4693b1c-a324-421a-be67-1e8d3a765245";
            URL url = new URL(accessTokenUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            String jsonResponse = response.toString();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            returnValue = jsonObject.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect(); // Menutup koneksi setelah digunakan
            }
        }
        return returnValue;
    }

    public String getClientCredentialsHVC() {
        String content = "grant_type=client_credentials";
        String returnValue = "";

        HttpsURLConnection connection = null;
        try {
            String accessTokenUrl = "https://apigw.telkom.co.id:7777/invoke/pub.apigateway.oauth2/getAccessToken";
            URL url = new URL(accessTokenUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + authentication);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");

            try ( PrintStream os = new PrintStream(connection.getOutputStream())) {
                os.print(content);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder out = new StringBuilder(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
            String line;

            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            String response = out.toString();
            Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(response);

            if (matcher.matches() && matcher.groupCount() > 0) {
                returnValue = matcher.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect(); // Menutup koneksi setelah digunakan
            }
        }
        return returnValue;
    }

    public String useBearerToken(String bearerToken, String deviceId) {
        int totalImpact = 0;
        String hasil = "";
        String clientUrl = "https://apigwsit.telkom.co.id:7777/gateway/telkom-invaggmytech/1.0/impactedServices";

        if (clientUrl == null || clientUrl.isEmpty()) {
            LogUtil.info(pluginClassName, "Client URL is empty or null");
            return hasil;
        }

        HttpURLConnection connection = null;
        try {
//            String accessToken = getAccessTokenHVC();
//            LogUtil.info(pluginClassName, "TOKEN HVC : "+accessToken);
            URL url = new URL(clientUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject requestBodyJson = new JSONObject();
            JSONObject impactedServicesRequest = new JSONObject();
            JSONObject eaiHeader = new JSONObject();
            JSONObject eaiBody = new JSONObject();

            // Set eaiHeader values
            eaiHeader.put("externalId", "");
            eaiHeader.put("timestamp", "");

            // Set eaiBody values
            eaiBody.put("input", deviceId); // Menggunakan deviceId sebagai input
//            eaiBody.put("type", ""); // Tipe tetap GPON, sesuai permintaan
            eaiBody.put("page", 0);
            eaiBody.put("limit", 0);

            // Add eaiHeader and eaiBody to request object
            impactedServicesRequest.put("eaiHeader", eaiHeader);
            impactedServicesRequest.put("eaiBody", eaiBody);
            requestBodyJson.put("impactedServicesRequest", impactedServicesRequest);

            // Convert the JSON object to a string
            String requestBody = requestBodyJson.toString();

            // Send data body to Server
            try ( OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get response from API
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String apiResponse = response.toString();

                // Parsing JSON response to get total impact
                JSONObject jsonObject = new JSONObject(apiResponse);
                JSONObject totalObject = jsonObject.getJSONObject("data").getJSONObject("total");
                JSONArray serviceInfo = new JSONArray();
                serviceInfo = jsonObject.getJSONObject("data").getJSONArray("service_info");
                JSONArray serviceNumbers = new JSONArray();
                int totalAllData = totalObject.getInt("total_all_data");
                int total_service_info = totalObject.getInt("total_service_info");
                int total_service_info_datin = totalObject.getInt("total_service_info_datin");
                int total_service_sdwan = totalObject.getInt("total_service_sdwan");
                int total_service_nodeb = totalObject.getInt("total_service_nodeb");

                // Inisialisasi total_hvc dan total_nonhvc
//                int totalHvc = 0;
//                int totalNonHvc = 0;

                // Iterasi melalui setiap service_info
//                for (int i = 0; i < serviceInfo.length(); i++) {
//                    JSONObject service = new JSONObject();
//                    service = serviceInfo.getJSONObject(i);
//                    String serviceNumber = service.getString("SERVICE_NUMBER");
//
//                    // Panggil API kedua untuk setiap SERVICE_NUMBER
//                    int serviceDetails = 0;
//                    serviceDetails = getServiceDetails(accessToken, serviceNumber);
//
//                    // Tambahkan jumlah HVC dan non-HVC ke total
//                    totalHvc += serviceDetails;
//
//                    serviceNumbers.put(serviceDetails);
//                }

                // Membuat hasil API
                JSONObject hasilAPI = new JSONObject();
                hasilAPI.put("total_impact", totalAllData);
                hasilAPI.put("total_service_info", total_service_info);
                hasilAPI.put("total_datin", total_service_info_datin);
                hasilAPI.put("total_sdwan", total_service_sdwan);
                hasilAPI.put("total_nodeb", total_service_nodeb);
//                hasilAPI.put("total_hvc", totalHvc);
                hasilAPI.put("total_hvc", 0);
//                hasilAPI.put("total_nonhvc", total_service_info-totalHvc);
                hasilAPI.put("total_nonhvc", 0);
                hasilAPI.put("serevity", "Minor");
                hasil = hasilAPI.toString();
            } else {
                LogUtil.info(pluginClassName, "HTTP Error: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect(); // Menutup koneksi setelah digunakan
            }
        }
        return hasil;
    }

    private static String getAccessTokenHVC() throws IOException, JSONException {
        String clientId = "4bca358d-7030-4ca0-8827-0b6e49356478";
        String clientSecret = "c6efd4a8-2560-4942-be76-5ebac2cab90d";
        String url = "https://apigw.telkom.co.id:7777/invoke/pub.apigateway.oauth2/getAccessToken?client_id=" + clientId + "&client_secret=" + clientSecret + "&grant_type=client_credentials";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try ( BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        JSONObject jsonResponse = new JSONObject(response.toString());

//        LogUtil.info("test", jsonResponse.getString("access_token"));
        return jsonResponse.getString("access_token");
    }

    private static int getServiceDetails(String accessToken, String serviceNumber) throws IOException, JSONException {
        int hasil = 0;
        String url = "https://apigw.telkom.co.id:7777/gateway/telkom-dbprofile-getHVC/1.0/getHVC";
        JSONObject requestBody = new JSONObject();
        requestBody.put("ND", serviceNumber);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        connection.getOutputStream().write(requestBody.toString().getBytes());

        StringBuilder response = new StringBuilder();
        try ( BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
//        LogUtil.info("test", jsonResponse.toString());

        // Periksa apakah respons memiliki status code "203" dan pesan "No Data Found"
        String statusCode = jsonResponse.optString("statusCode", "");
        String returnMessage = jsonResponse.optString("returnMessage", "");
        if ("203".equals(statusCode) && "No Data Found".equals(returnMessage)) {
            // Jika respons sesuai dengan kondisi, langsung kembalikan nilai hasil tanpa melakukan perhitungan
            return hasil;
        }

        // Hitung jumlah HVC dan non-HVC
        int hvcCount = 0;
        JSONArray data = jsonResponse.getJSONArray("data");
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            String katHvc = item.optString("KAT_HVC", "");
            if (katHvc.contains("HVC")) {
                hvcCount++;
            }
        }

        hasil = hvcCount;
        return hasil;
    }

    public List<ArrayList> getSeverityMinor() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ArrayList> hasilList = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_operator_non_hvc, c_non_hvc, c_operator_hvc, c_hvc from app_fd_cra_master_severity where c_nama_severity = 'Minor'";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                String OP_NON_HVC = rs.getString("c_operator_non_hvc");
                String NON_HVC = rs.getString("c_non_hvc");
                String OP_HVC = rs.getString("c_operator_hvc");
                String HVC = rs.getString("c_hvc");

                ArrayList<String> rowValues = new ArrayList<>();
                rowValues.add(OP_NON_HVC);
                rowValues.add(NON_HVC);
                rowValues.add(OP_HVC);
                rowValues.add(HVC);

                hasilList.add(rowValues);

            }
        } catch (SQLException ex) {
            LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
        }

        return hasilList;
    }

    public List<ArrayList> getSeverityModerate() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ArrayList> hasilList = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_operator_non_hvc, c_non_hvc, c_operator_hvc, c_hvc from app_fd_cra_master_severity where c_nama_severity = 'Moderate'";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                String OP_NON_HVC = rs.getString("c_operator_non_hvc");
                String NON_HVC = rs.getString("c_non_hvc");
                String OP_HVC = rs.getString("c_operator_hvc");
                String HVC = rs.getString("c_hvc");

                ArrayList<String> rowValues = new ArrayList<>();
                rowValues.add(OP_NON_HVC);
                rowValues.add(NON_HVC);
                rowValues.add(OP_HVC);
                rowValues.add(HVC);

                hasilList.add(rowValues);

            }
        } catch (SQLException ex) {
            LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
        }

        return hasilList;
    }
}
