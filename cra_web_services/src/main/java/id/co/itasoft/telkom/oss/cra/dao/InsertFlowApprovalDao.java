/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import jdk.tools.jlink.resources.plugins;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.UuidGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class InsertFlowApprovalDao {

    public void insertToFlowApproval(String user) throws Exception {
        boolean result = false;
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder query = new StringBuilder();
        con = ds.getConnection();
        List<String> listMgr = getAtasanManager(user);
        if (listMgr.isEmpty()) {
            LogUtil.info(this.getClass().getName(), "Data User False: " + listMgr.size());
            result = false;
        } else {
            LogUtil.info(this.getClass().getName(), "Data User True: " + listMgr.size());

            query
                    .append(" INSERT INTO APP_FD_CRA_FLOW_APPROVAL ")
                    .append(" ( ")
                    .append(" id, ")
                    .append(" dateCreated, ")
                    .append(" dateModified, ")
                    .append(" createdBy, ")
                    .append(" createdByName, ")
                    .append(" modifiedBy, ")
                    .append(" modifiedByName, ")
                    .append(" c_drafter, ")
                    .append(" c_manager_terkait, ")
                    .append(" c_band_pengaju ")
                    .append(" ) VALUES (SYS_GUID(),SYSDATE,SYSDATE,?,?,?,?,?,?,?) ");

            try {
//            List<String> listMgr = getAtasanManager(user);
                String val_nm_mgr_trk = "";
                String val_jb_mgr_trk = "";
                String val_fn_user = "";
                String val_ln_user = "";
                String val_jb_user = "";
                if (listMgr.size() > 3) {
                    val_nm_mgr_trk = listMgr.get(0);
                    val_jb_mgr_trk = listMgr.get(1);
                    val_fn_user = listMgr.get(2);
                    val_ln_user = listMgr.get(3);
                    val_jb_user = listMgr.get(4);
                } else {
                    val_fn_user = listMgr.get(0);
                    val_ln_user = listMgr.get(1);
                    val_jb_user = listMgr.get(2);
                }

                String val_nm_band = "";
                String val_jb_band = "";
                String val_fn_userband = "";
                String val_ln_userband = "";
                String val_jb_userband = "";

                if (!val_nm_mgr_trk.isEmpty()) {
                    List<String> listBandPengaju = getAtasanManager(val_nm_mgr_trk);
                    if (listBandPengaju.size() > 3) {
                        val_nm_band = listBandPengaju.get(0);
                        val_jb_band = listBandPengaju.get(1);
                        val_fn_userband = listBandPengaju.get(2);
                        val_ln_userband = listBandPengaju.get(3);
                        val_jb_userband = listBandPengaju.get(4);
                    } else {
                        val_fn_userband = listBandPengaju.get(0);
                        val_ln_userband = listBandPengaju.get(1);
                        val_jb_userband = listBandPengaju.get(2);
                    }
                }

                ps = con.prepareStatement(query.toString());
                UuidGenerator uuid = UuidGenerator.getInstance();
                ps.setString(1, "");//createdBy
                ps.setString(2, "");//createdByName
                ps.setString(3, "");//modifiedBy
                ps.setString(4, "");//modifiedByName
                ps.setString(5, user + ";" + val_fn_user + " " + val_ln_user + ";" + val_jb_user);
                ps.setString(6, val_nm_mgr_trk + ";" + val_nm_mgr_trk + ";" + val_jb_mgr_trk);
                ps.setString(7, val_nm_band + ";" + val_nm_band + ";" + val_jb_band);
                ps.executeUpdate();
            } catch (SQLException ex) {
                LogUtil.info(getClass().getName(), ex.getMessage());
            } finally {
                query = null;
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

    }

    public void updateFlowApproval(String user) throws Exception {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder();
        con = ds.getConnection();

        query
                .append("UPDATE APP_FD_CRA_FLOW_APPROVAL ")
                .append("SET ")
                .append("dateModified = SYSDATE, ")
                .append("modifiedBy = ?, ")
                .append("modifiedByName = ?, ")
                .append("c_drafter = ?, ")
                .append("c_manager_terkait = ?, ")
                .append("c_band_pengaju = ? ")
                .append("WHERE SUBSTR(c_drafter, 1, INSTR(c_drafter, ';', 1, 1) - 1) = ?");

        try {
            List<String> listMgr = getAtasanManager(user);
            LogUtil.info(this.getClass().getName(), "Data User True: " + listMgr.size());
            LogUtil.info(this.getClass().getName(), "Data Username: " + listMgr.toString());
            String val_nm_mgr_trk = "";
            String val_jb_mgr_trk = "";
            String val_fn_user = "";
            String val_ln_user = "";
            String val_jb_user = "";
            String val_user_ori = "";
            if (listMgr.size() > 3) {
                val_nm_mgr_trk = listMgr.get(0);
                val_jb_mgr_trk = listMgr.get(1);
                val_fn_user = listMgr.get(2);
                val_ln_user = listMgr.get(3);
                val_jb_user = listMgr.get(4);
                val_user_ori = listMgr.get(5);
            } else {
                val_fn_user = listMgr.get(0);
                val_ln_user = listMgr.get(1);
                val_jb_user = listMgr.get(2);
            }

            String val_nm_band = "";
            String val_jb_band = "";
            String val_fn_userband = "";
            String val_ln_userband = "";
            String val_jb_userband = "";

            if (!val_nm_mgr_trk.isEmpty()) {
                LogUtil.info("InsertFlowDao", "ini user param getAtasanManager " + val_user_ori);
                List<String> listBandPengaju = getBandPengaju(val_user_ori);
                if (listBandPengaju.size() > 3) {
                    val_nm_band = listBandPengaju.get(0);
                    val_jb_band = listBandPengaju.get(1);
                    val_fn_userband = listBandPengaju.get(2);
                    val_ln_userband = listBandPengaju.get(3);
                    val_jb_userband = listBandPengaju.get(4);
                } else {
                    val_fn_userband = listBandPengaju.get(0);
                    val_ln_userband = listBandPengaju.get(1);
                    val_jb_userband = listBandPengaju.get(2);
                }
            }

            ps = con.prepareStatement(query.toString());
            ps.setString(1, ""); // modifiedBy
            ps.setString(2, ""); // modifiedByName
            ps.setString(3, user + ";" + val_fn_user + " " + val_ln_user + ";" + val_jb_user); // c_drafter
            ps.setString(4, val_nm_mgr_trk + ";" + val_nm_mgr_trk + ";" + val_jb_mgr_trk); // c_manager_terkait
            ps.setString(5, val_nm_band + ";" + val_nm_band + ";" + val_jb_band); // c_band_pengaju
            ps.setString(6, user); // c_drafter (where clause)

            ps.executeUpdate();
        } catch (SQLException ex) {
            LogUtil.info(getClass().getName(), ex.getMessage());
        } finally {
            query = null;
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

    public boolean CekValidasiApproval(String username) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            con = ds.getConnection();
            String query = "SELECT COUNT(*) FROM APP_FD_CRA_FLOW_APPROVAL WHERE SUBSTR(c_drafter, 1, INSTR(c_drafter, ';', 1, 1) - 1) = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, username);
            LogUtil.info(this.getClass().getName(), "Username: " + username);
            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                exists = count > 0;
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

        return exists;
    }

    public void insertToGroupDomain(String domain) throws Exception {
        String personInfo = getGroupTembusan();
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder query = new StringBuilder();

        try {
            con = ds.getConnection();
            query.append("INSERT INTO app_fd_cra_user_group ")
                    .append("(id, dateCreated, dateModified, createdBy, createdByName, modifiedBy, modifiedByName, c_domain, c_username, c_first_name, c_last_name, c_jabatan) ")
                    .append("VALUES (SYS_GUID(), SYSDATE, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            ps = con.prepareStatement(query.toString());

            String[] lines = personInfo.split("\n");
            for (int i = 0; i < lines.length; i += 5) {
                String username = lines[i].substring(lines[i].indexOf(":") + 2);
                String firstName = lines[i + 1].substring(lines[i + 1].indexOf(":") + 2);
                String lastName = lines[i + 2].substring(lines[i + 2].indexOf(":") + 2);
                String jabatan = lines[i + 3].substring(lines[i + 3].indexOf(":") + 2);

                ps.setString(1, ""); // createdBy
                ps.setString(2, ""); // createdByName
                ps.setString(3, ""); // modifiedBy
                ps.setString(4, ""); // modifiedByName
                ps.setString(5, domain);
                ps.setString(6, username);
                ps.setString(7, firstName);
                ps.setString(8, lastName);
                ps.setString(9, jabatan);

                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            LogUtil.info(getClass().getName(), ex.getMessage());
        } finally {
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

    public void updateToGroupDomain(String domain) throws Exception {
        String personInfo = getGroupTembusan();
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder query = new StringBuilder();

        try {
            con = ds.getConnection();
            query.append("UPDATE app_fd_cra_user_group ")
                    .append("SET dateModified = SYSDATE, modifiedBy = ?, modifiedByName = ?, c_username = ?, c_first_name = ?, c_last_name = ?, c_jabatan = ? ")
                    .append("WHERE c_domain = ?");

            ps = con.prepareStatement(query.toString());

            String[] lines = personInfo.split("\n");
            for (int i = 0; i < lines.length; i += 5) {
                String username = lines[i].substring(lines[i].indexOf(":") + 2);
                String firstName = lines[i + 1].substring(lines[i + 1].indexOf(":") + 2);
                String lastName = lines[i + 2].substring(lines[i + 2].indexOf(":") + 2);
                String jabatan = lines[i + 3].substring(lines[i + 3].indexOf(":") + 2);

                ps.setString(1, ""); // modifiedBy
                ps.setString(2, ""); // modifiedByName
                ps.setString(3, username);
                ps.setString(4, firstName);
                ps.setString(5, lastName);
                ps.setString(6, jabatan);
                ps.setString(7, domain);

                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            LogUtil.info(getClass().getName(), ex.getMessage());
        } finally {
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

    public String getGroupTembusan() {
        String apiUrl = "https://person-dev-insera-dev.apps.cpaasstl1.telkom.co.id/jw/web/json/plugin/co.id.telkom.person.plugin.GetPersonByPersonGroup/service";
        String apiId = "API-3154b489-51b7-497e-89bc-a0d44778f4de";
        String apiKey = "108692c1db6b462b9027e442ce8e6c26";
        String result = "";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("api_id", apiId);
            conn.setRequestProperty("api_key", apiKey);
            conn.setDoOutput(true);

            String bodyJson = "{\"domain\":\"AKSES\"}";

            conn.getOutputStream().write(bodyJson.getBytes());

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray dataArray = jsonResponse.getJSONArray("Data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject personObject = dataArray.getJSONObject(i);
                String personCode = personObject.getString("Person code");
                String firstName = personObject.getString("First Name");
                String lastName = personObject.getString("Last Name");
                String jabatan = personObject.getString("Jabatan");

                result += "Person Code: " + personCode + "\n";
                result += "First Name: " + firstName + "\n";
                result += "Last Name: " + lastName + "\n";
                result += "Jabatan: " + jabatan + "\n\n";
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean CekValidasiGroupTembusan(String domain) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            con = ds.getConnection();
            String query = "SELECT COUNT(*) FROM app_fd_cra_user_group WHERE c_domain = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, domain);
            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                exists = count > 0;
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

        return exists;
    }

     public List<String> getAtasanManager(String user) {
        // Pengaturan URL dan parameter API
        String apiUrl = "https://person-dev-insera-dev.apps.cpaasstl1.telkom.co.id/jw/web/json/plugin/co.id.telkom.person.plugin.GetInfoJabatan/service";
        String delegationUrl = "https://person-dev-insera-dev.apps.cpaasstl1.telkom.co.id/jw/api/list/getDelegation";
        String personCode = user;
        String apiId = "API-3154b489-51b7-497e-89bc-a0d44778f4de";
        String apiKey = "108692c1db6b462b9027e442ce8e6c26";
        List<String> result = new ArrayList<>();

        try {
            // Membuat koneksi HTTP
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("api_id", apiId);
            conn.setRequestProperty("api_key", apiKey);
            conn.setDoOutput(true);

            // Membuat body JSON
            String bodyJson = "{\"person_code\":\"" + personCode + "\"}";

            conn.getOutputStream().write(bodyJson.getBytes());

            // Membaca respons
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }

            // Memproses respons JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray infoArray = jsonResponse.getJSONArray("Info");
            JSONObject infoObject = infoArray.getJSONObject(0);

            LogUtil.info(this.getClass().getName(), "Data Info Object: " + infoObject.toString());

            if (infoObject.getString("Status Atasan").equalsIgnoreCase("ON LEAVE")) {
                LogUtil.info(this.getClass().getName(), "Masuk Kondisi Cuti");
                try {
                    // Membuat koneksi HTTP untuk pemanggilan API delegation
                    String jabatanAtasan = infoObject.getString("Jabatan Atasan").toString();
                    jabatanAtasan = jabatanAtasan.replaceAll(" ", "%20");
                    URL delegationApiUrl = new URL("https://person-dev-insera-dev.apps.cpaasstl1.telkom.co.id/jw/api/list/getDelegation?jabatan_atasan=" + jabatanAtasan);
                    HttpURLConnection connD = (HttpURLConnection) delegationApiUrl.openConnection();
                    connD.setRequestMethod("GET");
                    connD.setRequestProperty("Content-Type", "application/json");
                    connD.setRequestProperty("api_id", apiId);
                    connD.setRequestProperty("api_key", apiKey);
                    LogUtil.info(this.getClass().getName(), "URL Delegation: "+ delegationApiUrl);

                    // Membaca respons dari API delegation
                    BufferedReader brD = new BufferedReader(new InputStreamReader(connD.getInputStream()));
                    StringBuilder responseD = new StringBuilder();
                    String outputD;
                    while ((outputD = brD.readLine()) != null) {
                        responseD.append(outputD);
                    }
                    
                    LogUtil.info(this.getClass().getName(), "Response Delegation: "+ responseD.toString());

                    // Memproses respons JSON
                    JSONObject delegationResponse = new JSONObject(responseD.toString());
                    JSONArray delegationDataArray = delegationResponse.getJSONArray("data");
                    JSONObject delegationDataObject = delegationDataArray.getJSONObject(0);
                    

                    // Ambil data yang diperlukan dari respons API delegation
                    String delegationPersonCode = delegationDataObject.getString("Person Code");
                    String delegationFirstName = delegationDataObject.getString("First Name");
                    String delegationJabatan = delegationDataObject.getString("JABATAN");
                    
                    LogUtil.info(this.getClass().getName(), "Response: "+ delegationPersonCode+delegationFirstName+delegationJabatan);
                    result.add(delegationPersonCode);
                    result.add(delegationJabatan);
                    result.add(delegationPersonCode);
                    result.add(delegationFirstName);
                    result.add(delegationJabatan);
                    result.add(infoObject.getString("Nama Atasan"));

                    // Tutup koneksi
                    connD.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    // atau lakukan penanganan kesalahan yang sesuai
                }
            } else {
                LogUtil.info(this.getClass().getName(), "Tidak Masuk kondisi Cuti");

                // Memeriksa kunci yang diperlukan dalam objek Info
                if (infoObject.has("Nama Atasan") && infoObject.has("Jabatan Atasan")) {
                    String namaAtasan = infoObject.getString("Nama Atasan");
                    String jabatanAtasan = infoObject.getString("Jabatan Atasan");
                    result.add(namaAtasan);
                    result.add(jabatanAtasan);
                    String fnUser = infoObject.getString("First Name");
                    String lnUser = infoObject.getString("Last Name");
                    String jbUser = infoObject.getString("Jabatan");
                    result.add(fnUser);
                    result.add(lnUser);
                    result.add(jbUser);
                } else {
                    // Jika kunci yang diperlukan tidak ada, ambil kunci lainnya
                    String fnUser = infoObject.getString("First Name");
                    String lnUser = infoObject.getString("Last Name");
                    String jbUser = infoObject.getString("Jabatan");
                    result.add(fnUser);
                    result.add(lnUser);
                    result.add(jbUser);
                }
            }
//            }

//             Memutuskan koneksi
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public List<String> getBandPengaju(String user) {
        // Pengaturan URL dan parameter API
        String apiUrl = "https://person-dev-insera-dev.apps.cpaasstl1.telkom.co.id/jw/web/json/plugin/co.id.telkom.person.plugin.GetInfoJabatan/service";
        String delegationUrl = "https://person-dev-insera-dev.apps.cpaasstl1.telkom.co.id/jw/api/list/getDelegation";
        String personCode = user;
        String apiId = "API-3154b489-51b7-497e-89bc-a0d44778f4de";
        String apiKey = "108692c1db6b462b9027e442ce8e6c26";
        List<String> result = new ArrayList<>();

        try {
            // Membuat koneksi HTTP
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("api_id", apiId);
            conn.setRequestProperty("api_key", apiKey);
            conn.setDoOutput(true);

            // Membuat body JSON
            String bodyJson = "{\"person_code\":\"" + personCode + "\"}";

            conn.getOutputStream().write(bodyJson.getBytes());

            // Membaca respons
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }

            // Memproses respons JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray infoArray = jsonResponse.getJSONArray("Info");
            JSONObject infoObject = infoArray.getJSONObject(0);

            LogUtil.info(this.getClass().getName(), "Data Info Object: " + infoObject.toString());

            if (infoObject.getString("Status Atasan").equalsIgnoreCase("ON LEAVE")) {
                LogUtil.info(this.getClass().getName(), "Masuk Kondisi Cuti");
                try {
                    // Membuat koneksi HTTP untuk pemanggilan API delegation
                    String jabatanAtasan = infoObject.getString("Jabatan Atasan").toString();
                    jabatanAtasan = jabatanAtasan.replaceAll(" ", "%20");
                    URL delegationApiUrl = new URL("https://person-dev-insera-dev.apps.cpaasstl1.telkom.co.id/jw/api/list/getDelegation?jabatan_atasan=" + jabatanAtasan);
                    HttpURLConnection connD = (HttpURLConnection) delegationApiUrl.openConnection();
                    connD.setRequestMethod("GET");
                    connD.setRequestProperty("Content-Type", "application/json");
                    connD.setRequestProperty("api_id", apiId);
                    connD.setRequestProperty("api_key", apiKey);
                    LogUtil.info(this.getClass().getName(), "URL Delegation: "+ delegationApiUrl);

                    // Membaca respons dari API delegation
                    BufferedReader brD = new BufferedReader(new InputStreamReader(connD.getInputStream()));
                    StringBuilder responseD = new StringBuilder();
                    String outputD;
                    while ((outputD = brD.readLine()) != null) {
                        responseD.append(outputD);
                    }
                    
                    LogUtil.info(this.getClass().getName(), "Response Delegation: "+ responseD.toString());

                    // Memproses respons JSON
                    JSONObject delegationResponse = new JSONObject(responseD.toString());
                    JSONArray delegationDataArray = delegationResponse.getJSONArray("data");
                    JSONObject delegationDataObject = delegationDataArray.getJSONObject(0);
                    

                    // Ambil data yang diperlukan dari respons API delegation
                    String delegationPersonCode = delegationDataObject.getString("Person Code");
                    String delegationFirstName = delegationDataObject.getString("First Name");
                    String delegationJabatan = delegationDataObject.getString("JABATAN");
                    
                    LogUtil.info(this.getClass().getName(), "Response: "+ delegationPersonCode+delegationFirstName+delegationJabatan);
                    result.add(delegationPersonCode);
                    result.add(delegationJabatan);
                    result.add(delegationPersonCode);
                    result.add(delegationFirstName);
                    result.add(delegationJabatan);

                    // Tutup koneksi
                    connD.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    // atau lakukan penanganan kesalahan yang sesuai
                }
            } else {
                LogUtil.info(this.getClass().getName(), "Tidak Masuk kondisi Cuti");

                // Memeriksa kunci yang diperlukan dalam objek Info
                if (infoObject.has("Nama Atasan") && infoObject.has("Jabatan Atasan")) {
                    String namaAtasan = infoObject.getString("Nama Atasan");
                    String jabatanAtasan = infoObject.getString("Jabatan Atasan");
                    result.add(namaAtasan);
                    result.add(jabatanAtasan);
                    String fnUser = infoObject.getString("First Name");
                    String lnUser = infoObject.getString("Last Name");
                    String jbUser = infoObject.getString("Jabatan");
                    result.add(fnUser);
                    result.add(lnUser);
                    result.add(jbUser);
                } else {
                    // Jika kunci yang diperlukan tidak ada, ambil kunci lainnya
                    String fnUser = infoObject.getString("First Name");
                    String lnUser = infoObject.getString("Last Name");
                    String jbUser = infoObject.getString("Jabatan");
                    result.add(fnUser);
                    result.add(lnUser);
                    result.add(jbUser);
                }
            }
//            }

//             Memutuskan koneksi
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
