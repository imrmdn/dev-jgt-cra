/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.GetDataTemplatCRADao;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class GetDataTemplateCRA extends Element implements PluginWebSupport {

    String pluginName = "CRA - TEL - Web Service Get Data Template CRA";
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
        // Set content type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Create an instance of GetDataTemplatCRADao to fetch data
        GetDataTemplatCRADao dao = new GetDataTemplatCRADao();
        JSONObject jsonResponse = new JSONObject();

        try {
            // Call the method to get the data template
            jsonResponse = dao.getDataTemplate();
        } catch (JSONException e) {
            e.printStackTrace();
            try {
                jsonResponse.put("error", "Error processing the response");
            } catch (JSONException ex) {
                LogUtil.info(pluginClassName, ex.getMessage());
            }
        }

        // Write the response to the client
        out.write(jsonResponse.toString());

    }

}
