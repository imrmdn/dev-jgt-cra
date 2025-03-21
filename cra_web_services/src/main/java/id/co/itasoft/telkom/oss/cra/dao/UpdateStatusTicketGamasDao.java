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
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;

/**
 *
 * @author Asani
 */
public class UpdateStatusTicketGamasDao {

    public boolean updateTicket(String status, String ticket) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ds.getConnection();
            ps = con.prepareStatement(UpdateQuery());

            ps.setString(1, status);
            ps.setString(2, ticket);
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
        return "UPDATE app_fd_create_ticket_gamas SET c_status_ticket = ? WHERE c_ticket_gamas = ? ";
    }
}
