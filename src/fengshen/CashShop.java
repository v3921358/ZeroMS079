package fengshen;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class CashShop extends JFrame {

    public static void main(String[] args) {
        CashShop cashShop = new CashShop();

        JFXPanel jfxPanel = new JFXPanel();
        Platform.runLater(() -> {

            try {
                URL resource = CashShop.class.getResource("CashShop.fxml");
                Parent root = FXMLLoader.load(resource);
                Scene scene = new Scene(root);
                jfxPanel.setScene(scene);
                FlowPane flowPane = (FlowPane) root.lookup("#flowPane");
                flowPane.setPrefWidth(800);
                flowPane.getChildren().clear();
                for (int i = 0; i < 10; i++) {
                    Parent rootClone = FXMLLoader.load(resource);
                    Scene scene1 = new Scene(rootClone);
                    new JFXPanel().setScene(scene1);
                    ImageView icon = (ImageView) rootClone.lookup("#icon");
//                    icon.setImage(new Image("file:///E:\\015xml\\015wz\\Data.wz\\Item\\Etc\\0402/04021003.info.icon.png"));
                    Label name = (Label) rootClone.lookup("#name");
                    name.setText("矿石");
                    Label price = (Label) rootClone.lookup("#price");
                    price.setText("5000");
                    Node node = rootClone.lookup("#flowPane .box");
                    flowPane.getChildren().add(node);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        cashShop.setLayout(new BorderLayout());
        cashShop.add(jfxPanel, BorderLayout.CENTER);
        cashShop.setSize(800, 400);
        cashShop.setLocationRelativeTo(null);
        cashShop.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cashShop.setVisible(true);
    }

}
