package arkheim.client.presentation.navigation;

import arkheim.client.presentation.controllers.LoginController;
import arkheim.client.presentation.controllers.RegisterController;
import arkheim.client.presentation.theme.ThemeMode;
import arkheim.client.presentation.viewmodels.AuthViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class JavaFxNavigator implements Navigator {

    private final Stage stage;
    private final AuthViewModel authViewModel;
    private ThemeMode themeMode;
    private final String lightStyle;
    private final String darkStyle;

    public JavaFxNavigator(Stage stage, AuthViewModel authViewModel, ThemeMode themeMode) {
        this.stage = stage;
        this.authViewModel = authViewModel;

        try {
            lightStyle = Objects.requireNonNull(getClass().getResource("/arkheim/client/presentation/Assets/Style.css")).toExternalForm();
        }
        catch (NullPointerException e) {
            throw new RuntimeException("Could not load the css file\n" + e);
        }

        try {
            darkStyle = Objects.requireNonNull(getClass().getResource("/arkheim/client/presentation/Assets/DarkMode.css")).toExternalForm();
        }
        catch (NullPointerException e) {
            throw new RuntimeException("Could not load the css file\n" + e);
        }

        this.themeMode = themeMode;
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
    }

    public void updateTheme() {
        Scene scene = stage.getScene();
        addStyle(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void addStyle(Scene scene) {
        if (themeMode == ThemeMode.LIGHT) {
            scene.getStylesheets().add(lightStyle);
        } else {
            scene.getStylesheets().add(darkStyle);
        }
    }

    @Override
    public void showLoginScreen() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/arkheim/client/presentation/views/login.fxml")
        );

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load login.fxml\n" + e);
        }

        LoginController controller = loader.getController();
        controller.setAuthViewModel(authViewModel);
        controller.setNavigator(this);

        Scene scene = new Scene(root);
        addStyle(scene);

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void showRegisterScreen() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/arkheim/client/presentation/views/register.fxml")
        );

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load register.fxml\n" + e);
        }

        RegisterController controller = loader.getController();
        controller.setAuthViewModel(authViewModel);
        controller.setNavigator(this);

        Scene scene = new Scene(root);
        addStyle(scene);

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void showHomeScreen() {
        // load home.fxml
    }
}
