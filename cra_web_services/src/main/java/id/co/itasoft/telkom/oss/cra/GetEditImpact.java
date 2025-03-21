package id.co.itasoft.telkom.oss.cra;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class GetEditImpact extends Element implements PluginWebSupport {

    public static String pluginName = "TELKOM - CRA - Get Data Edit Impacted Services";

    @Override
    public String renderTemplate(FormData formData, Map map) {
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
        return "Get Data Edit Impacted Services";
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject mainObj = new JSONObject();

        if (request.getParameterMap().containsKey("isd")) {
            String pid = request.getParameter("isd");
            String id = request.getParameter("id");
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            StringBuilder query = new StringBuilder();
            if (pid.equalsIgnoreCase("myRequest")) {
                query = new StringBuilder(
                        "SELECT a.ID, a.C_TEMP_CLOB_COLUMN AS isd_detil "
                        + "FROM APP_FD_CRA_CREATE_ACT a "
                        + "INNER JOIN APP_FD_CRA_CREATE_T4 b ON a.C_FK = b.ID "
                        + "INNER JOIN APP_FD_CRA_CREATE c ON b.C_PARENT_ID = c.ID "
                        + "WHERE a.ID = ? ");
            } else {
                query = new StringBuilder(
                        "SELECT a.ID, a.C_TEMP_CLOB_COLUMN AS isd_detil "
                        + "FROM APP_FD_CRA_CREATE_ACT a "
                        + "INNER JOIN APP_FD_CRA_CREATE_T4 b ON a.C_FK = b.ID "
                        + "INNER JOIN APP_FD_CRA_CREATE c ON b.C_PARENT_ID = c.ID "
                        + "INNER JOIN WF_PROCESS_LINK d ON c.ID = d.ORIGINPROCESSID "
                        + "WHERE d.PROCESSID = ? AND a.ID = ? ");
            }

            Connection con = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                con = ds.getConnection();
                if (pid.equalsIgnoreCase("myRequest")) {
                    ps = con.prepareStatement(query.toString());
                    ps.setString(1, id);
                } else {
                    ps = con.prepareStatement(query.toString());
                    ps.setString(1, pid);
                    ps.setString(2, id);
                }

                rs = ps.executeQuery();

                JSONArray loadDataIsd = new JSONArray();
                while (rs.next()) {
                    JSONObject valObject = new JSONObject();
                    valObject.put("isd_detil", rs.getString("isd_detil"));
                    loadDataIsd.put(valObject);
                }

                if (loadDataIsd.length() > 0) {
                    mainObj.put("status", "success");
                    mainObj.put("message", "Sukses mengambil data.");
                    mainObj.put("data", loadDataIsd);
                } else {
                    mainObj.put("status", "error");
                    mainObj.put("message", "Data tidak ditemukan atau parameter kurang/salah");
                }

            } catch (SQLException | JSONException e) {
                try {
                    mainObj.put("status", "error");
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    mainObj.put("message", e.getMessage());
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
                LogUtil.error(this.getClass().getName(), e, "Error: " + e.getMessage());
            } finally {
                // Close resources in reverse order of creation
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        LogUtil.error(this.getClass().getName(), e, "Error closing ResultSet: " + e.getMessage());
                    }
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        LogUtil.error(this.getClass().getName(), e, "Error closing PreparedStatement: " + e.getMessage());
                    }
                }
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        LogUtil.error(this.getClass().getName(), e, "Error closing Connection: " + e.getMessage());
                    }
                }
            }

            response.setContentType("application/json");
            response.getWriter().write(mainObj.toString());
        }
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
}
