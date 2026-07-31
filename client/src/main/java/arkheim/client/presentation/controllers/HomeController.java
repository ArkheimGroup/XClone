package arkheim.client.presentation.controllers;

import arkheim.client.domain.dtos.Media.response.MediaDto;
import arkheim.client.domain.dtos.Post.response.PostDetailDto;
import arkheim.client.domain.dtos.User.response.UserDto;
import arkheim.client.domain.dtos.User.response.UserProfileDto;
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
import arkheim.client.presentation.viewmodels.PostViewModel;
import arkheim.client.presentation.viewmodels.UserViewModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class HomeController extends BaseController {

    private AuthViewModel authViewModel;
    private FeedViewModel feedViewModel;
    private FollowViewModel followViewModel;
    private MediaViewModel mediaViewModel;
    private UserViewModel userViewModel;
    private PostViewModel postViewModel;

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
    private ImageView userVerificationBadgeIcon;
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

    public static final int MAX_POST_LENGTH = 280;

    @FXML
    private Circle composerAvatarCircle;
    @FXML
    private TextArea composerTextArea;
    @FXML
    private Button composerMediaButton;
    @FXML
    private Label composerCharCountLabel;
    @FXML
    private Button composerPostButton;
    @FXML
    private Label feedErrorLabel;
    @FXML
    private HBox composerMediaPreviewContainer;

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

    private static record PendingMedia(String url, String fileName) {}
    private final List<PendingMedia> pendingMediaList = new ArrayList<>();

    private boolean bindingsInitialized = false;
    private UserDto currentUser;

    @FXML
    private void initialize() {
        // Initial setup for static elements
        updateIcons();
    }

    public void setViewModels(
            AuthViewModel authViewModel,
            FeedViewModel feedViewModel,
            FollowViewModel followViewModel,
            MediaViewModel mediaViewModel,
            UserViewModel userViewModel,
            PostViewModel postViewModel
    ) {
        this.authViewModel = authViewModel;
        this.feedViewModel = feedViewModel;
        this.followViewModel = followViewModel;
        this.mediaViewModel = mediaViewModel;
        this.userViewModel = userViewModel;
        this.postViewModel = postViewModel;

        this.currentUser = authViewModel.currentUserProperty().get();
        if (currentUser != null) {
            userDisplayName.setText(currentUser.name());
            userHandleName.setText("@" + currentUser.username());
            MediaUiUtils.loadAvatar(userAvatarCircle, currentUser.pfpUrl(), themeMode);
            MediaUiUtils.loadAvatar(composerAvatarCircle, currentUser.pfpUrl(), themeMode);
            if (followViewModel != null) {
                new Thread(() -> followViewModel.loadFollowing(currentUser.id())).start();
            }
            userViewModel.loadProfileById(currentUser.id());
            updateIcons();
        }

        if (currentUserListener != null) {
            authViewModel.currentUserProperty().removeListener(currentUserListener);
        }
        currentUserListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.currentUser = newVal;
                if (userDisplayName != null) userDisplayName.setText(newVal.name());
                if (userHandleName != null) userHandleName.setText("@" + newVal.username());
                if (userAvatarCircle != null) MediaUiUtils.loadAvatar(userAvatarCircle, newVal.pfpUrl(), themeMode);
                if (composerAvatarCircle != null) MediaUiUtils.loadAvatar(composerAvatarCircle, newVal.pfpUrl(), themeMode);
                if (userViewModel != null) userViewModel.loadProfileById(newVal.id());
                updateIcons();
            }
        };
        authViewModel.currentUserProperty().addListener(currentUserListener);

        initializeStateBindings();
    }

    private ChangeListener<UserDto> currentUserListener;
    private ChangeListener<FeedUiState> feedStateListener;
    private ChangeListener<UserProfileDto> userProfileListener;

    private void initializeStateBindings() {
        if (bindingsInitialized) return;

        // whenever feeduistate changes reload the ui
        feedStateListener = (obs, oldState, newState) -> renderState(newState);
        feedViewModel.uiStateProperty().addListener(feedStateListener);

        userProfileListener = (obs, oldVal, newVal) -> {
            if (feedViewModel != null) {
                renderState(feedViewModel.getState());
            }
        };
        userViewModel.currentProfileProperty().addListener(userProfileListener);
        // Composer Input binding: Local state update only (no UI re-render on keystroke)
        composerTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > MAX_POST_LENGTH) {
                composerTextArea.setText(newVal.substring(0, MAX_POST_LENGTH));
                return;
            }
            updateCharCounter(newVal != null ? newVal.length() : 0);
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

    @Override
    public void cleanup() {
        if (authViewModel != null && currentUserListener != null) {
            authViewModel.currentUserProperty().removeListener(currentUserListener);
        }
        if (feedViewModel != null && feedStateListener != null) {
            feedViewModel.uiStateProperty().removeListener(feedStateListener);
        }
        if (userViewModel != null && userProfileListener != null) {
            userViewModel.currentProfileProperty().removeListener(userProfileListener);
        }
        if (feedTimelineContainer != null) {
            feedTimelineContainer.getChildren().clear();
        }
        if (searchResultsContainer != null) {
            searchResultsContainer.getChildren().clear();
        }
        bindingsInitialized = false;
    }

    // used for posting character limit
    private void updateCharCounter(int currentLength) {
        if (composerCharCountLabel != null) {
            int remaining = MAX_POST_LENGTH - currentLength;
            composerCharCountLabel.setText(String.valueOf(remaining));
            if (remaining <= 20) {
                composerCharCountLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #E02424; -fx-padding: 0 8 0 0;");
            } else if (remaining <= 50) {
                composerCharCountLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #F59E0B; -fx-padding: 0 8 0 0;");
            } else {
                composerCharCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -fx-text-secondary; -fx-padding: 0 8 0 0;");
            }
        }
    }

    // used to determine post button state
    private void updateComposerPostButtonState() {
        if (composerPostButton == null || composerTextArea == null) return;
        String text = composerTextArea.getText();
        int len = text == null ? 0 : text.strip().length();
        boolean isTextEmpty = len == 0;
        boolean isOverLimit = text != null && text.length() > MAX_POST_LENGTH;
        boolean isMediaEmpty = pendingMediaList.isEmpty();
        boolean isPosting = feedViewModel != null && feedViewModel.getState().isPosting();
        composerPostButton.setDisable(isPosting || isOverLimit || (isTextEmpty && isMediaEmpty));
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
        } else if (!pendingMediaList.isEmpty()) {
            int count = pendingMediaList.size();
            feedErrorLabel.setText("✓ " + count + (count == 1 ? " image attached" : " images attached"));
            feedErrorLabel.setStyle("-fx-text-fill: #1D9BF0; -fx-font-weight: bold;");
            feedErrorLabel.setVisible(true);
            feedErrorLabel.setManaged(true);
        } else {
            feedErrorLabel.setVisible(false);
            feedErrorLabel.setManaged(false);
        }

        renderComposerMediaPreviews();


        // Render main feed timeline elements in center column
        feedTimelineContainer.getChildren().clear();

        if (state.isLoading()) {
            renderLoadingSkeletons();
        } else if (state.posts().isEmpty()) {
            renderEmptyState();
        } else {
            UUID currentUserPinnedId = (currentUser != null && userViewModel != null && userViewModel.currentProfileProperty().get() != null)
                    ? userViewModel.currentProfileProperty().get().pinnedPostId()
                    : null;
            for (PostDetailDto post : state.posts()) {
                boolean isPinned = (state.activeTab() == FeedUiState.TabType.FOLLOWING)
                        && currentUserPinnedId != null
                        && currentUserPinnedId.equals(post.id());
                feedTimelineContainer.getChildren().add(createPostCard(post, isPinned));
            }
        }

        // Render search results in right sidebar container
        if (searchResultsContainer != null) {
            searchResultsContainer.getChildren().clear();
            if (state.isSearching() && state.searchQuery() != null && !state.searchQuery().isBlank()) {
                String query = state.searchQuery().trim();
                boolean isHashtag = query.startsWith("#");

                Label searchTitle = new Label("Search results for \"" + query + "\"");
                searchTitle.getStyleClass().add("empty-title");
                searchTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 4px 0 8px 0;");
                searchResultsContainer.getChildren().add(searchTitle);

                if (!isHashtag) {
                    // Pane 1: Users pane
                    VBox usersPane = new VBox(6.0);
                    Label usersHeader = new Label("Users");
                    usersHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -fx-text-primary; -fx-padding: 4px 0 2px 0;");
                    usersPane.getChildren().add(usersHeader);

                    String usernameToSearch = query.startsWith("@") ? query.substring(1).trim() : query;

                    new Thread(() -> {
                        UserProfileDto userProfile = null;
                        try {
                            if (!usernameToSearch.isBlank() && userViewModel != null) {
                                userProfile = userViewModel.fetchProfileByUsername(usernameToSearch);
                            }
                        } catch (Exception ignored) {}

                        final UserProfileDto finalUser = userProfile;
                        Platform.runLater(() -> {
                            if (finalUser != null) {
                                usersPane.getChildren().add(createUserSearchResultCard(finalUser));
                            } else {
                                Label noUsersLabel = new Label("No matching users found.");
                                noUsersLabel.getStyleClass().add("empty-desc");
                                usersPane.getChildren().add(noUsersLabel);
                            }
                        });
                    }).start();

                    searchResultsContainer.getChildren().add(usersPane);
                }

                // Pane 2: Posts pane
                VBox postsPane = new VBox(6.0);
                Label postsHeader = new Label("Posts");
                postsHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -fx-text-primary; -fx-padding: 8px 0 2px 0;");
                postsPane.getChildren().add(postsHeader);

                if (state.searchResults().isEmpty()) {
                    Label noResultsLabel = new Label("No matching posts found.");
                    noResultsLabel.getStyleClass().add("empty-desc");
                    searchResultsContainer.getChildren().add(noResultsLabel);
                } else {
                    for (PostDetailDto post : state.searchResults()) {
                        searchResultsContainer.getChildren().add(createPostCard(post));
                    }
                }

                searchResultsContainer.getChildren().add(postsPane);
            }
        }
    }

    private Node createUserSearchResultCard(UserProfileDto user) {
        HBox row = new HBox(12.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("post-card");
        row.setStyle("-fx-padding: 10px 12px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-border-color: transparent transparent -fx-border-color-muted transparent; -fx-border-width: 1px;");

        Circle avatar = new Circle(18.0);
        MediaUiUtils.loadAvatar(avatar, user.pfpUrl(), themeMode);
        avatar.setStroke(Color.web(themeMode == ThemeMode.LIGHT ? "#71767B" : "#2F3336"));

        VBox info = new VBox(2.0);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox nameRow = new HBox(4.0);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(user.name() != null ? user.name() : user.username());
        nameLabel.getStyleClass().add("post-author-name");
        nameLabel.setStyle("-fx-font-size: 14px;");
        nameRow.getChildren().add(nameLabel);

        if (user.isVerified()) {
            ImageView badge = IconUtils.createIconView("verification_badge", themeMode, 16);
            nameRow.getChildren().add(badge);
        }

        Label handleLabel = new Label("@" + user.username());
        handleLabel.getStyleClass().add("post-author-handle");
        handleLabel.setStyle("-fx-font-size: 13px;");

        info.getChildren().addAll(nameRow, handleLabel);

        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("button-secondary");
        viewBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4px 12px;");
        viewBtn.setOnAction(e -> {
            e.consume();
            if (navigator != null) {
                navigator.showProfileScreen(user.id());
            }
        });

        row.getChildren().addAll(avatar, info, viewBtn);
        row.setOnMouseClicked(e -> {
            if (navigator != null) {
                navigator.showProfileScreen(user.id());
            }
        });
        return row;
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
     * Reusable, modular UI component mapping directly to domain PostDetailDto models.
     * Integrates hover states, user actions, and deletes posts directly using events.
     */
    private Node createPostCard(PostDetailDto post) {
        return createPostCard(post, false);
    }

    private Node createPostCard(PostDetailDto post, boolean isPinned) {
        VBox card = new VBox(10.0);
        card.getStyleClass().add("post-card");
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1 && navigator != null) {
                navigator.showPostDetailsScreen(post.id());
            }
        });

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

        HBox header = new HBox(12.0);

        // Avatar
        Circle avatar = new Circle(20.0);
        MediaUiUtils.loadAvatar(avatar, post.authorPfpUrl(), themeMode);
        avatar.setStroke(Color.web(themeMode == ThemeMode.LIGHT ? "#71767B" : "#2F3336"));
        avatar.setCursor(Cursor.HAND);

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
        nameLabel.setCursor(Cursor.HAND);
        nameLabel.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null) {
                navigator.showProfileScreen(post.authorId());
            }
        });

        Label handleLabel = new Label("@" + post.authorUsername());
        handleLabel.getStyleClass().add("post-author-handle");
        handleLabel.setCursor(Cursor.HAND);
        handleLabel.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null) {
                navigator.showProfileScreen(post.authorId());
            }
        });

        Label dot = new Label("·");
        dot.getStyleClass().add("post-author-handle");

        String timeText = post.createdAt() != null
                ? post.createdAt().format(DateTimeFormatter.ofPattern("MMM dd"))
                : "Just now";
        Label timeLabel = new Label(timeText);
        timeLabel.getStyleClass().add("post-timestamp");

        if (post.authorVerified()) {
            ImageView badge = IconUtils.createIconView("verification_badge", themeMode, 16);
            metaRow.getChildren().addAll(nameLabel, badge, handleLabel, dot, timeLabel);
        } else {
            metaRow.getChildren().addAll(nameLabel, handleLabel, dot, timeLabel);
        }
        authorDetails.getChildren().add(metaRow);

        // Spacer to push delete button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, authorDetails, spacer);

        // Follow button for other users' posts
        if (currentUser != null && !currentUser.id().equals(post.authorId()) && followViewModel != null) {
            boolean isFollowing = followViewModel.isFollowingUser(currentUser.id(), post.authorId());
            Button followBtn = new Button(isFollowing ? "Following" : "Follow");
            followBtn.getStyleClass().add("post-action-btn");
            followBtn.setStyle("-fx-border-color: -fx-border-color-muted; -fx-border-radius: 12px; -fx-padding: 2px 8px; -fx-font-size: 12px;");

            followBtn.setOnAction(e -> {
                e.consume();
                if (followViewModel.isFollowingUser(currentUser.id(), post.authorId())) {
                    followViewModel.unfollowUser(currentUser.id(), post.authorId());
                    followBtn.setText("Follow");
                } else {
                    followViewModel.followUser(currentUser.id(), post.authorId());
                    followBtn.setText("Following");
                }
            });
            header.getChildren().add(followBtn);
        }

        // Pin / Unpin button (only for own posts)
        if (currentUser != null && currentUser.id().equals(post.authorId()) && userViewModel != null) {
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
                userViewModel.loadProfileById(currentUser.id());
                feedViewModel.processEvent(new FeedUiEvent.LoadFeed(currentUser.id()));
            });
            header.getChildren().add(pinBtn);
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
        if (post.isRepostedByMe()) {
            repostBtn.setStyle("-fx-text-fill: -fx-text-primary; -fx-font-weight: bold;");
        }
        repostBtn.setOnAction(e -> {
            e.consume();
            if (currentUser != null && feedViewModel != null) {
                feedViewModel.processEvent(new FeedUiEvent.ToggleRepost(post.id(), currentUser.id()));
            }
        });

        String likeIconName = post.isLikedByMe() ? "heart_full" : "heart";
        Button likeBtn = new Button(" " + post.likeCount());
        IconUtils.setButtonIcon(likeBtn, likeIconName, themeMode, 16);
        likeBtn.getStyleClass().add("post-action-btn");
        if (post.isLikedByMe()) {
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
            themeToggleBtn.setText(themeMode == ThemeMode.LIGHT ? "Dark Mode" : "Light Mode");
        }
        if (userAvatarCircle != null && currentUser != null) {
            MediaUiUtils.loadAvatar(userAvatarCircle, currentUser.pfpUrl(), themeMode);
        }
        if (composerAvatarCircle != null && currentUser != null) {
            MediaUiUtils.loadAvatar(composerAvatarCircle, currentUser.pfpUrl(), themeMode);
        }
        if (userVerificationBadgeIcon != null) {
            if (currentUser != null && currentUser.isVerified()) {
                userVerificationBadgeIcon.setImage(IconUtils.getIconImage("verification_badge", themeMode));
                userVerificationBadgeIcon.setVisible(true);
                userVerificationBadgeIcon.setManaged(true);
            } else {
                userVerificationBadgeIcon.setVisible(false);
                userVerificationBadgeIcon.setManaged(false);
            }
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

    private void renderComposerMediaPreviews() {
        if (composerMediaPreviewContainer == null) return;
        composerMediaPreviewContainer.getChildren().clear();

        if (pendingMediaList.isEmpty()) {
            composerMediaPreviewContainer.setVisible(false);
            composerMediaPreviewContainer.setManaged(false);
            return;
        }

        composerMediaPreviewContainer.setVisible(true);
        composerMediaPreviewContainer.setManaged(true);

        for (PendingMedia media : new ArrayList<>(pendingMediaList)) {
            StackPane previewBox = new StackPane();
            previewBox.setPrefSize(80, 80);
            previewBox.setMaxSize(80, 80);
            previewBox.setStyle("-fx-background-color: #202327; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: -fx-border-color-muted; -fx-border-width: 1px;");

            ImageView imgView = new ImageView();
            imgView.setFitWidth(80);
            imgView.setFitHeight(80);
            imgView.setPreserveRatio(false);
            imgView.setSmooth(true);

            String fullUrl = MediaUiUtils.resolveFullUrl(media.url());
            if (fullUrl != null) {
                imgView.setImage(new Image(fullUrl, true));
            }

            Rectangle clip = new Rectangle(80, 80);
            clip.setArcWidth(16);
            clip.setArcHeight(16);
            imgView.setClip(clip);

            Button removeBtn = new Button("✕");
            removeBtn.setStyle("-fx-background-color: rgba(15, 20, 25, 0.75); -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-padding: 2px 6px; -fx-cursor: hand;");
            StackPane.setAlignment(removeBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(removeBtn, new Insets(4, 4, 0, 0));

            removeBtn.setOnAction(e -> {
                pendingMediaList.remove(media);
                renderState(feedViewModel.getState());
            });

            previewBox.getChildren().addAll(imgView, removeBtn);
            composerMediaPreviewContainer.getChildren().add(previewBox);
        }
    }

    @FXML
    private void onComposerPostClicked() {
        if (currentUser != null) {
            String text = composerTextArea != null ? composerTextArea.getText() : "";
            String joinedMediaUrls = pendingMediaList.isEmpty()
                    ? null
                    : pendingMediaList.stream().map(PendingMedia::url).collect(Collectors.joining(","));
            feedViewModel.processEvent(new FeedUiEvent.UpdateComposerText(text != null ? text : ""));
            feedViewModel.processEvent(new FeedUiEvent.SubmitPost(currentUser.id(), joinedMediaUrls));
            if (composerTextArea != null) {
                composerTextArea.setText("");
            }
            pendingMediaList.clear();
            renderState(feedViewModel.getState());
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
        if ((composerTextArea != null && composerTextArea.getText() != null && !composerTextArea.getText().strip().isEmpty()) || !pendingMediaList.isEmpty()) {
            onComposerPostClicked();
        } else if (composerTextArea != null) {
            composerTextArea.requestFocus();
        }
    }

    @FXML
    private void onMediaAttachmentClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File(s)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Window window = composerTextArea != null && composerTextArea.getScene() != null
                ? composerTextArea.getScene().getWindow()
                : null;
        List<java.io.File> selectedFiles = fileChooser.showOpenMultipleDialog(window);

        if (selectedFiles != null && !selectedFiles.isEmpty() && currentUser != null && mediaViewModel != null) {
            long maxSizeBytes = 15L * 1024 * 1024; // 15MB
            new Thread(() -> {
                for (java.io.File selectedFile : selectedFiles) {
                    if (selectedFile.length() > maxSizeBytes) {
                        Platform.runLater(() -> {
                            if (feedErrorLabel != null) {
                                feedErrorLabel.setText("Failed to attach " + selectedFile.getName() + ": File size exceeds maximum limit of 15MB");
                                feedErrorLabel.setStyle("-fx-text-fill: #F4212E; -fx-font-weight: bold;");
                                feedErrorLabel.setVisible(true);
                                feedErrorLabel.setManaged(true);
                            }
                        });
                        continue;
                    }

                    try {
                        MediaDto uploadedMedia = mediaViewModel.uploadMedia(selectedFile, currentUser.id());
                        if (uploadedMedia != null) {
                            Platform.runLater(() -> {
                                pendingMediaList.add(new PendingMedia(uploadedMedia.url(), selectedFile.getName()));
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
                }
            }).start();
        }
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
