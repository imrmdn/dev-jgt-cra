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
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
public class DetailImpactActivityDao {

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

    public DetailImpactActivityDao() throws JSONException {
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

    public String useBearerToken(String bearerToken, String deviceId) throws JSONException {
        int jumlahKATHVCContainingHVC = 0;
        int jumlahKATHVCNotContainingHVC = 0;
        String SeverityMinor = "";
        String SeverityModerate = "";
        String SeverityMajor = "";
        String SeverityCritical = "";
        String hasil = "";

        List<String> getCreds = getListCredgetImpactServiceManual();
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

        List<String> getCredsLSI = getListCredGetListServiceInformation();
        String clientIdLSI = "";
        String clientSecretLSI = "";
        String getTokenUrlLSI = "";

        for (String getCredLSI : getCredsLSI) {
            JSONObject jsonObject = new JSONObject(getCredLSI);
            String c_url = jsonObject.getString("c_url");
            String c_client_id = jsonObject.getString("c_client_id");
            String c_client_secret = jsonObject.getString("c_client_secret");

            clientIdLSI = c_client_id;
            clientSecretLSI = c_client_secret;
            getTokenUrlLSI = c_url;
        }

        String clientUrl = getTokenUrl;
        if (clientUrl == null || clientUrl.isEmpty()) {
            LogUtil.info(pluginClassName, "Client URL is empty or null");
            return hasil;
        }

        HttpsURLConnection connection = null;
        try {
            URL url = new URL(clientUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject requestBodyJson = new JSONObject();
            JSONObject getImpactServiceManualRequest = new JSONObject();
            JSONObject eaiHeader = new JSONObject();
            JSONObject eaiBody = new JSONObject();

            // Set eaiHeader values
            eaiHeader.put("externalId", "");
            eaiHeader.put("timestamp", "");

            // Set eaiBody values
            eaiBody.put("IN_FAULTID", "");
//            eaiBody.put("IN_DEVICEID", "ODP-GTL-FAG/31 FAG/D06/31.01");
            eaiBody.put("IN_DEVICEID", deviceId);
            eaiBody.put("IN_STATUS", "");

            // Add eaiHeader and eaiBody to request object
            requestBodyJson.put("eaiHeader", eaiHeader);
            requestBodyJson.put("eaiBody", eaiBody);
            getImpactServiceManualRequest.put("getImpactServiceManualRequest", requestBodyJson);

            // Convert the JSON object to a string
            String requestBody = getImpactServiceManualRequest.toString();
            LogUtil.info(pluginClassName, requestBody);

            // Send data body to Server
            try ( OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get respons from API
            int responseCode = connection.getResponseCode();
            JSONArray assetNumPhonePairs = new JSONArray();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String apiResponse = response.toString();
                LogUtil.info(pluginClassName, "Response API Cek: " + apiResponse);
                // Mengurai JSON
                JSONObject jsonObject = new JSONObject(apiResponse);
                int totalImpact = 0;

                try {
                    totalImpact = jsonObject.getJSONObject("getImpactServiceManualResponse")
                            .getJSONObject("eaiBody")
                            .getInt("OUT_COUNT_SERVICE");
                } catch (JSONException e) {
                }
                LogUtil.info(pluginClassName, "Response API Total Impact: " + totalImpact);
                if (totalImpact > 0) {

                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        JsonNode root = objectMapper.readTree(apiResponse);
                        JsonNode impactResults = root.path("getImpactServiceManualResponse").path("eaiBody").path("OUT_IMPACT_RESULT");

                        List<String> assetNumValues = new ArrayList<>();
                        for (JsonNode result : impactResults) {
                            String assetNum = result.path("assetnum").asText();
                            assetNumValues.add(assetNum);
                        }

                        // Menampilkan hasil
                        for (String value : assetNumValues) {
                            String stringResponse = "";
                            try {
                                HttpUrl.Builder httpBuilder = HttpUrl.parse(getTokenUrlLSI).newBuilder();
                                httpBuilder.addQueryParameter("service_id", value);

                                Request request = new Request.Builder()
                                        .url(httpBuilder.build())
                                        .addHeader("api_key", clientIdLSI) // add request headers
                                        .addHeader("api_id", clientSecretLSI)
                                        .addHeader("Origin", "https://oss-incident.telkom.co.id")
                                        .build();
                                Response responseImpact = null;

                                try {

                                    String[] parts = value.split("_");
                                    String serviceIdIndri = "";

                                    if (parts.length == 3) { // Jika terdapat 2 _
                                        serviceIdIndri = parts[1];
                                    } else if (parts.length == 2) { // Jika terdapat 1 _
                                        char lastChar = parts[1].charAt(parts[1].length() - 1);
                                        if (Character.isLetter(lastChar)) { // Jika yang paling kanan huruf
                                            serviceIdIndri = parts[0];
                                        } else { // Jika yang paling kanan angka
                                            serviceIdIndri = parts[1];
                                        }
                                    } else { // Jika tidak ada _
                                        serviceIdIndri = value;
                                    }
//                                    LogUtil.info(pluginClassName, "Service Id for Indri: " + serviceIdIndri);

                                    responseImpact = httpClient.newCall(request).execute();
                                    if (!responseImpact.isSuccessful()) {
                                        stringResponse = responseImpact.body().string();
                                        responseImpact.body().close();
                                        responseImpact.close();
                                        throw new IOException("Unexpected code " + responseImpact);
                                    } else {
                                        stringResponse = responseImpact.body().string();
                                        responseImpact.body().close();
                                        responseImpact.close();
                                    }
                                    // Mengambil nilai phone_number dari respons JSON
                                    JSONObject jsonResponse = new JSONObject(stringResponse);
                                    JSONArray data = jsonResponse.getJSONArray("data");

                                    // Mengekstrak nilai phone_number dari data pertama (asumsi data hanya satu)
                                    if (data.length() > 0) {
                                        JSONObject firstData = data.getJSONObject(0);
                                        String phoneNumber = firstData.getString("phone_number");
                                        LogUtil.info(pluginClassName, "Phone Number: " + phoneNumber);

                                        if (phoneNumber == null || phoneNumber.isEmpty()) {
                                            // Ganti dengan nilai dari API
                                            phoneNumber = getPhoneNumberFromAPI(serviceIdIndri); // Panggil method untuk mendapatkan nomor telepon dari API
//                                            phoneNumber = getPhoneNumberFromAPI("131164116161"); // Panggil method untuk mendapatkan nomor telepon dari API
                                            LogUtil.info(pluginClassName, "Phone Number: " + phoneNumber);
                                        }

                                        // Menambahkan pasangan assetnum dan phoneNumber ke JSONArray
                                        JSONObject assetNumPhonePair = new JSONObject();
                                        assetNumPhonePair.put("assetnum", value);
                                        assetNumPhonePair.put("phoneNumber", phoneNumber);
                                        assetNumPhonePair.put("status", "-");
                                        assetNumPhonePairs.put(assetNumPhonePair);
                                    } else {
                                        LogUtil.info(pluginClassName, "Data not Found");
                                    }
                                    hasil = assetNumPhonePairs.toString();

                                    long time = responseImpact.receivedResponseAtMillis() - responseImpact.sentRequestAtMillis();
                                    // Get response body
                                } catch (Exception ex) {
                                    LogUtil.error(pluginClassName, ex, "Error: " + ex.getMessage());
                                } finally {
                                    responseImpact.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    LogUtil.info(pluginClassName, "Response API Severity Detail: ");
                    hasil = "Data tidak ditemukan";
                }

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
