package t;
import java.util.*;
import javax.management.*;
import java.lang.reflect.*;

public class HotSpotDiagnostic {
    // http://docs.oracle.com/javase/6/docs/jre/api/management/extension/com/sun/management/HotSpotDiagnosticMXBean.html

    static final HotSpotDiagnostic INSTANCE = new HotSpotDiagnostic();

    final String classHotSpotDiagnostic = "sun.management.HotSpotDiagnostic";
    final ObjectName ObjNameHotSpotDiagnostic;
    final MBeanServer mbeanServer;
    final ObjectInstance hotSpotDiagnostic;

    public HotSpotDiagnostic() {
        try {
            this.ObjNameHotSpotDiagnostic = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
            this.mbeanServer = MBeanServerFactory.createMBeanServer();
            this.hotSpotDiagnostic = this.mbeanServer.createMBean(classHotSpotDiagnostic, ObjNameHotSpotDiagnostic);
        } catch(RuntimeException ex) {throw ex;}
        catch(Exception ex) {throw new RuntimeException(ex);}
    }

    public String SoftRefLRUPolicyMSPerMB() {
        /*
        http://www.oracle.com/technetwork/java/gc-tuning-5-138395.html

        Soft references are cleared less aggressively in the server virtual
        machine than the client. The rate of clearing can be slowed by
        increasing the parameter SoftRefLRUPolicyMSPerMB with the command
        line flag -XX:SoftRefLRUPolicyMSPerMB=10000.
        SoftRefLRUPolicyMSPerMB is a measure of the time that a soft
        reference survives for a given amount of free space in the heap.
        The default value is 1000 ms per megabyte.

        This can be read to mean that a soft reference will survive
        (after the last strong reference to the object has been collected)
        for 1 second for each megabyte of free space in the heap.
        This is very approximate.

        http://www.oracle.com/technetwork/java/hotspotfaq-138619.html#gc_softrefs
        What determines when softly referenced objects are flushed?

        Starting with 1.3.1, softly reachable objects will remain alive for
        some amount of time after the last time they were referenced. The
        default value is one second of lifetime per free megabyte in the heap.
        This value can be adjusted using the -XX:SoftRefLRUPolicyMSPerMB flag,
        which accepts integer values representing milliseconds. For example,
        to change the value from one second to 2.5 seconds, use this flag:

        -XX:SoftRefLRUPolicyMSPerMB=2500

        The Java HotSpot Server VM uses the maximum possible heap size
        (as set with the -Xmx option) to calculate free space remaining.

        The Java Hotspot Client VM uses the current heap size to calculate
        the free space.

        This means that the general tendency is for the Server VM to grow
        the heap rather than flush soft references, and -Xmx therefore has
        a significant effect on when soft references are garbage collected.

        On the other hand, the Client VM will have a greater tendency to
        flush soft references rather than grow the heap.

        The behavior described above is true for 1.3.1 through Java SE 6
        versions of the Java HotSpot VMs. This behavior is not part of the
        VM specification, however, and is subject to change in future releases.
        Likewise the -XX:SoftRefLRUPolicyMSPerMB flag is not guaranteed to
        be present in any given release.
        */
        return getVMOption("SoftRefLRUPolicyMSPerMB");
    }

    public String getVMOption(String... params) {
        try {
            // http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/CompositeDataSupport.html
            Object compositeDataSupport = invoke("getVMOption",params,new String[] {"java.lang.String"});
            Method mget = compositeDataSupport.getClass().getMethod("get",String.class);
            return String.valueOf(mget.invoke(compositeDataSupport,"value"));
            /*
            StringBuilder sb = new StringBuilder(128);
            sb.append("name=").append(mget.invoke(compositeDataSupport,"name"))
            .append(",value=").append(mget.invoke(compositeDataSupport,"value"))
            .append(",origin=").append(mget.invoke(compositeDataSupport,"origin"))
            .append(",writeable=").append(mget.invoke(compositeDataSupport,"writeable"));
            */
        } catch(RuntimeException e) {throw e;}
        catch(Exception e) {throw new RuntimeException(e);}
    }

    public List<Map<String,String>> diagnosticOptions() {
        List<Map<String,String>> alist = new ArrayList<Map<String,String>>();

        try {
            String[] attrs = {"name","value","origin","writeable"};

            for(Object opt : (Object[])attribute("DiagnosticOptions")) {
                Method mget = opt.getClass().getMethod("get",String.class);
                Map<String,String> hm = new HashMap<String,String>(4);

                for(String attr : attrs)
                    hm.put(attr,
                           String.valueOf(mget.invoke(opt,attr))
                          );

                alist.add(hm);
            }
        } catch(RuntimeException e) {throw e;}
        catch(Exception e) {throw new RuntimeException(e);}

        return alist;
    }

    public Object invoke(
        String operationName,
        Object[] params,
        String[] signature) {
        try {
            return mbeanServer.invoke(
                       ObjNameHotSpotDiagnostic,
                       operationName,
                       params,
                       signature
                   );
        } catch(RuntimeException e) {throw e;}
        catch(Exception e) {throw new RuntimeException(e);}
    }

    public Object attribute(String name) {
        try {
            return mbeanServer.getAttribute(
                       ObjNameHotSpotDiagnostic,
                       name
                   );
        } catch(RuntimeException e) {throw e;}
        catch(Exception e) {throw new RuntimeException(e);}
    }

    public void print_all_methods(Object klass) {
        try {
            Class<?> c = klass.getClass();

            for(Method m : c.getMethods()) {
                System.out.printf("%s:%s%n",c,m);
            }
        } catch(RuntimeException e) {throw e;}
        catch(Exception e) {throw new RuntimeException(e);}
    }
}