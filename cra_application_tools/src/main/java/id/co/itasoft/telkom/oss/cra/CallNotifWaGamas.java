/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.CallNotifWaGamasDao;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.workflow.model.WorkflowAssignment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class CallNotifWaGamas extends DefaultApplicationPlugin {

    private String pluginName = "Telkom - CRA - Call Notif WA TSEL - GAMAS";
    private String pluginClassName = this.getClass().getName();

    @Override
    public Object execute(Map map) {
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        WorkflowAssignment workflowAssignment = (WorkflowAssignment) map.get("workflowAssignment");
        String processId = appService.getOriginProcessId(workflowAssignment.getProcessId());
        LogUtil.info(this.getClass().getName(), "Process ID: " + processId);
        String act_id = getCraId(processId);
        String impact_tsel = getImpactTsel(processId);

        CallNotifWaGamasDao callNotifWaDao = new CallNotifWaGamasDao();
        List<String> phoneActs = getPhoneAct(act_id);
        LogUtil.info(pluginClassName, "phoneActs: " + phoneActs);
        StringBuilder resultStringBuilderSpecified = new StringBuilder();
        StringBuilder resultStringBuilderOther = new StringBuilder();
        StringBuilder telkomselStringBuilder = new StringBuilder();
        JSONArray otherJsonArray = new JSONArray();
        Set<String> uniquePhoneNumbers = new HashSet<>();

// Process the results
        if (impact_tsel.equalsIgnoreCase("yes")) {
            if (phoneActs.isEmpty()) {
                // Handle the case where phoneActs is empty
            } else {
//                JSONArray otherJsonArray = new JSONArray();
//                StringBuilder telkomselStringBuilder = new StringBuilder();

                for (String phoneAct : phoneActs) {
                    try {
                        JSONObject jsonObject = new JSONObject(phoneAct);
                        String clobColumnString = jsonObject.getString("C_TEMP_CLOB_COLUMN");
                        String cCraId = jsonObject.getString("c_cra_id");
                        String waktuPelaksanaan = jsonObject.getString("waktu_pelaksanaan");
                        String waktu_start = jsonObject.getString("waktu_start");
                        String awal = jsonObject.getString("awal");

                        // Parse the first level JSON array within "C_TEMP_CLOB_COLUMN"
                        JSONArray phoneArrayFirstLevel = new JSONArray(clobColumnString);

                        for (int i = 0; i < phoneArrayFirstLevel.length(); i++) {
                            JSONObject phoneObject = phoneArrayFirstLevel.getJSONObject(i);
                            String phoneNumber = phoneObject.getString("phoneNumber");

                            // Check if phoneNumber is not empty
                            if (!phoneNumber.isEmpty()) {
                                // Replace "0" with "+62" in phone numbers
                                if (phoneNumber.startsWith("0")) {
                                    phoneNumber = "+62" + phoneNumber.substring(1);
                                }

                                // Check if phoneNumber is not in the set, then add it to the set and process it
                                if (uniquePhoneNumbers.add(phoneNumber)) {
                                    // Categorize phone numbers based on prefixes
//                                if (!phoneNumber.startsWith("+62811")
//                                        && !phoneNumber.startsWith("+62812")
//                                        && !phoneNumber.startsWith("+62813")
//                                        && !phoneNumber.startsWith("+62821")
//                                        && !phoneNumber.startsWith("+62822")
//                                        && !phoneNumber.startsWith("+62823")
//                                        && !phoneNumber.startsWith("+62852")
//                                        && !phoneNumber.startsWith("+62853")
//                                        && !phoneNumber.startsWith("+62851")) {
//                                    JSONObject entry = new JSONObject();
//                                    entry.put("phoneNumber", phoneNumber);
//                                    entry.put("cra_id", cCraId);
//                                    entry.put("waktu_pelaksanaan", waktuPelaksanaan);
//                                    otherJsonArray.put(entry);
//                                } else {
                                    // Append Telkomsel numbers to the StringBuilder
                                    telkomselStringBuilder.append("phoneNumber: ").append(phoneNumber)
                                            .append(", cra_id: ").append(cCraId)
                                            .append(", waktu_pelaksanaan: ").append(waktuPelaksanaan)
                                            .append("\n");
                                    LogUtil.info(this.getClass().getName(), "TOken: " + callNotifWaDao.getAccessToken());

                                    callNotifWaDao.callApi(callNotifWaDao.getAccessToken(), awal, waktu_start, cCraId, phoneNumber);
//                                }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // Log the exception or print the stack trace for debugging
                        e.printStackTrace();
                    } catch (IOException ex) {
                        Logger.getLogger(CallNotifWA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        } else {
            LogUtil.info(pluginName, "Pengiriman WA tidak dilaksanakan karena bukan Impact TSEL");
        }

        return null;
    }

    @Override
    public String getName() {
        return pluginName;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return pluginName;
    }

    @Override
    public String getLabel() {
        return pluginName;
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }

    public String getCraId(String recordId) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = "";

        try {
            con = ds.getConnection();
            String query = "select c_activity_id from app_fd_cra_create_act where id = (select c_activity_id from app_fd_create_ticket_gamas where id = ?)";
            ps = con.prepareStatement(query);
            ps.setString(1, recordId);

            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("c_activity_id");
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
        return result;
    }

    public String getImpactTsel(String recordId) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = "";

        try {
            con = ds.getConnection();
            String query = "SELECT \n"
                    + "    c_impact_service \n"
                    + " FROM \n"
                    + "    APP_FD_CRA_CREATE_ACT \n"
                    + " WHERE \n"
                    + "    id = (SELECT c_activity_id FROM app_fd_create_ticket_gamas where id = ?)";
            ps = con.prepareStatement(query);
            ps.setString(1, recordId);

            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("c_impact_service");
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
        return result;
    }
//test

    public List<String> getPhoneAct(String act_id) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> phoneActs = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "SELECT a.C_TEMP_CLOB_COLUMN, d.c_cra_id, "
                    + "concat(concat(a.c_berakhir, ' '), TO_CHAR(TO_DATE(c_waktu_selesai, 'HH:MI AM'), 'HH24:MI')) as waktu_pelaksanaan, "
                    + "REGEXP_REPLACE(TO_CHAR(TO_DATE(a.c_berakhir, 'YYYY-MM-DD'), 'DD Month YYYY', 'NLS_DATE_LANGUAGE = Indonesian'), '\\s+', ' ') as awal, TO_CHAR(TO_DATE(c_waktu_selesai, 'HH:MI AM'), 'HH24:MI') as waktu_start "
                    + "FROM APP_FD_CRA_CREATE_ACT a "
                    + "INNER JOIN APP_FD_CRA_CREATE_T4 b on a.C_FK = b.ID "
                    + "INNER JOIN APP_FD_CRA_CREATE c on b.c_parent_id = c.id "
                    + "INNER JOIN APP_FD_CRA_CREATE_T1 d on c.id = d.c_parent_id "
                    + "WHERE a.c_activity_id = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, act_id);

            rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("C_TEMP_CLOB_COLUMN", rs.getString("C_TEMP_CLOB_COLUMN"));
                jsonObject.put("c_cra_id", rs.getString("c_cra_id"));
                jsonObject.put("waktu_pelaksanaan", rs.getString("waktu_pelaksanaan"));
                jsonObject.put("waktu_start", rs.getString("waktu_start"));
                jsonObject.put("awal", rs.getString("awal"));

                phoneActs.add(jsonObject.toString());
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
        return phoneActs;
    }

//    private static String getAccessToken() throws Exception {
//        String clientId = "735235b2-7aa1-4264-a054-83109bc1d67f";
//        String clientSecret = "d4693b1c-a324-421a-be67-1e8d3a765245";
//        String tokenUrl = "https://apigwsit.telkom.co.id:7777/invoke/pub.apigateway.oauth2/getAccessToken";
//
//        URL url = new URL(tokenUrl);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        connection.setDoOutput(true);
//
//        // Construct the request body for POST
//        String requestBody = "client_id=" + clientId
//                + "&client_secret=" + clientSecret
//                + "&grant_type=client_credentials";
//
//        try ( OutputStream os = connection.getOutputStream()) {
//            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
//            os.write(input, 0, input.length);
//        }
//
//        int responseCode = connection.getResponseCode();
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            String response = UtilsGamas.readResponse(connection);
//
//            String accessToken = response.split("\"access_token\"")[1].split("\"")[1];
//            LogUtil.info("TOKEN: ", response);
//            return accessToken;
//        } else {
//            throw new RuntimeException("Failed to get access token. Response code: " + responseCode);
//        }
//    }
}

class UtilsGamas {

    static String readResponse(HttpURLConnection connection) throws Exception {
        StringBuilder response = new StringBuilder();
        try ( java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return response.toString();
    }
}
