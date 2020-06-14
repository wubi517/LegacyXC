package org.stoken;

public class LibStoken {
    public static final int FILE_NOT_FOUND = -3;
    public static final int INVALID_FORMAT = -1;
    public static final int IO_ERROR = -2;
    public static final int SUCCESS = 0;
    long libctx = init();

    static {
        System.loadLibrary("stoken");
    }

    public static class StokenGUID {
        public String GUID;
        public String longName;
        public String tag;
    }

    public static class StokenInfo {
        public int interval;
        public String serial;
        public int tokenVersion;
        public long unixExpDate;
        public boolean usesPin;
    }

    public native synchronized boolean checkDevID(String str);

    public native synchronized boolean checkPIN(String str);

    public native synchronized String computeTokencode(long j, String str);

    public native synchronized int decryptSeed(String str, String str2);

    public synchronized void destroy() {
        if (this.libctx != 0) {
            free();
            this.libctx = 0;
        }
    }

    public native synchronized String encryptSeed(String str, String str2);

    public native synchronized String formatTokencode(String str);

    native synchronized void free();

    public native synchronized StokenGUID[] getGUIDList();

    public native synchronized StokenInfo getInfo();

    public native synchronized int getMaxPIN();

    public native synchronized int getMinPIN();

    public native synchronized int importRCFile(String str);

    public native synchronized int importString(String str);

    native synchronized long init();

    public native synchronized boolean isDevIDRequired();

    public native synchronized boolean isPINRequired();

    public native synchronized boolean isPassRequired();
}
