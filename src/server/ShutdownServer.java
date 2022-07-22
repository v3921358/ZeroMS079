package server;

import constants.ServerConfig;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.world.World;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Timer.BuffTimer;
import server.Timer.CloneTimer;
import server.Timer.EtcTimer;
import server.Timer.EventTimer;
import server.Timer.MapTimer;
import server.Timer.PingTimer;
import server.Timer.WorldTimer;
import tools.FileoutputUtil;
import tools.packet.CWvsContext;

public class ShutdownServer implements ShutdownServerMBean {

    public static ShutdownServer instance;
    private static final Logger logger = LogManager.getLogger(ShutdownServer.class);

    public static void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            instance = new ShutdownServer();
            mBeanServer.registerMBean(instance, new ObjectName("server:type=ShutdownServer"));
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            System.out.println("Error registering Shutdown MBean");
        }
    }

    public static ShutdownServer getInstance() {
        return instance;
    }
    public int mode = 0;

    @Override
    public void shutdown() {//can execute twice
        run();
    }

    @Override
    public void run() {
        if (mode == 0) {
            int ret = 0;
            World.Broadcast.broadcastMessage(CWvsContext.broadcastMsg(0, ServerConfig.Turnoffserverprompt));
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.setShutdown();
                cs.setServerMessage(ServerConfig.Turnoffserverprompt);
                ret += cs.closeAllMerchant();
            }
            /*AtomicInteger FinishedThreads = new AtomicInteger(0);
             HiredMerchantSave.Execute(this);
             synchronized (this) {
             try {
             wait();
             } catch (InterruptedException ex) {
             Logger.getLogger(ShutdownServer.class.getName()).log(Level.SEVERE, null, ex);
             }
             }
             while (FinishedThreads.incrementAndGet() != HiredMerchantSave.NumSavingThreads) {
             synchronized (this) {
             try {
             wait();
             } catch (InterruptedException ex) {
             Logger.getLogger(ShutdownServer.class.getName()).log(Level.SEVERE, null, ex);
             }
             }
             }*/
            World.Guild.save();
            World.Alliance.save();
            World.Family.save();
            System.out.println("Shutdown 1 has completed. Hired merchants saved: " + ret);
            mode++;
        } else if (mode == 1) {
            mode++;
            System.out.println("Shutdown 2 commencing...");
            try {
                World.Broadcast.broadcastMessage(CWvsContext.broadcastMsg(0, ServerConfig.Turnoffserverprompt));
                Integer[] chs = ChannelServer.getAllInstance().toArray(new Integer[0]);

                for (int i : chs) {
                    try {
                        ChannelServer cs = ChannelServer.getInstance(i);
                        synchronized (this) {
                            cs.shutdown();
                        }
                    } catch (Exception e) {
                        FileoutputUtil.outputFileError(FileoutputUtil.Exception_Log, e);
                        System.err.println(e);
                    }
                }
                CashShopServer.shutdown();

            } catch (Exception ex) {
                FileoutputUtil.outputFileError(FileoutputUtil.Exception_Log, ex);
                System.err.println(ex);
            }
            WorldTimer.getInstance().stop();
            MapTimer.getInstance().stop();
            BuffTimer.getInstance().stop();
            CloneTimer.getInstance().stop();
            EventTimer.getInstance().stop();
            EtcTimer.getInstance().stop();
            PingTimer.getInstance().stop();
            System.out.println("Shutdown 2 has finished.");
            System.out.println("游戏服务已成功关闭。");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //shutdown
            }
            System.exit(0); //not sure if this is really needed for ChannelServer
        }
    }
}
