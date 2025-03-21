package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.MappingFlowApprovalDao;
import id.co.itasoft.telkom.oss.cra.model.*;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetDataApprover extends Element implements PluginWebSupport {
    private static final String PLUGIN_NAME = "TELKOM - CRA - Get Approver";
    private static final String NIK = "nik";
    private static final String SHORT_POSITION = "short_position";
    private static final String NAME = "name";
    private static final String FIELD_FLAGGING = "flagging";

    private static final String OFF1_CRA = "OFF 1 CRA";
    private static final String OFF1_CSO = "OFF 1 CSO";
    private static final String MANAGER_CRA = "MANAGER CRA";
    private static final String MANAGER_CSO = "MANAGER CSO";
    private static final String MANAGER_RMO_TERKAIT = "MANAGER RMO TERKAIT";
    private static final String EGM_DSO = "EGM DSO";
    private static final String OSM_NSQ = "OSM NSQ";
    private static final String OSM_RMO = "OSM RMO";
    private static final String MANAGER_TERKAIT = "MANAGER TERKAIT";
    private static final String BAND2_PENGAJU = "BAND 2 PENGAJU";

    @Override
    public String renderTemplate(FormData formData, Map map) {
        return "";
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return PLUGIN_NAME;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String recordId = getRecordId(request);
        JSONObject mainObj = new JSONObject();

        if (recordId == null || recordId.isEmpty()) {
            sendErrorResponse(response, mainObj, "Parameter 'recordId' is required.");
            return;
        }

        if (!workflowUserManager.isCurrentUserAnonymous()) {
            MappingFlowApprovalDao mfa = new MappingFlowApprovalDao();

            try {
                String domainParam = mfa.getDomain(recordId);

                List<LoadApproverManagerModel> managers = mfa.getMgrRMO(domainParam);
                List<LoadApproverOsmRmoModel> osmRmos = mfa.getOsmRmo(domainParam);
                List<GetOtherApproverModel> getOtherApprovers = mfa.getOtherApprover(OFF1_CRA);
                List<GetOtherApproverModel> getOtherApproverCso = mfa.getOtherApprover(OFF1_CSO);
                List<GetOtherApproverModel> getOtherApproverManagerCra = mfa.getOtherApprover(MANAGER_CRA);
                List<GetOtherApproverModel> getOtherApproverManagerCso = mfa.getOtherApprover(MANAGER_CSO);
                List<GetOtherApproverModel> getOtherApproverEgmDso = mfa.getOtherApprover(EGM_DSO);
                List<GetOtherApproverModel> getOtherApproverOsmNsq = mfa.getOtherApprover(OSM_NSQ);
                List<LoadApproverBand2Model> getOtherApproverBand2Pengaju = mfa.getBand2Pengaju(recordId);
                List<LoadApproverMgrTerkaitModel> getOtherApproverMgrTerkait = mfa.getMgrTerkait(recordId);

                JSONArray dataArray = getJsonArray(
                        recordId,
                        managers,
                        osmRmos,
                        getOtherApprovers,
                        getOtherApproverCso,
                        getOtherApproverManagerCra,
                        getOtherApproverManagerCso,
                        getOtherApproverEgmDso,
                        getOtherApproverOsmNsq,
                        getOtherApproverBand2Pengaju,
                        getOtherApproverMgrTerkait);

                mainObj.put("data", dataArray);
                mainObj.write(response.getWriter());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                mainObj.put("status", false);
                mainObj.put("message", "You Must Login First.");
                mainObj.write(response.getWriter());
            } catch (JSONException ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error : " + ex.getMessage());
            }
        }
    }

    private static JSONArray getJsonArray(String recordId,
                                          List<LoadApproverManagerModel> managers,
                                          List<LoadApproverOsmRmoModel> osmRmos,
                                          List<GetOtherApproverModel> getOtherApprovers,
                                          List<GetOtherApproverModel> getOtherApproverCso,
                                          List<GetOtherApproverModel> getOtherApproverManagerCra,
                                          List<GetOtherApproverModel> getOtherApproverManagerCso,
                                          List<GetOtherApproverModel> getOtherApproverEgmDso,
                                          List<GetOtherApproverModel> getOtherApproverOsmNsq,
                                          List<LoadApproverBand2Model> getOtherApproverBand2Pengaju,
                                          List<LoadApproverMgrTerkaitModel> getOtherApproverMgrTerkait) throws JSONException {

        // Retrieve the approver data by severity, category and flow
        MappingFlowApprovalDao mfa = new MappingFlowApprovalDao();
        JSONObject approverData = mfa.getFlowApprover(recordId);
        String severity = approverData.optString("severity");
        String category = approverData.optString("category");
        String flow = approverData.optString("flow");
        JSONArray dataArray = new JSONArray();

        if (("Preventive".equalsIgnoreCase(category) || "Predictive".equalsIgnoreCase(category)) &&
                "Minor".equalsIgnoreCase(severity) &&
                "Flow DSO".equalsIgnoreCase(flow)) {

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

        } else if (("Preventive".equalsIgnoreCase(category) || "Predictive".equalsIgnoreCase(category))
                && ("Major".equalsIgnoreCase(severity) || "Moderate".equalsIgnoreCase(severity))
                && "Flow DSO".equalsIgnoreCase(flow)) {

            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }
        } else if (("Preventive".equalsIgnoreCase(category) || "Predictive".equalsIgnoreCase(category))
                && ("Critical".equalsIgnoreCase(severity))
                && "Flow DSO".equalsIgnoreCase(flow)) {

            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            //OSM ROC

            //SME GROUP

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

            //DEGM

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, EGM_DSO);
                dataArray.put(getOtherApproveObj);
            }

        } else if ("Preventive".equalsIgnoreCase(category) && "Minor".equalsIgnoreCase(severity) && "Flow Regional".equalsIgnoreCase(flow)) {
            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

        } else if ("Predictive".equalsIgnoreCase(category) && "Minor".equalsIgnoreCase(severity) && "Flow Regional".equalsIgnoreCase(flow)) {
            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

        } else if ("Emergency".equalsIgnoreCase(category) && "Minor".equalsIgnoreCase(severity) && "Flow Regional".equalsIgnoreCase(flow)) {
            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }
        } else if ("Emergency".equalsIgnoreCase(category) && ("Major".equalsIgnoreCase(severity) || "Moderate".equalsIgnoreCase(severity)) && "Flow Regional".equalsIgnoreCase(flow)) {
            //OSM SPM
            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

            //DEGM

        } else if ("Emergency".equalsIgnoreCase(category) && "Critical".equalsIgnoreCase(severity) && "Flow Regional".equalsIgnoreCase(flow)) {
            //OSM ROC
            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            //SME GROUP

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, EGM_DSO);
                dataArray.put(getOtherApproveObj);
            }

        } else if (("Preventive".equalsIgnoreCase(category) || "Predictive".equalsIgnoreCase(category)) && "Minor".equalsIgnoreCase(severity) && "Flow DSO".equalsIgnoreCase(flow)) {
            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

        } else if (("Preventive".equalsIgnoreCase(category) || "Predictive".equalsIgnoreCase(category))
                && ("Major".equalsIgnoreCase(severity) || "Moderate".equalsIgnoreCase(severity))
                && "Flow DSO".equalsIgnoreCase(flow)) {

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

            //DEGM
        } else if (("Preventive".equalsIgnoreCase(category) || "Predictive".equalsIgnoreCase(category))
                && ("Critical".equalsIgnoreCase(severity) ) && "Flow DSO".equalsIgnoreCase(flow)) {

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            //OSM ROC

            //SME GROUP

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

            //DEGM

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, EGM_DSO);
                dataArray.put(getOtherApproveObj);
            }
        } else if ("Emergency".equalsIgnoreCase(category) && "Minor".equalsIgnoreCase(severity) && "Flow DSO".equalsIgnoreCase(flow)) {

            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

        } else if ("Emergency".equalsIgnoreCase(category)
                && ("Major".equalsIgnoreCase(severity) || "Moderate".equalsIgnoreCase(severity) ) && "Flow DSO".equalsIgnoreCase(flow)) {

            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

        } else if ("Emergency".equalsIgnoreCase(category)
                && ("Critical".equalsIgnoreCase(severity) ) && "Flow DSO".equalsIgnoreCase(flow)) {
            //OSM_ROC

            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            //SME GROUP

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }

            //DEGM

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, EGM_DSO);
                dataArray.put(getOtherApproveObj);
            }
        } else if ("Flow DSO".equalsIgnoreCase(flow) || "Others".equalsIgnoreCase(flow)) {
            for (LoadApproverMgrTerkaitModel getMgrTerkait : getOtherApproverMgrTerkait) {
                JSONObject mgrTerkaitObj = new JSONObject();
                mgrTerkaitObj.put(NIK, getMgrTerkait.getNik());
                mgrTerkaitObj.put(SHORT_POSITION, getMgrTerkait.getShortPosition());
                mgrTerkaitObj.put(NAME, getMgrTerkait.getName());
                mgrTerkaitObj.put(FIELD_FLAGGING, MANAGER_TERKAIT);
                dataArray.put(mgrTerkaitObj);
            }

            for (LoadApproverBand2Model getBand2Pengaju : getOtherApproverBand2Pengaju) {
                JSONObject band2PengajuObj = new JSONObject();
                band2PengajuObj.put(NIK, getBand2Pengaju.getNik());
                band2PengajuObj.put(SHORT_POSITION, getBand2Pengaju.getShortPosition());
                band2PengajuObj.put(NAME, getBand2Pengaju.getName());
                band2PengajuObj.put(FIELD_FLAGGING, BAND2_PENGAJU);
                dataArray.put(band2PengajuObj);
            }

            for (LoadApproverManagerModel manager : managers) {
                JSONObject managerObj = new JSONObject();
                managerObj.put(NIK, manager.getNik());
                managerObj.put(SHORT_POSITION, manager.getShortPosition());
                managerObj.put(NAME, manager.getName());
                managerObj.put(FIELD_FLAGGING, MANAGER_RMO_TERKAIT);
                dataArray.put(managerObj);
            }

            for (LoadApproverOsmRmoModel osmRmo : osmRmos) {
                JSONObject osmRmoObj = new JSONObject();
                osmRmoObj.put(NIK, osmRmo.getNik());
                osmRmoObj.put(SHORT_POSITION, osmRmo.getShortPosition());
                osmRmoObj.put(NAME, osmRmo.getName());
                osmRmoObj.put(FIELD_FLAGGING, OSM_RMO);
                dataArray.put(osmRmoObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApprovers) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OFF1_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCra) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CRA);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverManagerCso) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, MANAGER_CSO);
                dataArray.put(getOtherApproveObj);
            }

            for (GetOtherApproverModel getOtherApprover : getOtherApproverOsmNsq) {
                JSONObject getOtherApproveObj = new JSONObject();
                getOtherApproveObj.put(NIK, getOtherApprover.getNik());
                getOtherApproveObj.put(SHORT_POSITION, getOtherApprover.getShortPosition());
                getOtherApproveObj.put(NAME, getOtherApprover.getName());
                getOtherApproveObj.put(FIELD_FLAGGING, OSM_NSQ);
                dataArray.put(getOtherApproveObj);
            }
        }

        return dataArray;
    }

    private String getRecordId(HttpServletRequest request) {
        String recordId = request.getHeader("recordId");
        return (recordId != null && !recordId.isEmpty()) ? recordId : request.getParameter("recordId");
    }

    private void sendErrorResponse(HttpServletResponse response, JSONObject mainObj, String message) throws IOException {
        try {
            mainObj.put("status", false);
            mainObj.put("message", message);
            mainObj.write(response.getWriter());
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Error while sending response: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public String getLabel() {
        return PLUGIN_NAME;
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }
}
