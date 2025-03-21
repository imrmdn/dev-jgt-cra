/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.InsertFlowApprovalDao;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class InsertFlowApproval extends Element implements PluginWebSupport {

    String pluginName = "CRA - TEL - Insert Flow Approval";
    String pluginClassName = this.getClass().getName();

    @Override
    public String renderTemplate(FormData arg0, Map arg1) {
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
        return pluginClassName;
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            try {
                // Ambil nilai username dari header permintaan
                String username = request.getHeader("username");

                if (username != null && !username.isEmpty()) {
                    try {
                        InsertFlowApprovalDao ifa = new InsertFlowApprovalDao();
                        if (!ifa.CekValidasiApproval(username)) {
                            ifa.insertToFlowApproval(username);
                            if (!ifa.CekValidasiGroupTembusan("AKSES")) {
                                ifa.insertToGroupDomain("AKSES");
                                response.setContentType("application/json");
                                response.getWriter().write("INSERT");
                            } else {
//                                ifa.updateToGroupDomain("AKSES");
                                response.setContentType("application/json");
                                response.getWriter().write("INSERT");
                            }
                        } else {
                            ifa.updateFlowApproval(username);
                            if (!ifa.CekValidasiGroupTembusan("AKSES")) {
                                ifa.insertToGroupDomain("AKSES");
                                response.setContentType("application/json");
                                response.getWriter().write("UPDATE");
                            } else {
//                                ifa.updateToGroupDomain("AKSES");
                                response.setContentType("application/json");
                                response.getWriter().write("UPDATE");
                            }
                            return;
                        }

                    } catch (Exception e) {
                        JSONObject errorResponse = new JSONObject();
                        errorResponse.put("status", "error");
                        errorResponse.put("message", "Gagal memproses permintaan");
                        response.setContentType("application/json");
                        response.getWriter().write(errorResponse.toString());

                        // Selanjutnya, Anda dapat juga melakukan logging kesalahan ini
                        LogUtil.error(pluginClassName, e, "Error: " + e.getMessage());
                    }

                } else {
                    JSONObject errorResponse = new JSONObject();
                    errorResponse.put("status", "error");
                    errorResponse.put("message", "Header parameter 'username' kosong atau tidak ada");
                    response.setContentType("application/json");
                    response.getWriter().write(errorResponse.toString());
                    return;
                }
            } catch (Exception e) {
            }
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.getWriter().write("Metode HTTP yang diperbolehkan hanya GET");
        }

    }

}
