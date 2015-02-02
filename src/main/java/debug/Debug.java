package debug;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Centralizes the debugging process of the app. Use Debug.out and Debug.err to
 * print debug statements to the console. Lets developers selectively choose
 * which classes to debug without needing to alter variables across multiple
 * files.
 *
 */
public class Debug {

    // A universal shut-off switch. Ensures peace of mind when shipping product.
    private final static boolean QUIET = false;

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

    // String mappings.
    public final static String IPUTILS = "IPUTILS";
    public final static String LAN = "LAN";
    public final static String MAIN = "MAIN";
    public final static String QR = "QR";
    public final static String SERVER = "SERVER";
    // You CAN debug the debug class.
    public final static String DEBUG = "DEBUG";

    // final. Represents that the user does not want the logs to be text
    // aligned.
    private static final int NOT_ALIGNED = -1;

    // When alignTags() is called, this value changes to represent the text
    // padding
    private static int maxTagLength = NOT_ALIGNED;

    // Creates an unmodifiable Debug Map which maps String keys to debug status.
    private static final Map<String, Boolean> tagMap = Collections.unmodifiableMap(new DebugMap());

    /**
     * Prints a debug message to the console.
     *
     * @param tag String mapping
     * @param msg The text which should be displayed.
     */
    public static void out(final String tag, final String msg) {
        if (tagMap.get(tag) && !QUIET) {
            String paddedTag = tag;
            if (maxTagLength != NOT_ALIGNED) {
                paddedTag = padString(tag, maxTagLength);
            }
            System.out.format("%s:  %s\n", paddedTag, msg);
        }
    }

    /**
     * Prints an error message to the console.
     *
     * @param tag String mapping
     * @param msg the text which should be displayed.
     */
    public static void err(final String tag, final String msg) {
        if (tagMap.get(tag) && !QUIET) {
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
