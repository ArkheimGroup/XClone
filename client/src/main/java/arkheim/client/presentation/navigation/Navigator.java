package arkheim.client.presentation.navigation;

public interface Navigator {
    void showLoginScreen();
    void showRegisterScreen();
    void showHomeScreen();
    void showProfileScreen(java.util.UUID userId);
    void showPostDetailsScreen(java.util.UUID postId);
}
