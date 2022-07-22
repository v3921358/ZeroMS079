package constants;

public class LoadingOption {

    public enum SQLInfo {

        Default(1, ServerConfig.SQL_IP,ServerConfig.SQL_PORT, ServerConfig.SQL_USER, ServerConfig.SQL_PASSWORD, ServerConfig.SQL_DATABASE);
        private final String ip, port, user, pass, db;
        private final int id;

        private SQLInfo(int id, String ip,String  port, String user, String pass, String db) {
            this.id = id;
            this.ip = ip;
            this.port = port;
            this.user = user;
            this.pass = pass;
            this.db = db;
        }
        public String getIp() {
            return ip;
        }
        
        public String getPort() {
            return port;
        }

        public String getUser() {
            return user;
        }

        public String getPass() {
            return pass;
        }

        public String getDb() {
            return db;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(id).append(". ").append(this.name()).append("\r\n");
            sb.append("Ip :: ").append(ip).append("\r\n");
            sb.append("Port :: ").append(port).append("\r\n");
            sb.append("User :: ").append(user).append("\r\n");
            sb.append("Password :: ").append(pass).append("\r\n");
            sb.append("Database :: ").append(db).append("\r\n");
            sb.append("\r\n");
            return sb.toString();
        }

        public static SQLInfo getById(int id) {
            for (SQLInfo i : values()) {
                if (i.id == id) {
                    return i;
                }
            }
            return Default;
        }
    }

    public final static String[] ipSetting = {"127.0.0.1", "127.0.0.1"};

    private LoadingOption() {
    }

}
