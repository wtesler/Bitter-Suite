package debug;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Centralizes the debugging process of the app.
 * Use Debug.out and Debug.err to print debug statements to the console.
 * Lets developers selectively choose which classes to debug
 * without needing to alter variables across multiple files.
 *
 */
public class Debug {

    private static class DebugMap extends HashMap<String, Boolean> {
        public DebugMap() {
            this.put(IPUTILS, true);
            this.put(LAN, true);
            this.put(MAIN, true);
            this.put(QR, true);
            this.put(SERVER, true);
            this.put(DEBUG, true);
        }
    }

    public final static String IPUTILS = "IPUTILS";
    public final static String LAN     = "LAN";
    public final static String MAIN    = "MAIN";
    public final static String QR      = "QR";
    public final static String SERVER  = "SERVER";
    public final static String DEBUG  = "DEBUG";

    private static final int NOT_ALIGNED = -1;

    private static int maxTagLength = -1;

    private static final Map<String, Boolean> tagMap = Collections.unmodifiableMap(new DebugMap());

    public static void out(final String tag, final String msg) {
        if (tagMap.get(tag)) {
            String paddedTag = tag;
            if (maxTagLength != NOT_ALIGNED) {
                paddedTag = padString(tag, maxTagLength);
            }
            System.out.format("%s:  %s\n", paddedTag, msg);
        }
    }

    public static void err(final String tag, final String msg) {
        if (tagMap.get(tag)) {
            String paddedTag = tag;
            if (maxTagLength != NOT_ALIGNED) {
                paddedTag = padString(tag, maxTagLength);
            }
            System.err.format("%s:  %s\n", paddedTag, msg);
        }
    }

    private static String padString(String str, int desiredLength) {
        StringBuilder builder = new StringBuilder(desiredLength);
        int padding = desiredLength - str.length() - 1;
        for (int i = 0; i < padding; i++) {
            builder.append(' ');
        }
        builder.append(str);
        return builder.toString();
    }

    public static void alignTags() {
        for (Entry<String, Boolean> entry : tagMap.entrySet()) {
            if (entry.getValue() && entry.getKey().length() > maxTagLength) {
                maxTagLength = entry.getKey().length();
            }
        }
    }

}
