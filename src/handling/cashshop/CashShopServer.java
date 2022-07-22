package handling.cashshop;

import constants.ServerConfig;
import handling.channel.PlayerStorage;
import handling.netty.ServerConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ServerProperties;
import static tools.FileoutputUtil.CurrentReadable_Time;

public class CashShopServer {

    private static String ip;
    private static int PORT = 8600;
    private static ServerConnection acceptor;
    private static PlayerStorage players;
    private static boolean finishedShutdown = false;
    private static final Logger logger = LogManager.getLogger(CashShopServer.class);

    public static void run_startup_configurations() {
        ip = ServerConfig.IP + ":" + PORT;
        PORT = Short.valueOf(ServerProperties.getProperty("CashPort", "8600"));
        players = new PlayerStorage(-10);

        try {
            acceptor = new ServerConnection(PORT, 0, -1, true);
            acceptor.run();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】商城端口:" + PORT);
        } catch (final Exception e) {
            System.out.println(" Failed!");
            System.err.println("Could not bind to port " + PORT + ".");
            throw new RuntimeException("Binding failed.", e);
        }
    }

    public static String getIP() {
        return ip;
    }

    public static PlayerStorage getPlayerStorage() {
        return players;
    }

    public static void shutdown() {
        if (finishedShutdown) {
            return;
        }
        System.out.println("【" + CurrentReadable_Time() + "】[游戏商城] 准备关闭...");
        System.out.println("【" + CurrentReadable_Time() + "】[游戏商城] 保存资料中...");
        players.disconnectAll();
        System.out.println("【" + CurrentReadable_Time() + "】[游戏商城] 解除綁定端口...");
        //acceptor.unbindAll();
        System.out.println("【" + CurrentReadable_Time() + "】[游戏商城] 关闭完成...");
        finishedShutdown = true;
    }

    public static boolean isShutdown() {
        return finishedShutdown;
    }

    private CashShopServer() {
    }
}
