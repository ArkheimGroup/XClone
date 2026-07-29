package arkheim.client.presentation;

import arkheim.client.domain.ports.AuthPort;
import arkheim.client.infrastructure.adapter.HttpAuthAdapter;
import arkheim.client.presentation.navigation.JavaFxNavigator;
import arkheim.client.presentation.navigation.Navigator;
import arkheim.client.presentation.theme.ThemeMode;
import arkheim.client.presentation.viewmodels.AuthViewModel;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;
import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        String iconPath = "/arkheim/client/presentation/Assets/images/icons/light/XCloneLogo_LightMode_Transparent.png";
        Image icon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream(iconPath))
        );
        stage.getIcons().add(icon);

        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                try (InputStream is = getClass().getResourceAsStream(iconPath)) {
                    if (is != null) {
                        java.awt.Image image = ImageIO.read(is);
                        taskbar.setIconImage(image);
                    }
                } catch (Exception e) {
                    System.err.println("Could not set macOS dock icon: " + e.getMessage());
                }
            }
        }

        AuthPort authPort = new HttpAuthAdapter();
        AuthViewModel authViewModel = new AuthViewModel(authPort);

        Navigator navigator = new JavaFxNavigator(stage, authViewModel, ThemeMode.LIGHT);
        navigator.showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
