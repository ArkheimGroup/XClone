package arkheim.client.presentation.controllers;

import arkheim.client.presentation.viewmodels.AuthViewModel;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController extends BaseController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField passwordRepetitionField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label errorLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink goToLoginLink;

    private AuthViewModel authViewModel;
    private boolean bindingsInitialized;

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
        passwordField.textProperty().bindBidirectional(authViewModel.registerPasswordProperty());
        passwordRepetitionField.textProperty().bindBidirectional(authViewModel.registerPasswordRepetitionProperty());
        errorLabel.textProperty().bind(authViewModel.errorMessageProperty());
        usernameField.textProperty().bindBidirectional(authViewModel.registerUsernameProperty());
        nameField.textProperty().bindBidirectional(authViewModel.registerNameProperty());
        datePicker.valueProperty().bindBidirectional(authViewModel.registerDateOfBirthProperty());

        registerButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> authViewModel.emailProperty().get().isBlank()
                            || authViewModel.registerPasswordProperty().get().isBlank()
                            || authViewModel.registerPasswordRepetitionProperty().get().isBlank()
                            || authViewModel.registerUsernameProperty().get().isBlank()
                            || authViewModel.registerNameProperty().get().isBlank()
                            || authViewModel.registerDateOfBirthProperty().get() == null,
                        authViewModel.emailProperty(),
                        authViewModel.registerPasswordProperty(),
                        authViewModel.registerPasswordRepetitionProperty(),
                        authViewModel.registerUsernameProperty(),
                        authViewModel.registerNameProperty(),
                        authViewModel.registerDateOfBirthProperty()
                )
        );

        bindingsInitialized = true;
    }

    public void onRegisterClicked() {
        authViewModel.register();
        if (authViewModel.currentUserProperty().get() != null) {
            navigator.showHomeScreen();
        }

        emailField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), !authViewModel.emailIsValid());

        passwordRepetitionField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), !authViewModel.passwordRepetitionCorrect());
        passwordField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), !authViewModel.passwordRepetitionCorrect());
    }

    public void onGoToLoginClicked() {
        navigator.showLoginScreen();
    }
}
