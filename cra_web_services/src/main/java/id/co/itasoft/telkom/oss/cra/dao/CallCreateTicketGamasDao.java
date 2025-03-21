/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra.dao;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class CallCreateTicketGamasDao {

    String pluginClassName = this.getClass().getName();

    public String callCreateTicket(String processId) {
        String hasil = "";
        List<ArrayList> listCred = getCredentialTicketGamas();
        ArrayList credArrayList = listCred.get(0);
        List<ArrayList> listAct = getInfra(processId);
        ArrayList actArrayList = listAct.get(0);
        String api_id = String.valueOf(credArrayList.get(0));
        String api_key = String.valueOf(credArrayList.get(1));
        String urlCred = String.valueOf(credArrayList.get(2));
        String infra = String.valueOf(actArrayList.get(0));
        String act_id = String.valueOf(actArrayList.get(1));
        String exec = String.valueOf(actArrayList.get(2));
        String hp_exec = String.valueOf(actArrayList.get(3));
        String selisih_jam = String.valueOf(actArrayList.get(4));
        String workzone = String.valueOf(actArrayList.get(5));
        String judul = getJudulCRA(act_id);
        String symptom = getSymptom(act_id);
//        String infra = getInfra(processId);
        LogUtil.info(pluginClassName, "INFRA: " + infra);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlCred);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("api_id", api_id);
            conn.setRequestProperty("api_key", api_key);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String summary = "[CRA][" + infra + "] " + judul;
            String contact_name = exec;
            String contact_phone = hp_exec;
            String channel = "62";
            String customer_segment = null;
            String estimation = selisih_jam;
            String source_ticket = "PROACTIVE";
            String perangkat = infra;
            String service_id = null;
            String work_zone = workzone;
            String reported_by = "CRA";
            String classification_path = symptom;
            String details = judul;
            String scid = null;

            String bodyJSON = "{ \"summary\": \"" + summary + "\", "
                    + "\"contact_name\": \"" + contact_name + "\", "
                    + "\"contact_phone\": \"" + contact_phone + "\", "
                    + "\"channel\": \"" + channel + "\", "
                    + "\"customer_segment\": " + customer_segment + ", "
                    + "\"estimation\": \"" + estimation + "\", "
                    + "\"source_ticket\": \"" + source_ticket + "\", "
                    + "\"perangkat\": \"" + perangkat + "\", "
                    + "\"service_id\": " + service_id + ", "
                    + "\"work_zone\": " + work_zone + ", "
                    + "\"reported_by\": \"" + reported_by + "\", "
                    + "\"classification_path\": \"" + classification_path + "\", "
                    + "\"details\": \"" + details + "\", "
                    + "\"scid\": " + scid + "}";

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(bodyJSON);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            LogUtil.info(pluginClassName, "Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            LogUtil.info(pluginClassName, "Response: " + response.toString());
            JSONObject jsonResponse = new JSONObject(response.toString());

            if (jsonResponse.has("data")) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data.has("ticket_id")) {
                    hasil = data.getString("ticket_id");
                    String processIdTicketGamas = data.getString("process_id");
                    LogUtil.info(pluginClassName, "Ticket GAMAS " + hasil);
                    updateTicket(processId, hasil, processIdTicketGamas);
                } else {
                    hasil = "Gagal";
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect(); // Menutup koneksi setelah digunakan
            }
        }
        return hasil;
    }

    public List<ArrayList> getCredentialTicketGamas() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ArrayList> hasilList = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_api_id, c_api_key, c_url from app_fd_m_api where UPPER(c_desc) = UPPER('Call Create Ticket GAMAS')";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                String api_id = rs.getString("c_api_id");
                String api_key = rs.getString("c_api_key");
                String url = rs.getString("c_url");

                ArrayList<String> rowValues = new ArrayList<>();
                rowValues.add(api_id);
                rowValues.add(api_key);
                rowValues.add(url);

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

    public boolean updateTicket(String processId, String ticket, String procIdTicketGamas) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ds.getConnection();
            ps = con.prepareStatement(UpdateQuery());

            ps.setString(1, ticket);
            ps.setString(2, procIdTicketGamas);
            ps.setString(3, processId);
            // Jalankan operasi update dan periksa jumlah baris yang terpengaruh
            int rowsUpdated = ps.executeUpdate();

            // Mengembalikan true jika setidaknya satu baris diupdate, sebaliknya false
            return rowsUpdated > 0;
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
        return false; // Mengembalikan false hanya jika ada pengecualian SQL
    }

    private String UpdateQuery() {
        return "UPDATE app_fd_create_ticket_gamas SET c_ticket_gamas = ?, c_status_ticket = 'New', c_process_id_ticket = ? WHERE id = ? ";
    }

    public List<ArrayList> getInfra(String recordId) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ArrayList> hasilList = new ArrayList<>();

        try {
            con = ds.getConnection();
//            String query = "SELECT c_infrastruktur, c_activity_id, c_executor, c_no_hp_executor, \n"
//                    + "  CEIL((EXTRACT(HOUR FROM (TO_TIMESTAMP(c_waktu_selesai, 'HH:MI PM') - TO_TIMESTAMP(c_waktu_start, 'HH:MI PM'))) * 60 + \n"
//                    + "         EXTRACT(MINUTE FROM (TO_TIMESTAMP(c_waktu_selesai, 'HH:MI PM') - TO_TIMESTAMP(c_waktu_start, 'HH:MI PM')))) / 60) AS selisih_jam, c_sto_code \n"
//                    + " FROM APP_FD_CRA_CREATE_ACT \n"
//                    + " where id = (select c_activity_id from app_fd_create_ticket_gamas where id = ?) and rownum <=1";
            String query = "SELECT c_infrastruktur, c_activity_id, c_executor, c_no_hp_executor, \n"
                    + "  CEIL(EXTRACT(HOUR FROM (TO_TIMESTAMP(c_waktu_selesai, 'HH24:MI') - TO_TIMESTAMP(c_waktu_start, 'HH24:MI')))) AS selisih_jam, c_sto_code \n"
                    + " FROM APP_FD_CRA_CREATE_ACT \n"
                    + " where id = (select c_activity_id from app_fd_create_ticket_gamas where id = ?) and rownum <=1";
            ps = con.prepareStatement(query);
            ps.setString(1, recordId);

            rs = ps.executeQuery();
            while (rs.next()) {
                String infra = rs.getString("c_infrastruktur");
                String act_id = rs.getString("c_activity_id");
                String exec = rs.getString("c_executor");
                String hp_exec = rs.getString("c_no_hp_executor");
                String selisih_jam = rs.getString("selisih_jam");
                String c_sto_code = rs.getString("c_sto_code");

                ArrayList<String> rowValues = new ArrayList<>();
                rowValues.add(infra);
                rowValues.add(act_id);
                rowValues.add(exec);
                rowValues.add(hp_exec);
                rowValues.add(selisih_jam);
                rowValues.add(c_sto_code);

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

    public String getJudulCRA(String id) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "SELECT\n"
                        + " a.C_JUDUL \n"
                        + "FROM\n"
                        + " APP_FD_CRA_CREATE_T2 a\n"
                        + "INNER JOIN APP_FD_CRA_CREATE b ON a.C_PARENT_ID = b.ID\n"
                        + "INNER JOIN APP_FD_CRA_CREATE_T4 c ON b.ID = c.C_PARENT_ID\n"
                        + "INNER JOIN APP_FD_CRA_CREATE_ACT d ON c.ID = d.C_FK\n"
                        + "WHERE d.C_ACTIVITY_ID = ?";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, id);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("C_JUDUL");
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

    public String getJamNotifWa(String id) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "SELECT \n"
                        //                        + "    CEIL(EXTRACT(DAY FROM (TO_TIMESTAMP(c_awal || ' ' || c_waktu_start || ':00', 'YYYY-MM-DD HH24:MI:SS') - CURRENT_TIMESTAMP - INTERVAL '1' HOUR)) * 24) -1 AS notifWa\n"
                        + "    (EXTRACT(DAY FROM (TO_TIMESTAMP(c_awal || ' ' || c_waktu_start, 'YYYY-MM-DD HH24:MI') - SYSTIMESTAMP)) * 24 * 60) +\n"
                        + " (EXTRACT(HOUR FROM (TO_TIMESTAMP(c_awal || ' ' || c_waktu_start, 'YYYY-MM-DD HH24:MI') - SYSTIMESTAMP)) * 60) +\n"
                        + " (EXTRACT(MINUTE FROM (TO_TIMESTAMP(c_awal || ' ' || c_waktu_start, 'YYYY-MM-DD HH24:MI') - SYSTIMESTAMP))) AS notifWa \n"
                        + " FROM \n"
                        + "    APP_FD_CRA_CREATE_ACT \n"
                        + " WHERE \n"
                        + "    id = (SELECT c_activity_id FROM app_fd_create_ticket_gamas WHERE id = ?) \n"
                        + "    AND ROWNUM <= 1";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, id);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("notifWa");
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

    public String getSymptom(String id) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "SELECT \n"
                        + "e.c_symptom \n"
                        + "FROM \n"
                        + " APP_FD_CRA_CREATE_T1 a \n"
                        + "INNER JOIN APP_FD_CRA_CREATE b ON a.C_PARENT_ID = b.ID \n"
                        + "INNER JOIN APP_FD_CRA_CREATE_T4 c ON b.ID = c.C_PARENT_ID \n"
                        + "INNER JOIN APP_FD_CRA_CREATE_ACT d ON c.ID = d.C_FK \n"
                        + "LEFT JOIN APP_FD_M_CRQ_DOMAIN e ON e.id = a.c_network_element \n"
                        + "WHERE d.C_ACTIVITY_ID = ?";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, id);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_symptom");
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
