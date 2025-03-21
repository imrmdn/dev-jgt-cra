/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import com.google.gson.JsonObject;
import id.co.itasoft.telkom.oss.cra.dao.SelectTemplateCRADao;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class SelectTemplateCRA extends Element implements PluginWebSupport {

    String pluginName = "CRA - TEL - Select Template CRA";
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
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject jsonResponse = new JsonObject();

        if (workflowUserManager.getCurrentUsername() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "User not authenticated");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Method not allowed. Use POST.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        // Read JSON body from request
        StringBuilder requestBody = new StringBuilder();
        try ( BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        // Parse JSON body
        String recordIdAcuan;
        String processId;
        String userLogin;
        try {
            JSONObject jsonRequest = new JSONObject(requestBody.toString());
            recordIdAcuan = jsonRequest.optString("record_id_acuan", null);
            processId = jsonRequest.optString("process_id", null);
            userLogin = jsonRequest.optString("userLogin", null);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Invalid JSON format");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        // Validate parameters
        if (recordIdAcuan == null || processId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Missing required parameters");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        // Use the DAO class to insert data
        SelectTemplateCRADao stDao = new SelectTemplateCRADao();
        boolean success = stDao.SelectTemplate(processId, recordIdAcuan, userLogin);

        // Send response
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Record inserted successfully");
            jsonResponse.addProperty("record_id_acuan", recordIdAcuan);
            jsonResponse.addProperty("process_id", processId);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Failed to insert record");
        }

        response.getWriter().write(jsonResponse.toString());
    }

}
