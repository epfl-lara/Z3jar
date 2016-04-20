package z3;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.security.CodeSource;
import java.net.URL;

public final class Z3Loader {
    // related to the path in the jar file
    private static final String DS = java.io.File.separator;
    private static final String PS = java.io.File.pathSeparator;

    private static final String LIB_BIN = DS + "lib-bin" + DS;

    private static final String LIB_NAME     = "z3jar";
    private static final String LIBJAVA_NAME = "libz3java";
    private static final String LIBZ3_NAME   = "libz3";

    private static final String versionString = LibraryChecksum.value;

    private static final String isDebug = System.getProperty("z3jar.debug.load");

    // this is just to force class loading, and therefore library loading.
    static {
      if (!withinJar()) {
        System.err.println("It seems you are not running Z3jar from its JAR");
        System.exit(1);
      }

      loadFromJar();
    }

    private static void debug(String msg) {
        if (isDebug != null) {
          System.out.println(msg);
        }
    }

    public static boolean withinJar() {
       java.net.URL classJar  = Z3Loader.class.getResource("/lib-bin/");
       return classJar != null;
    }

    private static void loadFromJar() {
        String path = "Z3JAR_" + versionString;
        File libDir  = new File(System.getProperty("java.io.tmpdir") + DS + path + LIB_BIN);

        try {
          if (!libDir.isDirectory() || !libDir.canRead()) {
            libDir.mkdirs();
            extractFromJar(libDir);
          }

          addLibraryPath(libDir.getAbsolutePath());

          String os = System.getProperty("os.name");
          if (os != null && os.indexOf("Win") >= 0) {
            // Under windows, we first load libz3 explicitly, on which Z3jar depends
            debug("Loading libz3");
            System.loadLibrary(LIBZ3_NAME);
            debug("Loading libz3java");
            System.loadLibrary(LIBJAVA_NAME);
          }

          debug("Loading "+LIB_NAME);
          System.loadLibrary(LIB_NAME);
        } catch (Exception e) {
          System.err.println(e.getMessage());
          e.printStackTrace();
        }
    }

    private static void addLibraryPath(String pathToAdd) throws Exception {
        System.setProperty("java.library.path", pathToAdd + PS + System.getProperty("java.library.path"));

        // this forces JVM to reload "java.library.path" property
        Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
        fieldSysPath.setAccessible( true );
        fieldSysPath.set( null, null );
    }

    private static void extractFromJar(File toDir) throws Exception {
        CodeSource src = Z3Loader.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            while(true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null) break;

                String path = e.getName();

                if (path.startsWith("lib-bin/") && !e.isDirectory()) {

                    String name = new File(path).getName();

                    debug("Extracting "+path+" from jar to "+name+ "...");

                    File to = new File(toDir.getAbsolutePath() + DS + name);

                    InputStream in   = Z3Loader.class.getResourceAsStream("/"+path);
                    OutputStream out = new FileOutputStream(to);
                    byte buf[] = new byte[4096];
                    int len;
                    while((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                }
            }
        }
    }

    public static void load() {
      // simply used for the classloader to load this class,
      // and in turn load the native z3 jar through our statics
    }
}
