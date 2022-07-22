
package gui;

import java.awt.Color;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;


/**
 * @author Administrator
 */
public class skin {

    public static void 初始化皮肤(int id) {
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");//黑色
            //UIManager.setLookAndFeel("com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme");//黑色黄背
            //  UIManager.put( "TabbedPane.showTabSeparators" , true );//选项卡式窗格
            //  UIManager.put( "Button.arc" , 999 );//按钮圆形
            //  UIManager.put( "Component.arc" , 999 );
            //   UIManager.put( "ProgressBar.arc" , 999 );
            //  UIManager.put( "TextComponent.arc" , 999 );
            //  UIManager.put( "TabbedPane.selectedBackground" , Color.ORANGE );//选中栏背景色
        } catch (Exception ex) {
            System.err.println("LaF 初始化失败");
        }
    }

    private skin() {
    }
}