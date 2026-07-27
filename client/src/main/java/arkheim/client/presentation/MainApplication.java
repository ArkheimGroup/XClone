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

import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Image icon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/arkheim/client/presentation/Assets/images/icons/light/XCloneLogo_LightMode_Transparent.png"))
        );
        stage.getIcons().add(icon);

        AuthPort authPort = new HttpAuthAdapter();
        AuthViewModel authViewModel = new AuthViewModel(authPort);

        Navigator navigator = new JavaFxNavigator(stage, authViewModel, ThemeMode.LIGHT);
        navigator.showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
