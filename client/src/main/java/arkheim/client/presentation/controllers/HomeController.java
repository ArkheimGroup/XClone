package arkheim.client.presentation.controllers;

import arkheim.client.domain.ports.dtos.MediaDto;
import arkheim.client.domain.ports.dtos.PostDto;
import arkheim.client.domain.ports.dtos.UserDto;
import arkheim.client.presentation.state.FeedUiEvent;
import arkheim.client.presentation.state.FeedUiState;
import arkheim.client.presentation.theme.ThemeMode;
import arkheim.client.presentation.navigation.JavaFxNavigator;
import arkheim.client.presentation.utils.IconUtils;
import arkheim.client.presentation.utils.MediaUiUtils;
import arkheim.client.presentation.viewmodels.AuthViewModel;
import arkheim.client.presentation.viewmodels.FeedViewModel;
import arkheim.client.presentation.viewmodels.FollowViewModel;
import arkheim.client.presentation.viewmodels.MediaViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class HomeController extends BaseController {

    @FXML
    private ImageView logoImageView;
    @FXML
    private ImageView navHomeIcon;
    @FXML
    private ImageView navExploreIcon;
    @FXML
    private ImageView navProfileIcon;
    @FXML
    private ImageView navLogoutIcon;
    @FXML
    private Circle userAvatarCircle;
    @FXML
    private Label userDisplayName;
    @FXML
    private Label userHandleName;
    @FXML
    private Button themeToggleBtn;

    @FXML
    private VBox tabForYou;
    @FXML
    private VBox tabForYouIndicator;
    @FXML
    private VBox tabFollowing;
    @FXML
    private VBox tabFollowingIndicator;

    @FXML
    private Circle composerAvatarCircle;
    @FXML
    private TextArea composerTextArea;
    @FXML
    private Button composerMediaButton;
    @FXML
    private Button composerPostButton;
    @FXML
    private Label feedErrorLabel;

    @FXML
    private VBox feedTimelineContainer;
    @FXML
    private ScrollPane feedScrollPane;

    @FXML
    private TextField searchField;
    @FXML
    private VBox searchResultsContainer;
    @FXML
    private VBox whoToFollowContainer;

    private AuthViewModel authViewModel;
    private FeedViewModel feedViewModel;
    private FollowViewModel followViewModel;
    private MediaViewModel mediaViewModel;
    private String pendingMediaUrl = null;
    private String pendingMediaFileName = null;

    private boolean bindingsInitialized = false;
    private UserDto currentUser;

    @FXML
    private void initialize() {
        // Initial setup for static elements
        updateIcons();
    }

    public void setViewModels(AuthViewModel authViewModel, FeedViewModel feedViewModel, FollowViewModel followViewModel, MediaViewModel mediaViewModel) {
        this.authViewModel = authViewModel;
        this.feedViewModel = feedViewModel;
        this.followViewModel = followViewModel;
        this.mediaViewModel = mediaViewModel;

        this.currentUser = authViewModel.currentUserProperty().get();
        if (currentUser != null) {
            userDisplayName.setText(currentUser.name());
            userHandleName.setText("@" + currentUser.username());
            MediaUiUtils.loadAvatar(userAvatarCircle, currentUser.pfpUrl(), themeMode);
            MediaUiUtils.loadAvatar(composerAvatarCircle, currentUser.pfpUrl(), themeMode);
        }

        authViewModel.currentUserProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.currentUser = newVal;
                if (userDisplayName != null) userDisplayName.setText(newVal.name());
                if (userHandleName != null) userHandleName.setText("@" + newVal.username());
                if (userAvatarCircle != null) MediaUiUtils.loadAvatar(userAvatarCircle, newVal.pfpUrl(), themeMode);
                if (composerAvatarCircle != null) MediaUiUtils.loadAvatar(composerAvatarCircle, newVal.pfpUrl(), themeMode);
            }
        });

        initializeStateBindings();
    }

    public void setViewModels(AuthViewModel authViewModel, FeedViewModel feedViewModel, FollowViewModel followViewModel) {
        setViewModels(authViewModel, feedViewModel, followViewModel, new MediaViewModel(new arkheim.client.infrastructure.adapter.HttpMediaAdapter()));
    }

    private void initializeStateBindings() {
        if (bindingsInitialized) return;

        // Reactive binding: Whenever the FeedUiState changes, re-render the view
        feedViewModel.uiStateProperty().addListener((obs, oldState, newState) -> renderState(newState));

        // Composer Input binding: Local state update only (no UI re-render on keystroke)
        composerTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            updateComposerPostButtonState();
        });

        // Trigger first feed load
        if (currentUser != null) {
            feedViewModel.processEvent(new FeedUiEvent.LoadFeed(currentUser.id()));
        }

        // Render the initial state snapshot
        renderState(feedViewModel.getState());
        bindingsInitialized = true;
    }

    private void updateComposerPostButtonState() {
        if (composerPostButton == null || composerTextArea == null) return;
        boolean isTextEmpty = composerTextArea.getText() == null || composerTextArea.getText().strip().isEmpty();
        boolean isMediaEmpty = pendingMediaUrl == null;
        boolean isPosting = feedViewModel != null && feedViewModel.getState().isPosting();
        composerPostButton.setDisable(isPosting || (isTextEmpty && isMediaEmpty));
    }

    /**
     * Renders the UI nodes depending on the current FeedUiState snapshot.
     */
    private void renderState(FeedUiState state) {
        // Sync search field if search is cleared
        if (!state.isSearching() && searchField != null && searchField.getText() != null && !searchField.getText().isEmpty()) {
            searchField.setText("");
        }

        // Enable/Disable composer post button
        updateComposerPostButtonState();

        // Update tab indicators active styles
        updateTabStyles(state.activeTab());

        // Error & Media Attachment banner visibility
        if (state.error() != null) {
            feedErrorLabel.setText(state.error());
            feedErrorLabel.setStyle("-fx-text-fill: #F4212E; -fx-font-weight: bold;");
            feedErrorLabel.setVisible(true);
            feedErrorLabel.setManaged(true);
        } else if (pendingMediaUrl != null) {
            String name = pendingMediaFileName != null ? pendingMediaFileName : "attachment";
            feedErrorLabel.setText("✓ Media attached (" + name + ")");
            feedErrorLabel.setStyle("-fx-text-fill: #1D9BF0; -fx-font-weight: bold;");
            feedErrorLabel.setVisible(true);
            feedErrorLabel.setManaged(true);
        } else {
            feedErrorLabel.setVisible(false);
            feedErrorLabel.setManaged(false);
        }


        // Render main feed timeline elements in center column
        feedTimelineContainer.getChildren().clear();

        if (state.isLoading()) {
            renderLoadingSkeletons();
        } else if (state.posts().isEmpty()) {
            renderEmptyState();
        } else {
            for (PostDto post : state.posts()) {
                feedTimelineContainer.getChildren().add(createPostCard(post));
            }
        }

        // Render search results in right sidebar container
        if (searchResultsContainer != null) {
            searchResultsContainer.getChildren().clear();
            if (state.isSearching()) {
                Label searchTitle = new Label("Search results for \"" + state.searchQuery() + "\"");
                searchTitle.getStyleClass().add("empty-title");
                searchTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 4px 0 8px 0;");
                searchResultsContainer.getChildren().add(searchTitle);

                if (state.searchResults().isEmpty()) {
                    Label noResultsLabel = new Label("No matching posts found.");
                    noResultsLabel.getStyleClass().add("empty-desc");
                    searchResultsContainer.getChildren().add(noResultsLabel);
                } else {
                    for (PostDto post : state.searchResults()) {
                        searchResultsContainer.getChildren().add(createPostCard(post));
                    }
                }
            }
        }
    }

    private void updateTabStyles(FeedUiState.TabType activeTab) {
        tabForYou.getStyleClass().removeAll("tab-item-active");
        tabFollowing.getStyleClass().removeAll("tab-item-active");
        tabForYouIndicator.getStyleClass().removeAll("tab-item-active-indicator");
        tabFollowingIndicator.getStyleClass().removeAll("tab-item-active-indicator");

        if (activeTab == FeedUiState.TabType.FOR_YOU) {
            tabForYou.getStyleClass().add("tab-item-active");
            tabForYouIndicator.getStyleClass().add("tab-item-active-indicator");
        } else {
            tabFollowing.getStyleClass().add("tab-item-active");
            tabFollowingIndicator.getStyleClass().add("tab-item-active-indicator");
        }
    }

    private void renderLoadingSkeletons() {
        for (int i = 0; i < 3; i++) {
            VBox card = new VBox(12.0);
            card.getStyleClass().add("post-card");
            card.setStyle("-fx-padding: 16px;");

            HBox header = new HBox(12.0);
            Circle avatar = new Circle(20.0);
            avatar.getStyleClass().add("skeleton-circle");

            VBox meta = new VBox(6.0);
            Region line1 = new Region();
            line1.getStyleClass().add("skeleton-bg");
            line1.setPrefSize(140.0, 12.0);
            Region line2 = new Region();
            line2.getStyleClass().add("skeleton-bg");
            line2.setPrefSize(80.0, 8.0);
            meta.getChildren().addAll(line1, line2);

            header.getChildren().addAll(avatar, meta);

            Region body = new Region();
            body.getStyleClass().add("skeleton-bg");
            body.setPrefSize(450.0, 14.0);

            Region body2 = new Region();
            body2.getStyleClass().add("skeleton-bg");
            body2.setPrefSize(350.0, 14.0);

            card.getChildren().addAll(header, body, body2);
            feedTimelineContainer.getChildren().add(card);
        }
    }

    private void renderEmptyState() {
        VBox container = new VBox(16.0);
        container.getStyleClass().add("empty-container");

        Label title = new Label("What is happening?!");
        title.getStyleClass().add("empty-title");

        Label desc = new Label("Your feed is currently quiet. Follow users, check explore tags, or publish a new post to get started.");
        desc.getStyleClass().add("empty-desc");
        desc.setWrapText(true);

        container.getChildren().addAll(title, desc);
        feedTimelineContainer.getChildren().add(container);
    }

    /**
     * Reusable, modular UI component mapping directly to domain PostDto entities.
     * Integrates hover states, user actions, and deletes posts directly using events.
     */
    private Node createPostCard(PostDto post) {
        VBox card = new VBox(10.0);
        card.getStyleClass().add("post-card");
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1 && navigator != null) {
                navigator.showPostDetailsScreen(post.id());
            }
        });

        HBox header = new HBox(12.0);

        // Avatar
        Circle avatar = new Circle(20.0);
        MediaUiUtils.loadAvatar(avatar, post.authorPfpUrl(), themeMode);
        avatar.setStroke(Color.web(themeMode == ThemeMode.LIGHT ? "#71767B" : "#2F3336"));

        avatar.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null) {
                navigator.showProfileScreen(post.authorId());
            }
        });

        // User info details
        VBox authorDetails = new VBox(2.0);
        HBox metaRow = new HBox(6.0);
        metaRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(post.authorName());
        nameLabel.getStyleClass().add("post-author-name");
        nameLabel.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null) {
                navigator.showProfileScreen(post.authorId());
            }
        });

        Label handleLabel = new Label("@" + post.authorUsername());
        handleLabel.getStyleClass().add("post-author-handle");

        Label dot = new Label("·");
        dot.getStyleClass().add("post-author-handle");

        String timeText = post.createdAt() != null
                ? post.createdAt().format(DateTimeFormatter.ofPattern("MMM dd"))
                : "Just now";
        Label timeLabel = new Label(timeText);
        timeLabel.getStyleClass().add("post-timestamp");

        metaRow.getChildren().addAll(nameLabel, handleLabel, dot, timeLabel);
        authorDetails.getChildren().add(metaRow);

        // Spacer to push delete button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, authorDetails, spacer);

        // Follow button for other users' posts
        if (currentUser != null && !currentUser.id().equals(post.authorId()) && followViewModel != null) {
            Button followBtn = new Button("Follow");
            followBtn.getStyleClass().add("post-action-btn");
            followBtn.setStyle("-fx-border-color: -fx-border-color-muted; -fx-border-radius: 12px; -fx-padding: 2px 8px; -fx-font-size: 12px;");
            followBtn.setOnAction(e -> {
                e.consume();
                followViewModel.followUser(currentUser.id(), post.authorId());
                followBtn.setText("Following");
                followBtn.setDisable(true);
            });
            header.getChildren().add(followBtn);
        }

        // Delete post support if current user matches post author
        if (currentUser != null && currentUser.id().equals(post.authorId())) {
            Button deleteBtn = new Button();
            IconUtils.setButtonIcon(deleteBtn, "trash", themeMode, 16);
            deleteBtn.getStyleClass().add("post-action-btn");
            deleteBtn.setStyle("-fx-text-fill: #E02424; -fx-padding: 4px;");
            deleteBtn.setOnAction(e -> {
                e.consume();
                feedViewModel.processEvent(new FeedUiEvent.DeletePost(post.id(), currentUser.id()));
            });
            header.getChildren().add(deleteBtn);
        }

        // Post body content
        Node bodyNode = createFormattedPostBody(post.content(), themeMode, tag -> {
            searchField.setText(tag);
            onSearchSubmitted();
        });

        card.getChildren().addAll(header, bodyNode);

        // Add badges for repost or reply
        if (post.isRepost() || post.repostedFromUsername() != null) {
            String origAuthor = post.repostedFromUsername() != null ? post.repostedFromUsername() : (post.repliedUsername() != null ? post.repliedUsername() : "user");
            HBox repostBadge = new HBox(6.0);
            repostBadge.setAlignment(Pos.CENTER_LEFT);
            ImageView repostIcon = IconUtils.createIconView("repost", themeMode, 14);
            Label repostLabel = new Label("Reposted from @" + origAuthor);
            repostLabel.getStyleClass().add("post-author-handle");
            repostLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            repostBadge.getChildren().addAll(repostIcon, repostLabel);
            repostBadge.setStyle("-fx-padding: 0 0 4px 0;");
            card.getChildren().add(0, repostBadge);
        } else if (post.parentPostId() != null) {
            String targetUser = post.repliedUsername() != null ? post.repliedUsername() : "user";
            Label replyingLabel = new Label("Replying to @" + targetUser);
            replyingLabel.getStyleClass().add("post-author-handle");
            replyingLabel.setStyle("-fx-font-size: 12px; -fx-padding: 0 0 2px 0;");
            card.getChildren().add(1, replyingLabel);
        }

        // Render media attachment previews
        if (post.mediaUrls() != null && !post.mediaUrls().isEmpty()) {
            for (String mediaUrl : post.mediaUrls()) {
                if (mediaUrl != null && !mediaUrl.isBlank()) {
                    card.getChildren().add(MediaUiUtils.createMediaPreviewNode(mediaUrl, themeMode));
                }
            }
        }


        // Action icons bar (monochrome metrics)
        HBox actionsRow = new HBox(40.0);
        actionsRow.getStyleClass().add("post-actions");

        Button replyBtn = new Button(" " + post.replyCount());
        IconUtils.setButtonIcon(replyBtn, "comment", themeMode, 16);
        replyBtn.getStyleClass().add("post-action-btn");
        replyBtn.setOnAction(e -> {
            e.consume();
            if (navigator != null) {
                navigator.showPostDetailsScreen(post.id());
            }
        });

        Button repostBtn = new Button(" " + post.repostCount());
        IconUtils.setButtonIcon(repostBtn, "repost", themeMode, 16);
        repostBtn.getStyleClass().add("post-action-btn");
        if (post.repostedByMe()) {
            repostBtn.setStyle("-fx-text-fill: -fx-text-primary; -fx-font-weight: bold;");
        }
        repostBtn.setOnAction(e -> {
            e.consume();
            if (currentUser != null) {
                arkheim.client.infrastructure.adapter.HttpPostAdapter adapter = new arkheim.client.infrastructure.adapter.HttpPostAdapter();
                adapter.createPost(currentUser.id(), "", null, post.id());
                feedViewModel.processEvent(new FeedUiEvent.LoadFeed(currentUser.id()));
            }
        });

        String likeIconName = post.likedByMe() ? "heart_full" : "heart";
        Button likeBtn = new Button(" " + post.likeCount());
        IconUtils.setButtonIcon(likeBtn, likeIconName, themeMode, 16);
        likeBtn.getStyleClass().add("post-action-btn");
        if (post.likedByMe()) {
            likeBtn.setStyle("-fx-text-fill: -fx-text-primary; -fx-font-weight: bold;");
        }
        likeBtn.setOnAction(e -> {
            e.consume();
            if (currentUser != null) {
                feedViewModel.processEvent(new FeedUiEvent.ToggleLike(post.id(), currentUser.id()));
            }
        });

        actionsRow.getChildren().addAll(replyBtn, repostBtn, likeBtn);
        card.getChildren().add(actionsRow);

        return card;
    }

    @Override
    public void setThemeMode(ThemeMode themeMode) {
        super.setThemeMode(themeMode);
        if (feedViewModel != null) {
            renderState(feedViewModel.getState());
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

    @Override
    public void updateIcons() {
        String logoPath = (themeMode == ThemeMode.LIGHT)
                ? "/arkheim/client/presentation/Assets/images/icons/light/XCloneLogo_LightMode_Transparent.png"
                : "/arkheim/client/presentation/Assets/images/icons/dark/XCloneLogo_DarkMode_Transparent.png";
        try {
            if (logoImageView != null) {
                logoImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(logoPath))));
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }
        if (navHomeIcon != null) navHomeIcon.setImage(IconUtils.getIconImage("home", themeMode));
        if (navExploreIcon != null) navExploreIcon.setImage(IconUtils.getIconImage("search", themeMode));
        if (navProfileIcon != null) navProfileIcon.setImage(IconUtils.getIconImage("user", themeMode));
        if (navLogoutIcon != null) navLogoutIcon.setImage(IconUtils.getIconImage("door", themeMode));
        if (composerMediaButton != null) IconUtils.setButtonIcon(composerMediaButton, "image", themeMode, 18);
        if (themeToggleBtn != null) {
            themeToggleBtn.setText(themeMode == ThemeMode.LIGHT ? "☾ Dark Mode" : "☼ Light Mode");
        }
        if (userAvatarCircle != null && currentUser != null) {
            MediaUiUtils.loadAvatar(userAvatarCircle, currentUser.pfpUrl(), themeMode);
        }
        if (composerAvatarCircle != null && currentUser != null) {
            MediaUiUtils.loadAvatar(composerAvatarCircle, currentUser.pfpUrl(), themeMode);
        }
    }


    @FXML
    private void onTabForYouClicked() {
        if (currentUser != null) {
            feedViewModel.processEvent(new FeedUiEvent.SwitchTab(FeedUiState.TabType.FOR_YOU, currentUser.id()));
        }
    }

    @FXML
    private void onTabFollowingClicked() {
        if (currentUser != null) {
            feedViewModel.processEvent(new FeedUiEvent.SwitchTab(FeedUiState.TabType.FOLLOWING, currentUser.id()));
        }
    }

    @FXML
    private void onComposerPostClicked() {
        if (currentUser != null) {
            String text = composerTextArea != null ? composerTextArea.getText() : "";
            feedViewModel.processEvent(new FeedUiEvent.UpdateComposerText(text != null ? text : ""));
            feedViewModel.processEvent(new FeedUiEvent.SubmitPost(currentUser.id(), pendingMediaUrl));
            if (composerTextArea != null) {
                composerTextArea.setText("");
            }
            pendingMediaUrl = null;
            pendingMediaFileName = null;
            updateComposerPostButtonState();
        }
    }

    @FXML
    private void onSearchSubmitted() {
        String query = searchField.getText();
        if (currentUser != null) {
            feedViewModel.processEvent(new FeedUiEvent.PerformSearch(query, currentUser.id()));
        }
    }

    @FXML
    private void onSidebarPostClicked() {
        if ((composerTextArea != null && composerTextArea.getText() != null && !composerTextArea.getText().strip().isEmpty()) || pendingMediaUrl != null) {
            onComposerPostClicked();
        } else if (composerTextArea != null) {
            composerTextArea.requestFocus();
        }
    }

    @FXML
    private void onMediaAttachmentClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Window window = composerTextArea != null && composerTextArea.getScene() != null
                ? composerTextArea.getScene().getWindow()
                : null;
        java.io.File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile != null && currentUser != null && mediaViewModel != null) {
            long maxSizeBytes = 15L * 1024 * 1024; // 15MB
            if (selectedFile.length() > maxSizeBytes) {
                if (feedErrorLabel != null) {
                    feedErrorLabel.setText("Failed to attach media: File size exceeds maximum limit of 15MB");
                    feedErrorLabel.setStyle("-fx-text-fill: #F4212E; -fx-font-weight: bold;");
                    feedErrorLabel.setVisible(true);
                    feedErrorLabel.setManaged(true);
                }
                return;
            }

            new Thread(() -> {
                try {
                    MediaDto uploadedMedia = mediaViewModel.uploadMedia(selectedFile, currentUser.id());
                    if (uploadedMedia != null) {
                        Platform.runLater(() -> {
                            pendingMediaUrl = uploadedMedia.url();
                            pendingMediaFileName = selectedFile.getName();
                            renderState(feedViewModel.getState());
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        if (feedErrorLabel != null) {
                            feedErrorLabel.setText("Failed to upload media: " + e.getMessage());
                            feedErrorLabel.setStyle("-fx-text-fill: #F4212E; -fx-font-weight: bold;");
                            feedErrorLabel.setVisible(true);
                            feedErrorLabel.setManaged(true);
                        }
                    });
                }
            }).start();
        }
    }




    @FXML
    private void onEmojiClicked() {
        String currentText = composerTextArea.getText() == null ? "" : composerTextArea.getText();
        composerTextArea.setText(currentText + " ☺ ");
    }

    // Nav actions: show alert or mock transitions
    @FXML
    private void onNavHomeClicked() {
        if (searchField != null) {
            searchField.setText("");
        }
        if (currentUser != null) {
            feedViewModel.processEvent(new FeedUiEvent.PerformSearch("", currentUser.id()));
            feedViewModel.processEvent(new FeedUiEvent.LoadFeed(currentUser.id()));
        }
    }

    @FXML
    private void onNavExploreClicked() {
        searchField.requestFocus();
    }



    @FXML
    private void onNavProfileClicked() {
        if (currentUser != null && navigator != null) {
            navigator.showProfileScreen(currentUser.id());
        }
    }

    @FXML
    private void onNavLogoutClicked() {
        if (authViewModel != null) {
            authViewModel.logout();
        }
        if (navigator != null) {
            navigator.showLoginScreen();
        }
    }

    private void showMockAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Apply monochrome styling to dialog if it is active
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().clear();
        String styleFile = (themeMode == ThemeMode.LIGHT) ? "Style.css" : "DarkMode.css";
        try {
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/arkheim/client/presentation/Assets/" + styleFile)).toExternalForm());
        } catch (Exception ignored) {}


        alert.showAndWait();
    }
}
