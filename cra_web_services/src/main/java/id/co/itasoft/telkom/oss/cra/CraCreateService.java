/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.CraCreateServiceDao;
import id.co.itasoft.telkom.oss.cra.model.CraCreateServiceModel;
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
public class CraCreateService extends Element implements PluginWebSupport {

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

                    CraCreateServiceDao craCreateServDao = new CraCreateServiceDao();

                    List<String> results = craCreateServDao.getAllData(child_id);

                    if (!results.isEmpty()) {
                        JSONObject responseData = new JSONObject();
                        JSONArray dataArray = new JSONArray();

                        JSONObject data = new JSONObject();
                        data.put("id", results.get(0));
                        data.put("dateCreated", results.get(1));
                        data.put("dateModified", results.get(2));
                        data.put("createdBy", results.get(3));
                        data.put("createdByName", results.get(4));
                        data.put("modifiedBy", results.get(5));
                        data.put("modifiedByName", results.get(6));
                        data.put("c_dokumen_pendukung", results.get(7));
                        data.put("c_detil_giat", results.get(8));
                        data.put("c_dampak", results.get(9));
                        data.put("c_prosedur_giat", results.get(10));
                        data.put("c_fk", results.get(11));
                        data.put("c_latar_belakang", results.get(12));
                        data.put("c_choose_activity", results.get(13));
                        data.put("c_rollback_skenario", results.get(14));
                        data.put("c_network_element", results.get(15));
                        data.put("c_tujuan", results.get(16));
                        data.put("c_nama_ne", results.get(17));
                        data.put("c_cra_activity", results.get(18));
                        data.put("c_total_customer_impact", results.get(19));
                        data.put("c_parent_id", results.get(20));
                        data.put("c_direct_potensial_impact", results.get(21));
                        data.put("c_smcounter", results.get(22));
                        data.put("c_judul", results.get(23));
                        data.put("c_input_token", results.get(24));
                        data.put("c_tembusan", results.get(25));
                        data.put("c_penanggung_jawab", results.get(26));
                        data.put("c_band_2_pengaju", results.get(27));
                        data.put("c_wizard_1_severity_network_element", results.get(28));
                        data.put("c_wizard_1_severity_cra_activity", results.get(29));
                        data.put("c_approval", results.get(30));
                        data.put("c_id_create", results.get(31));

                        dataArray.put(data);
                        responseData.put("status", "success");
                        responseData.put("data", dataArray);
                        response.setContentType("application/json");
                        response.getWriter().write(responseData.toString());
                    } else {
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
            } catch (JSONException ex) {
                LogUtil.error(pluginClassName, ex, ex.getMessage());
            }
        }
    }

}
