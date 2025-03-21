/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.CraCreateDetilServiceDao;
import id.co.itasoft.telkom.oss.cra.dao.CraCreateServiceDao;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class CraCreateDetilService extends Element implements PluginWebSupport {

    String pluginName = "CRA - TEL - CRA Create Service";
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
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String requestBody = request.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);

            try {
                // Konversi data JSON dari body permintaan ke objek Java
                JSONObject requestData = new JSONObject(requestBody);
                if (requestData.has("child_id") && !requestData.getString("child_id").isEmpty()) {
                    String child_id = requestData.getString("child_id");

                    CraCreateDetilServiceDao craCreateDetilServDao = new CraCreateDetilServiceDao();

                    List<String> results = craCreateDetilServDao.getAllData(child_id);

                    if (!results.isEmpty()) {
                        JSONObject responseData = new JSONObject();
                        JSONArray dataArray = new JSONArray();
                        for (int i = 0; i < results.size(); i += 10) { // 10 adalah jumlah kolom per baris
                            JSONObject data = new JSONObject();
                            data.put("id", results.get(i));
                            data.put("dateCreated", results.get(i+1));
                            data.put("dateModified", results.get(i+2));
                            data.put("createdBy", results.get(i+3));
                            data.put("createdByName", results.get(i+4));
                            data.put("modifiedBy", results.get(i+5));
                            data.put("modifiedByName", results.get(i+6));
                            data.put("c_parent_id", results.get(i+7));
                            data.put("c_definition", results.get(i+8));
                            data.put("c_child_id", results.get(i+9));
                            dataArray.put(data);
                        }
//                            JSONObject data = new JSONObject();
//                            data.put("id", results.get(0));
//                            data.put("dateCreated", results.get(1));
//                            data.put("dateModified", results.get(2));
//                            data.put("createdBy", results.get(3));
//                            data.put("createdByName", results.get(4));
//                            data.put("modifiedBy", results.get(5));
//                            data.put("modifiedByName", results.get(6));
//                            data.put("c_parent_id", results.get(7));
//                            data.put("c_definition", results.get(8));
//                            data.put("c_child_id", results.get(9));

//                            dataArray.put(data);
                            responseData.put("status", "success");
                            responseData.put("data", dataArray);
                            response.setContentType("application/json");
                            response.getWriter().write(responseData.toString());
                        }else {
                        // Data tidak ditemukan, kirim respons kesalahan
                        JSONObject errorResponse = new JSONObject();
                        errorResponse.put("status", "error");
                        errorResponse.put("message", "Data not found");
                        response.setContentType("application/json");
                        response.getWriter().write(errorResponse.toString());
                    }

                    } else {
                        // Parameter yang diperlukan tidak lengkap, kirim respons kesalahan
                        JSONObject errorResponse = new JSONObject();
                        errorResponse.put("status", "error");
                        errorResponse.put("message", "Parameter yang diperlukan tidak lengkap");
                        response.setContentType("application/json");
                        response.getWriter().write(errorResponse.toString());
                    }
                }catch (JSONException ex) {
                LogUtil.error(pluginClassName, ex, ex.getMessage());
            }
            }
        }

    }
