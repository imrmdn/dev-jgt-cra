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
public class CraCreateServiceDao {

    public List<String> getAllData(String child_id) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select * from app_fd_cra_create where id =?";
            ps = con.prepareStatement(query);
            ps.setString(1, child_id);
            rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("id"));
                results.add(rs.getString("dateCreated"));
                results.add(rs.getString("dateModified"));
                results.add(rs.getString("createdBy"));
                results.add(rs.getString("createdByName"));
                results.add(rs.getString("modifiedBy"));
                results.add(rs.getString("modifiedByName"));
                results.add(rs.getString("c_dokumen_pendukung"));
                results.add(rs.getString("c_detil_giat"));
                results.add(rs.getString("c_dampak"));
                results.add(rs.getString("c_prosedur_giat"));
                results.add(rs.getString("c_fk"));
                results.add(rs.getString("c_latar_belakang"));
                results.add(rs.getString("c_choose_activity"));
                results.add(rs.getString("c_rollback_skenario"));
                results.add(rs.getString("c_network_element"));
                results.add(rs.getString("c_tujuan"));
                results.add(rs.getString("c_nama_ne"));
                results.add(rs.getString("c_cra_activity"));
                results.add(rs.getString("c_total_customer_impact"));
                results.add(rs.getString("c_parent_id"));
                results.add(rs.getString("c_direct_potensial_impact"));
                results.add(rs.getString("c_smcounter"));
                results.add(rs.getString("c_judul"));
                results.add(rs.getString("c_input_token"));
                results.add(rs.getString("c_tembusan"));
                results.add(rs.getString("c_penanggung_jawab"));
                results.add(rs.getString("c_band_2_pengaju"));
                results.add(rs.getString("c_wizard_1_severity_network_element"));
                results.add(rs.getString("c_wizard_1_severity_cra_activity"));
                results.add(rs.getString("c_approval"));
                results.add(rs.getString("c_id_create"));
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

        return results;
    }
}
