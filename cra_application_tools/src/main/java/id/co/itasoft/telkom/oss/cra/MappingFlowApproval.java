/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.MappingFlowApprovalDao;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;

/**
 *
 * @author Asani
 */
public class MappingFlowApproval extends DefaultApplicationPlugin {

    private final String pluginName = "TELKOM- CRA - Maping Flow Approval";
    private final String pluginClassName = this.getClass().getName();

    @Override
    public Object execute(Map map) {
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        WorkflowAssignment workflowAssignment = (WorkflowAssignment) map.get("workflowAssignment");
        String processId = appService.getOriginProcessId(workflowAssignment.getProcessId());
        String currentUser = WorkflowUtil.getCurrentUsername();
        LogUtil.info(pluginClassName, "Process ID: " + processId);

        MappingFlowApprovalDao mfa = new MappingFlowApprovalDao();

        String domainParam = mfa.getDomain(processId);
        String mgr_rmo = mfa.getMgrRMO(domainParam);
        String osm_rmo = mfa.getOSMRMO(domainParam);
        String off_cra = mfa.getOtherApprover("OFF 1 CRA");
        String off_cso = mfa.getOtherApprover("OFF 1 CSO");
        String manager_cra = mfa.getOtherApprover("MANAGER CRA");
        String manager_cso = mfa.getOtherApprover("MANAGER CSO");
        String degm = mfa.getPersonByJabatan("DEGM");
        String egm_dso = mfa.getOtherApprover("EGM DSO");
        String osm_nsq = mfa.getOtherApprover("OSM NSQ");
        String sme_group = mfa.getPersonByJabatan("SME GROUP");
        HashMap<String, String> wv = new HashMap<String, String>();
        wv.put("manager_rmo", mgr_rmo);
        wv.put("osm_rmo", osm_rmo);
        wv.put("off_cra", off_cra);
        wv.put("off_cso", off_cso);
        wv.put("manager_cra", manager_cra);
        wv.put("manager_cso", manager_cso);
        wv.put("osm_nsq", osm_nsq);
        wv.put("osm_roc", "010101"); //hardcode
        wv.put("degm", degm);
        wv.put("egm_dso", egm_dso);
        wv.put("sme_group", sme_group);

        /*SAVE VALUE TO WORKFLOW VARIABLE*/
        saveWorkFlowVariable(map, wv);
        return null;
    }

    public void saveWorkFlowVariable(Map map, HashMap<String, String> workflowVariable) {

        PluginManager pluginManager = (PluginManager) map.get("pluginManager");
        WorkflowAssignment workflowAssignment = (WorkflowAssignment) map.get("workflowAssignment");
        WorkflowManager wm = (WorkflowManager) pluginManager.getBean("workflowManager");

        for (Map.Entry<String, String> entry : workflowVariable.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            wm.activityVariable(workflowAssignment.getActivityId(), key, value);

        }
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
}
