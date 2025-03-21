package id.co.itasoft.telkom.oss.cra;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.service.WorkflowUserManager;
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

public class CreateCraViewPermission extends Element implements PluginWebSupport {
    public static String pluginName = "TELKOM - CRA - Get Flagging For Hiding Menu Create CRA";

    @Override
    public String renderTemplate(FormData formData, Map map) {
        return "";
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

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        JSONObject mainObj = new JSONObject();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (!workflowUserManager.isCurrentUserAnonymous()) {
                if (request.getParameterMap().containsKey("username")) {
                    String username = request.getParameter("username");

                    DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
                    String query = "SELECT crt.ID, " +
                            "CASE WHEN TO_DATE(act.C_AWAL || ' ' || act.C_WAKTU_SELESAI, 'YYYY-MM-DD HH24:MI') " +
                            "        < TO_DATE(TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI'), 'YYYY-MM-DD HH24:MI') THEN 'NO_INPUT' " +
                            "        ELSE 'INPUT' " +
                            "    END AS FLAGGING " +
                            "FROM APP_FD_CRA_CREATE crt " +
                            "INNER JOIN APP_FD_CRA_CREATE_T4 t4 ON crt.ID = t4.C_PARENT_ID " +
                            "INNER JOIN APP_FD_CRA_CREATE_ACT act ON t4.ID = act.C_FK " +
                            "WHERE crt.CREATEDBY = ? " +
                            "AND TO_CHAR(TO_DATE(act.C_WAKTU_START, 'HH24:MI'), 'HH24:MI') BETWEEN '01:00' AND '04:00' ";

                    con = ds.getConnection();
                    ps = con.prepareStatement(query);

                    ps.setString(1, username);

                    rs = ps.executeQuery();
                    JSONArray LoadDataFlagging = new JSONArray();
                    while (rs.next()) {
                        JSONObject valObject = new JSONObject();
                        valObject.put("FLAGGING", rs.getString("FLAGGING"));
                        LoadDataFlagging.put(valObject);
                    }

                    mainObj.put("data", LoadDataFlagging);
                    mainObj.write(response.getWriter());
                }
            } else {
                // Jika pengguna anonim, kembalikan respons dengan status false dan pesan
                mainObj.put("status", false);
                mainObj.put("message", "You Must Login First.");
                mainObj.write(response.getWriter());
            }
        } catch (SQLException e) {
            LogUtil.error(this.getClass().getName(), e, "Error : " + e.getMessage());
        } catch (JSONException e) {
            LogUtil.error(this.getClass().getName(), e, "Error : " + e.getMessage());
        } finally {
            // Close resources in the finally block to ensure they are always closed
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                LogUtil.error(this.getClass().getName(), e, "Error closing ResultSet: " + e.getMessage());
            }

            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                LogUtil.error(this.getClass().getName(), e, "Error closing PreparedStatement: " + e.getMessage());
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                LogUtil.error(this.getClass().getName(), e, "Error closing Connection: " + e.getMessage());
            }
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
        return "";
    }
}
