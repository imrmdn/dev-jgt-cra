/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.itasoft.telkom.oss.cra.dao.TokenValidatorSubmitDao;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.workflow.model.WorkflowAssignment;

/**
 *
 * @author Asani
 */
public class TokenValidatorSubmit extends DefaultApplicationPlugin {

    public static String pluginName = "TELKOM - CRA - Token Validator Submit";

    @Override
    public Object execute(Map map) {
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        WorkflowAssignment workflowAssignment = (WorkflowAssignment) map.get("workflowAssignment");
        String processId = appService.getOriginProcessId(workflowAssignment.getProcessId());
        TokenValidatorSubmitDao Tvds = new TokenValidatorSubmitDao();
        String tokenSub = Tvds.getToken(processId);
        LogUtil.info(this.getClass().getName(), "Process ID Token: " + processId);
        LogUtil.info(this.getClass().getName(), "Token: " + tokenSub);
        HttpURLConnection conn = null;
        try {
            // URL API
            List<String> result = Tvds.getUrlCallTokenValidator();

            String urlVal = "";
            String apiKeyVal = "";
            String apiIdVal = "";
            if (result.size() >= 3) {
                urlVal = result.get(0);
                apiKeyVal = result.get(1);
                apiIdVal = result.get(2);
            }

            String apiUrl = urlVal;

            // Token value (ganti dengan nilai token yang valid)
            String tokenVerif = tokenSub;

            // Membuat objek URL
            URL url = new URL(apiUrl);

            // Membuat koneksi HttpURLConnection
            conn = (HttpURLConnection) url.openConnection();

            // Set metode request
            conn.setRequestMethod("POST");

            // Set header Authorization dengan nilai token
            conn.setRequestProperty("Authorization", tokenVerif);
            conn.setRequestProperty("token_submit", "Submit");

            // Set header api_id dan api_key
            conn.setRequestProperty("api_id", apiIdVal);
            conn.setRequestProperty("api_key", apiKeyVal);

            // Mengirimkan request
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.flush();
            os.close();

            // Membaca response dari server
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Respon sukses, baca nilai token
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                LogUtil.info(pluginName, "response Submit Token: " + response.toString());

            } else {
                // Respon error, baca pesan error
                BufferedReader errorIn = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String errorInputLine;
                StringBuffer errorResponse = new StringBuffer();

                while ((errorInputLine = errorIn.readLine()) != null) {
                    errorResponse.append(errorInputLine);
                }
                errorIn.close();
            }
        } catch (MalformedURLException ex) {
            LogUtil.info(this.getClass().getName(), ex.getMessage());
        } catch (IOException ex) {
            LogUtil.info(this.getClass().getName(), ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();// Menutup koneksi setelah digunakan
            }
        }

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
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }

}
