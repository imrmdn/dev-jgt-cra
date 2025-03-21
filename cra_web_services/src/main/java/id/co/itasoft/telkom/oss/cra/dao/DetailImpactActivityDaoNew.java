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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class DetailImpactActivityDaoNew {

    private final String pluginClassName = this.getClass().getName();
    private final String authentication;
    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");

    ConnectionPool connectionPool = new ConnectionPool(20, 5, TimeUnit.MINUTES);
    private final OkHttpClient httpClient
            = new OkHttpClient()
                    .newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectionPool(connectionPool)
                    .retryOnConnectionFailure(false)
                    .build();

    public DetailImpactActivityDaoNew() throws JSONException {
        List<String> getCreds = getListCred();
        String clientIdVal = "";
        String clientSecretVal = "";
        String getTokenUrl = "";

        for (String getCred : getCreds) {
            JSONObject jsonObject = new JSONObject(getCred);
            String c_client_id = jsonObject.getString("c_client_id");
            String c_client_secret = jsonObject.getString("c_client_secret");

            clientIdVal = c_client_id;
            clientSecretVal = c_client_secret;
        }
        String clientId = clientIdVal;
        String clientSecret = clientSecretVal;
        this.authentication = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
    }

    public String getClientCredentials() throws JSONException {
        String content = "grant_type=client_credentials";
        String returnValue = "";
        HttpsURLConnection connection = null; // Inisialisasi koneksi di luar blok try

        List<String> getCreds = getListCred();
        String clientIdVal = "";
        String clientSecretVal = "";
        String getTokenUrl = "";

        for (String getCred : getCreds) {
            JSONObject jsonObject = new JSONObject(getCred);
            String c_url = jsonObject.getString("c_url");

            getTokenUrl = c_url;
        }

        try {
            String accessTokenUrl = getTokenUrl;
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

    public String useBearerToken(String deviceId) throws JSONException {
        String hasil = "";

        // Mendapatkan bearer token
        String getTokenUrl = "https://apigwsit.telkom.co.id:7777/invoke/pub.apigateway.oauth2/getAccessToken?grant_type=client_credentials&client_id=735235b2-7aa1-4264-a054-83109bc1d67f&client_secret=d4693b1c-a324-421a-be67-1e8d3a765245";
        String bearerToken = getBearerToken(getTokenUrl);

        // Membuat request body JSON
        JSONObject requestBodyJson = new JSONObject();
        JSONObject impactedServicesRequest = new JSONObject();
        JSONObject eaiHeader = new JSONObject();
        JSONObject eaiBody = new JSONObject();

        // Set eaiHeader values
        eaiHeader.put("externalId", "");
        eaiHeader.put("timestamp", "");

        // Set eaiBody values
        eaiBody.put("input", deviceId);
//        eaiBody.put("type", "");
        eaiBody.put("page", 0);
        eaiBody.put("limit", 0);

        // Add eaiHeader and eaiBody to request object
        requestBodyJson.put("eaiHeader", eaiHeader);
        requestBodyJson.put("eaiBody", eaiBody);
        impactedServicesRequest.put("impactedServicesRequest", requestBodyJson);

        // Convert the JSON object to a string
        String requestBody = impactedServicesRequest.toString();
        LogUtil.info(pluginClassName, requestBody);

        // Mengirim permintaan ke server
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody);
            Request request = new Request.Builder()
                    .url("https://apigwsit.telkom.co.id:7777/gateway/telkom-invaggmytech/1.0/impactedServices")
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseData = response.body().string();
//                LogUtil.info(pluginClassName, "Response API Cek: " + responseData);
                hasil = convertToPreviousFormat(responseData);
            } else {
                LogUtil.info(pluginClassName, "HTTP Error: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hasil;
    }

    private String convertToPreviousFormat(String responseData) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            JSONArray serviceArray = jsonResponse.getJSONObject("data").getJSONArray("service_info");

            JSONArray resultArray = new JSONArray();

            for (int i = 0; i < serviceArray.length(); i++) {
//                JSONObject serviceObject = serviceArray.getJSONObject(i);
//                JSONObject resultObject = new JSONObject();
//                String serviceContact = serviceObject.get("SERVICE_CONTACT").toString();
//                String[] phoneNumbers = serviceContact.split(" ");
//                String firstPhoneNumber = phoneNumbers[0];

                JSONObject serviceObject = serviceArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();
                String serviceContact = serviceObject.optString("SERVICE_CONTACT", "-");
                String[] phoneNumbers = serviceContact.split(" ");
                String firstPhoneNumber = "-";

                if (phoneNumbers.length > 0) {
                    firstPhoneNumber = phoneNumbers[0];
                }

                resultObject.put("assetnum", serviceObject.get("SERVICE_ID"));
                resultObject.put("phoneNumber", firstPhoneNumber);
                resultObject.put("status", serviceObject.get("SERVICE_ADMINSTATE"));
                resultObject.put("type", serviceObject.get("TYPE"));
                resultObject.put("service_type", serviceObject.get("SERVICE_TYPE"));
                resultArray.put(resultObject);
            }

            return resultArray.toString();

        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

// Metode untuk mendapatkan bearer token dari URL
    private String getBearerToken(String getTokenUrl) {
        String bearerToken = "";
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(getTokenUrl)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                JSONObject jsonResponse = new JSONObject(response.body().string());
                bearerToken = jsonResponse.getString("access_token");
            } else {
                LogUtil.info(pluginClassName, "Failed to get bearer token. HTTP Error: " + response.code());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return bearerToken;
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

    public String getPhoneNumberFromAPI(String assetnum) throws JSONException {
        String hasil = "";
        String tok = getToken();
        LogUtil.info(pluginClassName, "TOKEN:" + tok);

        if (tok == null || tok.isEmpty()) {
            LogUtil.info(pluginClassName, "Failed to get access token.");
            return hasil;
        }
        HttpsURLConnection con = null;
        try {
            String urlVal = "";
            urlVal = getUrlIndri();
            URL url = new URL(urlVal);
            con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + tok);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String jsonInputString = "{\"apiIndriRequest\": {\"eaiHeader\": {\"externalId\": \"\",\"timestamp\": \"\"},\"eaiBody\": {\"guid\": \"0\",\"code\": \"2\",\"data\": {\"nd\": \"" + assetnum + "\",\"contact\": true}}}}";
            LogUtil.info(pluginClassName, "request indri: " + jsonInputString);

            try ( OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                try ( BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

//                    JSONObject jsonResponse = new JSONObject(response.toString());
//                    JSONObject responseData = jsonResponse.getJSONObject("apiIndriResponse").getJSONObject("eaiBody").getJSONObject("data");
//                    JSONArray contactArray = responseData.getJSONArray("contact");
////                    LogUtil.info(pluginClassName, "response indri: " + response.toString());
//
//                    for (int i = 0; i < contactArray.length(); i++) {
//                        JSONObject contact = contactArray.getJSONObject(i);
////                        if (contact.getString("contact_type_name").equals("WHATSAPP")) {
////                            hasil = contact.getString("contact_desc");
////                        }
//                        if (contact.getString("contact_type_name").equals("NO HP") && contact.getString("xs10").equals("TSEL")) {
//                            hasil = contact.getString("contact_desc");
//                        }
//                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject apiIndriResponse = jsonResponse.optJSONObject("apiIndriResponse");
                    String hasilIndri = "-"; // Nilai default jika tidak ditemukan data yang sesuai

                    if (apiIndriResponse != null) {
                        JSONObject eaiBody = apiIndriResponse.optJSONObject("eaiBody");
                        if (eaiBody != null) {
                            JSONObject data = eaiBody.optJSONObject("data");
                            if (data != null) {
                                JSONArray contactArray = data.optJSONArray("contact");
                                if (contactArray != null) {
                                    for (int i = 0; i < contactArray.length(); i++) {
                                        JSONObject contact = contactArray.getJSONObject(i);
                                        if (contact.getString("contact_type_name").equals("NO HP") && contact.getString("xs10").equals("TSEL")) {
                                            hasilIndri = contact.getString("contact_desc");
                                            break; // Jika data yang dicari sudah ditemukan, hentikan loop
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LogUtil.info(pluginClassName, "Failed to get response from API. Response code: " + responseCode);
            }

            con.disconnect();
        } catch (IOException | JSONException e) {
            LogUtil.info(pluginClassName, "Error during API call: " + e.getMessage());
        } finally {
            if (con != null) {
                con.disconnect(); // Menutup koneksi setelah digunakan
            }
        }

        return hasil;
    }

    private String getToken() throws JSONException {
        List<String> getCreds = getListCredTokenIndri();
        String clientId = "";
        String clientSecret = "";
        String getTokenUrl = "";

        for (String getCred : getCreds) {
            JSONObject jsonObject = new JSONObject(getCred);
            String c_url = jsonObject.getString("c_url");
            String c_client_id = jsonObject.getString("c_client_id");
            String c_client_secret = jsonObject.getString("c_client_secret");

            clientId = c_client_id;
            clientSecret = c_client_secret;
            getTokenUrl = c_url;
        }

        HttpsURLConnection con = null;
        try {
            URL url = new URL(getTokenUrl);
            con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String jsonInputString = "{\"grant_type\": \"client_credentials\",\"client_id\": \"" + clientId + "\",\"client_secret\": \"" + clientSecret + "\"}";

            try ( OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try ( BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getString("access_token");
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect(); // Menutup koneksi setelah digunakan
            }
        }

        return null;
    }

    public List<String> getListCred() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_url, c_client_id, c_client_secret from app_fd_m_token_api where c_type = 'token_indri'";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("c_url", rs.getString("c_url"));
                jsonObject.put("c_client_id", rs.getString("c_client_id"));
                jsonObject.put("c_client_secret", rs.getString("c_client_secret"));
                results.add(jsonObject.toString());
            }
        } catch (SQLException | JSONException ex) {
            LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
        }
        return results;
    }

    public List<String> getListCredgetImpactServiceManual() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_url, c_client_id, c_client_secret from app_fd_m_token_api where c_type = 'call_get_impact_service_manual'";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("c_url", rs.getString("c_url"));
                jsonObject.put("c_client_id", rs.getString("c_client_id"));
                jsonObject.put("c_client_secret", rs.getString("c_client_secret"));
                results.add(jsonObject.toString());
            }
        } catch (SQLException | JSONException ex) {
            LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
        }
        return results;
    }

    public List<String> getListCredGetListServiceInformation() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_url, c_client_id, c_client_secret from app_fd_m_token_api where c_type = 'call_asset_get_service_information'";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("c_url", rs.getString("c_url"));
                jsonObject.put("c_client_id", rs.getString("c_client_id"));
                jsonObject.put("c_client_secret", rs.getString("c_client_secret"));
                results.add(jsonObject.toString());
            }
        } catch (SQLException | JSONException ex) {
            LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
        }
        return results;
    }

    public List<String> getListCredTokenIndri() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_url, c_client_id, c_client_secret from app_fd_m_token_api where c_type = 'call_token_indri'";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("c_url", rs.getString("c_url"));
                jsonObject.put("c_client_id", rs.getString("c_client_id"));
                jsonObject.put("c_client_secret", rs.getString("c_client_secret"));
                results.add(jsonObject.toString());
            }
        } catch (SQLException | JSONException ex) {
            LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
        }
        return results;
    }

    public String getUrlIndri() {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select c_url from app_fd_m_token_api where c_type = 'call_indri_get_customer'";
                stmt = con.prepareStatement(selectQuery);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_url");
                }
            }
        } catch (Exception e) {
            LogUtil.info(this.getClass().getName(), "Error retrieving data " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
            }
        }

        return result;
    }
}
