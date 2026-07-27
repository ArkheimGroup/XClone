package arkheim.client.presentation.controllers;

import arkheim.client.domain.ports.dtos.PostDto;
import arkheim.client.domain.ports.dtos.UserDto;
import arkheim.client.domain.ports.dtos.UserProfileDto;
import arkheim.client.presentation.theme.ThemeMode;
import arkheim.client.presentation.navigation.JavaFxNavigator;
import arkheim.client.presentation.utils.IconUtils;
import arkheim.client.presentation.utils.MediaUiUtils;
import arkheim.client.presentation.viewmodels.AuthViewModel;
import arkheim.client.presentation.viewmodels.FollowViewModel;
import arkheim.client.presentation.viewmodels.PostViewModel;
import arkheim.client.presentation.viewmodels.UserViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class ProfileController extends BaseController {

    @FXML
    private ImageView logoImageView;
    @FXML
    private ImageView navHomeIcon;
    @FXML
    private ImageView navExploreIcon;
    @FXML
    private ImageView navProfileIcon;
    @FXML
    private Circle userAvatarCircle;
    @FXML
    private Label userDisplayName;
    @FXML
    private Label userHandleName;
    @FXML
    private Button themeToggleBtn;

    @FXML
    private Button backButton;
    @FXML
    private Label headerProfileName;
    @FXML
    private Label headerPostCount;
    @FXML
    private Circle profileAvatarCircle;

    @FXML
    private Button editProfileButton;
    @FXML
    private Button followButton;
    @FXML
    private Button unfollowButton;

    @FXML
    private VBox profileInfoBox;
    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileHandleLabel;
    @FXML
    private Label profileBioLabel;
    @FXML
    private Label profileDobLabel;
    @FXML
    private Label profileJoinedLabel;
    @FXML
    private Label followingCountLabel;
    @FXML
    private Label followersCountLabel;

    @FXML
    private VBox editProfileFormBox;
    @FXML
    private TextField editNameField;
    @FXML
    private TextArea editBioArea;
    @FXML
    private DatePicker editDobPicker;
    @FXML
    private Button uploadAvatarBtn;

    @FXML
    private Label profileErrorLabel;
    @FXML
    private VBox profileTimelineContainer;
    @FXML
    private ScrollPane profileScrollPane;
    @FXML
    private TextField searchField;
    @FXML
    private VBox searchResultsContainer;

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    private FollowViewModel followViewModel;
    private PostViewModel postViewModel;
    private arkheim.client.presentation.viewmodels.MediaViewModel mediaViewModel;

    private UserDto currentUser;
    private UUID profileId;

    @FXML
    private void initialize() {
        updateIcons();
    }

    public void setViewModels(AuthViewModel authViewModel, UserViewModel userViewModel,
                              FollowViewModel followViewModel, PostViewModel postViewModel, UUID profileId) {
        this.authViewModel = authViewModel;
        this.userViewModel = userViewModel;
        this.followViewModel = followViewModel;
        this.postViewModel = postViewModel;
        this.mediaViewModel = new arkheim.client.presentation.viewmodels.MediaViewModel(new arkheim.client.infrastructure.adapter.HttpMediaAdapter());
        this.profileId = profileId;

        this.currentUser = authViewModel.currentUserProperty().get();
        if (currentUser != null) {
            userDisplayName.setText(currentUser.name());
            userHandleName.setText("@" + currentUser.username());
            MediaUiUtils.loadAvatar(userAvatarCircle, currentUser.pfpUrl(), themeMode);
        }

        initializeStateBindings();
        loadData();
    }

    private void initializeStateBindings() {
        // Bind form fields to UserViewModel
        editNameField.textProperty().bindBidirectional(userViewModel.editNameProperty());
        editBioArea.textProperty().bindBidirectional(userViewModel.editBiographyProperty());
        editDobPicker.valueProperty().bindBidirectional(userViewModel.editDateOfBirthProperty());

        // Bind Profile changes
        userViewModel.currentProfileProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                renderProfileDetails(newVal);
            }
        });

        // Bind Follow state changes
        followViewModel.isFollowingProperty().addListener((obs, oldVal, newVal) -> {
            updateFollowButtonVisibility(newVal);
        });

        // Bind Timeline changes
        postViewModel.userPostsProperty().addListener((ListChangeListener<PostDto>) change -> {
            renderTimeline();
        });

        // Bind Error messages
        profileErrorLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            if (!userViewModel.errorMessageProperty().get().isEmpty()) {
                                return userViewModel.errorMessageProperty().get();
                            }
                            if (!followViewModel.errorMessageProperty().get().isEmpty()) {
                                return followViewModel.errorMessageProperty().get();
                            }
                            if (!postViewModel.errorMessageProperty().get().isEmpty()) {
                                return postViewModel.errorMessageProperty().get();
                            }
                            return "";
                        },
                        userViewModel.errorMessageProperty(),
                        followViewModel.errorMessageProperty(),
                        postViewModel.errorMessageProperty()
                )
        );

        // Bind Error label visibility
        profileErrorLabel.visibleProperty().bind(profileErrorLabel.textProperty().isNotEmpty());
        profileErrorLabel.managedProperty().bind(profileErrorLabel.textProperty().isNotEmpty());
    }

    private void loadData() {
        userViewModel.loadProfileById(profileId);

        if (currentUser != null && !currentUser.id().equals(profileId)) {
            // Viewing another user: hide Edit Profile and check Follow state
            editProfileButton.setVisible(false);
            editProfileButton.setManaged(false);
            followViewModel.checkIsFollowing(currentUser.id(), profileId);
            updateFollowButtonVisibility(followViewModel.isFollowingProperty().get());
        } else {
            // Viewing own profile: show Edit Profile and hide Follow actions
            editProfileButton.setVisible(true);
            editProfileButton.setManaged(true);
            followButton.setVisible(false);
            followButton.setManaged(false);
            unfollowButton.setVisible(false);
            unfollowButton.setManaged(false);
        }
    }

    private void renderProfileDetails(UserProfileDto profile) {
        headerProfileName.setText(profile.name());
        profileNameLabel.setText(profile.name());
        profileHandleLabel.setText("@" + profile.username());
        profileBioLabel.setText(profile.biography() == null ? "" : profile.biography());

        MediaUiUtils.loadAvatar(profileAvatarCircle, profile.pfpUrl(), themeMode);

        String dobText = profile.dateOfBirth() != null
                ? "Born " + profile.dateOfBirth().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                : "Date of birth undisclosed";
        profileDobLabel.setText(dobText);
        IconUtils.setLabelIcon(profileDobLabel, "calendar", themeMode, 14);

        String joinedText = profile.createdAt() != null
                ? "Joined " + profile.createdAt().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                : "Joined recently";
        profileJoinedLabel.setText(joinedText);
        IconUtils.setLabelIcon(profileJoinedLabel, "calendar", themeMode, 14);

        followingCountLabel.setText(String.valueOf(profile.followingCount()));
        followersCountLabel.setText(String.valueOf(profile.followerCount()));

        postViewModel.loadUserPosts(profile.username());
    }

    @FXML
    private void onUploadAvatarClicked() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Avatar Image");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"),
                new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
        );

        javafx.stage.Window window = profileAvatarCircle != null && profileAvatarCircle.getScene() != null
                ? profileAvatarCircle.getScene().getWindow()
                : null;
        java.io.File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile != null && currentUser != null && mediaViewModel != null) {
            long maxSizeBytes = 15L * 1024 * 1024; // 15MB
            if (selectedFile.length() > maxSizeBytes) {
                if (profileErrorLabel != null) {
                    profileErrorLabel.setText("Failed to upload avatar: File size exceeds maximum limit of 15MB");
                    profileErrorLabel.setVisible(true);
                    profileErrorLabel.setManaged(true);
                }
                return;
            }

            new Thread(() -> {
                try {
                    arkheim.client.domain.ports.dtos.MediaDto uploadedMedia = mediaViewModel.uploadMedia(selectedFile, currentUser.id());
                    if (uploadedMedia != null) {
                        Platform.runLater(() -> {
                            userViewModel.editPfpUrlProperty().set(uploadedMedia.url());
                            MediaUiUtils.loadAvatar(profileAvatarCircle, uploadedMedia.url(), themeMode);
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        if (profileErrorLabel != null) {
                            profileErrorLabel.setText("Failed to upload avatar: " + e.getMessage());
                            profileErrorLabel.setVisible(true);
                            profileErrorLabel.setManaged(true);
                        }
                    });
                }
            }).start();
        }
    }


    private void updateFollowButtonVisibility(boolean isFollowing) {
        if (currentUser != null && currentUser.id().equals(profileId)) {
            return; // Own profile doesn't show follow buttons
        }
        followButton.setVisible(!isFollowing);
        followButton.setManaged(!isFollowing);
        unfollowButton.setVisible(isFollowing);
        unfollowButton.setManaged(isFollowing);
    }

    private void renderTimeline() {
        profileTimelineContainer.getChildren().clear();
        headerPostCount.setText(postViewModel.userPostsProperty().size() + " posts");

        if (postViewModel.userPostsProperty().isEmpty()) {
            VBox emptyBox = new VBox(12.0);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 40px;");
            Label title = new Label("No posts yet");
            title.getStyleClass().add("empty-title");
            Label desc = new Label("Posts shared by this user will appear here.");
            desc.getStyleClass().add("empty-desc");
            emptyBox.getChildren().addAll(title, desc);
            profileTimelineContainer.getChildren().add(emptyBox);
        } else {
            UUID pinnedId = userViewModel.currentProfileProperty().get() != null
                    ? userViewModel.currentProfileProperty().get().pinnedPostId()
                    : null;

            // Sort pinned post to top
            java.util.List<PostDto> sorted = new java.util.ArrayList<>(postViewModel.userPostsProperty());
            if (pinnedId != null) {
                sorted.sort((a, b) -> {
                    if (a.id().equals(pinnedId)) return -1;
                    if (b.id().equals(pinnedId)) return 1;
                    return 0;
                });
            }

            for (PostDto post : sorted) {
                boolean isPinned = pinnedId != null && pinnedId.equals(post.id());
                profileTimelineContainer.getChildren().add(createPostCard(post, isPinned));
            }
        }
    }

    private Node createPostCard(PostDto post) {
        return createPostCard(post, false);
    }

    private Node createPostCard(PostDto post, boolean isPinned) {
        VBox card = new VBox(10.0);
        card.getStyleClass().add("post-card");

        // Pinned badge at top of card
        if (isPinned) {
            HBox pinnedBadge = new HBox(6.0);
            pinnedBadge.setAlignment(Pos.CENTER_LEFT);
            ImageView pinIcon = IconUtils.createIconView("pin", themeMode, 14);
            Label pinnedLabel = new Label("Pinned");
            pinnedLabel.getStyleClass().add("post-author-handle");
            pinnedLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            pinnedBadge.getChildren().addAll(pinIcon, pinnedLabel);
            pinnedBadge.setStyle("-fx-padding: 0 0 4px 0;");
            card.getChildren().add(pinnedBadge);
        }

        // Hook up card clicking to show details
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1 && navigator != null) {
                navigator.showPostDetailsScreen(post.id());
            }
        });

        HBox header = new HBox(12.0);
        Circle avatar = new Circle(20.0);
        MediaUiUtils.loadAvatar(avatar, post.authorPfpUrl(), themeMode);

        avatar.setStroke(Color.web(themeMode == ThemeMode.LIGHT ? "#71767B" : "#2F3336"));

        VBox meta = new VBox(2.0);
        HBox metaRow = new HBox(6.0);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(post.authorName());
        name.getStyleClass().add("post-author-name");

        Label handle = new Label("@" + post.authorUsername());
        handle.getStyleClass().add("post-author-handle");

        Label dot = new Label("·");
        dot.getStyleClass().add("post-author-handle");

        String time = post.createdAt() != null ? post.createdAt().format(DateTimeFormatter.ofPattern("MMM dd")) : "Just now";
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("post-timestamp");

        metaRow.getChildren().addAll(name, handle, dot, timeLabel);
        meta.getChildren().add(metaRow);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, meta, spacer);

        // Follow button
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

        // Pin / Unpin button (only for own posts)
        if (currentUser != null && currentUser.id().equals(post.authorId())) {
            Button pinBtn = new Button();
            String iconName = isPinned ? "unpin" : "pin";
            IconUtils.setButtonIcon(pinBtn, iconName, themeMode, 16);
            pinBtn.getStyleClass().add("post-action-btn");
            pinBtn.setStyle("-fx-padding: 4px;");
            pinBtn.setOnAction(e -> {
                e.consume();
                if (isPinned) {
                    userViewModel.unpinPost(currentUser.id());
                } else {
                    userViewModel.pinPost(currentUser.id(), post.id());
                }
                // Re-render to reflect new pin state
                userViewModel.loadProfileById(profileId);
            });
            header.getChildren().add(pinBtn);
        }

        // Delete button
        if (currentUser != null && currentUser.id().equals(post.authorId())) {
            Button deleteBtn = new Button();
            IconUtils.setButtonIcon(deleteBtn, "trash", themeMode, 16);
            deleteBtn.getStyleClass().add("post-action-btn");
            deleteBtn.setStyle("-fx-text-fill: #E02424; -fx-padding: 4px;");
            deleteBtn.setOnAction(e -> {
                e.consume();
                postViewModel.deletePost(post.id(), currentUser.id());
                userViewModel.loadProfileById(profileId);
            });
            header.getChildren().add(deleteBtn);
        }

        Node bodyNode = createFormattedPostBody(post.content(), themeMode, tag -> {
            searchField.setText(tag);
            onSearchSubmitted();
        });

        card.getChildren().addAll(header, bodyNode);

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

        if (post.mediaUrls() != null && !post.mediaUrls().isEmpty()) {
            for (String mediaUrl : post.mediaUrls()) {
                if (mediaUrl != null && !mediaUrl.isBlank()) {
                    card.getChildren().add(MediaUiUtils.createMediaPreviewNode(mediaUrl, themeMode));
                }
            }
        }


        HBox actions = new HBox(40.0);
        actions.getStyleClass().add("post-actions");

        Button replyBtn = new Button(" " + post.replyCount());
        IconUtils.setButtonIcon(replyBtn, "comment", themeMode, 14);
        replyBtn.getStyleClass().add("post-action-btn");
        replyBtn.setOnAction(e -> {
            e.consume();
            if (navigator != null) {
                navigator.showPostDetailsScreen(post.id());
            }
        });

        Button repostBtn = new Button(" " + post.repostCount());
        IconUtils.setButtonIcon(repostBtn, "repost", themeMode, 14);
        repostBtn.getStyleClass().add("post-action-btn");
        if (post.repostedByMe()) {
            repostBtn.setStyle("-fx-text-fill: -fx-text-primary; -fx-font-weight: bold;");
        }
        repostBtn.setOnAction(e -> {
            e.consume();
            if (currentUser != null) {
                postViewModel.repost(post.id(), currentUser.id());
            }
        });

        String likeIconName = post.likedByMe() ? "heart_full" : "heart";
        Button likeBtn = new Button(" " + post.likeCount());
        IconUtils.setButtonIcon(likeBtn, likeIconName, themeMode, 14);
        likeBtn.getStyleClass().add("post-action-btn");
        if (post.likedByMe()) {
            likeBtn.setStyle("-fx-text-fill: -fx-text-primary; -fx-font-weight: bold;");
        }
        likeBtn.setOnAction(e -> {
            e.consume(); // prevent navigation trigger
            if (currentUser != null) {
                postViewModel.toggleLike(post.id(), currentUser.id());
            }
        });

        actions.getChildren().addAll(replyBtn, repostBtn, likeBtn);
        card.getChildren().add(actions);

        return card;
    }

    @FXML
    private void onBackClicked() {
        navigator.showHomeScreen();
    }

    @FXML
    private void onEditProfileClicked() {
        userViewModel.loadFormFromCurrentProfile();
        profileInfoBox.setVisible(false);
        profileInfoBox.setManaged(false);
        editProfileFormBox.setVisible(true);
        editProfileFormBox.setManaged(true);
    }

    @FXML
    private void onCancelEditClicked() {
        editProfileFormBox.setVisible(false);
        editProfileFormBox.setManaged(false);
        profileInfoBox.setVisible(true);
        profileInfoBox.setManaged(true);
    }

    @FXML
    private void onSaveProfileClicked() {
        userViewModel.updateProfile(profileId);
        UserProfileDto updatedProfile = userViewModel.currentProfileProperty().get();
        if (updatedProfile != null && authViewModel != null && currentUser != null && currentUser.id().equals(profileId)) {
            authViewModel.updateCurrentUserDetails(updatedProfile.name(), updatedProfile.pfpUrl());
            this.currentUser = authViewModel.currentUserProperty().get();
            if (currentUser != null) {
                userDisplayName.setText(currentUser.name());
                userHandleName.setText("@" + currentUser.username());
                MediaUiUtils.loadAvatar(userAvatarCircle, currentUser.pfpUrl(), themeMode);
            }
        }
        editProfileFormBox.setVisible(false);
        editProfileFormBox.setManaged(false);
        profileInfoBox.setVisible(true);
        profileInfoBox.setManaged(true);
        // Refresh counts
        loadData();
    }

    @FXML
    private void onFollowClicked() {
        if (currentUser != null) {
            followViewModel.followUser(currentUser.id(), profileId);
            // Refresh counts
            userViewModel.loadProfileById(profileId);
        }
    }

    @FXML
    private void onUnfollowClicked() {
        if (currentUser != null) {
            followViewModel.unfollowUser(currentUser.id(), profileId);
            // Refresh counts
            userViewModel.loadProfileById(profileId);
        }
    }

    @FXML
    private void onFollowingClicked() {
        followViewModel.loadFollowing(profileId);
        showUserListDialog("Following", followViewModel.followingProperty());
    }

    @FXML
    private void onFollowersClicked() {
        followViewModel.loadFollowers(profileId);
        showUserListDialog("Followers", followViewModel.followersProperty());
    }

    private void showUserListDialog(String title, java.util.List<UserDto> users) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(title);
        dialog.setHeaderText(title + " (" + users.size() + ")");

        VBox listBox = new VBox(8.0);
        listBox.setStyle("-fx-padding: 10px;");
        listBox.setPrefWidth(350);
        listBox.setMaxHeight(400);

        if (users.isEmpty()) {
            Label emptyLabel = new Label("No " + title.toLowerCase() + " yet.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #71767B;");
            listBox.getChildren().add(emptyLabel);
        } else {
            for (UserDto user : users) {
                HBox row = new HBox(12.0);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 8px; -fx-background-radius: 8px; -fx-cursor: hand;");

                Circle avatar = new Circle(18.0);
                avatar.setFill(Color.web(themeMode == ThemeMode.LIGHT ? "#E7E7E8" : "#16181C"));
                avatar.setStroke(Color.web(themeMode == ThemeMode.LIGHT ? "#71767B" : "#2F3336"));

                VBox info = new VBox(2.0);
                Label nameLabel = new Label(user.name() != null ? user.name() : user.username());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                Label handleLabel = new Label("@" + user.username());
                handleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #71767B;");
                info.getChildren().addAll(nameLabel, handleLabel);

                row.getChildren().addAll(avatar, info);

                row.setOnMouseClicked(e -> {
                    dialog.close();
                    if (navigator != null) {
                        navigator.showProfileScreen(user.id());
                    }
                });

                // Hover effect
                row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 8px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-background-color: rgba(128,128,128,0.1);"));
                row.setOnMouseExited(e -> row.setStyle("-fx-padding: 8px; -fx-background-radius: 8px; -fx-cursor: hand;"));

                listBox.getChildren().add(row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(listBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(Math.min(users.size() * 60 + 20, 400));
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(400);

        // Apply theme styling
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().clear();
        String styleFile = (themeMode == ThemeMode.LIGHT) ? "Style.css" : "DarkMode.css";
        try {
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/arkheim/client/presentation/Assets/" + styleFile)).toExternalForm());
        } catch (Exception ignored) {}


        dialog.showAndWait();
    }

    @Override
    public void setThemeMode(ThemeMode themeMode) {
        super.setThemeMode(themeMode);
        if (userViewModel != null && userViewModel.currentProfileProperty().get() != null) {
            renderProfileDetails(userViewModel.currentProfileProperty().get());
            renderTimeline();
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
        if (uploadAvatarBtn != null) IconUtils.setButtonIcon(uploadAvatarBtn, "image", themeMode, 16);
        if (profileDobLabel != null) IconUtils.setLabelIcon(profileDobLabel, "calendar", themeMode, 14);
        if (profileJoinedLabel != null) IconUtils.setLabelIcon(profileJoinedLabel, "calendar", themeMode, 14);
        if (themeToggleBtn != null) {
            themeToggleBtn.setText(themeMode == ThemeMode.LIGHT ? "☾ Dark Mode" : "☼ Light Mode");
        }
        if (userAvatarCircle != null && currentUser != null) {
            MediaUiUtils.loadAvatar(userAvatarCircle, currentUser.pfpUrl(), themeMode);
        }
    }


    @FXML
    private void onSearchSubmitted() {
        String query = searchField.getText() != null ? searchField.getText().trim() : "";
        if (query.isEmpty()) {
            if (searchResultsContainer != null) searchResultsContainer.getChildren().clear();
        } else {
            postViewModel.findPostsByWord(query, currentUser != null ? currentUser.id() : null);
            renderSearchResults();
        }
    }

    private void renderSearchResults() {
        if (searchResultsContainer == null) return;
        searchResultsContainer.getChildren().clear();
        Label searchTitle = new Label("Search results");
        searchTitle.getStyleClass().add("empty-title");
        searchTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 4px 0 8px 0;");
        searchResultsContainer.getChildren().add(searchTitle);

        if (postViewModel.searchResultsProperty().isEmpty()) {
            Label desc = new Label("No matching posts found.");
            desc.getStyleClass().add("empty-desc");
            searchResultsContainer.getChildren().add(desc);
        } else {
            for (PostDto post : postViewModel.searchResultsProperty()) {
                searchResultsContainer.getChildren().add(createPostCard(post));
            }
        }
    }

    @FXML
    private void onSidebarPostClicked() {
        navigator.showHomeScreen();
    }

    // Side nav actions
    @FXML
    private void onNavHomeClicked() {
        navigator.showHomeScreen();
    }

    @FXML
    private void onNavExploreClicked() {
        searchField.requestFocus();
    }



    @FXML
    private void onNavProfileClicked() {
        if (currentUser != null) {
            navigator.showProfileScreen(currentUser.id());
        }
    }

    private void showMockAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().clear();
        String styleFile = (themeMode == ThemeMode.LIGHT) ? "Style.css" : "DarkMode.css";
        try {
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/arkheim/client/presentation/Assets/" + styleFile)).toExternalForm());
        } catch (Exception ignored) {}


        alert.showAndWait();
    }
}
