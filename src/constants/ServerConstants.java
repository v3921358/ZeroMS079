package constants;

import java.util.Calendar;
import tools.Triple;

public class ServerConstants {

    public static byte Class_Bonus_EXP(final int job) {
        switch (job) {
            case 501:
            case 530:
            case 531:
            case 532:
            case 2300:
            case 2310:
            case 2311:
            case 2312:
            case 3100:
            case 3110:
            case 3111:
            case 3112:
            case 11212:
            case 800:
            case 900:
            case 910:
                return 0;
        }
        return 0;
    }

    public static boolean getEventTime() {
        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        switch (Calendar.DAY_OF_WEEK) {
            case 1:
                return time >= 1 && time <= 5;
            case 2:
                return time >= 4 && time <= 9;
            case 3:
                return time >= 7 && time <= 12;
            case 4:
                return time >= 10 && time <= 15;
            case 5:
                return time >= 13 && time <= 18;
            case 6:
                return time >= 16 && time <= 21;
        }
        return time >= 19 && time <= 24;
    }

    // GMS stuff
    public static boolean TESPIA = false;
    public static final short MAPLE_VERSION = (short) 79;
    public static final String MAPLE_PATCH = "1";
    public static final String MAIN_WORLD = "SNDA_MS";
    public static final byte[] NEXON_IP = new byte[]{(byte) 221, (byte) 231, (byte) 130, (byte) 70};
    //public static final byte[] NEXON_IP = new byte[]{(byte) 118, (byte) 25, (byte) 22, (byte) 18};//员外
    //public static final byte[] NEXON_IP = new byte[]{(byte) 103, (byte) 91, (byte) 208, (byte) 171};

    // Server stuff
    public static final String ASCII = "GBK";
    public static final String SOURCE_REVISION = "2";
    public static final boolean Redirector = true;//未知
    public static final boolean AntiKS = false;//未知
    public static final int miracleRate = 1;//未知
    public static final byte SHOP_DISCOUNT = 0;//商城优惠价格[折扣]
    public static boolean isBetaForAdmins = true;//应该管理号的开启

    public static Triple<String, Integer, Boolean>[] backgrounds = new Triple[]{ //boolean for randomize
        new Triple<>("20140430/0", 1, false),
        new Triple<>("20140326/0", 0, false),
        new Triple<>("20140326/1", 0, false)
    };

    public static enum PlayerGMRank {

        NORMAL('@', 0),
        INTERN('!', 1),
        GM('!', 2),
        SUPERGM('!', 3),
        ADMIN('!', 4);
        private final char commandPrefix;
        private final int level;

        PlayerGMRank(char ch, int level) {
            commandPrefix = ch;
            this.level = level;
        }

        public String getCommandPrefix() {
            return String.valueOf(commandPrefix);
        }

        public int getLevel() {
            return level;
        }
    }

    
        public static enum MapleType {

        UNKNOWN(-1, "UTF-8"),
     //   한국(1, "EUC_KR"),
        韩国(1, "EUC_KR"),
        日本(3, "Shift_JIS"),
        中国(4, "GBK"),
        TESPIA(5, "UTF-8"),
        台灣(6, "BIG5"),
        SEA(7, "UTF-8"),
        GLOBAL(8, "UTF-8"),
        BRAZIL(9, "UTF-8");
        byte type;
        final String ANSI;

        private MapleType(int type, String ANSI) {
            this.type = (byte) type;
            this.ANSI = ANSI;
        }

        public byte getType() {
            if (!ServerConstants.TESPIA) {
                return type;
            }
            if (this ==韩国|| this ==韩国) {
                return 2;//KMS測試機
            } else {
                return 5;//測試機
            }
        }

        public String getANSI() {
            return ANSI;
        }

        public void setType(int type) {
            this.type = (byte) type;
        }

        public static MapleType getByType(byte type) {
            for (MapleType l : MapleType.values()) {
                if (l.getType() == type) {
                    return l;
                }
            }
            return UNKNOWN;
        }
    }

    
    public static enum CommandType {

        NORMAL(0),
        TRADE(1);
        private final int level;

        CommandType(int level) {
            this.level = level;
        }

        public int getType() {
            return level;
        }
    }

    private ServerConstants() {
    }
}
