package arkheim.client.presentation.navigation;

import arkheim.client.domain.ports.*;
import arkheim.client.infrastructure.adapter.*;
import arkheim.client.presentation.controllers.LoginController;
import arkheim.client.presentation.controllers.RegisterController;
import arkheim.client.presentation.controllers.HomeController;
import arkheim.client.presentation.controllers.ProfileController;
import arkheim.client.presentation.controllers.PostDetailsController;
import arkheim.client.presentation.theme.ThemeMode;
import arkheim.client.presentation.viewmodels.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

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
            lightStyle = Objects.requireNonNull(JavaFxNavigator.class.getResource("/arkheim/client/presentation/Assets/Style.css")).toExternalForm();
        }
        catch (NullPointerException e) {
            throw new RuntimeException("Could not load the css file\n" + e);
        }

        try {
            darkStyle = Objects.requireNonNull(JavaFxNavigator.class.getResource("/arkheim/client/presentation/Assets/DarkMode.css")).toExternalForm();
        }
        catch (NullPointerException e) {
            throw new RuntimeException("Could not load the css file\n" + e);
        }

        this.themeMode = themeMode;
        stage.setMinWidth(1100.0);
        stage.setMinHeight(720.0);
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
    }

    public void updateTheme() {
        Scene scene = stage.getScene();
        if (scene != null) {
            applySceneTheme(scene);
        }
    }

    private void applySceneTheme(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        if (themeMode == ThemeMode.DARK) {
            scene.setFill(Color.web("#000000"));
            scene.getStylesheets().add(darkStyle);
        } else {
            scene.setFill(Color.web("#FFFFFF"));
            scene.getStylesheets().add(lightStyle);
        }
    }

    private void setRootOrNewScene(Parent root, double width, double height) {
        Scene scene = stage.getScene();
        if (scene != null) {
            scene.setRoot(root);
            applySceneTheme(scene);
            if (stage.getWidth() < width || stage.getHeight() < height) {
                stage.setWidth(width);
                stage.setHeight(height);
                stage.centerOnScreen();
            }
        } else {
            scene = new Scene(root, width, height);
            applySceneTheme(scene);
            stage.setScene(scene);
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
        controller.setThemeMode(themeMode);
        controller.setAuthViewModel(authViewModel);
        controller.setNavigator(this);

        setRootOrNewScene(root, 1280, 800);
        stage.centerOnScreen();
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
        controller.setThemeMode(themeMode);
        controller.setAuthViewModel(authViewModel);
        controller.setNavigator(this);

        setRootOrNewScene(root, 1280, 800);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showHomeScreen() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/arkheim/client/presentation/views/home.fxml")
        );

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load home.fxml\n" + e);
        }

        HomeController controller = loader.getController();
        controller.setThemeMode(themeMode);

        // Inject Infrastructure Adapters conforming to Domain Ports
        FeedPort feedPort = new TcpFeedAdapter("localhost", 8082);
        PostPort postPort = new HttpPostAdapter();
        FollowPort followPort = new HttpFollowAdapter();
        HashtagPort hashtagPort = new HttpHashtagAdapter();
        MediaPort mediaPort = new HttpMediaAdapter();
        UserPort userPort = new HttpUserAdapter();

        // Build presentation viewmodels
        FeedViewModel feedViewModel = new FeedViewModel(feedPort, postPort, hashtagPort);
        FollowViewModel followViewModel = new FollowViewModel(followPort);
        MediaViewModel mediaViewModel = new MediaViewModel(mediaPort);
        UserViewModel userViewModel = new UserViewModel(userPort);
        PostViewModel postViewModel = new PostViewModel(postPort, hashtagPort);

        // Inject dependencies into HomeController
        controller.setViewModels(authViewModel, feedViewModel, followViewModel, mediaViewModel, userViewModel, postViewModel);
        controller.setNavigator(this);

        setRootOrNewScene(root, 1280, 850);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showProfileScreen(UUID userId) {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/arkheim/client/presentation/views/profile.fxml")
        );

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load profile.fxml\n" + e);
        }

        ProfileController controller = loader.getController();
        controller.setThemeMode(themeMode);

        UserPort userPort = new HttpUserAdapter();
        FollowPort followPort = new HttpFollowAdapter();
        PostPort postPort = new HttpPostAdapter();
        HashtagPort hashtagPort = new HttpHashtagAdapter();
        MediaPort mediaPort = new HttpMediaAdapter();

        UserViewModel userViewModel = new UserViewModel(userPort);
        FollowViewModel followViewModel = new FollowViewModel(followPort);
        PostViewModel postViewModel = new PostViewModel(postPort, hashtagPort);
        MediaViewModel mediaViewModel = new MediaViewModel(mediaPort);

        controller.setViewModels(authViewModel, userViewModel, followViewModel, postViewModel, mediaViewModel, userId);
        controller.setNavigator(this);

        setRootOrNewScene(root, 1280, 850);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showPostDetailsScreen(UUID postId) {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/arkheim/client/presentation/views/post_details.fxml")
        );

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load post_details.fxml\n" + e);
        }

        PostDetailsController controller = loader.getController();
        controller.setThemeMode(themeMode);

        PostPort postPort = new HttpPostAdapter();
        HashtagPort hashtagPort = new HttpHashtagAdapter();
        UserPort userPort = new HttpUserAdapter();

        PostViewModel postViewModel = new PostViewModel(postPort, hashtagPort);
        UserViewModel userViewModel = new UserViewModel(userPort);

        controller.setViewModels(authViewModel, postViewModel, userViewModel, postId);
        controller.setNavigator(this);

        setRootOrNewScene(root, 1280, 850);
        stage.centerOnScreen();
        stage.show();
    }
}
