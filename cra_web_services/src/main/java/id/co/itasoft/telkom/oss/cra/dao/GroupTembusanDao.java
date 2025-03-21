/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.joget.commons.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class GroupTembusanDao {

    private static final String API_KEY = "108692c1db6b462b9027e442ce8e6c26";
    private static final String API_ID = "API-3154b489-51b7-497e-89bc-a0d44778f4de";

    public String ForAPI() throws JSONException {
        String hasil = "";
        try {
            // Memanggil API pertama
            JSONObject groupResponse = callAPI("https://person-dev-insera-dev.apps.cpaasstl1.telkom.co.id/jw/web/json/plugin/co.id.telkom.person.plugin.GetListGroupByType/service", "POST", "{\"transaction_type\": \"CRA\"}");
            LogUtil.info("RESP", "OUTPUT: " + groupResponse.toString());
            // Mendapatkan data Person Group
            JSONArray groups = groupResponse.getJSONArray("Info");

            // List untuk menyimpan hasil JSON
            List<JSONObject> outputList = new ArrayList<>();

            // Memproses setiap grup
            for (int i = 0; i < groups.length(); i++) {
                JSONObject group = groups.getJSONObject(i);
                String groupName = group.getString("Person Group");

                // Memanggil API kedua untuk grup saat ini
                JSONObject userResponse = callAPI("https://person-dev-insera-dev.apps.cpaasstl1.telkom.co.id/jw/web/json/plugin/co.id.telkom.person.plugin.GetListUsers/service", "POST", "{\"person_group_name\": \"" + groupName + "\"}");
                JSONArray users = userResponse.getJSONArray("data").getJSONObject(0).getJSONArray("person_code");

                // Membuat objek JSON untuk grup saat ini
                JSONObject groupObj = new JSONObject();
                groupObj.put("group_name", groupName);
                groupObj.put("person_codes", users);

                // Menambahkan objek JSON ke dalam list output
                outputList.add(groupObj);
            }

            // Membuat objek JSON utama
            JSONObject mainObj = new JSONObject();
            mainObj.put("person_groups", outputList);

            // Mengonversi objek JSON utama ke dalam format JSON
            String jsonOutput = mainObj.toString();
            System.out.println("OUTPUT: " + jsonOutput);
            hasil = jsonOutput;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return hasil;
    }

    private static JSONObject callAPI(String apiUrl, String method, String body) throws IOException, JSONException {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("api_id", API_ID);
        conn.setRequestProperty("api_key", API_KEY);
        conn.setDoOutput(true);

        if (body != null) {
            try ( OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }

        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line.trim());
            }
            return new JSONObject(response.toString());
        } finally {
            conn.disconnect();
        }
    }

}
