/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.ApiDao;
import id.co.itasoft.telkom.oss.cra.dao.DetailImpactActivityDao;
import id.co.itasoft.telkom.oss.cra.dao.SeverityImpactedServiceDao;
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
public class DetailImpactActivityService extends Element implements PluginWebSupport {
    
    String pluginName = "CRA - TEL - CRA Get Impact Activity Detail Service";
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
        String apiId = request.getHeader("api_id");
        String apiKey = request.getHeader("api_key");
        ApiDao apiDao = new ApiDao();

//        if (apiDao.isValidApiKey(apiId, apiKey)) {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                String requestBody = request.getReader().lines()
                        .reduce("", (accumulator, actual) -> accumulator + actual);

                try {

                    // Konversi data JSON dari body permintaan ke objek Java
                    JSONObject requestData = new JSONObject(requestBody);

                    if ((requestData.has("deviceId")
                            && !requestData.getString("deviceId").isEmpty())) {
                        try {
                            String deviceId = requestData.getString("deviceId");
                            DetailImpactActivityDao detailImpactActivityDao = new DetailImpactActivityDao();
                            String hasilManggilApi = detailImpactActivityDao.useBearerToken(detailImpactActivityDao.getClientCredentials(), deviceId);
                            
                            if (hasilManggilApi.equalsIgnoreCase("Data tidak ditemukan")) {
                                JSONObject errorResponse = new JSONObject();
                                errorResponse.put("status", "error");
                                errorResponse.put("message", "Data Tidak Ditemukan");
                                response.setContentType("application/json");
                                response.getWriter().write(errorResponse.toString());
                                return;
                            } else {
                                JSONObject responseData = new JSONObject();
                                responseData.put("status", "success");
                                responseData.put("message", "Data Berhasil Ditemukan");
                                responseData.put("data", hasilManggilApi);
                                response.setContentType("application/json");
                                response.getWriter().write(responseData.toString());
                            }

                        } catch (Exception e) {
                            JSONObject errorResponse = new JSONObject();
                            errorResponse.put("status", "error");
                            errorResponse.put("message", "Gagal memproses permintaan. Terjadi kesalahan internal.");
                            response.setContentType("application/json");
                            response.getWriter().write(errorResponse.toString());

                            // Selanjutnya, Anda dapat juga melakukan logging kesalahan ini
                            LogUtil.error(pluginClassName, e, "Error: " + e.getMessage());
                        }

                    } else {
                        JSONObject errorResponse = new JSONObject();
                        errorResponse.put("status", "error");
                        errorResponse.put("message", "Salah satu parameter kosong");
                        response.setContentType("application/json");
                        response.getWriter().write(errorResponse.toString());
                        return;
                    }
                } catch (Exception e) {
                }
            } else {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                response.getWriter().write("Metode HTTP yang diperbolehkan hanya POST");
            }
//        } else {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED, "Tidak diizinkan. Periksa api_id dan api_key Anda.");
//        }
    }
}
