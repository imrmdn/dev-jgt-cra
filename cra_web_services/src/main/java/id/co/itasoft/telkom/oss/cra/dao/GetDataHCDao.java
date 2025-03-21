/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class GetDataHCDao {

    private static final String queryHCCompCode = "select distinct c_v_company_code from app_fd_cra_master_hc where c_flag_chief is not null";
    private static final String queryHCShortUnit = "select distinct c_short_divisi from app_fd_cra_master_hc where c_flag_chief is not null";
    private static final String queryHCNamaUnitParent = "select distinct c_nama_unit_parent, c_short_divisi from app_fd_cra_master_hc where c_flag_chief is not null";
    private static final String queryHC = "select * from app_fd_cra_master_hc";

    // Method that executes the query and returns data as JSON
    public JSONObject getDataHC(String table, String filter) throws JSONException {
    DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    JSONObject jsonResponse = new JSONObject();
    JSONArray dataArray = new JSONArray();
    
    // Select query based on table parameter
    String query;
    switch (table.toLowerCase()) {
        case "compcode":
            query = queryHCCompCode;
            break;
        case "shortunit":
            if(filter.isEmpty() || filter.isBlank()){
                query = queryHCShortUnit;
            }else{
                filter = '%'+filter+'%';
                query = queryHCShortUnit + " and c_v_company_code like '" + filter + "'";
            }
            break;
        case "unitparent":
            String[] pisahFIlter = filter.split("\\;");
            String divisi = "";
            String company = "";
            if (pisahFIlter.length == 2) {
                String pisahDivisi = pisahFIlter[0].trim();
                String pisahCompany = pisahFIlter[1].trim();

                divisi = pisahDivisi;
                company = pisahCompany;
            }
            
            if(filter.isEmpty() || filter.isBlank()){
                query = queryHCNamaUnitParent;
            }else{
                String filterDivisi = '%'+divisi+'%';
                String filterCompany = '%'+company+'%';
                query = queryHCNamaUnitParent + " and c_v_company_code like '" + filterCompany + "' and c_short_divisi like '" + filterDivisi+"'";
            }
            break;
        case "datahc":
            String[] pisahFilterHC = filter.split("\\;");
            String unitHC = "";
            String divisiHC = "";
            String companyHC = "";
            if (pisahFilterHC.length == 3) {
                String pisahUnit = pisahFilterHC[0].trim();
                String pisahDivisi = pisahFilterHC[1].trim();
                String pisahCompany = pisahFilterHC[2].trim();

                unitHC = pisahUnit;
                divisiHC = pisahDivisi;
                companyHC = pisahCompany;
            }
            
            if(filter.isEmpty() || filter.isBlank()){
                query = queryHC;
            }else{
                String filterUnit = '%'+unitHC+'%';
                String filterDivisi = '%'+divisiHC+'%';
                String filterCompany = '%'+companyHC+'%';
                query = queryHC + " where c_v_company_code like '" + filterCompany + "' and c_short_divisi like '" + filterDivisi + "' and c_nama_unit_parent like '"+ filterUnit+"'";
            }
            break;
        case "datahcsearch":            
            if(filter.isEmpty() || filter.isBlank()){
                query = queryHCCompCode;
            }else{
                String filterUnit = '%'+filter+'%';
                String filterDivisi = '%'+filter+'%';
                String filterCompany = '%'+filter+'%';
                String filterNama = '%'+filter+'%';
                query = queryHC + " where c_v_company_code like '" + filterCompany.toUpperCase() + "' or c_short_divisi like '" + filterDivisi.toUpperCase() + "' or c_nama_unit_parent like '"+ filterUnit.toUpperCase()+"' or c_nama_karyawan like '"+ filterNama.toUpperCase()+"'";
                LogUtil.info("Query", query);
            }
            break;
        default:
            throw new IllegalArgumentException("Invalid table parameter");
    }

    try {
        con = ds.getConnection();
        ps = con.prepareStatement(query);
        rs = ps.executeQuery();

        // Process results based on query type
        while (rs.next()) {
            JSONObject dataObject = new JSONObject();
            
            switch (table.toLowerCase()) {
                case "compcode":
                    dataObject.put("C_V_COMPANY_CODE", rs.getString("c_v_company_code"));
                    break;
                    
                case "shortunit":
                    dataObject.put("C_SHORT_DIVISI", rs.getString("c_short_divisi"));
                    break;
                    
                case "unitparent":
                    dataObject.put("C_NAMA_UNIT_PARENT", rs.getString("c_nama_unit_parent"));
                    break;
                    
                case "datahc":
                case "datahcsearch":
                    dataObject.put("function_unit", rs.getString("c_function_unit"));
                    dataObject.put("tahun", rs.getString("c_tahun"));
                    dataObject.put("flag_chief", rs.getString("c_flag_chief"));
                    dataObject.put("nik_atasan", rs.getString("c_nik_atasan"));
                    dataObject.put("band_posisi", rs.getString("c_band_posisi"));
                    dataObject.put("short_unit", rs.getString("c_short_unit"));
                    dataObject.put("nik", rs.getString("c_nik"));
                    dataObject.put("short_divisi", rs.getString("c_short_divisi"));
                    dataObject.put("v_company_code", rs.getString("c_v_company_code"));
                    dataObject.put("nama_karyawan", rs.getString("c_nama_karyawan"));
                    dataObject.put("nama_unit_parent", rs.getString("c_nama_unit_parent"));
                    dataObject.put("company_code", rs.getString("c_company_code"));
                    dataObject.put("bulan", rs.getString("c_bulan"));
                    dataObject.put("short_posisi", rs.getString("c_short_posisi"));
                    dataObject.put("nama_atasan", rs.getString("c_nama_atasan"));
                    break;
            }
            dataArray.put(dataObject);
        }

        jsonResponse.put("total", dataArray.length());
        jsonResponse.put("data", dataArray);

    } catch (SQLException e) {
        e.printStackTrace();
        jsonResponse.put("error", "Database error: " + e.getMessage());
    } finally {
        closeQuietly(rs);
        closeQuietly(ps);
        closeQuietly(con);
    }
    
    return jsonResponse;
}

    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Log error jika penutupan gagal, tapi jangan crash aplikasi
                LogUtil.error(this.getClass().getName(), e, "Failed to close resource: " + e.getMessage());
            }
        }
    }

}
