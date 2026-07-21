package arkheim.client.presentation.controllers;

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

    @FXML
    private void onLoginClicked() {
        authViewModel.login();
        if (authViewModel.currentUserProperty() != null) {
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
