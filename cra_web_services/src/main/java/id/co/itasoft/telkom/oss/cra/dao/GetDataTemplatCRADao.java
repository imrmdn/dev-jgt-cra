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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class GetDataTemplatCRADao {

    private static final String QUERY = "SELECT * FROM ("
            + "SELECT DISTINCT"
            + "    a.ID, "
            + "    a.DATECREATED, "
            + "    b.C_JUDUL, "
            + "    d.C_CATCH_SEVERITY, "
            + "    b.C_MAPPING_CATEGORY, "
            + "    e.C_CRA_ID "
            + "FROM APP_FD_CRA_CREATE a "
            + "    INNER JOIN APP_FD_CRA_CREATE_T2 b ON a.ID = b.C_PARENT_ID "
            + "    INNER JOIN APP_FD_CRA_CREATE_T4 c ON a.ID = c.C_PARENT_ID "
            + "    INNER JOIN APP_FD_CRA_CREATE_T1 e ON a.ID = e.C_PARENT_ID "
            + "    INNER JOIN APP_FD_CRA_CREATE_ACT d ON c.ID = d.C_FK "
            + "WHERE a.c_full_approved = 'Complete' "
            + "GROUP BY"
            + "    a.ID, "
            + "    a.DATECREATED, "
            + "    b.C_JUDUL, "
            + "    d.C_CATCH_SEVERITY, "
            + "    b.C_MAPPING_CATEGORY, "
            + "    e.C_CRA_ID "
            + "ORDER BY "
            + "    a.DATECREATED DESC) tmp0";

    // Method that executes the query and returns data as JSON
    public JSONObject getDataTemplate() throws JSONException {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONObject jsonResponse = new JSONObject();
        JSONArray dataArray = new JSONArray();

        try {
            // Get connection from the DataSource
            con = ds.getConnection();
            ps = con.prepareStatement(QUERY);
            rs = ps.executeQuery();

            // Process the result set and construct JSON response
            while (rs.next()) {
                JSONObject dataObject = new JSONObject();
                dataObject.put("C_CRA_ID", rs.getString("C_CRA_ID"));
                dataObject.put("ID", rs.getString("ID"));
                dataObject.put("C_JUDUL", rs.getString("C_JUDUL"));
                dataArray.put(dataObject);
            }

            // Add data to the JSON response
            jsonResponse.put("total", dataArray.length());
            jsonResponse.put("data", dataArray);

        } catch (SQLException e) {
            e.printStackTrace();
            // Add error message to response
            jsonResponse.put("error", "Database error");
        } finally {
            closeQuietly(con);
            closeQuietly(rs);
            closeQuietly(ps);
        }

        return jsonResponse;
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
