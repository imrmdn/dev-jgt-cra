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
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class MappingFlowApprovalDao {

//        public String getAtasanManager(String user) {
//        String apiUrl = "https://joget-person-oracle-joget-dev.apps.mypaas.telkom.co.id/jw/web/json/plugin/co.id.telkom.person.plugin.GetInfoJabatan/service";
//        String personCode = user;
//        String apiId = "API-3154b489-51b7-497e-89bc-a0d44778f4de";
//        String apiKey = "108692c1db6b462b9027e442ce8e6c26";
//        String result = "";
//
//        try {
//            URL url = new URL(apiUrl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("api_id", apiId);
//            conn.setRequestProperty("api_key", apiKey);
//            conn.setDoOutput(true);
//
//            String bodyJson = "{\"person_code\":\"" + personCode + "\"}";
//
//            conn.getOutputStream().write(bodyJson.getBytes());
//
//            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//            StringBuilder response = new StringBuilder();
//            String output;
//            while ((output = br.readLine()) != null) {
//                response.append(output);
//            }
//
//            JSONObject jsonResponse = new JSONObject(response.toString());
//            JSONArray infoArray = jsonResponse.getJSONArray("Info");
//            JSONObject infoObject = infoArray.getJSONObject(0);
//            String namaAtasan = infoObject.getString("Nama Atasan");
//
//            result = namaAtasan;
//
//            conn.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
    public String getAtasanManager(String user) {
        String result = "";
        List<String> credInfo = getListCredInfoJabatan();

        if (credInfo.size() > 0) {
            String credJson = credInfo.get(0);
            try {
                JSONObject credObject = new JSONObject(credJson);
                String apiUrl = credObject.getString("c_url");
                String apiId = credObject.getString("c_api_id");
                String apiKey = credObject.getString("c_api_key");

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("api_id", apiId);
                conn.setRequestProperty("api_key", apiKey);
                conn.setDoOutput(true);

                String bodyJson = "{\"person_code\":\"" + user + "\"}";

                conn.getOutputStream().write(bodyJson.getBytes());

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                StringBuilder response = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray infoArray = jsonResponse.getJSONArray("Info");
                JSONObject infoObject = infoArray.getJSONObject(0);
                String namaAtasan = infoObject.getString("Nama Atasan");

                result = namaAtasan;

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.info("MappingFlowApprovalDao", "Tidak ada informasi yang diperoleh dari database.");
        }
        return result;
    }

//    public String getAtasanBand(String user) {
//        String apiUrl = "https://joget-person-oracle-joget-dev.apps.mypaas.telkom.co.id/jw/web/json/plugin/co.id.telkom.person.plugin.GetInfoJabatan/service";
//        String personCode = user;
//        String apiId = "API-3154b489-51b7-497e-89bc-a0d44778f4de";
//        String apiKey = "108692c1db6b462b9027e442ce8e6c26";
//        String result = "";
//
//        try {
//            URL url = new URL(apiUrl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("api_id", apiId);
//            conn.setRequestProperty("api_key", apiKey);
//            conn.setDoOutput(true);
//
//            String bodyJson = "{\"person_code\":\"" + personCode + "\"}";
//
//            conn.getOutputStream().write(bodyJson.getBytes());
//
//            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//            StringBuilder response = new StringBuilder();
//            String output;
//            while ((output = br.readLine()) != null) {
//                response.append(output);
//            }
//
//            JSONObject jsonResponse = new JSONObject(response.toString());
//            JSONArray infoArray = jsonResponse.getJSONArray("Info");
//            JSONObject infoObject = infoArray.getJSONObject(0);
//            String namaAtasan = infoObject.getString("Nama Atasan");
//
//            result = namaAtasan;
//
//            conn.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
    public String getAtasanBand(String user) {
        // Mengambil informasi yang diperlukan dari database
        List<String> credInfo = getListCredInfoJabatan();

        // Inisialisasi variabel untuk menyimpan informasi
        String apiUrl = "";
        String apiId = "";
        String apiKey = "";
        String result = "";

        // Mengecek apakah informasi diperoleh dari database
        if (credInfo.size() > 0) {
            // Mengambil informasi dari list (asumsi hanya ada satu baris data yang diambil)
            String credJson = credInfo.get(0);

            try {
                // Parsing informasi menjadi objek JSON
                JSONObject credObject = new JSONObject(credJson);

                // Mengisi variabel dengan nilai dari objek JSON
                apiUrl = credObject.getString("c_url");
                apiId = credObject.getString("c_api_id");
                apiKey = credObject.getString("c_api_key");

                // Melakukan pengambilan data menggunakan nilai-nilai yang telah diperoleh
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("api_id", apiId);
                conn.setRequestProperty("api_key", apiKey);
                conn.setDoOutput(true);

                String bodyJson = "{\"person_code\":\"" + user + "\"}";

                conn.getOutputStream().write(bodyJson.getBytes());

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                StringBuilder response = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray infoArray = jsonResponse.getJSONArray("Info");
                JSONObject infoObject = infoArray.getJSONObject(0);
                String namaAtasan = infoObject.getString("Nama Atasan");

                result = namaAtasan;

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.info("MappingFlowApprovalDao", "Tidak ada informasi yang diperoleh dari database.");
        }

        return result;
    }

//    public String getMgrRMO(String domain) {
//        String apiUrl = "https://joget-person-oracle-joget-dev.apps.mypaas.telkom.co.id/jw/web/json/plugin/co.id.telkom.person.plugin.GetPersonJabatan/service";
//        String domainParam = domain;
//        String apiId = "API-3154b489-51b7-497e-89bc-a0d44778f4de";
//        String apiKey = "108692c1db6b462b9027e442ce8e6c26";
//        String result = "";
//
//        try {
//            URL url = new URL(apiUrl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("api_id", apiId);
//            conn.setRequestProperty("api_key", apiKey);
//            conn.setDoOutput(true);
//
//                String bodyJson = "{\"domain\":\"" + domainParam + "\",\"jabatan\":\"\"}";
//
//            conn.getOutputStream().write(bodyJson.getBytes());
//
//            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//            StringBuilder response = new StringBuilder();
//            String output;
//            while ((output = br.readLine()) != null) {
//                response.append(output);
//            }
//
//            JSONObject jsonResponse = new JSONObject(response.toString());
//            JSONArray infoArray = jsonResponse.getJSONArray("Data");
//            JSONObject infoObject = infoArray.getJSONObject(0);
//            String namaAtasan = infoObject.getString("Person code");
//
//            result = namaAtasan;
//
//            conn.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
    public String getMgrRMO(String domain) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select c_nik from app_fd_cra_master_mgr_rmo where c_posisi = 'Manager RMO' and INSTR(UPPER(c_ne_domain), UPPER(?)) > 0";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, domain);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_nik");
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

    public String getOSMRMO(String domain) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select c_nik from app_fd_cra_master_mgr_rmo where c_posisi = 'OSM RMO' and INSTR(UPPER(c_ne_domain), UPPER(?)) > 0";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, domain);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_nik");
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
    
    public String getOtherApprover(String posisi) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select c_nik from app_fd_cra_master_mgr_rmo where c_posisi = ? and c_ne_domain is null";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, posisi);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_nik");
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

    /*untuk ke person   
    public String getMgrRMO(String domain) {
        // Mengambil informasi yang diperlukan dari database
        List<String> credInfo = getListCredPersonJabatanGroup();

        // Inisialisasi variabel untuk menyimpan informasi
        String apiUrl = "";
        String apiId = "";
        String apiKey = "";
        String result = "";

        // Mengecek apakah informasi diperoleh dari database
        if (credInfo.size() > 0) {
            // Mengambil informasi dari list (asumsi hanya ada satu baris data yang diambil)
            String credJson = credInfo.get(0);

            try {
                // Parsing informasi menjadi objek JSON
                JSONObject credObject = new JSONObject(credJson);

                // Mengisi variabel dengan nilai dari objek JSON
                apiUrl = credObject.getString("c_url");
                apiId = credObject.getString("c_api_id");
                apiKey = credObject.getString("c_api_key");

                // Melakukan pengambilan data menggunakan nilai-nilai yang telah diperoleh
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("api_id", apiId);
                conn.setRequestProperty("api_key", apiKey);
                conn.setDoOutput(true);

                String bodyJson = "{\"domain\":\"" + domain + "\",\"jabatan\":\"\"}";

                conn.getOutputStream().write(bodyJson.getBytes());

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                StringBuilder response = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }

//                JSONObject jsonResponse = new JSONObject(response.toString());
//                JSONArray infoArray = jsonResponse.getJSONArray("Data");
//                JSONObject infoObject = infoArray.getJSONObject(0);
//                String namaAtasan = infoObject.getString("Person code");

                JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray infoArray = jsonResponse.getJSONArray("Data");
                    LogUtil.info("MAF DAO", "Length Response data: "+infoArray.length());

                    // Mencari Person code yang memiliki jabatan yang mengandung kata "Manager RMO"
                    String namaAtasan = "";
                    for (int i = 0; i < infoArray.length(); i++) {
                        JSONObject infoObject = infoArray.getJSONObject(i);
                        String jabatan = infoObject.getString("Jabatan");
                        if (jabatan.contains("Manager RMO")) {
                            namaAtasan = infoObject.getString("Person code");
                            break;
                        }
                    }
//
//                    result = namaAtasan;
//                
//                LogUtil.info("MAF DAO", "Nama User: "+namaAtasan);

                result = namaAtasan;

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.info("MappingFlowApprovalDao", "Tidak ada informasi yang diperoleh dari database.");
        }

        return result;
    }
     */
    public String getPersonByJabatan(String jabatan) {
        // Mengambil informasi yang diperlukan dari database
        List<String> credInfo = getListCredPersonJabatan();

        // Inisialisasi variabel untuk menyimpan informasi
        String apiUrl = "";
        String apiId = "";
        String apiKey = "";
        String result = "";

        // Mengecek apakah informasi diperoleh dari database
        if (credInfo.size() > 0) {
            // Mengambil informasi dari list (asumsi hanya ada satu baris data yang diambil)
            String credJson = credInfo.get(0);

            try {
                // Parsing informasi menjadi objek JSON
                JSONObject credObject = new JSONObject(credJson);

                // Mengisi variabel dengan nilai dari objek JSON
                apiUrl = credObject.getString("c_url");
                apiId = credObject.getString("c_api_id");
                apiKey = credObject.getString("c_api_key");

                // Melakukan pengambilan data menggunakan nilai-nilai yang telah diperoleh
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("api_id", apiId);
                conn.setRequestProperty("api_key", apiKey);
                conn.setDoOutput(true);

                String bodyJson = "{\"jabatan\":\"" + jabatan + "\",\"domain\":\"\"}";

                conn.getOutputStream().write(bodyJson.getBytes());

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                StringBuilder response = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray infoArray = jsonResponse.getJSONArray("Data");
                JSONObject infoObject = infoArray.getJSONObject(0);
                String namaAtasan = infoObject.getString("Person code");

//                    JSONObject jsonResponse = new JSONObject(response.toString());
//                    JSONArray infoArray = jsonResponse.getJSONArray("Data");
//
//                    // Ambil entri pertama dengan status kosong ("")
//                    String namaAtasan = null;
//                    for (int i = 0; i < infoArray.length(); i++) {
//                        JSONObject infoObject = infoArray.getJSONObject(i);
//                        if (infoObject.getString("Status").isEmpty()) {
//                            namaAtasan = infoObject.getString("Person code");
//                            break; // Keluar dari loop setelah menemukan entri yang cocok
//                        }
//                    }
//                    LogUtil.info("MAF DAO", "Nama User: "+namaAtasan);
                result = namaAtasan;


                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.info("MappingFlowApprovalDao", "Tidak ada informasi yang diperoleh dari database.");
        }

        return result;
    }

//    public String getAtasanMgrRmo(String user) {
//        String apiUrl = "https://joget-person-oracle-joget-dev.apps.mypaas.telkom.co.id/jw/web/json/plugin/co.id.telkom.person.plugin.GetInfoJabatan/service";
//        String personCode = user;
//        String apiId = "API-3154b489-51b7-497e-89bc-a0d44778f4de";
//        String apiKey = "108692c1db6b462b9027e442ce8e6c26";
//        String result = "";
//
//        try {
//            URL url = new URL(apiUrl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("api_id", apiId);
//            conn.setRequestProperty("api_key", apiKey);
//            conn.setDoOutput(true);
//
//            String bodyJson = "{\"person_code\":\"" + personCode + "\"}";
//
//            conn.getOutputStream().write(bodyJson.getBytes());
//
//            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//            StringBuilder response = new StringBuilder();
//            String output;
//            while ((output = br.readLine()) != null) {
//                response.append(output);
//            }
//
//            JSONObject jsonResponse = new JSONObject(response.toString());
//            JSONArray infoArray = jsonResponse.getJSONArray("Info");
//            JSONObject infoObject = infoArray.getJSONObject(0);
//            String namaAtasan = infoObject.getString("Nama Atasan");
//
//            result = namaAtasan;
//
//            conn.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
    public String getAtasanMgrRmo(String user) {
        // Mengambil informasi yang diperlukan dari database
        List<String> credInfo = getListCredInfoJabatan();

        // Inisialisasi variabel untuk menyimpan informasi
        String apiUrl = "";
        String apiId = "";
        String apiKey = "";
        String result = "";

        // Mengecek apakah informasi diperoleh dari database
        if (credInfo.size() > 0) {
            // Mengambil informasi dari list (asumsi hanya ada satu baris data yang diambil)
            String credJson = credInfo.get(0);

            try {
                // Parsing informasi menjadi objek JSON
                JSONObject credObject = new JSONObject(credJson);

                // Mengisi variabel dengan nilai dari objek JSON
                apiUrl = credObject.getString("c_url");
                apiId = credObject.getString("c_api_id");
                apiKey = credObject.getString("c_api_key");

                // Melakukan pengambilan data menggunakan nilai-nilai yang telah diperoleh
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("api_id", apiId);
                conn.setRequestProperty("api_key", apiKey);
                conn.setDoOutput(true);

                String bodyJson = "{\"person_code\":\"" + user + "\"}";

                conn.getOutputStream().write(bodyJson.getBytes());

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                StringBuilder response = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray infoArray = jsonResponse.getJSONArray("Info");
                JSONObject infoObject = infoArray.getJSONObject(0);
                String namaAtasan = infoObject.getString("Nama Atasan");

                result = namaAtasan;

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.info("MappingFlowApprovalDao", "Tidak ada informasi yang diperoleh dari database.");
        }

        return result;
    }

    public String getDomain(String id) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select UPPER(a.c_ne_domain_cra) as c_ne_domain_cra from app_fd_m_crq_domain a\n"
                        + " join app_fd_cra_create_t1 b on b.c_network_element = a.id\n"
                        + " where b.c_parent_id = ?";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, id);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_ne_domain_cra");
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

    public String getDataMgrTerkait(String id) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select UPPER(c_penanggung_jawab) as c_penanggung_jawab from app_fd_cra_create_t2 where c_parent_id = ?";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, id);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_penanggung_jawab");
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

    public String getDataBandPengaju(String id) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select UPPER(c_band_2_pengaju) as c_band_2_pengaju from app_fd_cra_create_t2 where c_parent_id = ?";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, id);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_band_2_pengaju");
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

    public String getCategory(String id) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select a.c_ne_domain_cra from app_fd_m_crq_domain a\n"
                        + " join app_fd_cra_create_t1 b on b.c_network_element = a.id\n"
                        + " where b.c_parent_id = ?";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, id);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_ne_domain_cra");
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

    public List<String> getListCredPersonJabatan() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_url, c_api_id, c_api_key from app_fd_m_api where c_desc = 'GetPersonJabatan'";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("c_url", rs.getString("c_url"));
                jsonObject.put("c_api_id", rs.getString("c_api_id"));
                jsonObject.put("c_api_key", rs.getString("c_api_key"));
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

    public List<String> getListCredPersonJabatanGroup() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_url, c_api_id, c_api_key from app_fd_m_api where c_desc = 'GetPersonByPersonGroup'";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("c_url", rs.getString("c_url"));
                jsonObject.put("c_api_id", rs.getString("c_api_id"));
                jsonObject.put("c_api_key", rs.getString("c_api_key"));
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

    public List<String> getListCredInfoJabatan() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_url, c_api_id, c_api_key from app_fd_m_api where c_desc = 'GetInfoJabatan'";
            ps = con.prepareStatement(query);

            rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("c_url", rs.getString("c_url"));
                jsonObject.put("c_api_id", rs.getString("c_api_id"));
                jsonObject.put("c_api_key", rs.getString("c_api_key"));
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
}
