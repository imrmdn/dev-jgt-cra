/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.CallCreateTicketGamasDao;
import id.co.itasoft.telkom.oss.cra.dao.SeverityImpactedServiceDao;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.json.JSONObject;

/**
 *
 * @author Asani
 */
public class CallCreateTicketGamas extends DefaultApplicationPlugin {

    String pluginName = "CRA - TEL - CRA Call Create Ticket GAMAS";
    String pluginClassName = this.getClass().getName();

//    @Override
//    public String renderTemplate(FormData arg0, Map arg1) {
//        return null;
//    }
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
    public Object execute(Map map) {
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        WorkflowAssignment workflowAssignment = (WorkflowAssignment) map.get("workflowAssignment");
        String processId = appService.getOriginProcessId(workflowAssignment.getProcessId());
        LogUtil.info(this.getClass().getName(), "Process ID: " + processId);
        CallCreateTicketGamasDao callCreateTicketGamasDao = new CallCreateTicketGamasDao();
        callCreateTicketGamasDao.callCreateTicket(processId);
        String jam_notif = callCreateTicketGamasDao.getJamNotifWa(processId);
        LogUtil.info(this.getClass().getName(), "Jam auto Start" + jam_notif);
        HashMap<String, String> wv = new HashMap<String, String>();
        wv.put("jam_notif", jam_notif);        
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

}
