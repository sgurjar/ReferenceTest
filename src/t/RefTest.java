package t;
import java.lang.ref.*;
import java.util.concurrent.*;
import java.util.*;

public class RefTest
{
    static class A {
        void sayHello() {
            System.out.println("Hello");
        }
    }

    public void testPhantomReference() {
        final String M = "testPhantomReference: ";

        A a = new A(); // strong ref

        final ReferenceQueue<A> q = new ReferenceQueue<A>();

        PhantomReference<A> phref = new PhantomReference<A>(a,q);

        new Thread(new Runnable() {
            public void run() {
                for(;;) {
                    Reference<? extends A> aref = q.poll();

                    if(aref!=null) {
                        System.out.printf(M +"collected ref=%s referent=%s %n",
                                          aref,
                                          aref.get()
                                         );
                        break;
                    }
                }
            }
        }).start();

        a.sayHello();

        a = null; // make unreachable via a strong ref

        memstat(M + "BEFORE allocate");

        // alloacate more memory then young gen can handle
        byte[] should_cause_gc = new byte[1024*1024*64]; // 64mb

        memstat(M + "AFTER allocate");
    }

    public void testWeakReference() {
        final String M = "testWeakReference: ";

        A a = new A(); // strong ref

        final ReferenceQueue<A> q = new ReferenceQueue<A>();

        WeakReference<A> wkref = new WeakReference<A>(a, q);

        new Thread(new Runnable() {
            public void run() {
                for(;;) {
                    Reference<? extends A> aref = q.poll();

                    if(aref!=null) {
                        System.out.printf(M+"collected ref=%s referent=%s %n",
                                          aref,
                                          aref.get()
                                         );
                        break;
                    }
                }
            }
        }).start();

        a.sayHello();

        a = null; // make unreachable via a strong ref

        memstat(M + "BEFORE allocate");

        // alloacate more memory then young gen can handle
        byte[] should_cause_gc = new byte[1024*1024*64]; // 64mb

        memstat(M + "AFTER allocate");
    }

    public void testSoftReference() throws Exception {
        final String M = "testSoftReference: ";

        A a = new A(); // strong ref

        final ReferenceQueue<A> q = new ReferenceQueue<A>();

        SoftReference<A> wkref = new SoftReference<A>(a, q);

        new Thread(new Runnable() {
            public void run() {
                for(;;) {
                    Reference<? extends A> aref = q.poll();

                    if(aref!=null) {
                        System.out.printf(M+"collected ref=%s referent=%s %n",
                                          aref,
                                          aref.get()
                                         );
                        break;
                    }
                }
            }
        }).start();

        a.sayHello();

        a = null; // make unreachable via a strong ref

        int N = 10;
        ArrayList<byte[]> alist = new ArrayList<byte[]>(N);

        for(int i=0; i < N; i++) {
            memstat(M+"BEFORE " + i);

            try {
                // alloacate more memory then young gen can handle
                long free = Runtime.getRuntime().freeMemory();
                //byte[] may_cause_gc = new byte[1024*1024*64]; // 64mb
                byte[] may_cause_gc = new byte[(int)free]; // size of freeMem
                alist.add(may_cause_gc);
                memstat(M+"AFTER " + i);
                TimeUnit.MILLISECONDS.sleep(100);
            } catch(OutOfMemoryError oom) {
                System.out.println(M + oom + " at " + oom.getStackTrace()[0]);
                memstat(M +"AFTER " + i);
                i = N; // breaks loop
            }
        }
    }

    public static void memstat(String tag) {
        Runtime RT = Runtime.getRuntime();
        long free = RT.freeMemory();
        long tot  = RT.totalMemory();
        long max  = RT.maxMemory();
        long used = tot - free;
        System.out.printf(
            tag + " mem max=%s tot=%s used=%s free=%s%n",
            h(max),h(tot),h(used),h(free)
        );
    }

    public static final String h(long size) {
        // 1048576 = 1024 * 1024
        // 1073741824 = 1024 * 1024 * 1024
        long gBytes = size / 1073741824L;
        long mBytes = size % 1073741824L / 1048576L;
        long kBytes = size % 1048576L / 1024L;
        long bytes = size % 1024L;
        StringBuilder str = new StringBuilder();

        if (gBytes != 0) {
            str.append(gBytes).append(".").append(Math.abs(mBytes)).append(" GB");
        } else if (mBytes != 0) {
            str.append(mBytes).append(".").append(Math.abs(kBytes)).append(" MB");
        } else if (kBytes != 0) {
            str.append(kBytes).append(".").append(Math.abs(bytes)).append(" KB");
        } else {
            str.append(bytes).append(" B");
        }

        return str.toString();
    }

}