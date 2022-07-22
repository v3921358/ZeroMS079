package tools;


import client.LoginCrypto;
import constants.ServerConstants;
import database.ConnectionMysqlJDBC;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import static server.Start.getCPUSerial;
import static server.Start.getHardDiskSerialNumber;
import tools.wangzhan.BareBonesBrowserLaunch;
import tools.MacAddressTool;

/**
 * 验证算法
 *
 * @author 风神
 */
public class ZeroMS {
    //下标0开始序列号必须与服的名字一一对应，方可授权成功!
    //取得的机器码最后根据下标用<-下标数>的方式加在机器码后面

    private static String[] serialNumberStr = new String[]{"825617b9af0d2dbd4fe4d20bb5e6224e57917c77", //我自己的
            "66c41d9c7fe4ae6dec81a62af7bcbc62d7555bd6", //qq120072314
            "48b171eda4c747587eaf2b79c86cd2fa930c56b1", //蛋蛋冒险岛 QQ916142810
            "1babd0c7fa22b5a3a9a1a14b03c3416596c8afd9",//欢乐岛
            "3086c321af8b6e480071e8f6503dea365e80625f",//QQ9089950
            "58cc4648caf7a375dd9b3d44bdd4f4f6902604d2",//qq504170896
            "e534ccbcff21ceadc9684af0b72da68391a5d727"//大勺的
        };
    private static boolean showPacket = false;//是否为开发模式
    private static ConnectionMysqlJDBC mysqljdbc;
    private static Connection conn = null;
    private static PreparedStatement pstm = null;
    private static ResultSet rs = null;
    private static int open;
    private static String serialNumber;

    public static boolean getAuthorizedFeedBack() {
        boolean isok = false;
        String mac = MacAddressTool.getMacAddress(false);
        String num = returnSerialNumber();//获取当前机器序列号
        String localMac = LoginCrypto.hexSha1(num + mac);
        boolean is = getAuthorizedInfo(localMac);//根据名字去找对应的数据信息 返回ID,open,serialNumber

        if (showPacket) {
            return true;
        }
        System.out.println("【信息】授权码[" + localMac +"]请稍等.正在识别中...");
        if (is) {//如果数据库连接上了,从数据库取数据验证
            if (serialNumber.equals(localMac) && open == 1) {//远程取回的机器码与本机机器码对比
                System.out.println("【成功信息】你通过ZeroMS授权服务器--验证成功");
                FileoutputUtil.log("授权码.txt", localMac);//打印成TXT
                isok = true;
            }
        
        } else {//如果数据库没连接上,使用本地记录的机器码来验证本机机器码
                if (localMac != null) {
            for (int i = 0; i < serialNumberStr.length; i++) {
                String tempnum = getSerialNumberStrStr(i);
                if (localMac.equals(tempnum)) {
                    System.out.println("【成功信息】你通过本机识别--验证成功");
                    FileoutputUtil.log("授权码.txt", localMac);//打印成TXT
                    isok = true;
                    break;
                    }
                  }

            }
        }
        if (isok) {
       return true;
        } else {
            FileoutputUtil.log("授权码.txt", localMac);//打印成TXT
            System.out.println("【失败信息】你的授权码为[" + localMac + "]你没有被授权，购买正版请联系：疯神冒险岛技术论坛.");
            BareBonesBrowserLaunch.OpenWeb1();
            return false;
        }
         }

    //返回CPU+硬盘序列号
    public static String returnSerialNumber() {
        String cpu = getCPUSerial();
        String disk = getHardDiskSerialNumber("C");

        int newdisk = Integer.parseInt(disk);

        String s = cpu + newdisk;
        String newStr = s.substring(8, s.length());
        return newStr;
    }

    //获取CPU序列号
    public static String getCPUSerial() {
        String result = "";
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\nSet colItems = objWMIService.ExecQuery _ \n   (\"Select * from Win32_Processor\") \nFor Each objItem in colItems \n    Wscript.Echo objItem.ProcessorId \n    exit for  ' do the first cpu only! \nNext \n";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = input.readLine()) != null) {
                result = result + line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        if ((result.trim().length() < 1) || (result == null)) {
            result = "无机器码被读取";
        }
        return result.trim();
    }

    //获取硬盘序列号  传人参数为盘符
    //String sn = DiskUtils.getSerialNumber("C");
    public static String getHardDiskSerialNumber(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\nSet colDrives = objFSO.Drives\nSet objDrive = colDrives.item(\"" + drive + "\")\n" + "Wscript.Echo objDrive.SerialNumber";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result = result + line;
            }
            input.close();
        } catch (Exception e) {
        }
        return result.trim();
    }

    public static boolean isshowPacket() {
        return showPacket;
    }

    public static String getSerialNumberStrStr(int index) {
        String tempStr = serialNumberStr[index];//取得已经授权的机器码
        return tempStr;
    }

    public static boolean getAuthorizedInfo(String serialNumbers) {
        String sql = "select* from ver085 where serialnumber like ?";
         mysqljdbc = new ConnectionMysqlJDBC("129.211.164.89", "zeroms", "sa!@#456", "zeroms", "65534");
        //IP              //帐号        //密码        //数据库名字   //端口
        try {
            conn = mysqljdbc.getConn();
            if (conn == null) {
                return false;
            }
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, serialNumbers);
            rs = pstm.executeQuery();
            if (rs.next()) {
                serialNumber = rs.getString("serialNumber");
                open = rs.getInt("open");//是否开启open开启
                return true;
            }
        } catch (SQLException ex) {
            System.out.println("远程验证连接失败，请联系开发者!");
            return false;
        } finally {
            try {
                mysqljdbc.closeAllConnection(conn, pstm, rs);
            } catch (SQLException ex) {
                System.out.println("关闭连接失败，请联系开发者!");
            }
        }
        return false;
    }

    public static boolean getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("M");
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        String timeStr = sdf.format(getBeiJingTime());
        if (timeStr.equals("11")) {
            return true;
        }
        return false;
    }

    /**
     * 从网络获取北京时间
     *
     * @return
     */
    private static long getBeiJingTime() {
        long time = 0;
        String urls = "http://open.baidu.com/special/time/";
        try {
            URL url = new URL(urls);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            InputStream is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int index = 0;
            while ((line = br.readLine()) != null) {
                index++;
                if (index < 123) {
                    continue;
                }
                if (line.indexOf("window.baidu_time(") != -1) {
                    String[] s = line.split("\\(");
                    time = Long.parseLong(s[1].substring(0, s[1].length() - 2));
                    break;
                }
            }
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    private ZeroMS() {
    }
}
