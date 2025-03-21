/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.telkom.oss.cra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.joget.apps.app.lib.RulesDecisionPlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.DecisionResult;

/**
 *
 * @author Asani
 */
public class RouteFlow extends RulesDecisionPlugin {

    private String pluginName = "Telkom - CRA - Penentuan Flow dengan Route";

    @Override
    public String getName() {
        return pluginName;
    }

    @Override
    public String getVersion() {
        return "7.0.0";
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
        return AppUtil.readPluginResource(
                getClass().getName(), "/properties/RouteProperties.json", null, true, null);
    }

    DecisionResult decision;
    Integer dataChecker;

    @Override
    public DecisionResult getDecision(
            String processDefId, String processId, String routeId, Map<String, String> variables) {
        decision = new DecisionResult();

        try {

            String recordId = getPropertyString("record_id");
            LogUtil.info(this.getClass().getName(), "RECORD ID " + recordId);
            String route = getPropertyString("route_id");
            LogUtil.info(this.getClass().getName(), "ROUTE ID " + route);
            String cat = "";
            String sev = "";
            String imp = "";
            List<String> results = getData(recordId);
            List<String> resultsCat = getCategory(recordId);
            String approval = getApproval(recordId);

            String impactTsel = results.get(0);
            String catchSeverity = results.get(1);
            String catchCategory = resultsCat.get(0);
            String chooseFlow = resultsCat.get(1);
//            String catchCategory = getCategory(recordId);
            LogUtil.info(this.getClass().getName(), "DATA FLOW: " + impactTsel + "|" + catchCategory + "|" + catchSeverity + "|" + chooseFlow);

            switch (route) {
                case "route7":
                    // Flow DSO - Preventive, Major, Impact
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("major") || catchSeverity.equalsIgnoreCase("moderate")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Major Impact");
                        //Flow Regional Preventive Minor Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Minor Impact");
                        //Flow DSO Preventive Minor
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Minor");
                        //Flow DSO Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Critical Impact");
                        //Flow DSO Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
// ganti dulu ke dso preventive                        decision.addTransition("Flow DSO Emergency Minor Impact");
                        decision.addTransition("Flow DSO Preventive Minor");
                        //Flow DSO Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Emergency Moderate Impact");
                        //Flow Regional Preventive Moderate Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Moderate Impact");
                        //Flow Regional Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Critical Impact");
                        //Flow Regional Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Minor Impact");
                        //Flow Regional Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Moderate Impact");
                        //Flow Regional Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Critical Impact");
                         //Flow DSO Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Emergency Critical Impact");
                    } else {
                        decision.addTransition("Flow Biasa");
                    }
                    break;
                case "route2":
                    //Flow Regional Preventive Critical Impact
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Critical Impact");
                    } else {
                        decision.addTransition("Flow Biasa");
                    }
                    break;
                case "route3":
                    //Flow Regional Preventive Minor Impact
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Minor Impact");
                        //Flow DSO Preventive Minor
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Minor");
                        // Flow DSO - Preventive, Major, Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("major") || catchSeverity.equalsIgnoreCase("moderate")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Major Impact");
                        //Flow DSO Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Critical Impact");
                        //Flow DSO Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
// ganti dulu ke dso preventive                       decision.addTransition("Flow DSO Emergency Minor Impact");
                        decision.addTransition("Flow DSO Preventive Minor");
                        //Flow Regional Preventive Moderate Impact
                    }  else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Emergency Moderate Impact");

                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Moderate Impact");
                        //Flow Regional Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Critical Impact");
                        //Flow Regional Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Minor Impact");
                        //Flow Regional Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Moderate Impact");
                        //Flow Regional Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Critical Impact");
                         //Flow DSO Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow Biasa");
                    }
                    break;
                case "route4":
                    //Flow Regional Preventive Minor Impact
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Minor Impact");
                        //Flow DSO Preventive Minor
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Minor");
                        // Flow DSO - Preventive, Major, Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("major") || catchSeverity.equalsIgnoreCase("moderate")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Major Impact");
                        //Flow DSO Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Critical Impact");
                        //Flow DSO Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
// ganti dulu ke dso preventive                       decision.addTransition("Flow DSO Emergency Minor Impact");
                        decision.addTransition("Flow DSO Preventive Minor");
                        //Flow DSO Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Emergency Moderate Impact");
                        //Flow Regional Preventive Moderate Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Moderate Impact");
                        //Flow Regional Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Critical Impact");
                        //Flow Regional Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Minor Impact");
                        //Flow Regional Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Moderate Impact");
                        //Flow Regional Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Critical Impact");
                         //Flow DSO Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow Biasa");
                    }
                    break;
                  // SME Group
                case "route14":
                        //Flow DSO Preventive Critical Impact
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("continue");
//                        LogUtil.info(pluginName, "Masuk Kondisi SME Group - " +catchCategory+catchSeverity+impactTsel);
                         //Flow DSO Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("continue");
                    }else{
                        decision.addTransition("continue");
                        LogUtil.info(pluginName, "Kondisi SME Group dilewati - "+catchCategory+catchSeverity+impactTsel);
                    }
                    break;
                case "route5":
                    //Flow Regional Preventive Minor Impact
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Minor Impact");
                        //Flow DSO Preventive Minor
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Minor");
                        // Flow DSO - Preventive, Major, Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("major") || catchSeverity.equalsIgnoreCase("moderate")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Major Impact");
                        //Flow DSO Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Critical Impact");
                        //Flow DSO Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
// ganti dulu ke dso preventive                       decision.addTransition("Flow DSO Emergency Minor Impact");
                        decision.addTransition("Flow DSO Preventive Minor");
                        //Flow DSO Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Emergency Moderate Impact");
                        //Flow Regional Preventive Moderate Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Moderate Impact");
                        //Flow Regional Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Critical Impact");
                        //Flow Regional Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Minor Impact");
                        //Flow Regional Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Moderate Impact");
                        //Flow Regional Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Critical Impact");
                         //Flow DSO Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow Biasa");
                    }
                    break;
                case "route6":
                    //Flow Regional Preventive Minor Impact
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Minor Impact");
                        //Flow DSO Preventive Minor
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Minor");
                        // Flow DSO - Preventive, Major, Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("major") || catchSeverity.equalsIgnoreCase("moderate")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Major Impact");
                        //Flow DSO Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Critical Impact");
                        //Flow DSO Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
// ganti dulu ke dso preventive                        decision.addTransition("Flow DSO Emergency Minor Impact");
                        decision.addTransition("Flow DSO Preventive Minort");
                        //Flow DSO Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Emergency Moderate Impact");
                        //Flow Regional Preventive Moderate Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Moderate Impact");
                        //Flow Regional Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Critical Impact");
                        //Flow Regional Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Minor Impact");
                        //Flow Regional Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Moderate Impact");
                        //Flow Regional Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Critical Impact");
                         //Flow DSO Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow Biasa");
                    }
                    break;
                case "route8":
                    // Preventive, Major, Impact
//                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("major") && impactTsel.equalsIgnoreCase("yes")) {
//                        decision.addTransition("Flow Preventive Major Impact");
                        // Flow DSO - Preventive, Major, Impact
//                    } else 
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("major") || catchSeverity.equalsIgnoreCase("moderate")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Major Impact");
                        //Flow DSO Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Critical Impact");
                        //i Add this in route8
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Emergency Moderate Impact");
                        //Flow Regional Preventive Moderate Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Moderate Impact");
                        //Flow Regional Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Critical Impact");
                        //Flow Regional Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Moderate Impact");
                        //Flow Regional Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Critical Impact");
                             //Flow DSO Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow Biasa");
                    } else {
                        // Flow Biasa Emergency, Critical, Impact Tsel
                        decision.addTransition("Flow Biasa");
                    }
                    break;
                case "route9":
                    //Kembali Ke Drafter
                    if (approval.equalsIgnoreCase("return")) {
                        decision.addTransition("Kembali Ke Drafter");
                        //Flow DSO Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
// ganti dulu ke dso preventive                        decision.addTransition("Flow DSO Emergency Minor Impact");
                        decision.addTransition("Flow DSO Preventive Minor");
                        //Flow DSO Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Emergency Moderate Impact");
                        //Flow Regional Emergency Minor Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("minor") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Minor Impact");
                        //Flow Regional Emergency Moderate Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && (catchSeverity.equalsIgnoreCase("moderate") || catchSeverity.equalsIgnoreCase("major")) && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Moderate Impact");
                    } else {
                        decision.addTransition("Lanjut Flow");
                    }
                    //Lanjut Flow
                    break;
                case "route10":
                        //Flow DSO Preventive Critical Impact
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Critical Impact");
                        //Flow Regional Preventive Critical Impact
                    } else if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Preventive Critical Impact");
                        LogUtil.info(pluginName, "Flow: " + route + " - Flow Regional Preventive Critical Impact");
                        //Flow Regional Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && (chooseFlow.equalsIgnoreCase("Flow Regional") || chooseFlow.equalsIgnoreCase("Others"))) {
                        decision.addTransition("Flow Regional Emergency Critical Impact");
                        LogUtil.info(pluginName, "Flow: " + route + " - Flow Regional Emergency Critical Impact");
                         //Flow DSO Emergency Critical Impact
                    } else if (catchCategory.equalsIgnoreCase("emergency") && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Emergency Critical Impact");
                    } else {
                        decision.addTransition("continue");
                        LogUtil.info(pluginName, "Flow: " + route + " - Continue");
                    }
                    break;
                case "route11":
                       //Flow DSO Preventive Critical Impact
                    if ((catchCategory.equalsIgnoreCase("preventive") || catchCategory.equalsIgnoreCase("predictive")) && catchSeverity.equalsIgnoreCase("critical") && impactTsel.equalsIgnoreCase("yes") && chooseFlow.equalsIgnoreCase("Flow DSO")) {
                        decision.addTransition("Flow DSO Preventive Critical Impact");
                    } else {
                        decision.addTransition("continue");
                    }
                    break;
                // Support Team
                case "route1":
                    String support = "no";
                    if (support.equalsIgnoreCase("yes")) {
                        decision.addTransition("Dengan Support Team");
                    } else {
                        decision.addTransition("Tanpa Support Team");
                    }
                    break;
            }
        } catch (Exception ex) {
            LogUtil.info(getClassName(), "Error: " + ex.getMessage());
        }

        return decision;
    }

    public List<String> getData(String recordId) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
//            String query = "SELECT c_impact_tsel, c_catch_severity FROM APP_FD_CRA_CREATE_ACT where "
//                    + "c_fk = (select c_child_id4 from app_fd_cra_create where id = ?)";
            String query = "SELECT \n"
                    + "    c_impact_tsel, \n"
                    + "    CASE \n"
                    + "        WHEN 'Critical' IN (SELECT c_catch_severity FROM APP_FD_CRA_CREATE_ACT WHERE c_fk = (SELECT c_child_id4 FROM app_fd_cra_create WHERE id = ?)) THEN 'Critical' \n"
                    + "        WHEN 'Moderate' IN (SELECT c_catch_severity FROM APP_FD_CRA_CREATE_ACT WHERE c_fk = (SELECT c_child_id4 FROM app_fd_cra_create WHERE id = ?)) THEN 'Moderate' \n"
                    + "        WHEN 'Major' IN (SELECT c_catch_severity FROM APP_FD_CRA_CREATE_ACT WHERE c_fk = (SELECT c_child_id4 FROM app_fd_cra_create WHERE id = ?)) THEN 'Moderate' \n"
                    + "        ELSE 'Minor' \n"
                    + "    END AS c_catch_severity \n"
                    + " FROM \n"
                    + "    APP_FD_CRA_CREATE_ACT \n"
                    + " WHERE \n"
                    + "    c_fk = (SELECT c_child_id4 FROM app_fd_cra_create WHERE id = ?)";
            ps = con.prepareStatement(query);
            ps.setString(1, recordId);
            ps.setString(2, recordId);
            ps.setString(3, recordId);
            ps.setString(4, recordId);
            rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("c_impact_tsel"));
                results.add(rs.getString("c_catch_severity"));
            }
        } catch (SQLException ex) {
            LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.info(this.getClass().getName(), "Error: " + ex.getMessage());
            }
        }

        return results;
    }

    public String getApproval(String recordId) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String results = "";

        try {
            con = ds.getConnection();
            String query = "SELECT c_approval FROM APP_FD_CRA_CREATE where c_approval is not null and (id = \n"
                    + "(select c_child_id from app_fd_cra_create_parent where id = ?)\n"
                    + "or id = ?)";
            ps = con.prepareStatement(query);
            ps.setString(1, recordId);
            ps.setString(2, recordId);
            rs = ps.executeQuery();
            if (rs.next()) {
                results = rs.getString(1);
            }
        } catch (SQLException ex) {
            LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.info(this.getClass().getName(), "Error: " + ex.getMessage());
            }
        }

        return results;
    }

    public List<String> getCategory(String recordId) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> results = new ArrayList<>();

        try {
            con = ds.getConnection();
            String query = "select C_MAPPING_CATEGORY, C_CHOOSE_FLOW from app_fd_cra_create_t1 where c_parent_id = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, recordId);
            rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("C_MAPPING_CATEGORY"));
                results.add(rs.getString("C_CHOOSE_FLOW"));
            }
        } catch (SQLException ex) {
            LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Error: " + ex.getMessage());
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.info(this.getClass().getName(), "Error: " + ex.getMessage());
            }
        }

        return results;
    }

}
