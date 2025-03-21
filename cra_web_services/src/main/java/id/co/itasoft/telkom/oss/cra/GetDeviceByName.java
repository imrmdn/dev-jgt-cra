/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.ApiDao;
import id.co.itasoft.telkom.oss.cra.dao.GetDeviceByNameDao;
import id.co.itasoft.telkom.oss.cra.dao.UpdateStatusTicketGamasDao;
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
public class GetDeviceByName extends Element implements PluginWebSupport {

    String pluginName = "CRA - TEL - CRA Get Device By Name";
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
        return this.getClass().getName();
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

        if (apiDao.isValidApiKey(apiId, apiKey)) {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                String requestBody = request.getReader().lines()
                        .reduce("", (accumulator, actual) -> accumulator + actual);

                try {

                    // Konversi data JSON dari body permintaan ke objek Java
                    JSONObject requestData = new JSONObject(requestBody);

                    if (requestData.has("device")) {
                        try {
                            String device = requestData.getString("device");

                            // Inisialisasi ApiCaller dengan URL login dan URL device
                            GetDeviceByNameDao apiCaller = new GetDeviceByNameDao(
                                    "http://10.60.168.11:3000/api/aggregator/login",
                                    "http://10.60.168.11:3000/api/aggregator/getDeviceLikeName"
                            );

                            // Melakukan login
                            boolean loginSuccess = apiCaller.login("user_insera", "4ggInseraDev#2024");

                            if (!loginSuccess) {
                                JSONObject errorResponse = new JSONObject();
                                errorResponse.put("status", "error");
                                errorResponse.put("message", "Login gagal.");
                                response.setContentType("application/json");
                                response.getWriter().write(errorResponse.toString());
                                return;
                            }

                            // Memanggil API getDeviceLikeName dengan parameter `device`
                            String apiResponse = apiCaller.getDeviceLikeName(device, 0, 0);

                            if (apiResponse == null) {
                                JSONObject errorResponse = new JSONObject();
                                errorResponse.put("status", "error");
                                errorResponse.put("message", "Status CRA Gagal Update");
                                response.setContentType("application/json");
                                response.getWriter().write(errorResponse.toString());
                                return;
                            } else {
                                JSONObject responseData = new JSONObject(apiResponse); // Parsing JSON response dari API
                                responseData.put("status", "success");
                                responseData.put("message", "Data retrieved successfully");
                                response.setContentType("application/json");
                                response.getWriter().write(responseData.toString());
                            }

                        } catch (Exception e) {
                            JSONObject errorResponse = new JSONObject();
                            errorResponse.put("status", "error");
                            errorResponse.put("message", "Gagal memproses permintaan. Terjadi kesalahan internal.");
                            response.setContentType("application/json");
                            response.getWriter().write(errorResponse.toString());

                            // Melakukan logging kesalahan ini
                            LogUtil.error(pluginClassName, e, "Error: " + e.getMessage());
                        }
                    }

                } catch (Exception e) {
                }
            } else {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                response.getWriter().write("Metode HTTP yang diperbolehkan hanya POST");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED, "Tidak diizinkan. Periksa api_id dan api_key Anda.");
        }
    }

}
