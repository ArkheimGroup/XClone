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
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
    }

    public void updateTheme() {
        Scene scene = stage.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();
            addStyle(scene);
        }
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
        stage.setWidth(1000);
        stage.setHeight(600);
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
        controller.setAuthViewModel(authViewModel);
        controller.setNavigator(this);

        Scene scene = new Scene(root);
        addStyle(scene);

        stage.setScene(scene);
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

        // Inject Infrastructure Adapters conforming to Domain Ports
        FeedPort feedPort = new TcpFeedAdapter("localhost", 8082);
        PostPort postPort = new HttpPostAdapter();
        FollowPort followPort = new HttpFollowAdapter();
        HashtagPort hashtagPort = new HttpHashtagAdapter();
        MediaPort mediaPort = new HttpMediaAdapter();

        // Build presentation viewmodels
        FeedViewModel feedViewModel = new FeedViewModel(feedPort, postPort, hashtagPort);
        FollowViewModel followViewModel = new FollowViewModel(followPort);
        MediaViewModel mediaViewModel = new MediaViewModel(mediaPort);

        // Inject dependencies into HomeController
        controller.setViewModels(authViewModel, feedViewModel, followViewModel, mediaViewModel);
        controller.setNavigator(this);

        Scene scene = new Scene(root);
        addStyle(scene);

        stage.setScene(scene);
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showProfileScreen(java.util.UUID userId) {
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

        UserPort userPort = new HttpUserAdapter();
        FollowPort followPort = new HttpFollowAdapter();
        PostPort postPort = new HttpPostAdapter();
        HashtagPort hashtagPort = new HttpHashtagAdapter();

        UserViewModel userViewModel = new UserViewModel(userPort);
        FollowViewModel followViewModel = new FollowViewModel(followPort);
        PostViewModel postViewModel = new PostViewModel(postPort, hashtagPort);

        controller.setViewModels(authViewModel, userViewModel, followViewModel, postViewModel, userId);
        controller.setNavigator(this);

        Scene scene = new Scene(root);
        addStyle(scene);

        stage.setScene(scene);
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showPostDetailsScreen(java.util.UUID postId) {
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

        PostPort postPort = new HttpPostAdapter();
        HashtagPort hashtagPort = new HttpHashtagAdapter();
        PostViewModel postViewModel = new PostViewModel(postPort, hashtagPort);

        controller.setViewModels(authViewModel, postViewModel, postId);
        controller.setNavigator(this);

        Scene scene = new Scene(root);
        addStyle(scene);

        stage.setScene(scene);
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.show();
    }
}
