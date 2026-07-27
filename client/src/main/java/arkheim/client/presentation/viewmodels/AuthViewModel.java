package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.ports.AuthPort;
import arkheim.client.domain.ports.dtos.UserDto;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Presentation-layer state and actions for the login/register screens.
 * Wraps {@link AuthPort} and exposes JavaFX-bindable properties so the
 * view never talks to the port directly.
 */
public class AuthViewModel {

    private final AuthPort authPort;

    // --- login form fields ---
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");

    // --- register form fields ---
    private final StringProperty registerUsername = new SimpleStringProperty("");
    private final StringProperty registerName = new SimpleStringProperty("");
    private final StringProperty registerEmail = new SimpleStringProperty("");
    private final StringProperty registerPassword = new SimpleStringProperty("");
    private final StringProperty registerPasswordRepetitionProperty = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> registerDateOfBirth = new SimpleObjectProperty<>();

    // --- shared UI state ---
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final ObjectProperty<UserDto> currentUser = new SimpleObjectProperty<>();

    // --- validation ---
    private final BooleanProperty emailIsValid = new SimpleBooleanProperty();
    private final BooleanProperty passwordRepetitionCorrect = new SimpleBooleanProperty(false);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );


    public AuthViewModel(AuthPort authPort){
        this.authPort = authPort;
    }

    /**
     * Attempts to log in using the current {@link #emailProperty()} and {@link #passwordProperty()}
     * field values.
     */
    public void login(){
        emailIsValid.set(true);
        errorMessage.set("");

        String rawInput = emailProperty().get();
        String input = rawInput != null ? rawInput.trim() : "";

        if (input.isBlank()) {
            errorMessage.set("Please enter an email");
            emailIsValid.set(false);
            return;
        }

        if (!isEmailValid(input)) {
            errorMessage.set(input + " is not a valid email format");
            emailIsValid.set(false);
            return;
        }

        try {
            UserDto user = authPort.login(input, password.get());
            currentUser.set(user);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Attempts to register a new account using the current register-form field
     * values ({@link #registerUsernameProperty()}, {@link #registerNameProperty()}, {@link #registerEmailProperty()},
     * {@link #registerPasswordProperty()}, {@link #registerDateOfBirthProperty()}).
     */
    public void register(){
        emailIsValid.set(true);
        passwordRepetitionCorrect.set(true);
        errorMessage.set("");

        if (!isEmailValid(registerEmailProperty().get())) {
            errorMessage.set(registerEmailProperty().get() + " is not a valid email");
            emailIsValid.set(false);
            return;
        }

        if (!Objects.equals(registerPasswordRepetitionProperty().get(), registerPasswordProperty().get())) {
            errorMessage.set("Password and its repetition does not match");
            passwordRepetitionCorrect.set(false);
            return;
        }

        try{
            LocalDateTime dob = registerDateOfBirth.get() != null
                    ? registerDateOfBirth.get().atStartOfDay()
                    : null;

            UserDto user = authPort.register(
                    registerUsername.get(),
                    registerName.get(),
                    registerPassword.get(),
                    registerEmail.get(),
                    dob
            );
            currentUser.set(user);
        } catch (Exception e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Updates name and pfpUrl of current logged in user state.
     */
    public void updateCurrentUserDetails(String newName, String newPfpUrl) {
        UserDto current = currentUser.get();
        if (current != null) {
            currentUser.set(new UserDto(current.id(), current.username(), current.email(), newName, newPfpUrl));
        }
    }

    // Getters

    // --- login property getters ---
    public StringProperty emailProperty() { return email; }
    public StringProperty passwordProperty() { return password; }

    // --- register property getters ---
    public StringProperty registerUsernameProperty() { return registerUsername; }
    public StringProperty registerNameProperty() { return registerName; }
    public StringProperty registerEmailProperty() { return registerEmail; }
    public StringProperty registerPasswordProperty() { return registerPassword; }
    public StringProperty registerPasswordRepetitionProperty() { return registerPasswordRepetitionProperty; }
    public ObjectProperty<LocalDate> registerDateOfBirthProperty() { return registerDateOfBirth; }

    // --- shared state getters ---
    public StringProperty errorMessageProperty() { return errorMessage; }
    public ReadOnlyObjectProperty<UserDto> currentUserProperty() { return currentUser; }

    // --- validation ---
    public BooleanProperty emailIsValid() { return emailIsValid; }
    public BooleanProperty passwordRepetitionCorrect() { return passwordRepetitionCorrect; }
    private boolean isEmailValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
