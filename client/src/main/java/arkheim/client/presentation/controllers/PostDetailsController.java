package arkheim.client.presentation.controllers;

import arkheim.client.domain.ports.dtos.PostDto;
import arkheim.client.domain.ports.dtos.UserDto;
import arkheim.client.presentation.theme.ThemeMode;
import arkheim.client.presentation.navigation.JavaFxNavigator;
import arkheim.client.presentation.utils.IconUtils;
import arkheim.client.presentation.utils.MediaUiUtils;
import arkheim.client.presentation.viewmodels.AuthViewModel;
import arkheim.client.presentation.viewmodels.PostViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
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

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class PostDetailsController extends BaseController {

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
    private Button backButton;
    @FXML
    private VBox parentPostContainer;

    @FXML
    private Circle focalAvatarCircle;
    @FXML
    private Label focalAuthorName;
    @FXML
    private Label focalAuthorHandle;
    @FXML
    private Button deleteFocalBtn;
    @FXML
    private Label focalContentText;
    @FXML
    private StackPane focalMediaContainer;
    @FXML
    private Label focalTimestampLabel;

    @FXML
    private Label metricsLikesCount;
    @FXML
    private Label metricsRepostsCount;
    @FXML
    private Label metricsRepliesCount;

    @FXML
    private Button focalReplyBtn;
    @FXML
    private Button focalRepostBtn;
    @FXML
    private Button focalLikeBtn;

    @FXML
    private Circle replyComposerAvatar;
    @FXML
    private TextArea replyTextArea;
    @FXML
    private Label replyCharCountLabel;
    @FXML
    private Button replyPostBtn;

    @FXML
    private Label detailsErrorLabel;
    @FXML
    private VBox repliesListContainer;
    @FXML
    private ScrollPane detailsScrollPane;
    @FXML
    private TextField searchField;
    @FXML
    private VBox searchResultsContainer;

    private AuthViewModel authViewModel;
    private PostViewModel postViewModel;

    private UserDto currentUser;
    private UUID postId;

    @FXML
    private void initialize() {
        updateIcons();
    }

    public void setViewModels(AuthViewModel authViewModel, PostViewModel postViewModel, UUID postId) {
        this.authViewModel = authViewModel;
        this.postViewModel = postViewModel;
        this.postId = postId;

        this.currentUser = authViewModel.currentUserProperty().get();
        if (currentUser != null) {
            userDisplayName.setText(currentUser.name());
            userHandleName.setText("@" + currentUser.username());
            MediaUiUtils.loadAvatar(userAvatarCircle, currentUser.pfpUrl(), themeMode);
            MediaUiUtils.loadAvatar(replyComposerAvatar, currentUser.pfpUrl(), themeMode);
        }

        authViewModel.currentUserProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.currentUser = newVal;
                if (userDisplayName != null) userDisplayName.setText(newVal.name());
                if (userHandleName != null) userHandleName.setText("@" + newVal.username());
                if (userAvatarCircle != null) MediaUiUtils.loadAvatar(userAvatarCircle, newVal.pfpUrl(), themeMode);
                if (replyComposerAvatar != null) MediaUiUtils.loadAvatar(replyComposerAvatar, newVal.pfpUrl(), themeMode);
            }
        });

        initializeStateBindings();
        loadData();
    }

    private void initializeStateBindings() {
        // Sync composer text
        replyTextArea.textProperty().bindBidirectional(postViewModel.newPostContentProperty());

        replyTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 280) {
                replyTextArea.setText(newVal.substring(0, 280));
                return;
            }
            updateReplyCharCounter(newVal != null ? newVal.length() : 0);
        });

        // Enable button only when valid text (1-280 chars) is typed
        replyPostBtn.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> {
                            String text = postViewModel.newPostContentProperty().get();
                            return text == null || text.isBlank() || text.length() > 280;
                        },
                        postViewModel.newPostContentProperty()
                )
        );

        // Bind focal post details changes
        postViewModel.currentPostProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                renderFocalPost(newVal);
            }
        });

        // Bind replies changes
        postViewModel.repliesProperty().addListener((ListChangeListener<PostDto>) change -> {
            renderReplies();
        });

        // Bind error messages
        detailsErrorLabel.textProperty().bind(postViewModel.errorMessageProperty());
        detailsErrorLabel.visibleProperty().bind(postViewModel.errorMessageProperty().isNotEmpty());
        detailsErrorLabel.managedProperty().bind(postViewModel.errorMessageProperty().isNotEmpty());
    }

    private void updateReplyCharCounter(int currentLength) {
        if (replyCharCountLabel != null) {
            int remaining = 280 - currentLength;
            replyCharCountLabel.setText(String.valueOf(remaining));
            if (remaining <= 20) {
                replyCharCountLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #E02424; -fx-padding: 0 8 0 0;");
            } else if (remaining <= 50) {
                replyCharCountLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #F59E0B; -fx-padding: 0 8 0 0;");
            } else {
                replyCharCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -fx-text-secondary; -fx-padding: 0 8 0 0;");
            }
        }
    }

    private void loadData() {
        postViewModel.loadPostDetails(postId, currentUser != null ? currentUser.id() : null);
        postViewModel.loadPostReplies(postId, currentUser != null ? currentUser.id() : null);
    }

    private void renderFocalPost(PostDto post) {
        focalAuthorName.setText(post.authorName());
        focalAuthorHandle.setText("@" + post.authorUsername());
        focalContentText.setText(post.content());
        MediaUiUtils.loadAvatar(focalAvatarCircle, post.authorPfpUrl(), themeMode);

        if (post.authorId() != null) {
            focalAuthorName.setCursor(javafx.scene.Cursor.HAND);
            focalAuthorName.setOnMouseClicked(e -> {
                e.consume();
                if (navigator != null) navigator.showProfileScreen(post.authorId());
            });
            focalAuthorHandle.setCursor(javafx.scene.Cursor.HAND);
            focalAuthorHandle.setOnMouseClicked(e -> {
                e.consume();
                if (navigator != null) navigator.showProfileScreen(post.authorId());
            });
            focalAvatarCircle.setCursor(javafx.scene.Cursor.HAND);
            focalAvatarCircle.setOnMouseClicked(e -> {
                e.consume();
                if (navigator != null) navigator.showProfileScreen(post.authorId());
            });
        }

        String dateText = post.createdAt() != null
                ? post.createdAt().format(DateTimeFormatter.ofPattern("h:mm a · MMM dd, yyyy"))
                : "Just now";
        focalTimestampLabel.setText(dateText);

        metricsLikesCount.setText(String.valueOf(post.likeCount()));
        metricsRepostsCount.setText(String.valueOf(post.repostCount()));
        metricsRepliesCount.setText(String.valueOf(post.replyCount()));

        IconUtils.setButtonIcon(focalLikeBtn, post.likedByMe() ? "heart_full" : "heart", themeMode, 20);
        focalLikeBtn.setText("");
        if (focalReplyBtn != null) IconUtils.setButtonIcon(focalReplyBtn, "comment", themeMode, 20);
        if (focalRepostBtn != null) IconUtils.setButtonIcon(focalRepostBtn, "repost", themeMode, 20);
        if (deleteFocalBtn != null) IconUtils.setButtonIcon(deleteFocalBtn, "trash", themeMode, 16);

        if (post.likedByMe()) {
            focalLikeBtn.setStyle("-fx-text-fill: -fx-text-primary; -fx-font-weight: bold;");
        } else {
            focalLikeBtn.setStyle("");
        }

        // Delete button for focal post
        if (currentUser != null && currentUser.id().equals(post.authorId())) {
            deleteFocalBtn.setVisible(true);
            deleteFocalBtn.setManaged(true);
        } else {
            deleteFocalBtn.setVisible(false);
            deleteFocalBtn.setManaged(false);
        }

        // Render focal post media attachment
        if (focalMediaContainer != null) {
            focalMediaContainer.getChildren().clear();
            if (post.mediaUrls() != null && !post.mediaUrls().isEmpty()) {
                for (String url : post.mediaUrls()) {
                    if (url != null && !url.isBlank()) {
                        focalMediaContainer.getChildren().add(MediaUiUtils.createMediaPreviewNode(url, themeMode));
                    }
                }
                focalMediaContainer.setVisible(true);
                focalMediaContainer.setManaged(true);
            } else {
                focalMediaContainer.setVisible(false);
                focalMediaContainer.setManaged(false);
            }
        }

        // Render parent post chain if present
        parentPostContainer.getChildren().clear();
        if (post.parentPostId() != null) {
            UUID parentId = post.parentPostId();
            UUID requesterId = currentUser != null ? currentUser.id() : null;
            new Thread(() -> {
                java.util.List<PostDto> chain = postViewModel.fetchParentChain(parentId, requesterId);
                Platform.runLater(() -> {
                    if (chain != null && !chain.isEmpty()) {
                        renderParentChain(chain);
                    } else {
                        renderParentFallback(parentId);
                    }
                });
            }).start();
        }
    }

    private void renderParentChain(java.util.List<PostDto> chain) {
        parentPostContainer.getChildren().clear();
        for (int i = 0; i < chain.size(); i++) {
            PostDto parentPost = chain.get(i);
            boolean isLast = (i == chain.size() - 1);
            parentPostContainer.getChildren().add(createParentChainCard(parentPost, isLast));
        }
    }

    private Node createParentChainCard(PostDto parentPost, boolean isLast) {
        HBox cardRow = new HBox(12.0);
        cardRow.getStyleClass().add("post-card");
        cardRow.setStyle("-fx-padding: 8px 16px 0px 16px; -fx-cursor: hand;");
        cardRow.setOnMouseClicked(e -> {
            if (navigator != null) {
                navigator.showPostDetailsScreen(parentPost.id());
            }
        });

        // Left Column: Avatar + Continuous Thread Connector Line
        VBox leftCol = new VBox(2.0);
        leftCol.setAlignment(Pos.TOP_CENTER);
        leftCol.setMinWidth(36.0);
        leftCol.setMaxWidth(36.0);

        Circle avatar = new Circle(18.0);
        MediaUiUtils.loadAvatar(avatar, parentPost.authorPfpUrl(), themeMode);
        avatar.setStroke(Color.web(themeMode == ThemeMode.LIGHT ? "#71767B" : "#2F3336"));
        avatar.setCursor(Cursor.HAND);
        avatar.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null && parentPost.authorId() != null) {
                navigator.showProfileScreen(parentPost.authorId());
            }
        });

        Region line = new Region();
        line.setStyle("-fx-background-color: -fx-border-color-muted; -fx-min-width: 2px; -fx-max-width: 2px;");
        VBox.setVgrow(line, Priority.ALWAYS);

        leftCol.getChildren().addAll(avatar, line);

        // Right Column: Author Info & Content Body
        VBox rightCol = new VBox(4.0);
        HBox.setHgrow(rightCol, Priority.ALWAYS);
        rightCol.setStyle("-fx-padding: 0 0 10px 0;");

        HBox metaRow = new HBox(6.0);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(parentPost.authorName());
        name.getStyleClass().add("post-author-name");
        name.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");
        name.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null && parentPost.authorId() != null) {
                navigator.showProfileScreen(parentPost.authorId());
            }
        });

        Label handle = new Label("@" + parentPost.authorUsername());
        handle.getStyleClass().add("post-author-handle");
        handle.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");
        handle.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null && parentPost.authorId() != null) {
                navigator.showProfileScreen(parentPost.authorId());
            }
        });

        Label dot = new Label("·");
        dot.getStyleClass().add("post-author-handle");

        String time = parentPost.createdAt() != null
                ? parentPost.createdAt().format(DateTimeFormatter.ofPattern("MMM dd"))
                : "Just now";
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("post-timestamp");

        metaRow.getChildren().addAll(name, handle, dot, timeLabel);

        Node bodyNode = createFormattedPostBody(parentPost.content(), themeMode, hashtag -> {
            if (navigator != null) {
                navigator.showHomeScreen();
            }
        });

        rightCol.getChildren().addAll(metaRow, bodyNode);

        if (parentPost.mediaUrls() != null && !parentPost.mediaUrls().isEmpty()) {
            VBox mediaBox = new VBox(8.0);
            for (String url : parentPost.mediaUrls()) {
                if (url != null && !url.isBlank()) {
                    mediaBox.getChildren().add(MediaUiUtils.createMediaPreviewNode(url, themeMode));
                }
            }
            rightCol.getChildren().add(mediaBox);
        }

        cardRow.getChildren().addAll(leftCol, rightCol);
        return cardRow;
    }

    private void renderParentFallback(UUID parentId) {
        parentPostContainer.getChildren().clear();
        HBox parentPreview = new HBox(12.0);
        parentPreview.setStyle("-fx-padding: 12px 16px 4px 16px; -fx-cursor: hand;");

        VBox leftConnector = new VBox(2.0);
        leftConnector.setAlignment(Pos.TOP_CENTER);
        Circle avatar = new Circle(14.0, Color.LIGHTGRAY);
        Region line = new Region();
        line.setStyle("-fx-background-color: -fx-border-color-muted; -fx-min-width: 2px; -fx-max-width: 2px; -fx-pref-height: 25px;");
        leftConnector.getChildren().addAll(avatar, line);

        VBox rightDetails = new VBox(4.0);
        Label parentTitle = new Label("View parent post in thread...");
        parentTitle.setStyle("-fx-font-size: 13px; -fx-font-style: italic;");
        parentTitle.getStyleClass().add("post-author-handle");
        rightDetails.getChildren().add(parentTitle);

        parentPreview.getChildren().addAll(leftConnector, rightDetails);
        parentPreview.setOnMouseClicked(e -> {
            if (navigator != null) {
                navigator.showPostDetailsScreen(parentId);
            }
        });
        parentPostContainer.getChildren().add(parentPreview);
    }

    private void renderReplies() {
        repliesListContainer.getChildren().clear();

        if (postViewModel.repliesProperty().isEmpty()) {
            VBox emptyBox = new VBox(12.0);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 30px;");
            Label title = new Label("Be the first to reply");
            title.getStyleClass().add("empty-title");
            title.setStyle("-fx-font-size: 15px;");
            Label desc = new Label("Share your thoughts on this conversation.");
            desc.getStyleClass().add("empty-desc");
            emptyBox.getChildren().addAll(title, desc);
            repliesListContainer.getChildren().add(emptyBox);
        } else {
            for (PostDto reply : postViewModel.repliesProperty()) {
                repliesListContainer.getChildren().add(createReplyCard(reply));
            }
        }
    }

    private Node createReplyCard(PostDto reply) {
        VBox card = new VBox(10.0);
        card.getStyleClass().add("post-card");
        card.setStyle("-fx-padding: 12px 16px 12px 36px; -fx-border-color: transparent transparent -fx-border-color-muted transparent; -fx-border-width: 1px;");

        // Hook up reply click to load details
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1 && navigator != null) {
                navigator.showPostDetailsScreen(reply.id());
            }
        });

        HBox header = new HBox(12.0);
        Circle avatar = new Circle(16.0);
        MediaUiUtils.loadAvatar(avatar, reply.authorPfpUrl(), themeMode);
        avatar.setStroke(Color.web(themeMode == ThemeMode.LIGHT ? "#71767B" : "#2F3336"));
        avatar.setCursor(javafx.scene.Cursor.HAND);
        avatar.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null && reply.authorId() != null) navigator.showProfileScreen(reply.authorId());
        });

        VBox meta = new VBox(2.0);
        HBox metaRow = new HBox(6.0);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(reply.authorName());
        name.getStyleClass().add("post-author-name");
        name.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");
        name.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null && reply.authorId() != null) navigator.showProfileScreen(reply.authorId());
        });

        Label handle = new Label("@" + reply.authorUsername());
        handle.getStyleClass().add("post-author-handle");
        handle.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");
        handle.setOnMouseClicked(e -> {
            e.consume();
            if (navigator != null && reply.authorId() != null) navigator.showProfileScreen(reply.authorId());
        });

        Label dot = new Label("·");
        dot.getStyleClass().add("post-author-handle");

        String time = reply.createdAt() != null ? reply.createdAt().format(DateTimeFormatter.ofPattern("MMM dd")) : "Just now";
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("post-timestamp");

        metaRow.getChildren().addAll(name, handle, dot, timeLabel);
        meta.getChildren().add(metaRow);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, meta, spacer);

        // Delete button for replies
        if (currentUser != null && currentUser.id().equals(reply.authorId())) {
            Button deleteBtn = new Button();
            IconUtils.setButtonIcon(deleteBtn, "trash", themeMode, 16);
            deleteBtn.getStyleClass().add("post-action-btn");
            deleteBtn.setStyle("-fx-text-fill: #E02424; -fx-padding: 4px;");
            deleteBtn.setOnAction(e -> {
                e.consume(); // prevent navigation trigger
                postViewModel.deletePost(reply.id(), currentUser.id());
                // re-fetch replies
                postViewModel.loadPostReplies(postId, currentUser.id());
            });
            header.getChildren().add(deleteBtn);
        }

        Node bodyNode = createFormattedPostBody(reply.content(), themeMode, tag -> {
            searchField.setText(tag);
            onSearchSubmitted();
        });

        card.getChildren().addAll(header, bodyNode);

        PostDto parentPost = postViewModel.currentPostProperty().get();
        if (reply.isRepost() || reply.repostedFromUsername() != null) {
            String origAuthor = reply.repostedFromUsername() != null ? reply.repostedFromUsername() : (reply.repliedUsername() != null ? reply.repliedUsername() : "user");
            HBox repostBadge = new HBox(6.0);
            repostBadge.setAlignment(Pos.CENTER_LEFT);
            ImageView repostIcon = IconUtils.createIconView("repost", themeMode, 14);
            Label repostLabel = new Label("Reposted from @" + origAuthor);
            repostLabel.getStyleClass().add("post-author-handle");
            repostLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            repostBadge.getChildren().addAll(repostIcon, repostLabel);
            repostBadge.setStyle("-fx-padding: 0 0 4px 0;");
            card.getChildren().add(0, repostBadge);
        } else {
            String targetUser = reply.repliedUsername() != null ? reply.repliedUsername() : (parentPost != null ? parentPost.authorUsername() : "user");
            Label replyingLabel = new Label("Replying to @" + targetUser);
            replyingLabel.getStyleClass().add("post-author-handle");
            replyingLabel.setStyle("-fx-font-size: 12px; -fx-padding: 0 0 2px 0;");
        }

        if (reply.mediaUrls() != null && !reply.mediaUrls().isEmpty()) {
            for (String mediaUrl : reply.mediaUrls()) {
                if (mediaUrl != null && !mediaUrl.isBlank()) {
                    card.getChildren().add(MediaUiUtils.createMediaPreviewNode(mediaUrl, themeMode));
                }
            }
        }

        HBox actions = new HBox(40.0);
        actions.getStyleClass().add("post-actions");

        Button replyBtn = new Button(" " + reply.replyCount());
        IconUtils.setButtonIcon(replyBtn, "comment", themeMode, 14);
        replyBtn.getStyleClass().add("post-action-btn");
        replyBtn.setStyle("-fx-font-size: 12px;");
        replyBtn.setOnAction(e -> {
            e.consume();
            if (navigator != null) {
                navigator.showPostDetailsScreen(reply.id());
            }
        });

        Button repostBtn = new Button(" " + reply.repostCount());
        IconUtils.setButtonIcon(repostBtn, "repost", themeMode, 14);
        repostBtn.getStyleClass().add("post-action-btn");
        repostBtn.setStyle("-fx-font-size: 12px;");
        if (reply.repostedByMe()) {
            repostBtn.setStyle("-fx-text-fill: -fx-text-primary; -fx-font-weight: bold; -fx-font-size: 12px;");
        }
        repostBtn.setOnAction(e -> {
            e.consume();
            if (currentUser != null) {
                postViewModel.repost(reply.id(), currentUser.id());
            }
        });

        String likeIconName = reply.likedByMe() ? "heart_full" : "heart";
        Button likeBtn = new Button(" " + reply.likeCount());
        IconUtils.setButtonIcon(likeBtn, likeIconName, themeMode, 14);
        likeBtn.getStyleClass().add("post-action-btn");
        likeBtn.setStyle("-fx-font-size: 12px;");
        if (reply.likedByMe()) {
            likeBtn.setStyle("-fx-text-fill: -fx-text-primary; -fx-font-weight: bold; -fx-font-size: 12px;");
        }
        likeBtn.setOnAction(e -> {
            e.consume(); // prevent navigation trigger
            if (currentUser != null) {
                postViewModel.toggleLike(reply.id(), currentUser.id());
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
    private void onDeleteFocalClicked() {
        if (currentUser != null) {
            postViewModel.deletePost(postId, currentUser.id());
            navigator.showHomeScreen();
        }
    }

    @FXML
    private void onFocalLikeClicked() {
        if (currentUser != null) {
            postViewModel.toggleLike(postId, currentUser.id());
        }
    }

    @FXML
    private void onReplySubmitClicked() {
        if (currentUser != null) {
            postViewModel.newPostParentIdProperty().set(postId);
            postViewModel.createPost(currentUser.id());
            // reload data
            loadData();
        }
    }

    @Override
    public void setThemeMode(ThemeMode themeMode) {
        super.setThemeMode(themeMode);
        if (postViewModel != null && postViewModel.currentPostProperty().get() != null) {
            renderFocalPost(postViewModel.currentPostProperty().get());
            renderReplies();
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
        if (themeToggleBtn != null) {
            themeToggleBtn.setText(themeMode == ThemeMode.LIGHT ? "☾ Dark Mode" : "☼ Light Mode");
        }
        if (userAvatarCircle != null && currentUser != null) {
            MediaUiUtils.loadAvatar(userAvatarCircle, currentUser.pfpUrl(), themeMode);
        }
        if (replyComposerAvatar != null && currentUser != null) {
            MediaUiUtils.loadAvatar(replyComposerAvatar, currentUser.pfpUrl(), themeMode);
        }
        if (focalReplyBtn != null) IconUtils.setButtonIcon(focalReplyBtn, "comment", themeMode, 20);
        if (focalRepostBtn != null) IconUtils.setButtonIcon(focalRepostBtn, "repost", themeMode, 20);
        if (deleteFocalBtn != null) IconUtils.setButtonIcon(deleteFocalBtn, "trash", themeMode, 16);
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
            for (PostDto reply : postViewModel.searchResultsProperty()) {
                searchResultsContainer.getChildren().add(createReplyCard(reply));
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

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().clear();
        String styleFile = (themeMode == ThemeMode.LIGHT) ? "Style.css" : "DarkMode.css";
        try {
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/arkheim/client/presentation/Assets/" + styleFile)).toExternalForm());
        } catch (Exception ignored) {}

        alert.showAndWait();
    }
}
