/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;

/**
 *
 * @author Asani
 */
public class GenerateTokenCRA extends Element implements PluginWebSupport {

    String pluginName = "CRA - CRA Get Token";
    String pluginClassName = this.getClass().getName();
    private static final long serialVersionUID = 1L;
    private static final Map<String, Long> tokenMap = new HashMap<>();
    private static final long TOKEN_VALIDITY_DURATION_MS = TimeUnit.MINUTES.toMillis(30);
    private static Map<String, Boolean> tokenStatus = new HashMap<>();

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
        String method = request.getMethod();
        List<String> resultUrl = getUrlCallTokenValidator();

        String urlVal = "";
        String apiKeyVal = "";
        String apiIdVal = "";
        if (resultUrl.size() >= 3) {
            urlVal = resultUrl.get(0);
            apiKeyVal = resultUrl.get(1);
            apiIdVal = resultUrl.get(2);
        }

        if ("GET".equals(method)) {
            // Generate token for GET request
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            String token = generateToken();
            tokenMap.put(token, System.currentTimeMillis());
            out.print("{\"token\": \"" + token + "\"}");
            out.flush();
        } else if ("POST".equals(method)) {
            // Validate token for POST request
            String token = request.getHeader("Authorization");
            String tokenSubmit = request.getHeader("token_submit");
            String apiIdHeader = request.getHeader("api_id");
            String apiKeyHeader = request.getHeader("api_key");

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            if (!apiIdVal.equals(apiIdHeader) || !apiKeyVal.equals(apiKeyHeader)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"message\": \"Invalid api_id or api_key\"}");
                out.flush();
                return;
            }

            if (isTokenValid(token)) {
                if (isValidToken(token)) {
                    if (tokenSubmit != null && tokenSubmit.equalsIgnoreCase("Submit")) {
                        out.print("{\"message\": \"Token is valid\"}");
                        markTokenAsUsed(token);
                    } else {
                        out.print("{\"message\": \"Token is valid\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print("{\"message\": \"Invalid token\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"message\": \"Invalid token\"}");
            }
            out.flush();
        } else {
            // Handle unsupported methods
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

    }

    private String generateToken() {
        String token = UUID.randomUUID().toString();
        tokenMap.put(token, System.currentTimeMillis());
        tokenStatus.put(token, false);
        return token;
    }

    private boolean isValidToken(String token) {
        if (token == null || !tokenMap.containsKey(token)) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long tokenTime = tokenMap.get(token);
        return (currentTime - tokenTime) <= TOKEN_VALIDITY_DURATION_MS;
    }

    public static boolean isTokenValid(String token) {
//        return tokenStatus.containsKey(token) && !tokenStatus.get(token);
        return token != null && tokenStatus.containsKey(token) && !tokenStatus.get(token);
    }

    public static void markTokenAsUsed(String token) {
        tokenStatus.put(token, true); // Menandai token sebagai sudah digunakan
    }

    public List<String> getUrlCallTokenValidator() {
        List<String> result = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();

            if (!con.isClosed()) {
                String selectQuery = "select c_url, c_api_key, c_api_id from app_fd_m_api \n"
                        + "where c_desc = 'generate_token_request_cra'";
                stmt = con.prepareStatement(selectQuery);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result.add(rs.getString("c_url"));
                    result.add(rs.getString("c_api_key"));
                    result.add(rs.getString("c_api_id"));
                }
            }
        } catch (Exception e) {
            LogUtil.info(this.getClass().getName(), "Error retrieving data " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                LogUtil.info(this.getClass().getName(), "Error retrieving data " + e.getMessage());
            }
        }

        return result;
    }

}
