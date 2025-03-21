/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.CallReqCRQDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import okhttp3.*;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.workflow.model.WorkflowAssignment;

/**
 *
 * @author Asani
 */
public class CallReqCRQ extends DefaultApplicationPlugin {

    private String pluginName = "Telkom - CRA - Call Req CRQ TSEL";

    @Override
    public Object execute(Map map) {

        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        WorkflowAssignment workflowAssignment = (WorkflowAssignment) map.get("workflowAssignment");
        String processId = appService.getOriginProcessId(workflowAssignment.getProcessId());
        LogUtil.info(this.getClass().getName(), "Process ID: " + processId);
        CallCRQ(processId);
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

    public String CallCRQ(String processId) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        List<ArrayList> listCred = getCredentialAPI();
        ArrayList credArrayList = listCred.get(0);
        String api_id = String.valueOf(credArrayList.get(0));
        String api_key = String.valueOf(credArrayList.get(1));
        String urlCred = String.valueOf(credArrayList.get(2));

        String cra_id = getCraId(processId);
        String act_id = getActId(processId);

//        CallReqCRQDao callReqCRQDao = new CallReqCRQDao();
        List<ArrayList> listHeadCra = getHeaderCra(cra_id);
        ArrayList headCraArrayList = listHeadCra.get(0);
        String Hjudul = String.valueOf(headCraArrayList.get(0));
        String Hcategory = String.valueOf(headCraArrayList.get(1));
        String Hlatar_belakang = String.valueOf(headCraArrayList.get(2));
        String Himpact_tsel = String.valueOf(headCraArrayList.get(3));
        String Hne = String.valueOf(headCraArrayList.get(4));
        String Hstart = String.valueOf(headCraArrayList.get(5));
        String Hend = String.valueOf(headCraArrayList.get(6));

        List<ArrayList> listActCra = getActivitiesCra(cra_id);
        int jumlahAct = listActCra.size();
        String activitiesJson = "";
        for (int i = 0; i < jumlahAct; i++) {
            ArrayList actCraArrayList = listActCra.get(i);
            String Ainfra = String.valueOf(actCraArrayList.get(0));
            String Awitel = String.valueOf(actCraArrayList.get(1));
            String Adowntime = String.valueOf(actCraArrayList.get(2) + " Menit");
            String Aimpact_tsel = String.valueOf(actCraArrayList.get(3));
            String Aact_id = String.valueOf(actCraArrayList.get(4));
            String Astart_epoch = String.valueOf(actCraArrayList.get(5));
            String Aend_epoch = String.valueOf(actCraArrayList.get(6));
            activitiesJson += "{ \"activity_id\": \"" + Aact_id + "\", "
                    + "\"ne_name\": \"" + Ainfra + "\", "
                    + "\"location\": \"" + Awitel + "\", "
                    + "\"description\": \"ini description untuk timeline1 \", "
                    + "\"downtime\": \"" + Adowntime + "\", "
                    + "\"execution_start\": " + Astart_epoch + ", "
                    + "\"execution_end\": " + Aend_epoch + ", "
                    + "\"execution_zone\": \"" + Awitel + "\" "
                    + "}";
            if (i < jumlahAct - 1) {
                activitiesJson += ", ";
            }
        }
//        List<ArrayList> listActCra = callReqCRQDao.getActivitiesCra(act_id);
//        int jumlahAct = listActCra.size();
        LogUtil.info(this.getClass().getName(), "cra: " + cra_id);
        LogUtil.info(this.getClass().getName(), "act: " + act_id);

        // Ganti URL dengan endpoint yang benar
        String url = urlCred;

        // Ganti dengan body JSON yang ingin dikirimkan
        String jsonBody = "{ \"cra_id\": \"" + cra_id + "\", "
                + "\"title\": \"" + Hjudul + "\", "
                + "\"ne_domain\": \"" + Hne + "\", "
                + "\"category\": \"" + Hcategory + "\", "
                + "\"attachment_link\": \"https://google.com, https://google.co.id\", "
                + "\"approved_by\": \"Nama PIC | 0822123123\", "
                + "\"reason\": \"" + Hlatar_belakang + "\", "
                + "\"time_start\": " + Hstart + ", "
                + "\"time_end\": " + Hend + ", "
                + "\"timezone\": \"+7\", "
                + "\"tsel_impact\": " + Himpact_tsel + ", "
                + "\"activities\": [ " + activitiesJson + " ] }";

        LogUtil.info(pluginName, "JSON: " + jsonBody.toString());
        // Ganti dengan API Key yang benar
        String apiKey = api_key;

        // Ganti dengan API ID yang benar
        String apiId = api_id;

        RequestBody body = RequestBody.create(mediaType, jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("api_id", apiId)
                .addHeader("api_key", apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                LogUtil.info(this.getClass().getName(), "Respon API: " + responseBody);
            } else {
                LogUtil.info(this.getClass().getName(), "Respon API: " + response.code() + " " + response.message());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close(); // Menutup koneksi setelah digunakan
            }
        }
        return null;

    }

    public List<ArrayList> getCredentialAPI() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ArrayList> hasilList = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select c_api_id, c_api_key, c_url from app_fd_m_api where UPPER(c_desc) = UPPER('CRA API ID AND KEY')";
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

    public String getCraId(String recordId) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = "";

        try {
            con = ds.getConnection();
            String query = "select c_cra_id from app_fd_cra_create_t1 where c_parent_id = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, recordId);

            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("c_cra_id");
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

    public List<ArrayList> getDataAct(String recordId) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ArrayList> hasilList = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "SELECT c_activity_id, c_infrastruktur, c_witel  FROM APP_FD_CRA_CREATE_ACT where c_fk = (select c_child_id4 from app_fd_cra_create where id = ?) and rownum <=1";
            ps = con.prepareStatement(query);
            ps.setString(1, recordId);

            rs = ps.executeQuery();
            while (rs.next()) {
                String act_id = rs.getString("c_activity_id");
                String infra = rs.getString("c_infrastruktur");
                String witel = rs.getString("c_witel");

                ArrayList<String> rowValues = new ArrayList<>();
                rowValues.add(act_id);
                rowValues.add(infra);
                rowValues.add(witel);

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

    public String getActId(String recordId) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = "";

        try {
            con = ds.getConnection();
            String query = "SELECT c_activity_id FROM APP_FD_CRA_CREATE_ACT where c_fk = (select c_child_id4 from app_fd_cra_create where id = ?) and rownum <=1";
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

    public List<ArrayList> getHeaderCra(String cra_id) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ArrayList> hasilList = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "SELECT DISTINCT h.c_judul, d.c_mapping_category, TO_CHAR(h.c_latar_belakang) AS c_latar_belakang, CASE WHEN a.c_impact_tsel = 'yes' THEN 'TSEL' ELSE 'no' END as c_impact_tsel, h.c_ne_domain_cra, "
                    + " MIN(ROUND((CAST(TO_TIMESTAMP(a.c_awal || ' ' || TO_CHAR(TO_DATE(a.c_waktu_start, 'HH24:MI'),'HH24:MI'), 'YYYY-MM-DD HH24:MI') AS DATE) - TO_DATE('1970-01-01', 'YYYY-MM-DD')) * 86400)) AS start_epoch,\n"
                    + "    MAX(ROUND((CAST(TO_TIMESTAMP(a.c_berakhir || ' ' || TO_CHAR(TO_DATE(a.c_waktu_selesai, 'HH24:MI'),'HH24:MI'), 'YYYY-MM-DD HH24:MI') AS DATE) - TO_DATE('1970-01-01', 'YYYY-MM-DD')) * 86400)) AS end_epoch\n"
                    + " FROM\n"
                    + "    APP_FD_CRA_CREATE_ACT a\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE_t4 b ON a.C_FK = b.ID\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE c ON b.C_PARENT_ID = c.ID\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE_T1 d ON c.ID = d.C_PARENT_ID\n"
                    + "    INNER JOIN APP_FD_CRA_MASTER_MAP_CAT e ON d.C_CRA_ACTIVITY = e.C_ID_MAP_CAT\n"
                    + "    INNER JOIN APP_FD_CRA_MASTER_KEGIATAN f ON e.C_KEGIATAN = f.C_ID_KEGIATAN \n"
                    + "    LEFT JOIN APP_FD_CRA_MASTER_DOWNTIME g ON a.C_DURASI_DOWNTIME = g.C_AUTOCODE_DOWNTIME\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE_T2 h ON h.id = c.c_child_id2\n"
                    + "    JOIN APP_FD_M_CRQ_DOMAIN h ON h.id = d.c_network_element"
                    + " WHERE d.c_cra_id = ?"
                    + " GROUP BY h.c_judul, d.c_mapping_category, TO_CHAR(h.c_latar_belakang), a.c_impact_tsel, h.c_ne_domain_cra";
//            String query = "SELECT DISTINCT h.c_judul, d.c_mapping_category, h.c_latar_belakang, CASE WHEN a.c_impact_tsel = 'yes' THEN 'TSEL' ELSE 'no' END as c_impact_tsel, h.c_ne_domain_cra, "
//                    + " MIN(ROUND((CAST(TO_TIMESTAMP(a.c_awal || ' ' || a.c_waktu_start, 'YYYY-MM-DD HH:MI PM') AS DATE) - TO_DATE('1970-01-01', 'YYYY-MM-DD')) * 86400)) AS start_epoch,\n"
//                    + "    MAX(ROUND((CAST(TO_TIMESTAMP(a.c_berakhir || ' ' || a.c_waktu_selesai, 'DD-MM-YYYY HH:MI PM') AS DATE) - TO_DATE('1970-01-01', 'YYYY-MM-DD')) * 86400)) AS end_epoch\n"
//                    + " FROM\n"
//                    + "    APP_FD_CRA_CREATE_ACT a\n"
//                    + "    INNER JOIN APP_FD_CRA_CREATE_t4 b ON a.C_FK = b.ID\n"
//                    + "    INNER JOIN APP_FD_CRA_CREATE c ON b.C_PARENT_ID = c.ID\n"
//                    + "    INNER JOIN APP_FD_CRA_CREATE_T1 d ON c.ID = d.C_PARENT_ID\n"
//                    + "    INNER JOIN APP_FD_CRA_MASTER_MAP_CAT e ON d.C_CRA_ACTIVITY = e.C_ID_MAP_CAT\n"
//                    + "    INNER JOIN APP_FD_CRA_MASTER_KEGIATAN f ON e.C_KEGIATAN = f.C_ID_KEGIATAN \n"
//                    + "    LEFT JOIN APP_FD_CRA_MASTER_DOWNTIME g ON a.C_DURASI_DOWNTIME = g.C_AUTOCODE_DOWNTIME\n"
//                    + "    INNER JOIN APP_FD_CRA_CREATE_T2 h ON h.id = c.c_child_id2\n"
//                    + "    JOIN APP_FD_M_CRQ_DOMAIN h ON h.id = d.c_network_element"
//                    + " WHERE d.c_cra_id = ?"
//                    + " GROUP BY h.c_judul, d.c_mapping_category, h.c_latar_belakang, a.c_impact_tsel, h.c_ne_domain_cra";
            ps = con.prepareStatement(query);
            ps.setString(1, cra_id);

            rs = ps.executeQuery();
            while (rs.next()) {
                String judul = rs.getString("c_judul");
                String category = rs.getString("c_mapping_category");
                String latar_belakang = rs.getString("c_latar_belakang");
                String impact_tsel = rs.getString("c_impact_tsel");
                String ne = rs.getString("c_ne_domain_cra");
                String start = rs.getString("start_epoch");
                String end = rs.getString("end_epoch");

                ArrayList<String> rowValues = new ArrayList<>();
                rowValues.add(judul);
                rowValues.add(category);
                rowValues.add(latar_belakang);
                rowValues.add(impact_tsel);
                rowValues.add(ne);
                rowValues.add(start);
                rowValues.add(end);

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

    public List<ArrayList> getActivitiesCra(String cra_id) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ArrayList> hasilList = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "SELECT DISTINCT a.c_infrastruktur, a.c_witel, g.c_downtime, a.c_impact_tsel, a.c_activity_id,"
                    + "    ROUND((CAST(TO_TIMESTAMP(a.c_awal || ' ' || TO_CHAR(TO_DATE(a.c_waktu_start, 'HH24:MI'),'HH24:MI'), 'YYYY-MM-DD HH24:MI') AS DATE) - TO_DATE('1970-01-01', 'YYYY-MM-DD')) * 86400) AS start_epoch,\n"
                    + "    ROUND((CAST(TO_TIMESTAMP(a.c_berakhir || ' ' || TO_CHAR(TO_DATE(a.c_waktu_selesai, 'HH24:MI'),'HH24:MI'), 'YYYY-MM-DD HH24:MI') AS DATE) - TO_DATE('1970-01-01', 'YYYY-MM-DD')) * 86400) AS end_epoch\n"
                    + " FROM\n"
                    + "    APP_FD_CRA_CREATE_ACT a\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE_t4 b ON a.C_FK = b.ID\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE c ON b.C_PARENT_ID = c.ID\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE_T1 d ON c.ID = d.C_PARENT_ID\n"
                    + "    INNER JOIN APP_FD_CRA_MASTER_MAP_CAT e ON d.C_CRA_ACTIVITY = e.C_ID_MAP_CAT\n"
                    + "    INNER JOIN APP_FD_CRA_MASTER_KEGIATAN f ON e.C_KEGIATAN = f.C_ID_KEGIATAN \n"
                    + "    LEFT JOIN APP_FD_CRA_MASTER_DOWNTIME g ON a.C_DURASI_DOWNTIME = g.C_AUTOCODE_DOWNTIME\n"
                    + "WHERE d.c_cra_id = ? and a.c_request_crq = 'yes'";
            ps = con.prepareStatement(query);
            ps.setString(1, cra_id);

            rs = ps.executeQuery();
            while (rs.next()) {
                String infra = rs.getString("c_infrastruktur");
                String witel = rs.getString("c_witel");
                String downtime = rs.getString("c_downtime");
                String impact_tsel = rs.getString("c_impact_tsel");
                String act_id = rs.getString("c_activity_id");
                String start_epoch = rs.getString("start_epoch");
                String end_epoch = rs.getString("end_epoch");

                ArrayList<String> rowValues = new ArrayList<>();
                rowValues.add(infra);
                rowValues.add(witel);
                rowValues.add(downtime);
                rowValues.add(impact_tsel);
                rowValues.add(act_id);
                rowValues.add(start_epoch);
                rowValues.add(end_epoch);

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
