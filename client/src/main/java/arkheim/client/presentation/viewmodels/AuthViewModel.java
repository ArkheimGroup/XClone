package arkheim.client.presentation.viewmodels;

import arkheim.client.domain.ports.AuthPort;
import arkheim.client.domain.ports.dtos.UserDto;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private final ObjectProperty<LocalDate> registerDateOfBirth = new SimpleObjectProperty<>();

    // --- shared UI state ---
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final ObjectProperty<UserDto> currentUser = new SimpleObjectProperty<>();


    public AuthViewModel(AuthPort authPort){
        this.authPort = authPort;
    }

    /**
     * Attempts to log in using the current {@link #emailProperty()} and {@link #passwordProperty()}
     * field values.
     */
    public void login(){
        errorMessage.set("");
        try{
            UserDto user = authPort.login(email.get(), password.get());
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
        errorMessage.set("");
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

    // Getters

    // --- login property getters ---
    public StringProperty emailProperty() { return email; }
    public StringProperty passwordProperty() { return password; }

    // --- register property getters ---
    public StringProperty registerUsernameProperty() { return registerUsername; }
    public StringProperty registerNameProperty() { return registerName; }
    public StringProperty registerEmailProperty() { return registerEmail; }
    public StringProperty registerPasswordProperty() { return registerPassword; }
    public ObjectProperty<LocalDate> registerDateOfBirthProperty() { return registerDateOfBirth; }

    // --- shared state getters ---
    public StringProperty errorMessageProperty() { return errorMessage; }
    public ReadOnlyObjectProperty<UserDto> currentUserProperty() { return currentUser; }

}
