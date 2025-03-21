package id.co.itasoft.telkom.oss.cra.dao;

import id.co.itasoft.telkom.oss.cra.model.*;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.sql.DataSource;
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

public class MappingFlowApprovalDao {

    public JSONObject getFlowApprover(String recordId) {
        JSONObject result = new JSONObject();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "SELECT crt.id, " +
                        "crt.CREATEDBY, " +
                        "t1.C_MAPPING_CATEGORY AS category, " +
                        "act.C_CATCH_SEVERITY AS severity, " +
                        "t1.C_CHOOSE_FLOW AS flow, " +
                        "ne.C_NE_DOMAIN_CRA " +
                        "FROM APP_FD_CRA_CREATE crt " +
                        "INNER JOIN APP_FD_CRA_CREATE_T1 t1 ON crt.id = t1.C_PARENT_ID " +
                        "INNER JOIN APP_FD_M_CRQ_DOMAIN ne ON t1.C_NETWORK_ELEMENT = ne.ID " +
                        "INNER JOIN APP_FD_CRA_CREATE_T4 t4 ON crt.id = t4.C_PARENT_ID " +
                        "INNER JOIN APP_FD_CRA_CREATE_ACT act ON t4.ID = act.C_FK " +
                        "WHERE crt.id = ? ";

//                LogUtil.info(this.getClass().getName(), "Executing query: " + selectQuery);
//                LogUtil.info(this.getClass().getName(), "Parameter [recordId]: " + recordId);

                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, recordId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result.put("id", rs.getString("id"));
                    result.put("category", rs.getString("category"));
                    result.put("severity", rs.getString("severity"));
//                    String severity = rs.getString("severity");
//                    LogUtil.info(this.getClass().getName(), "Fetched severity value: " + severity);

                    result.put("flow", rs.getString("flow"));
                }
            }
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Error retrieving data: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                LogUtil.error(this.getClass().getName(), e, "Error closing resources: " + e.getMessage());
            }
        }

        return result;
    }

//    public List<LoadApproverManagerModel> getMgrRMO(String domain) {
//        List<LoadApproverManagerModel> managers = new ArrayList<>();
//        Connection con = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//
//        try {
//            // Retrieve connection from the default datasource
//            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
//            con = ds.getConnection();
//
//            if (!con.isClosed()) {
//                String selectQuery = "SELECT rmo.c_nik, hc.C_NAMA_KARYAWAN, hc.C_SHORT_POSISI FROM app_fd_cra_master_mgr_rmo rmo " +
//                        "LEFT JOIN APP_FD_CRA_MASTER_HC hc ON rmo.C_NIK = hc.C_NIK " +
//                        "WHERE rmo.c_posisi = 'Manager RMO' AND INSTR(UPPER( rmo.c_ne_domain), UPPER(?)) > 0 ";
//                stmt = con.prepareStatement(selectQuery);
//                stmt.setString(1, domain);
//                rs = stmt.executeQuery();
//
//                while (rs.next()) {
//                    LoadApproverManagerModel manager = new LoadApproverManagerModel();
//                    manager.setNik(rs.getString("c_nik"));
//                    manager.setShortPosition(rs.getString("C_SHORT_POSISI"));
//                    manager.setName(rs.getString("C_NAMA_KARYAWAN"));
//                    managers.add(manager);
//                }
//            }
//        } catch (Exception e) {
//            LogUtil.info(this.getClass().getName(), "Error retrieving data: " + e.getMessage());
//        } finally {
//            try {
//                if (rs != null) rs.close();
//                if (stmt != null) stmt.close();
//                if (con != null) con.close();
//            } catch (SQLException e) {
//                LogUtil.error(this.getClass().getName(), e, "Error closing resources");
//            }
//        }
//
//        return managers;
//    }

    public List<LoadApproverManagerModel> getMgrRMO(String domain) {
        List<LoadApproverManagerModel> managers = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
//                String selectQuery = "SELECT rmo.C_NIK, hc.C_NAMA_KARYAWAN, hc.C_SHORT_POSISI " +
//                        "FROM app_fd_cra_master_mgr_rmo rmo " +
//                        "LEFT JOIN APP_FD_CRA_MASTER_HC hc ON rmo.C_NIK = hc.C_NIK " +
//                        "WHERE rmo.c_posisi = 'Manager RMO' " +
//                        "AND INSTR(UPPER(rmo.c_ne_domain), UPPER(?)) > 0";

                String selectQuery = "WITH Split_NIK AS ( " +
                        "    SELECT rmo.c_posisi, rmo.c_ne_domain, " +
                        "           TRIM(REGEXP_SUBSTR(rmo.c_nik, '[^;]+', 1, LEVEL)) AS c_nik " +
                        "    FROM app_fd_cra_master_mgr_rmo rmo " +
                        "    CONNECT BY REGEXP_SUBSTR(rmo.c_nik, '[^;]+', 1, LEVEL) IS NOT NULL " +
                        "        AND PRIOR c_nik = c_nik " +
                        "        AND PRIOR SYS_GUID() IS NOT NULL) " +
                        "SELECT Split_NIK.c_nik, hc.C_NAMA_KARYAWAN, hc.C_SHORT_POSISI " +
                        "FROM Split_NIK " +
                        "LEFT JOIN APP_FD_CRA_MASTER_HC hc ON Split_NIK.c_nik = hc.C_NIK " +
                        "WHERE Split_NIK.c_posisi = 'Manager RMO' " +
                        "  AND INSTR(UPPER(Split_NIK.c_ne_domain), UPPER(?)) > 0 ";

                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, domain);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String nikString = rs.getString("C_NIK");
                    String name = rs.getString("C_NAMA_KARYAWAN");
                    String shortPosition = rs.getString("C_SHORT_POSISI");

                    if (nikString != null && !nikString.isEmpty()) {
                        // Split c_nik by semicolon
                        String[] nikArray = nikString.split(";");
                        for (String nik : nikArray) {
                            LoadApproverManagerModel manager = new LoadApproverManagerModel();
                            manager.setNik(nik.trim()); // Trim untuk menghindari spasi ekstra
                            manager.setShortPosition(shortPosition);
                            manager.setName(name);
                            managers.add(manager);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.info(this.getClass().getName(), "Error retrieving data: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                LogUtil.error(this.getClass().getName(), e, "Error closing resources");
            }
        }

        return managers;
    }

    public List<LoadApproverOsmRmoModel> getOsmRmo(String domain) {
        List<LoadApproverOsmRmoModel> osmRmos = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "SELECT rmo.c_nik, hc.C_NAMA_KARYAWAN, hc.C_SHORT_POSISI FROM app_fd_cra_master_mgr_rmo rmo " +
                        "LEFT JOIN APP_FD_CRA_MASTER_HC hc ON rmo.C_NIK = hc.C_NIK " +
                        "WHERE rmo.c_posisi = 'OSM RMO' AND INSTR( UPPER( rmo.c_ne_domain ), UPPER(?)) > 0 ";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, domain);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    LoadApproverOsmRmoModel osmRmo = new LoadApproverOsmRmoModel();
                    osmRmo.setNik(rs.getString("C_NIK"));
                    osmRmo.setShortPosition(rs.getString("C_SHORT_POSISI"));
                    osmRmo.setName(rs.getString("C_NAMA_KARYAWAN"));
                    osmRmos.add(osmRmo);
                }
            }
        } catch (Exception e) {
            LogUtil.info(this.getClass().getName(), "Error retrieving data: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                LogUtil.error(this.getClass().getName(), e, "Error closing resources");
            }
        }

        return osmRmos;
    }

    public List<GetOtherApproverModel> getOtherApprover(String posisi) {
        List<GetOtherApproverModel> getOtherApprovers = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "WITH Split_C_NIK AS " +
                        "(SELECT DISTINCT TRIM(REGEXP_SUBSTR(a.c_nik, '[^;]+', 1, LEVEL)) AS c_nik, " +
                        "        a.c_posisi " +
                        "    FROM app_fd_cra_master_mgr_rmo a " +
                        "    WHERE a.c_posisi = ? " +
                        "    CONNECT BY REGEXP_SUBSTR(a.c_nik, '[^;]+', 1, LEVEL) IS NOT NULL) " +
                        "SELECT Split_C_NIK.c_nik, " +
                        "    b.C_SHORT_POSISI, " +
                        "    b.C_NAMA_KARYAWAN " +
                        "FROM Split_C_NIK " +
                        "LEFT JOIN APP_FD_CRA_MASTER_HC b ON Split_C_NIK.c_nik = b.C_NIK ";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, posisi);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    GetOtherApproverModel getOtherApprover = new GetOtherApproverModel();
                    getOtherApprover.setNik(rs.getString("c_nik"));
                    getOtherApprover.setShortPosition(rs.getString("C_SHORT_POSISI"));
                    getOtherApprover.setName(rs.getString("C_NAMA_KARYAWAN"));
                    getOtherApprovers.add(getOtherApprover);
                }
            }
        } catch (Exception e) {
            LogUtil.info(this.getClass().getName(), "Error retrieving data: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                LogUtil.error(this.getClass().getName(), e, "Error closing resources");
            }
        }

        return getOtherApprovers;
    }

    public List<LoadApproverBand2Model> getBand2Pengaju(String recordId) {
        List<LoadApproverBand2Model> band2Pengajus = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "SELECT crt.ID, " +
                        "hc.C_NIK, " +
                        "hc.C_NAMA_KARYAWAN, " +
                        "hc.C_SHORT_POSISI " +
                        "FROM APP_FD_CRA_CREATE crt " +
                        "INNER JOIN APP_FD_CRA_CREATE_T2 t2 ON crt.ID = t2.C_PARENT_ID " +
                        "INNER JOIN APP_FD_CRA_MASTER_HC hc ON t2.C_BAND_2_PENGAJU = hc.C_NIK " +
                        "WHERE crt.ID = ? ";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, recordId);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    LoadApproverBand2Model band2Pengaju = new LoadApproverBand2Model();
                    band2Pengaju.setNik(rs.getString("C_NIK"));

                    String posisi = rs.getString("C_SHORT_POSISI");
                    band2Pengaju.setShortPosition(posisi);
//                    LogUtil.info(this.getClass().getName(), "Fetched posisi value: " + posisi);

                    String nama = rs.getString("C_NAMA_KARYAWAN");
                    band2Pengaju.setName(nama);
//                    LogUtil.info(this.getClass().getName(), "Fetched nama value: " + nama);

                    band2Pengajus.add(band2Pengaju);
                }
            }
        } catch (Exception e) {
            LogUtil.info(this.getClass().getName(), "Error retrieving data: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                LogUtil.error(this.getClass().getName(), e, "Error closing resources");
            }
        }

        return band2Pengajus;
    }

    public List<LoadApproverMgrTerkaitModel> getMgrTerkait(String recordId) {
        List<LoadApproverMgrTerkaitModel> getMgrTerkaits = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "SELECT crt.ID, " +
                        "hc.C_NIK, " +
                        "hc.C_NAMA_KARYAWAN, " +
                        "hc.C_SHORT_POSISI " +
                        "FROM APP_FD_CRA_CREATE crt " +
                        "INNER JOIN APP_FD_CRA_CREATE_T2 t2 ON crt.ID = t2.C_PARENT_ID " +
                        "INNER JOIN APP_FD_CRA_MASTER_HC hc ON t2.C_PENANGGUNG_JAWAB = hc.C_NIK " +
                        "WHERE crt.ID = ? ";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, recordId);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    LoadApproverMgrTerkaitModel getMgrTerkaitApprover = new LoadApproverMgrTerkaitModel();
                    getMgrTerkaitApprover.setNik(rs.getString("C_NIK"));
                    getMgrTerkaitApprover.setShortPosition(rs.getString("C_SHORT_POSISI"));
                    getMgrTerkaitApprover.setName(rs.getString("C_NAMA_KARYAWAN"));

                    getMgrTerkaits.add(getMgrTerkaitApprover);
                }
            }
        } catch (Exception e) {
            LogUtil.info(this.getClass().getName(), "Error retrieving data: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                LogUtil.error(this.getClass().getName(), e, "Error closing resources");
            }
        }

        return getMgrTerkaits;
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
                String selectQuery = "select UPPER(a.c_ne_domain_cra) as c_ne_domain_cra from app_fd_m_crq_domain a "
                        + " join app_fd_cra_create_t1 b on b.c_network_element = a.id "
                        + " where b.c_parent_id = ? ";
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
}
