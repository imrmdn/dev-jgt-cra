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
public class TokenValidatorSubmitDao {

    public String getToken(String id) {
        String result = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select c_input_token from app_fd_cra_create_t1 \n"
                        + "where id = (select c_child_id1 from app_fd_cra_create where id = ?)";
                stmt = con.prepareStatement(selectQuery);
                stmt.setString(1, id);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result = rs.getString("c_input_token");
                }
            }
        } catch (Exception e) {
            LogUtil.info(this.getClass().getName(), e.getMessage());
        } finally {
            closeQuietly(con);
            closeQuietly(rs);
            closeQuietly(stmt);
        }

        return result;
    }

    public List<String> getUrlCallTokenValidator() {
        List<String> result = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select c_url, c_api_key, c_api_id from app_fd_m_api \n"
                        + "where c_desc = 'generate_token_request_cra'";
                stmt = con.prepareStatement(selectQuery);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result.add(rs.getString("c_url"));
                    result.add(rs.getString("c_api_key"));
                    result.add(rs.getString("c_api_id"));
                }
            }
        } catch (Exception e) {
            LogUtil.info(this.getClass().getName(), e.getMessage());
        } finally {
            closeQuietly(con);
            closeQuietly(rs);
            closeQuietly(stmt);
        }

        return result;
    }

    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Log error jika penutupan gagal, tapi jangan crash aplikasi
                LogUtil.error(this.getClass().getName(), e, "Failed to close resource: " + e.getMessage());
            }
        }
    }
}
