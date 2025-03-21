/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;

/**
 *
 * @author Asani
 */
public class CallReqCRQDao {

    public List<ArrayList> getHeaderCra(String cra_id) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ArrayList> hasilList = new ArrayList<>();

        try {
            con = ds.getConnection();
                String query = "SELECT DISTINCT h.c_judul, d.c_mapping_category, h.c_latar_belakang, a.c_impact_tsel, h.c_ne_domain_cra\n"
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
                        + " WHERE d.c_cra_id = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, cra_id);

            rs = ps.executeQuery();
            while (rs.next()) {
                String judul = rs.getString("c_judul");
                String category = rs.getString("c_mapping_category");
                String latar_belakang = rs.getString("c_latar_belakang");
                String impact_tsel = rs.getString("c_impact_tsel");
                String ne = rs.getString("c_ne_domain_cra");

                ArrayList<String> rowValues = new ArrayList<>();
                rowValues.add(judul);
                rowValues.add(category);
                rowValues.add(latar_belakang);
                rowValues.add(impact_tsel);
                rowValues.add(ne);

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
            String query = "SELECT DISTINCT a.c_infrastruktur, a.c_witel, g.c_downtime, a.c_impact_tsel, a.c_activity_id\n"
                    + " FROM\n"
                    + "    APP_FD_CRA_CREATE_ACT a\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE_t4 b ON a.C_FK = b.ID\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE c ON b.C_PARENT_ID = c.ID\n"
                    + "    INNER JOIN APP_FD_CRA_CREATE_T1 d ON c.ID = d.C_PARENT_ID\n"
                    + "    INNER JOIN APP_FD_CRA_MASTER_MAP_CAT e ON d.C_CRA_ACTIVITY = e.C_ID_MAP_CAT\n"
                    + "    INNER JOIN APP_FD_CRA_MASTER_KEGIATAN f ON e.C_KEGIATAN = f.C_ID_KEGIATAN \n"
                    + "    LEFT JOIN APP_FD_CRA_MASTER_DOWNTIME g ON a.C_DURASI_DOWNTIME = g.C_AUTOCODE_DOWNTIME\n"
                    + "WHERE d.c_cra_id = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, cra_id);

            rs = ps.executeQuery();
            while (rs.next()) {
                String infra = rs.getString("c_infrastruktur");
                String witel = rs.getString("c_witel");
                String downtime = rs.getString("c_downtime");
                String impact_tsel = rs.getString("c_impact_tsel");
                String act_id = rs.getString("c_activity_id");

                ArrayList<String> rowValues = new ArrayList<>();
                rowValues.add(infra);
                rowValues.add(witel);
                rowValues.add(downtime);
                rowValues.add(impact_tsel);
                rowValues.add(act_id);

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
