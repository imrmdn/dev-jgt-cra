package id.co.itasoft.telkom.oss.cra;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        //registrationList.add(context.registerService(MyPlugin.class.getName(), new MyPlugin(), null));
        registrationList.add(context.registerService(CraCreateService.class.getName(), new CraCreateService(), null));
        registrationList.add(context.registerService(CraCreateDetilService.class.getName(), new CraCreateDetilService(), null));
        registrationList.add(context.registerService(SeverityImpactedService.class.getName(), new SeverityImpactedService(), null));
        registrationList.add(context.registerService(DetailImpactActivityService.class.getName(), new DetailImpactActivityService(), null));
        registrationList.add(context.registerService(CallCreateTicketGamas.class.getName(), new CallCreateTicketGamas(), null));
        registrationList.add(context.registerService(UpdateStatusTicketGamas.class.getName(), new UpdateStatusTicketGamas(), null));
        registrationList.add(context.registerService(GenerateTokenCRA.class.getName(), new GenerateTokenCRA(), null));
        registrationList.add(context.registerService(InsertFlowApproval.class.getName(), new InsertFlowApproval(), null));
        registrationList.add(context.registerService(GroupTembusan.class.getName(), new GroupTembusan(), null));
        registrationList.add(context.registerService(SeverityImpactedServiceNew.class.getName(), new SeverityImpactedServiceNew(), null));
        registrationList.add(context.registerService(DetailImpactActivityServiceNew.class.getName(), new DetailImpactActivityServiceNew(), null));
        registrationList.add(context.registerService(GetDeviceByName.class.getName(), new GetDeviceByName(), null));
        registrationList.add(context.registerService(GetEditImpact.class.getName(), new GetEditImpact(), null));
        registrationList.add(context.registerService(CreateCraViewPermission.class.getName(), new CreateCraViewPermission(), null));
        registrationList.add(context.registerService(SelectTemplateCRA.class.getName(), new SelectTemplateCRA(), null));
        registrationList.add(context.registerService(GetDataTemplateCRA.class.getName(), new GetDataTemplateCRA(), null));
        registrationList.add(context.registerService(GetDataHC.class.getName(), new GetDataHC(), null));
        registrationList.add(context.registerService(GetDataApprover.class.getName(), new GetDataApprover(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}