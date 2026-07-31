package arkheim.client.presentation.controllers;

import arkheim.client.presentation.navigation.JavaFxNavigator;
import arkheim.client.presentation.theme.ThemeMode;
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

    @FXML
    private Button themeToggleBtn;

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

        emailField.textProperty().bindBidirectional(authViewModel.registerEmailProperty());
        passwordField.textProperty().bindBidirectional(authViewModel.registerPasswordProperty());
        passwordRepetitionField.textProperty().bindBidirectional(authViewModel.registerPasswordRepetitionProperty());
        errorLabel.textProperty().bind(authViewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(authViewModel.errorMessageProperty().isNotEmpty());
        errorLabel.managedProperty().bind(authViewModel.errorMessageProperty().isNotEmpty());
        usernameField.textProperty().bindBidirectional(authViewModel.registerUsernameProperty());
        nameField.textProperty().bindBidirectional(authViewModel.registerNameProperty());
        datePicker.valueProperty().bindBidirectional(authViewModel.registerDateOfBirthProperty());

        registerButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> isBlank(authViewModel.registerEmailProperty().get())
                            || isBlank(authViewModel.registerPasswordProperty().get())
                            || isBlank(authViewModel.registerPasswordRepetitionProperty().get())
                            || isBlank(authViewModel.registerUsernameProperty().get())
                            || isBlank(authViewModel.registerNameProperty().get()),
                        authViewModel.registerEmailProperty(),
                        authViewModel.registerPasswordProperty(),
                        authViewModel.registerPasswordRepetitionProperty(),
                        authViewModel.registerUsernameProperty(),
                        authViewModel.registerNameProperty(),
                        authViewModel.registerDateOfBirthProperty()
                )
        );

        bindingsInitialized = true;
    }

    private static boolean isBlank(String str) {
        return str == null || str.isBlank();
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

    public void onRegisterClicked() {
        authViewModel.register();
        if (authViewModel.currentUserProperty().get() != null) {
            navigator.showHomeScreen();
        }

        boolean validEmail = authViewModel.emailIsValid().get();
        boolean passwordMatch = authViewModel.passwordRepetitionCorrect().get();

        emailField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), !validEmail);

        passwordRepetitionField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), !passwordMatch);
        passwordField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), !passwordMatch);
    }

    public void onGoToLoginClicked() {
        navigator.showLoginScreen();
    }
}
