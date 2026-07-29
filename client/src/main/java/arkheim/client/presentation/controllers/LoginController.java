package arkheim.client.presentation.controllers;

import arkheim.client.presentation.navigation.JavaFxNavigator;
import arkheim.client.presentation.theme.ThemeMode;
import arkheim.client.presentation.viewmodels.AuthViewModel;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController extends BaseController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink goToRegisterLink;

    @FXML
    private Button themeToggleBtn;

    private AuthViewModel authViewModel;
    private boolean bindingsInitialized = false;

    @FXML
    private void initialize() {
        bindIfReady();
    }

    public void setAuthViewModel(AuthViewModel authViewModel) {
        this.authViewModel = authViewModel;
        bindIfReady();
    }

    private void bindIfReady() {
        if (bindingsInitialized || authViewModel == null) {
            return;
        }

        emailField.textProperty().bindBidirectional(authViewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(authViewModel.passwordProperty());
        errorLabel.textProperty().bind(authViewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(authViewModel.errorMessageProperty().isNotEmpty());
        errorLabel.managedProperty().bind(authViewModel.errorMessageProperty().isNotEmpty());

        loginButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> authViewModel.emailProperty().get().isBlank()
                                || authViewModel.passwordProperty().get().isBlank(),
                        authViewModel.emailProperty(),
                        authViewModel.passwordProperty()
                )
        );

        bindingsInitialized = true;
    }

    @Override
    public void updateIcons() {
        if (themeToggleBtn != null) {
            themeToggleBtn.setText(themeMode == ThemeMode.LIGHT ? "☾ Dark Mode" : "☼ Light Mode");
        }
    }

    @FXML
    private void onThemeToggleClicked() {
        if (navigator instanceof JavaFxNavigator fxNavigator) {
            ThemeMode newMode = (themeMode == ThemeMode.LIGHT) ? ThemeMode.DARK : ThemeMode.LIGHT;
            fxNavigator.setThemeMode(newMode);
            fxNavigator.updateTheme();
            setThemeMode(newMode);
        }
    }

    @FXML
    private void onLoginClicked() {
        authViewModel.login();
        if (authViewModel.currentUserProperty().get() != null) {
            navigator.showHomeScreen();
        }

        boolean validEmail = authViewModel.emailIsValid().get();

        emailField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), !validEmail);
    }

    @FXML
    private void onGoToRegisterClicked() {
        navigator.showRegisterScreen();
    }
}
