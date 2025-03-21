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
import java.util.UUID;

/**
 *
 * @author Asani
 */
public class SelectTemplateCRADao {

    public boolean SelectTemplate(String processId, String recordIdAcuan, String userLogin) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            con = ds.getConnection();
            // First delete existing record if it exists
            String deleteSql = "DELETE FROM app_fd_cra_create WHERE id = ?";
            ps = con.prepareStatement(deleteSql);
            ps.setString(1, processId);
            ps.executeUpdate();
            ps.close();

            //T1
            String deleteSqlt1 = "DELETE FROM app_fd_cra_create_t1 WHERE c_parent_id = ?";
            ps = con.prepareStatement(deleteSqlt1);
            ps.setString(1, processId);
            ps.executeUpdate();
            ps.close();

            //T2
            String deleteSqlt2 = "DELETE FROM app_fd_cra_create_t2 WHERE c_parent_id = ?";
            ps = con.prepareStatement(deleteSqlt2);
            ps.setString(1, processId);
            ps.executeUpdate();
            ps.close();

            //T3
            String deleteSqlt3 = "DELETE FROM app_fd_cra_create_t3 WHERE c_parent_id = ?";
            ps = con.prepareStatement(deleteSqlt3);
            ps.setString(1, processId);
            ps.executeUpdate();
            ps.close();

            //T4
            String deleteSqlt4 = "DELETE FROM app_fd_cra_create_t4 WHERE c_parent_id = ?";
            ps = con.prepareStatement(deleteSqlt4);
            ps.setString(1, processId);
            ps.executeUpdate();
            ps.close();

            //act
            String deleteSqlAct = "DELETE FROM app_fd_cra_create_act WHERE c_fk = (select id from app_fd_cra_create_t4 where c_parent_id = ?)";
            ps = con.prepareStatement(deleteSqlAct);
            ps.setString(1, processId);
            ps.executeUpdate();
            ps.close();

            //dod
            String deleteSqlDod = "DELETE FROM app_fd_cra_create_dod_detil WHERE c_parent_id = (select id from app_fd_cra_create_t3 where c_parent_id = ?)";
            ps = con.prepareStatement(deleteSqlDod);
            ps.setString(1, processId);
            ps.executeUpdate();
            ps.close();

            //metrix
            String deleteSqlMetrix = "DELETE FROM app_fd_cra_create_sm_detil WHERE c_parent_id = (select id from app_fd_cra_create_t3 where c_parent_id = ?)";
            ps = con.prepareStatement(deleteSqlMetrix);
            ps.setString(1, processId);
            ps.executeUpdate();
            ps.close();

            // Generate UUIDs
            String uniqueUUID1 = UUID.randomUUID().toString();
            String uniqueUUID2 = UUID.randomUUID().toString();
            String uniqueUUID3 = UUID.randomUUID().toString();
            String uniqueUUID4 = UUID.randomUUID().toString();
            String uniqueUUID5 = UUID.randomUUID().toString();
            String uniqueUUID6 = UUID.randomUUID().toString();

            // Insert into app_fd_cra_create
            String insertSql = "INSERT INTO app_fd_cra_create (id, dateCreated, dateModified, c_child_id1, c_child_id2, c_child_id3, c_child_id4, c_child_id5, c_child_id6, createdBy, modifiedBy) VALUES (?, SYSDATE, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(insertSql);
            ps.setString(1, processId);
            ps.setString(2, uniqueUUID1);
            ps.setString(3, uniqueUUID2);
            ps.setString(4, uniqueUUID3);
            ps.setString(5, uniqueUUID4);
            ps.setString(6, uniqueUUID5);
            ps.setString(7, uniqueUUID6);
            ps.setString(8, userLogin);
            ps.setString(9, userLogin);
            ps.executeUpdate();
            ps.close();

            // Get data from existing record for T1
            String selectSql = "SELECT c_cra_activity, c_cra_id, c_network_element, c_choose_flow, c_mapping_category "
                    + "FROM app_fd_cra_create_t1 WHERE c_parent_id = ?";
            ps = con.prepareStatement(selectSql);
            ps.setString(1, recordIdAcuan);
            rs = ps.executeQuery();

            if (rs.next()) {
                // Simpan data dari ResultSet ke variable
                String craActivity = rs.getString("c_cra_activity");
                String craId = rs.getString("c_cra_id");
                String networkElement = rs.getString("c_network_element");
                String chooseFlow = rs.getString("c_choose_flow");
                String mappingCategory = rs.getString("c_mapping_category");

                // Tutup ResultSet dan PreparedStatement setelah mengambil data
                rs.close();
                ps.close();

                // Insert into app_fd_cra_create_t1
                String insertT1Sql = "INSERT INTO app_fd_cra_create_t1 "
                        + "(id, c_cra_activity, c_parent_id, c_network_element, c_choose_flow, c_mapping_category, dateCreated, dateModified, createdBy, modifiedBy) "
                        + "VALUES (?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE, ?, ?)";

                ps = con.prepareStatement(insertT1Sql);
                ps.setString(1, uniqueUUID1);
                ps.setString(2, craActivity);
                ps.setString(3, processId);
                ps.setString(4, networkElement);
                ps.setString(5, chooseFlow);
                ps.setString(6, mappingCategory);
                ps.setString(7, userLogin);
                ps.setString(8, userLogin);
                ps.executeUpdate();
                ps.close();
            }
            closeQuietly(ps);

            // Get data from existing record for T2
            String selectT2Sql = "SELECT c_tembusan_gn_person, c_tembusan_gn, c_penanggung_jawab_label, "
                    + "c_band_2_pengaju_label, c_mapping_severity, c_tujuan, c_judul, c_penanggung_jawab, "
                    + "c_tembusan, c_latar_belakang, c_dampak, c_mapping_category "
                    + "FROM app_fd_cra_create_t2 WHERE c_parent_id = ?";
            ps = con.prepareStatement(selectT2Sql);
            ps.setString(1, recordIdAcuan);
            rs = ps.executeQuery();

            if (rs.next()) {
                // Simpan data dari ResultSet ke variable
                String tembusanGnPerson = rs.getString("c_tembusan_gn_person");
                String tembusanGn = rs.getString("c_tembusan_gn");
                String penanggungJawabLabel = rs.getString("c_penanggung_jawab_label");
                String band2PengajuLabel = rs.getString("c_band_2_pengaju_label");
                String mappingSeverity = rs.getString("c_mapping_severity");
                String tujuan = rs.getString("c_tujuan");
                String judul = rs.getString("c_judul");
                String penanggungJawab = rs.getString("c_penanggung_jawab");
                String tembusan = rs.getString("c_tembusan");
                String latarBelakang = rs.getString("c_latar_belakang");
                String dampak = rs.getString("c_dampak");
                String mappingCategory = rs.getString("c_mapping_category");

                // Tutup ResultSet dan PreparedStatement setelah mengambil data
                rs.close();
                ps.close();

                // Insert into app_fd_cra_create_t2
                String insertT2Sql = "INSERT INTO app_fd_cra_create_t2 "
                        + "(id, c_tembusan_gn_person, c_tembusan_gn, c_penanggung_jawab_label, "
                        + "c_band_2_pengaju_label, c_mapping_severity, c_tujuan, c_judul, c_penanggung_jawab, "
                        + "c_tembusan, c_latar_belakang, c_dampak, c_mapping_category, c_parent_id, dateCreated, dateModified, createdBy, modifiedBy) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE, ?, ?)";

                ps = con.prepareStatement(insertT2Sql);
                ps.setString(1, uniqueUUID2);
                ps.setString(2, tembusanGnPerson);
                ps.setString(3, tembusanGn);
                ps.setString(4, penanggungJawabLabel);
                ps.setString(5, band2PengajuLabel);
                ps.setString(6, mappingSeverity);
                ps.setString(7, tujuan);
                ps.setString(8, judul);
                ps.setString(9, penanggungJawab);
                ps.setString(10, tembusan);
                ps.setString(11, latarBelakang);
                ps.setString(12, dampak);
                ps.setString(13, mappingCategory);
                ps.setString(14, processId);
                ps.setString(15, userLogin);
                ps.setString(16, userLogin);

                ps.executeUpdate();
                ps.close();
            }
            closeQuietly(ps);

            // Get data from existing record for T3
            String selectT3Sql = "select c_mapping_category, c_mapping_severity, c_dod, c_prosedur_giat, c_detil_giat, "
                    + "c_rollback_skenario from APP_FD_CRA_CREATE_T3 where c_parent_id = ?";
            ps = con.prepareStatement(selectT3Sql);
            ps.setString(1, recordIdAcuan);
            rs = ps.executeQuery();

            if (rs.next()) {
                // Simpan data dari ResultSet ke variable
                String t3mappingCategory = rs.getString("c_mapping_category");
                String t3mappingSeverity = rs.getString("c_mapping_severity");
                String t3dod = rs.getString("c_dod");
                String t3prosedurGiat = rs.getString("c_prosedur_giat");
                String t3detailGiat = rs.getString("c_detil_giat");
                String t3rollbackSkenario = rs.getString("c_rollback_skenario");

                // Tutup ResultSet dan PreparedStatement setelah mengambil data
                rs.close();
                ps.close();

                // Insert into app_fd_cra_create_t3
                String insertT3Sql = "INSERT INTO app_fd_cra_create_t3 "
                        + "(id, c_mapping_category, c_mapping_severity, c_dod, "
                        + "c_prosedur_giat, c_detil_giat, c_rollback_skenario, "
                        + "c_parent_id, dateCreated, dateModified, createdBy, modifiedBy) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE, ?, ?)";

                ps = con.prepareStatement(insertT3Sql);
                ps.setString(1, uniqueUUID3);
                ps.setString(2, t3mappingCategory);
                ps.setString(3, t3mappingSeverity);
                ps.setString(4, t3dod);
                ps.setString(5, t3prosedurGiat);
                ps.setString(6, t3detailGiat);
                ps.setString(7, t3rollbackSkenario);
                ps.setString(8, processId);
                ps.setString(9, userLogin);
                ps.setString(10, userLogin);

                ps.executeUpdate();
                ps.close();
            }
            closeQuietly(ps);

            // Get data from existing record for T4
            String selectT4Sql = "select c_mapping_category from APP_FD_CRA_CREATE_T4 where c_parent_id = ?";
            ps = con.prepareStatement(selectT4Sql);
            ps.setString(1, recordIdAcuan);
            rs = ps.executeQuery();

            if (rs.next()) {
                // Simpan data dari ResultSet ke variable
                String t4mappingCategory = rs.getString("c_mapping_category");

                // Tutup ResultSet dan PreparedStatement setelah mengambil data
                rs.close();
                ps.close();

                // Insert into app_fd_cra_create_t4
                String insertT4Sql = "INSERT INTO app_fd_cra_create_t4 "
                        + "(id, c_mapping_category, "
                        + "c_parent_id, dateCreated, dateModified, createdBy, modifiedBy) "
                        + "VALUES (?, ?, ?, SYSDATE, SYSDATE, ?, ?)";

                ps = con.prepareStatement(insertT4Sql);
                ps.setString(1, uniqueUUID4);
                ps.setString(2, t4mappingCategory);
                ps.setString(3, processId);
                ps.setString(4, userLogin);
                ps.setString(5, userLogin);

                ps.executeUpdate();
                ps.close();
            }
            closeQuietly(ps);

            // Get data from existing record for act
            String selectActSql = "select c_waktu_start, c_waktu_selesai, c_impact_service, c_durasi_downtime, c_keterangan, c_infrastruktur, "
                    + "c_support_unit, c_supporting_unit, c_pic_monitoring, c_waspang, c_actdetil_counter, c_kebutuhan_support, "
                    + "c_no_hp_executor, c_no_hp_monitoring, c_executor, c_tanggal, c_impact_tsel, c_catch_severity, c_catch_category, "
                    + "c_berakhir, c_awal, c_mapping_category, c_due_date, c_witel, c_n_n_total_hvc, c_n_total_impact, c_n_total_non_hvc, c_temp_clob_column, "
                    + "c_masuk_ruangan, c_catch_impact, c_infrastruktur_temp, c_sto_code, c_regional, c_request_crq, c_ts_info_datin, c_ts_info_sdwan, "
                    + "c_ts_info_retail, c_ts_info_nodeb, c_support_unit_gn from APP_FD_CRA_CREATE_ACT where c_fk = (select id from app_fd_cra_create_t4 where c_parent_id = ?)";
            ps = con.prepareStatement(selectActSql);
            ps.setString(1, recordIdAcuan);
            rs = ps.executeQuery();
            while (rs.next()) {
                // Simpan data dari ResultSet ke variabel
                String actc_waktu_start = rs.getString("c_waktu_start");
                String actc_waktu_selesai = rs.getString("c_waktu_selesai");
                String actc_impact_service = rs.getString("c_impact_service");
                String actc_durasi_downtime = rs.getString("c_durasi_downtime");
                String actc_keterangan = rs.getString("c_keterangan");
                String actc_infrastruktur = rs.getString("c_infrastruktur");
                String actc_support_unit = rs.getString("c_support_unit");
                String actc_supporting_unit = rs.getString("c_supporting_unit");
                String actc_pic_monitoring = rs.getString("c_pic_monitoring");
                String actc_waspang = rs.getString("c_waspang");
                String actc_actdetil_counter = rs.getString("c_actdetil_counter");
                String actc_kebutuhan_support = rs.getString("c_kebutuhan_support");
                String actc_no_hp_executor = rs.getString("c_no_hp_executor");
                String actc_no_hp_monitoring = rs.getString("c_no_hp_monitoring");
                String actc_executor = rs.getString("c_executor");
                String actc_tanggal = rs.getString("c_tanggal");
                String actc_impact_tsel = rs.getString("c_impact_tsel");
                String actc_catch_severity = rs.getString("c_catch_severity");
                String actc_catch_category = rs.getString("c_catch_category");
                String actc_berakhir = rs.getString("c_berakhir");
                String actc_awal = rs.getString("c_awal");
                String actc_mapping_category = rs.getString("c_mapping_category");
                String actc_due_date = rs.getString("c_due_date");
                String actc_witel = rs.getString("c_witel");
                String actc_n_n_total_hvc = rs.getString("c_n_n_total_hvc");
                String actc_n_total_impact = rs.getString("c_n_total_impact");
                String actc_n_total_non_hvc = rs.getString("c_n_total_non_hvc");
                String actc_temp_clob_column = rs.getString("c_temp_clob_column");
                String actc_masuk_ruangan = rs.getString("c_masuk_ruangan");
                String actc_catch_impact = rs.getString("c_catch_impact");
                String actc_infrastruktur_temp = rs.getString("c_infrastruktur_temp");
                String actc_sto_code = rs.getString("c_sto_code");
                String actc_regional = rs.getString("c_regional");
                String actc_request_crq = rs.getString("c_request_crq");
                String actc_ts_info_datin = rs.getString("c_ts_info_datin");
                String actc_ts_info_sdwan = rs.getString("c_ts_info_sdwan");
                String actc_ts_info_retail = rs.getString("c_ts_info_retail");
                String actc_ts_info_nodeb = rs.getString("c_ts_info_nodeb");
                String actc_support_unit_gn = rs.getString("c_support_unit_gn");

                // Insert into app_fd_cra_create_act
                String insertActSql = "INSERT INTO app_fd_cra_create_act "
                        + "(id, c_fk, dateCreated, dateModified, "
                        + "c_waktu_start, c_waktu_selesai, c_impact_service, c_durasi_downtime, c_keterangan, "
                        + "c_infrastruktur, c_support_unit, c_supporting_unit, c_pic_monitoring, c_waspang, "
                        + "c_actdetil_counter, c_kebutuhan_support, c_no_hp_executor, c_no_hp_monitoring, c_executor, "
                        + "c_tanggal, c_impact_tsel, c_catch_severity, c_catch_category, c_berakhir, c_awal, "
                        + "c_mapping_category, c_due_date, c_witel, c_n_n_total_hvc, c_n_total_impact, c_n_total_non_hvc, "
                        + "c_temp_clob_column, c_masuk_ruangan, c_catch_impact, c_infrastruktur_temp, c_sto_code, "
                        + "c_regional, c_request_crq, c_ts_info_datin, c_ts_info_sdwan, c_ts_info_retail, "
                        + "c_ts_info_nodeb, c_support_unit_gn, createdBy, modifiedBy) "
                        + "VALUES (?, ?, SYSDATE, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                ps = con.prepareStatement(insertActSql);
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, uniqueUUID4);
                ps.setString(3, actc_waktu_start);
                ps.setString(4, actc_waktu_selesai);
                ps.setString(5, actc_impact_service);
                ps.setString(6, actc_durasi_downtime);
                ps.setString(7, actc_keterangan);
                ps.setString(8, actc_infrastruktur);
                ps.setString(9, actc_support_unit);
                ps.setString(10, actc_supporting_unit);
                ps.setString(11, actc_pic_monitoring);
                ps.setString(12, actc_waspang);
                ps.setString(13, actc_actdetil_counter);
                ps.setString(14, actc_kebutuhan_support);
                ps.setString(15, actc_no_hp_executor);
                ps.setString(16, actc_no_hp_monitoring);
                ps.setString(17, actc_executor);
                ps.setString(18, actc_tanggal);
                ps.setString(19, actc_impact_tsel);
                ps.setString(20, actc_catch_severity);
                ps.setString(21, actc_catch_category);
                ps.setString(22, actc_berakhir);
                ps.setString(23, actc_awal);
                ps.setString(24, actc_mapping_category);
                ps.setString(25, actc_due_date);
                ps.setString(26, actc_witel);
                ps.setString(27, actc_n_n_total_hvc);
                ps.setString(28, actc_n_total_impact);
                ps.setString(29, actc_n_total_non_hvc);
                ps.setString(30, actc_temp_clob_column);
                ps.setString(31, actc_masuk_ruangan);
                ps.setString(32, actc_catch_impact);
                ps.setString(33, actc_infrastruktur_temp);
                ps.setString(34, actc_sto_code);
                ps.setString(35, actc_regional);
                ps.setString(36, actc_request_crq);
                ps.setString(37, actc_ts_info_datin);
                ps.setString(38, actc_ts_info_sdwan);
                ps.setString(39, actc_ts_info_retail);
                ps.setString(40, actc_ts_info_nodeb);
                ps.setString(41, actc_support_unit_gn);
                ps.setString(42, userLogin);
                ps.setString(43, userLogin);

                int result = ps.executeUpdate();
                return result > 0;
            }
            closeQuietly(ps);

            // Get data from existing record for Dod
            String selectDodSql = "select c_definition from app_fd_cra_create_dod_detil where c_parent_id = (select id from app_fd_cra_create_t3 where c_parent_id = ?)";
            ps = con.prepareStatement(selectDodSql);
            ps.setString(1, recordIdAcuan);
            rs = ps.executeQuery();

            if (rs.next()) {
                // Simpan data dari ResultSet ke variable
                String dod_definition = rs.getString("c_definition");

                // Tutup ResultSet dan PreparedStatement setelah mengambil data
                rs.close();
                ps.close();

                // Insert into app_fd_cra_create_t4
                String insertDodSql = "INSERT INTO app_fd_cra_create_dod_detil "
                        + "(id, c_definition, "
                        + "c_parent_id, dateCreated, dateModified, createdBy, modifiedBy) "
                        + "VALUES (?, ?, ?, SYSDATE, SYSDATE, ?, ?)";

                ps = con.prepareStatement(insertDodSql);
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, dod_definition);
                ps.setString(3, uniqueUUID3);
                ps.setString(4, userLogin);
                ps.setString(5, userLogin);

                ps.executeUpdate();
                ps.close();
            }
            closeQuietly(ps);
            
            // Get data from existing record for Metrix
            String selectMetrixSql = "select c_success_metrix from app_fd_cra_create_sm_detil where c_parent_id = (select id from app_fd_cra_create_t3 where c_parent_id = ?)";
            ps = con.prepareStatement(selectMetrixSql);
            ps.setString(1, recordIdAcuan);
            rs = ps.executeQuery();

            if (rs.next()) {
                // Simpan data dari ResultSet ke variable
                String success_metrix = rs.getString("c_success_metrix");

                // Tutup ResultSet dan PreparedStatement setelah mengambil data
                rs.close();
                ps.close();

                // Insert into app_fd_cra_create_t4
                String insertMetrixSql = "INSERT INTO app_fd_cra_create_sm_detil "
                        + "(id, c_success_metrix, "
                        + "c_parent_id, dateCreated, dateModified, createdBy, modifiedBy) "
                        + "VALUES (?, ?, ?, SYSDATE, SYSDATE, ?, ?)";

                ps = con.prepareStatement(insertMetrixSql);
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, success_metrix);
                ps.setString(3, uniqueUUID3);
                ps.setString(4, userLogin);
                ps.setString(5, userLogin);

                ps.executeUpdate();
                ps.close();
            }
            closeQuietly(ps);
            
            return true;
        } catch (SQLException ex) {
            LogUtil.info(this.getClass().getName(), "Error: " + ex.getMessage());
        } finally {
            closeQuietly(con);
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return exists;
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
