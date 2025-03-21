/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
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
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class SeverityImpactedServiceDao {

    private final String pluginClassName = this.getClass().getName();
    private final String authentication;
    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");

    public SeverityImpactedServiceDao() {
        String clientId = "4bca358d-7030-4ca0-8827-0b6e49356478";
        String clientSecret = "c6efd4a8-2560-4942-be76-5ebac2cab90d";
        this.authentication = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
    }

    public String getClientCredentials() {
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
        int jumlahKATHVCContainingHVC = 0;
        int jumlahKATHVCNotContainingHVC = 0;
        String SeverityMinor = "";
        String SeverityModerate = "";
        String SeverityMajor = "";
        String SeverityCritical = "";
        String hasil = "";
        String clientUrl = "https://apigw.telkom.co.id:7777/gateway/telkom-uim-service/1.0/getImpactServiceManual";
        if (clientUrl == null || clientUrl.isEmpty()) {
            LogUtil.info(pluginClassName, "Client URL is empty or null");
            return hasil;
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(clientUrl);
            connection = (HttpURLConnection) url.openConnection();
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
//            LogUtil.info(pluginClassName, requestBody);

            // Send data body to Server
            try ( OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get respons from API
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String apiResponse = response.toString();
//                LogUtil.info(pluginClassName, "Response API Cek: " + apiResponse);
                // Mengurai JSON
                JSONObject jsonObject = new JSONObject(apiResponse);
                int totalImpact = 0;

                try {
                    totalImpact = jsonObject.getJSONObject("getImpactServiceManualResponse")
                            .getJSONObject("eaiBody")
                            .getInt("OUT_COUNT_SERVICE");
                } catch (JSONException e) {
                }
//                LogUtil.info(pluginClassName, "Response API Total Impact: " + totalImpact);
                if (totalImpact > 0) {

                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        JsonNode root = objectMapper.readTree(apiResponse);
                        JsonNode impactResults = root.path("getImpactServiceManualResponse").path("eaiBody").path("OUT_IMPACT_RESULT");

                        List<String> assetNumValues = new ArrayList<>();
                        for (JsonNode result : impactResults) {
                            String assetNum = result.path("assetnum").asText();
                            String[] parts = assetNum.split("_");

                            // Logika untuk pemilihan nilai assetnum
                            String selectedValue;
                            if (parts.length == 3) {
                                selectedValue = parts[1]; // Jika ada 2 delimeter "_", ambil yang di tengah
                            } else if (parts.length == 2) {
                                selectedValue = parts[1]; // Jika ada 1 delimeter "_", ambil yang terakhir
                            } else {
                                selectedValue = parts[0]; // Jika tidak ada delimeter "_", ambil semuanya
                            }

                            assetNumValues.add(selectedValue);
                        }

//                        // Menampilkan hasil
//                        for (String value : assetNumValues) {
//                            HttpURLConnection connectionHVC = null;
//                            try {
//                                URL urlHVC = new URL("https://apigw.telkom.co.id:7777/gateway/telkom-dbprofile-getHVC/1.0/getHVC");
//                                connectionHVC = (HttpURLConnection) urlHVC.openConnection();
//                                connectionHVC.setRequestMethod("POST");
//                                connectionHVC.setRequestProperty("Authorization", "Bearer " + bearerToken);
//                                connectionHVC.setRequestProperty("Content-Type", "application/json");
//                                connectionHVC.setDoOutput(true);
//
//                                JSONObject requestBodyJsonHVC = new JSONObject();
//
//                                requestBodyJsonHVC.put("ND", value);
//
//                                // Convert the JSON object to a string
//                                String requestBodyHVC = requestBodyJsonHVC.toString();
////                                LogUtil.info(pluginClassName, requestBodyHVC);
//
//                                // Send data body to Server
//                                try ( OutputStream os = connectionHVC.getOutputStream()) {
//                                    byte[] inputHVC = requestBodyHVC.getBytes(StandardCharsets.UTF_8);
//                                    os.write(inputHVC, 0, inputHVC.length);
//                                }
//
//                                // Get respons from API
//                                int responseCodeHVC = connectionHVC.getResponseCode();
//                                if (responseCodeHVC == HttpURLConnection.HTTP_OK) {
//                                    BufferedReader readerHVC = new BufferedReader(new InputStreamReader(connectionHVC.getInputStream()));
//                                    StringBuilder responseHVC = new StringBuilder();
//                                    String lineHVC;
//
//                                    while ((lineHVC = readerHVC.readLine()) != null) {
//                                        responseHVC.append(lineHVC);
//                                    }
//
//                                    String apiResponseHVC = responseHVC.toString();
//                                    JsonNode JSONapiResponseHVC = objectMapper.readTree(apiResponseHVC);
//                                    JsonNode data = JSONapiResponseHVC.get("data");
//                                    JSONObject jsonReturnMsg = new JSONObject(apiResponseHVC);
//                                    String returnMsgApi = jsonReturnMsg.getString("returnMessage");
//                                    if (returnMsgApi.equalsIgnoreCase("No Data Found")) {
//                                        jumlahKATHVCNotContainingHVC++;
//                                    }
//                                    if (data.isArray()) {
//                                        for (JsonNode entry : data) {
//                                            String katHvc = entry.path("KAT_HVC").asText();
//                                            if (katHvc.contains("HVC")) {
//                                                jumlahKATHVCContainingHVC++;
//                                            } else if (!katHvc.contains("HVC")) {
//                                                jumlahKATHVCNotContainingHVC++;
//                                            }
//                                        }
//                                    }
//                                    LogUtil.info(pluginClassName, "Response API HVC Message: " + jsonReturnMsg.toString());
//                                    LogUtil.info(pluginClassName, "Response API HVC Message: " + returnMsgApi);
////                                    LogUtil.info(pluginClassName, "Jumlah Non-HVC: " + jumlahKATHVCNotContainingHVC);
//
//                                } else {
////                                    LogUtil.info(pluginClassName, "HTTP Error: " + responseCode);
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            } finally {
//                                if (connectionHVC != null) {
//                                    connectionHVC.disconnect(); // Menutup koneksi setelah digunakan
//                                }
//                            }
//                        }
                        JSONObject hasilAPI = new JSONObject();

                        List<ArrayList> listMinor = getSeverityMinor();
                        List<ArrayList> listModerate = getSeverityModerate();
                        ArrayList firstArrayList = listMinor.get(0);

                        ArrayList moderateArrayList = listModerate.get(0);
                        String op_non_hvc_min = String.valueOf(firstArrayList.get(0));
                        String non_hvc_min = String.valueOf(firstArrayList.get(1));
                        String op_hvc_min = String.valueOf(firstArrayList.get(2));
                        String hvc_min = String.valueOf(firstArrayList.get(3));
                        String op_non_hvc_mod = String.valueOf(moderateArrayList.get(0));
                        String non_hvc_mod = String.valueOf(moderateArrayList.get(1));
                        String split_non_hvc_mod1 = "";
                        String split_non_hvc_mod2 = "";
                        if (non_hvc_mod.contains("-")) {
                            String[] split_non_hvc_mod = non_hvc_mod.split("-");
                            if (split_non_hvc_mod.length == 2) {
                                split_non_hvc_mod1 = split_non_hvc_mod[0].trim();
                                split_non_hvc_mod2 = split_non_hvc_mod[1].trim();
                            } else {
                                non_hvc_mod = non_hvc_mod;
                            }
                        }
                        String op_hvc_mod = String.valueOf(moderateArrayList.get(2));
                        String hvc_mod = String.valueOf(moderateArrayList.get(3));

                        try {
                            //minor
                            if (op_non_hvc_min.equalsIgnoreCase("<") && op_hvc_min.equalsIgnoreCase("<")) {
                                if (totalImpact < Integer.valueOf(non_hvc_min) && ((jumlahKATHVCContainingHVC / totalImpact) * 100) < Float.valueOf(hvc_min)) {
                                    LogUtil.info(pluginClassName, "A");
                                    SeverityMinor = "Yes";
                                } else {
                                    LogUtil.info(pluginClassName, "EA");
                                    SeverityMinor = "No";
                                }
                            } else if (op_non_hvc_min.equalsIgnoreCase(">") && op_hvc_min.equalsIgnoreCase(">")) {
                                if (totalImpact > Integer.valueOf(non_hvc_min) && ((jumlahKATHVCContainingHVC / totalImpact) * 100) > Float.valueOf(hvc_min)) {
                                    LogUtil.info(pluginClassName, "B");
                                    SeverityMinor = "Yes";
                                } else {
                                    LogUtil.info(pluginClassName, "EB");
                                    SeverityMinor = "No";
                                }
                            } else if (op_non_hvc_min.equalsIgnoreCase("<") && op_hvc_min.equalsIgnoreCase(">")) {
                                if (totalImpact < Integer.valueOf(non_hvc_min) && ((jumlahKATHVCContainingHVC / totalImpact) * 100) > Float.valueOf(hvc_min)) {
                                    LogUtil.info(pluginClassName, "C");
                                    SeverityMinor = "Yes";
                                } else {
                                    LogUtil.info(pluginClassName, "EC");
                                    SeverityMinor = "No";
                                }
                            } else if (op_non_hvc_min.equalsIgnoreCase(">") && op_hvc_min.equalsIgnoreCase("<")) {
                                if (totalImpact > Integer.valueOf(non_hvc_min) && ((jumlahKATHVCContainingHVC / totalImpact) * 100) < Float.valueOf(hvc_min)) {
                                    LogUtil.info(pluginClassName, "D");
                                    SeverityMinor = "Yes";
                                } else {
                                    LogUtil.info(pluginClassName, "ED");
                                    SeverityMinor = "No";
                                }
                            } else {
                                LogUtil.info(pluginClassName, "X");
                                SeverityMinor = "No";
                            }

                            //moderate
                            if (op_non_hvc_mod.equalsIgnoreCase("<") && op_hvc_mod.equalsIgnoreCase("<")) {
                                if (totalImpact < Integer.valueOf(non_hvc_mod) && ((jumlahKATHVCContainingHVC / totalImpact) * 100) < Float.valueOf(hvc_mod)) {
                                    LogUtil.info(pluginClassName, "F");
                                    SeverityModerate = "Yes";
                                } else {
                                    LogUtil.info(pluginClassName, "EF");
                                    SeverityModerate = "No";
                                }
                            } else if (op_non_hvc_mod.equalsIgnoreCase(">") && op_hvc_mod.equalsIgnoreCase(">")) {
                                if (totalImpact > Integer.valueOf(non_hvc_mod) && ((jumlahKATHVCContainingHVC / totalImpact) * 100) > Float.valueOf(hvc_mod)) {
                                    LogUtil.info(pluginClassName, "G");
                                    SeverityModerate = "Yes";
                                } else {
                                    LogUtil.info(pluginClassName, "EG");
                                    SeverityModerate = "No";
                                }
                            } else if (op_non_hvc_mod.equalsIgnoreCase("<") && op_hvc_mod.equalsIgnoreCase(">")) {
                                if (totalImpact < Integer.valueOf(non_hvc_mod) && ((jumlahKATHVCContainingHVC / totalImpact) * 100) > Float.valueOf(hvc_mod)) {
                                    LogUtil.info(pluginClassName, "H");
                                    SeverityModerate = "Yes";
                                } else {
                                    LogUtil.info(pluginClassName, "EH");
                                    SeverityModerate = "No";
                                }
                            } else if (op_non_hvc_mod.equalsIgnoreCase(">") && op_hvc_mod.equalsIgnoreCase("<")) {
                                if (totalImpact > Integer.valueOf(non_hvc_mod) && ((jumlahKATHVCContainingHVC / totalImpact) * 100) < Float.valueOf(hvc_mod)) {
                                    LogUtil.info(pluginClassName, "I");
                                    SeverityModerate = "Yes";
                                } else {
                                    LogUtil.info(pluginClassName, "EI");
                                    SeverityModerate = "No";
                                }
                            } else if (op_non_hvc_mod.equalsIgnoreCase("-") && op_hvc_mod.equalsIgnoreCase("<")) {
                                if ((totalImpact > Integer.valueOf(split_non_hvc_mod1) && totalImpact < Integer.valueOf(split_non_hvc_mod2)) && ((jumlahKATHVCContainingHVC / totalImpact) * 100) < Float.valueOf(hvc_mod)) {
//                                    LogUtil.info(pluginClassName, "mod1: "+split_non_hvc_mod1);
//                                    LogUtil.info(pluginClassName, "mod2: "+split_non_hvc_mod2);
                                    LogUtil.info(pluginClassName, "J");
                                    SeverityModerate = "Yes";
                                } else {
                                    LogUtil.info(pluginClassName, "EJ");
                                    SeverityModerate = "No";
                                }
                            } else {
                                LogUtil.info(pluginClassName, "Z");
                                SeverityModerate = "No";
                            }
                        } catch (Exception e) {
                            LogUtil.error(pluginClassName, e, e.getMessage());
                        }

                        hasilAPI.put("total_impact", totalImpact);
                        hasilAPI.put("total_hvc", jumlahKATHVCContainingHVC);
                        hasilAPI.put("total_nonhvc", jumlahKATHVCNotContainingHVC);

                        String severity = "Belum diketahui";

                        if (SeverityMinor.equalsIgnoreCase("Yes")) {
                            severity = SeverityModerate.equalsIgnoreCase("Yes") ? "Moderate" : "Minor";
                        } else {
                            severity = SeverityModerate.equalsIgnoreCase("Yes") ? "Moderate" : "Belum diketahui";
                        }

                        hasilAPI.put("severity", severity);
                        hasil = hasilAPI.toString();
//                        LogUtil.info(pluginClassName, "hasil: " + hasilAPI.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
//                    LogUtil.info(pluginClassName, "Response API Total Impact: " + totalImpact);
                    hasil = "Data tidak ditemukan";
                }

            } else {
//                LogUtil.info(pluginClassName, "HTTP Error: " + responseCode);
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
}
