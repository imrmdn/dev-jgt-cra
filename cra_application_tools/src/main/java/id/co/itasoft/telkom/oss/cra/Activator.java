package id.co.itasoft.telkom.oss.cra;

import id.co.itasoft.telkom.oss.cra.dao.CallNotifWADao;
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
        registrationList.add(context.registerService(RouteFlow.class.getName(), new RouteFlow(), null));
        registrationList.add(context.registerService(CallReqCRQ.class.getName(), new CallReqCRQ(), null));
        registrationList.add(context.registerService(CallNotifWA.class.getName(), new CallNotifWA(), null));
        registrationList.add(context.registerService(TokenValidatorSubmit.class.getName(), new TokenValidatorSubmit(), null));
        registrationList.add(context.registerService(MappingFlowApproval.class.getName(), new MappingFlowApproval(), null));
        registrationList.add(context.registerService(CallNotifWaGamas.class.getName(), new CallNotifWaGamas(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}