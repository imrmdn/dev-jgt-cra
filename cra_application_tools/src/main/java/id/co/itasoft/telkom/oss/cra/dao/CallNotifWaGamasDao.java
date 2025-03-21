/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra.dao;

import id.co.itasoft.telkom.oss.cra.model.LogHistoryModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.UuidGenerator;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class CallNotifWaGamasDao {

    private final String pluginClassName = this.getClass().getName();

    // Method untuk mendapatkan access token
    public String getAccessToken() throws IOException, JSONException {
        List<String> getCreds = getListCred();
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
//        String clientId = "735235b2-7aa1-4264-a054-83109bc1d67f";
//        String clientSecret = "d4693b1c-a324-421a-be67-1e8d3a765245";
//        String getTokenUrl = "https://apigwsit.telkom.co.id:7777/invoke/pub.apigateway.oauth2/getAccessToken";


        URL url = new URL(getTokenUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        String postData = "client_id=" + clientId + "&client_secret=" + clientSecret + "&grant_type=client_credentials";

        try ( OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try ( BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getString("access_token");
        } finally {
            connection.disconnect();
        }
    }

    // Method untuk memanggil API dengan bearer token
    public void callApi(String accessToken, String tanggal, String waktu, String craId, String phoneNumber)
            throws IOException, JSONException {
        String urlVal = getUrlCallWA();
//        String apiUrl = "https://apigwsit.telkom.co.id:7777/ws/telkom-nossa-whatsapp/1.0/notificationWA";
        String apiUrl = urlVal;
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject requestBody = new JSONObject();
        JSONObject notificationWA = new JSONObject();
        JSONObject input = new JSONObject();
        JSONObject templateData = new JSONObject();

        templateData.put("data1", tanggal);
        templateData.put("data2", waktu + " WIB");
        templateData.put("data3", "#");
        templateData.put("data4", "#");
        templateData.put("data5", "#");
        templateData.put("data6", "#");
        templateData.put("data7", "#");
        templateData.put("data8", "#");
        templateData.put("data9", "#");
        templateData.put("data10", "#");

        input.put("ticketID", craId);
        input.put("phone", phoneNumber + "123test");
        input.put("source", "GAMASREG");
        input.put("templateID", "gmstr_terencana");
        input.put("templateData", templateData);

        notificationWA.put("input", input);
        requestBody.put("notificationWA", notificationWA);

        try ( OutputStream os = connection.getOutputStream()) {
            byte[] inputBytes = requestBody.toString().getBytes("utf-8");
            os.write(inputBytes, 0, inputBytes.length);
        }
        int responseCode = connection.getResponseCode();

        try ( BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            // Handle response jika diperlukan
            LogUtil.info("NotifWADao", "response: " + response.toString());
            LogHistoryModel lh = new LogHistoryModel();
            lh.setRequest(requestBody.toString());
            lh.setMethod("POST");
            lh.setResponse(response.toString());
            lh.setAction("Call Notif WA" + phoneNumber);
            lh.setUrl(apiUrl);
            lh.setTicketId(craId);
            lh.setStatus(String.valueOf(responseCode));
            try {
                insertToLogHistory(lh);
            } catch (Exception ex) {
                Logger.getLogger(CallNotifWADao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            connection.disconnect();
        }
    }

    public void insertToLogHistory(LogHistoryModel lh) throws Exception {
        boolean result = false;
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder query = new StringBuilder();
        con = ds.getConnection();

        query
                .append(" INSERT INTO app_fd_api_response ")
                .append(" ( ")
                .append(" id, ")
                .append(" dateCreated, ")
                .append(" dateModified, ")
                .append(" createdBy, ")
                .append(" createdByName, ")
                .append(" modifiedBy, ")
                .append(" modifiedByName, ")
                .append(" c_request, ")
                .append(" c_method, ")
                .append(" c_response, ")
                .append(" c_action, ")
                .append(" c_url, ")
                .append(" c_cra_id, ")
                .append(" c_status ")
                .append(" ) VALUES (SYS_GUID(),SYSDATE,SYSDATE,?,?,?,?,?,?,?,?,?,?,?) ");

        try {
            ps = con.prepareStatement(query.toString());
            UuidGenerator uuid = UuidGenerator.getInstance();
            ps.setString(1, "");//createdBy
            ps.setString(2, "");//createdByName
            ps.setString(3, "");//modifiedBy
            ps.setString(4, "");//modifiedByName
            ps.setString(5, lh.getRequest());
            ps.setString(6, lh.getMethod());
            ps.setString(7, lh.getResponse());
            ps.setString(8, lh.getAction());
            ps.setString(9, lh.getUrl());
            ps.setString(10, lh.getTicketId());
            ps.setString(11, lh.getStatus());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LogUtil.info(getClass().getName(), ex.getMessage());
        } finally {
            query = null;
            lh = null;
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                LogUtil.info(getClass().getName(), ex.getMessage());
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.info(getClass().getName(), ex.getMessage());
            }
        }

    }

    public List<String> getPhoneAct(String act_id) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> phoneActs = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "SELECT a.C_TEMP_CLOB_COLUMN, d.c_cra_id, "
                    + "concat(concat(a.c_berakhir, ' '), TO_CHAR(TO_DATE(c_waktu_selesai, 'HH24:MI'), 'HH24:MI')) as waktu_pelaksanaan, "
                    + "a.c_berakhir as awal, TO_CHAR(TO_DATE(c_waktu_selesai, 'HH24:MI'), 'HH24:MI') as waktu_start "
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

    public List<String> getListCred() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_url, c_client_id, c_client_secret from app_fd_m_token_api where c_type = 'token_call_notif_wa'";
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

    public String getUrlCallWA() {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select c_url from app_fd_m_token_api \n"
                        + "where c_type = 'call_notif_wa'";
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
