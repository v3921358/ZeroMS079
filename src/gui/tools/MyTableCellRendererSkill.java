package gui.tools;

import java.awt.Component;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MyTableCellRendererSkill implements TableCellRenderer {
    
    private int itemId;

    public MyTableCellRendererSkill() {
        super();
    }

    public MyTableCellRendererSkill(int itemId) {
        super();
        this.itemId = itemId;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        // 根据特定的单元格设置不同的Renderer,假如你要在第2行第3列显示图标
        // System.out.println(JieMian.photoid1.size());
        JLabel label = new JLabel();

        String path = "Icon/Skill/" + value + ".png";
        File f = new File(path);
        if (!f.exists()) {
            label.setText("没有图标");
            return label;
        }
        
        ImageIcon icon = new ImageIcon(path);

        label.setIcon(icon);
        label.setOpaque(false);
        // System.out.println(JieMian.photoid1.size());
        return label;

    }

}
