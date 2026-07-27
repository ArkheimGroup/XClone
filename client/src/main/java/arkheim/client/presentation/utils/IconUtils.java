package arkheim.client.presentation.utils;

import arkheim.client.presentation.theme.ThemeMode;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class IconUtils {

    public static Image getIconImage(String iconName, ThemeMode themeMode) {
        String themeDir = (themeMode == ThemeMode.DARK) ? "dark" : "light";
        String path = "/arkheim/client/presentation/Assets/images/icons/" + themeDir + "/" + iconName + ".png";
        try {
            InputStream stream = IconUtils.class.getResourceAsStream(path);
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + path + " - " + e.getMessage());
        }
        return null;
    }

    public static ImageView createIconView(String iconName, ThemeMode themeMode, double size) {
        Image img = getIconImage(iconName, themeMode);
        if (img == null) {
            return new ImageView();
        }
        ImageView iv = new ImageView(img);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    public static void setButtonIcon(Button button, String iconName, ThemeMode themeMode, double size) {
        if (button != null) {
            button.setGraphic(createIconView(iconName, themeMode, size));
        }
    }

    public static void setLabelIcon(Label label, String iconName, ThemeMode themeMode, double size) {
        if (label != null) {
            label.setGraphic(createIconView(iconName, themeMode, size));
        }
    }
}
