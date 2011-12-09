import t.*;

public class T
{
  public static void main(String[] args) throws Exception {

        RefTest reftest = new RefTest();

        reftest.testPhantomReference();
        reftest.testWeakReference();
        reftest.testSoftReference();

        /*
        // make sure to run with -Dcom.sun.management.jmxremote
        // http://docs.oracle.com/javase/tutorial/jmx/overview/javavm.html

        System.out.printf(
            "SoftRefLRUPolicyMSPerMB=%s diagnosticOptions=%s%n",
            HotSpotDiagnostic.INSTANCE.SoftRefLRUPolicyMSPerMB(),
            HotSpotDiagnostic.INSTANCE.diagnosticOptions()
        );*/

  }
}