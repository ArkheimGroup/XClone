package arkheim.client.presentation.utils;

import arkheim.client.presentation.theme.ThemeMode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import arkheim.client.infrastructure.config.ClientConfig;

public class MediaUiUtils {

    public static final String DEFAULT_PFP_URL = "uploads/profile_pictures/default_pfp.png";
    public static final String DEFAULT_BANNER_URL = "uploads/banners/default_banner.png";

    public static String getBaseUrl() {
        return ClientConfig.getBaseUrl();
    }

    /**
     * Resolves a media URL string to an absolute HTTP URL usable by JavaFX Image loader.
     */
    public static String resolveFullUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            mediaUrl = DEFAULT_PFP_URL;
        }
        String trimmed = mediaUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        String baseUrl = getBaseUrl();
        if (trimmed.startsWith("/")) {
            return baseUrl + trimmed;
        }
        return baseUrl + "/" + trimmed;
    }

    /**
     * Loads a banner image into a JavaFX ImageView.
     * If bannerUrl is null or blank, falls back to default_banner.png.
     * Clicking the banner opens it in full resolution modal with download capability.
     */
    public static void loadBanner(ImageView imageView, String bannerUrl, ThemeMode themeMode) {
        if (imageView == null) return;

        String targetUrl = (bannerUrl == null || bannerUrl.isBlank()) ? DEFAULT_BANNER_URL : bannerUrl;
        String fullUrl = resolveFullUrl(targetUrl);

        if (fullUrl != null) {
            Image img = new Image(fullUrl, true);
            Runnable applyImage = () -> {
                if (!img.isError() && img.getWidth() > 0) {
                    imageView.setImage(img);
                } else if (!DEFAULT_BANNER_URL.equals(targetUrl)) {
                    String defaultFullUrl = resolveFullUrl(DEFAULT_BANNER_URL);
                    Image defaultImg = new Image(defaultFullUrl, true);
                    if (defaultImg.getProgress() >= 1.0 && !defaultImg.isError()) {
                        imageView.setImage(defaultImg);
                    } else {
                        defaultImg.progressProperty().addListener((o, ov, nv) -> {
                            if (nv.doubleValue() >= 1.0 && !defaultImg.isError()) {
                                Platform.runLater(() -> imageView.setImage(defaultImg));
                            }
                        });
                    }
                }
            };

            if (img.getProgress() >= 1.0) {
                applyImage.run();
            } else {
                img.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() >= 1.0) {
                        Platform.runLater(applyImage);
                    }
                });
                img.errorProperty().addListener((obs, oldVal, isError) -> {
                    if (isError) {
                        Platform.runLater(applyImage);
                    }
                });
            }

            imageView.setCursor(Cursor.HAND);
            imageView.setOnMouseClicked(e -> {
                e.consume();
                showFullResolutionDialog(fullUrl, themeMode);
            });
        }
    }

    /**
     * Loads an avatar image into a JavaFX Circle using ImagePattern.
     * If pfpUrl is null or blank, falls back to default_pfp.png.
     * Clicking the avatar opens it in full resolution modal with download capability (like post media).
     */
    public static void loadAvatar(Circle circle, String pfpUrl, ThemeMode themeMode) {
        if (circle == null) return;

        String targetUrl = (pfpUrl == null || pfpUrl.isBlank()) ? DEFAULT_PFP_URL : pfpUrl;
        String fullUrl = resolveFullUrl(targetUrl);

        if (fullUrl != null) {
            Image img = new Image(fullUrl, true);

            Runnable applyImage = () -> {
                if (!img.isError() && img.getWidth() > 0) {
                    circle.setFill(new ImagePattern(img));
                } else if (!DEFAULT_PFP_URL.equals(targetUrl)) {
                    String defaultFullUrl = resolveFullUrl(DEFAULT_PFP_URL);
                    Image defaultImg = new Image(defaultFullUrl, true);
                    if (defaultImg.getProgress() >= 1.0 && !defaultImg.isError()) {
                        circle.setFill(new ImagePattern(defaultImg));
                    } else {
                        defaultImg.progressProperty().addListener((o, ov, nv) -> {
                            if (nv.doubleValue() >= 1.0 && !defaultImg.isError()) {
                                Platform.runLater(() -> circle.setFill(new ImagePattern(defaultImg)));
                            } else {
                                Platform.runLater(() -> circle.setFill(Color.web(themeMode == ThemeMode.LIGHT ? "#1D9BF0" : "#1D9BF0")));
                            }
                        });
                        defaultImg.errorProperty().addListener((o, ov, nv) -> {
                            if (nv) {
                                Platform.runLater(() -> circle.setFill(Color.web(themeMode == ThemeMode.LIGHT ? "#1D9BF0" : "#1D9BF0")));
                            }
                        });
                    }
                } else {
                    circle.setFill(Color.web(themeMode == ThemeMode.LIGHT ? "#1D9BF0" : "#1D9BF0"));
                }
            };

            if (img.getProgress() >= 1.0) {
                applyImage.run();
            } else {
                img.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() >= 1.0) {
                        Platform.runLater(applyImage);
                    }
                });
                img.errorProperty().addListener((obs, oldVal, isError) -> {
                    if (isError) {
                        Platform.runLater(applyImage);
                    }
                });
            }

            circle.setCursor(Cursor.HAND);
            circle.setOnMouseClicked(e -> {
                e.consume();
                showFullResolutionDialog(fullUrl, themeMode);
            });
        }
    }



    /**
     * Creates a shrunken preview component for a post card.
     * Scales down media until it reaches a fixed height bound (max 260px), preserving aspect ratio.
     * Clicking the preview opens the media in full resolution modal with a download button.
     */
    public static Node createMediaPreviewNode(String mediaUrl, ThemeMode themeMode) {
        String fullUrl = resolveFullUrl(mediaUrl);
        if (fullUrl == null) {
            return new Region();
        }

        StackPane container = new StackPane();
        container.getStyleClass().add("post-media-container");
        container.setMaxWidth(540);
        container.setMaxHeight(280);
        container.setAlignment(Pos.CENTER);
        container.setCursor(Cursor.HAND);
        container.setStyle(
                "-fx-background-color: " + (themeMode == ThemeMode.LIGHT ? "#F7F9F9" : "#16181C") + ";" +
                        "-fx-background-radius: 14px;" +
                        "-fx-border-color: " + (themeMode == ThemeMode.LIGHT ? "#CFD9DE" : "#2F3336") + ";" +
                        "-fx-border-radius: 14px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-padding: 4px;"
        );

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(30, 30);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setFitHeight(260);
        imageView.setFitWidth(520);

        Image image = new Image(fullUrl, true); // background loading
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0) {
                container.getChildren().remove(spinner);
            }
        });
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if (isError) {
                container.getChildren().remove(spinner);
                Label errorLabel = new Label("🖼 Image unavailable");
                errorLabel.setStyle("-fx-text-fill: -fx-text-secondary; -fx-font-size: 13px;");
                container.getChildren().add(errorLabel);
            }
        });

        imageView.setImage(image);
        container.getChildren().addAll(imageView, spinner);

        container.setOnMouseClicked(e -> {
            e.consume();
            showFullResolutionDialog(fullUrl, themeMode);
        });

        // Hover feedback
        container.setOnMouseEntered(e -> container.setStyle(
                "-fx-background-color: " + (themeMode == ThemeMode.LIGHT ? "#EFF3F4" : "#1C1F23") + ";" +
                        "-fx-background-radius: 14px;" +
                        "-fx-border-color: " + (themeMode == ThemeMode.LIGHT ? "#1D9BF0" : "#1D9BF0") + ";" +
                        "-fx-border-radius: 14px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 4px;"
        ));
        container.setOnMouseExited(e -> container.setStyle(
                "-fx-background-color: " + (themeMode == ThemeMode.LIGHT ? "#F7F9F9" : "#16181C") + ";" +
                        "-fx-background-radius: 14px;" +
                        "-fx-border-color: " + (themeMode == ThemeMode.LIGHT ? "#CFD9DE" : "#2F3336") + ";" +
                        "-fx-border-radius: 14px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-padding: 4px;"
        ));

        return container;
    }

    /**
     * Opens a full-resolution modal stage containing the media and a Download button.
     */
    public static void showFullResolutionDialog(String fullUrl, ThemeMode themeMode) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Media View - Full Resolution");

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + (themeMode == ThemeMode.LIGHT ? "#FFFFFF" : "#000000") + ";");

        // Header bar with status and action buttons
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label("Full Resolution Preview");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (themeMode == ThemeMode.LIGHT ? "#0F1419" : "#E7E9EA") + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button downloadBtn = new Button("⬇ Download to Downloads Folder");
        downloadBtn.setStyle(
                "-fx-background-color: #1D9BF0; -fx-text-fill: #FFFFFF; " +
                        "-fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 8px 16px; -fx-cursor: hand;"
        );

        Button closeBtn = new Button("✕ Close");
        closeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + (themeMode == ThemeMode.LIGHT ? "#0F1419" : "#E7E9EA") + "; " +
                        "-fx-font-weight: bold; -fx-border-color: #71767B; -fx-border-radius: 20px; -fx-padding: 6px 14px; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> modalStage.close());

        topBar.getChildren().addAll(statusLabel, spacer, downloadBtn, closeBtn);

        // Full resolution image view inside scroll pane
        ImageView fullImageView = new ImageView();
        fullImageView.setPreserveRatio(true);
        fullImageView.setSmooth(true);

        Image fullImage = new Image(fullUrl, true);
        fullImageView.setImage(fullImage);

        // Size image to fit modal initial bounds
        fullImageView.setFitWidth(900);
        fullImageView.setFitHeight(650);

        ScrollPane scrollPane = new ScrollPane(fullImageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Feedback message banner below image
        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        downloadBtn.setOnAction(e -> {
            downloadBtn.setDisable(true);
            downloadBtn.setText("Downloading...");
            new Thread(() -> {
                try {
                    File downloadsFolder = getOSDownloadsFolder();
                    String fileName = getFileNameFromUrl(fullUrl);
                    File targetFile = getUniqueTargetFile(downloadsFolder, fileName);

                    downloadFileFromUrl(fullUrl, targetFile);

                    Platform.runLater(() -> {
                        downloadBtn.setDisable(false);
                        downloadBtn.setText("✓ Downloaded");
                        feedbackLabel.setText("Successfully saved to: " + targetFile.getAbsolutePath());
                        feedbackLabel.setStyle("-fx-text-fill: #00BA7C; -fx-font-size: 13px; -fx-font-weight: bold;");
                        feedbackLabel.setVisible(true);
                        feedbackLabel.setManaged(true);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        downloadBtn.setDisable(false);
                        downloadBtn.setText("⬇ Download to Downloads Folder");
                        feedbackLabel.setText("Download failed: " + ex.getMessage());
                        feedbackLabel.setStyle("-fx-text-fill: #F4212E; -fx-font-size: 13px; -fx-font-weight: bold;");
                        feedbackLabel.setVisible(true);
                        feedbackLabel.setManaged(true);
                    });
                }
            }).start();
        });

        root.getChildren().addAll(topBar, scrollPane, feedbackLabel);

        Scene scene = new Scene(root, 960, 740);
        modalStage.setScene(scene);
        modalStage.show();
    }

    /**
     * Resolves user's OS downloads directory (~/Downloads).
     */
    public static File getOSDownloadsFolder() {
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        return downloadsDir;
    }

    private static String getFileNameFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "media_" + System.currentTimeMillis() + ".png";
        }
        int lastSlash = url.lastIndexOf('/');
        String name = lastSlash != -1 ? url.substring(lastSlash + 1) : url;
        if (name.contains("?")) {
            name = name.substring(0, name.indexOf('?'));
        }
        return name.isBlank() ? "media_" + System.currentTimeMillis() + ".png" : name;
    }

    private static File getUniqueTargetFile(File folder, String fileName) {
        File target = new File(folder, fileName);
        if (!target.exists()) {
            return target;
        }

        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        int count = 1;
        while (target.exists()) {
            target = new File(folder, baseName + " (" + count + ")" + extension);
            count++;
        }
        return target;
    }

    private static void downloadFileFromUrl(String fileUrl, File destination) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fileUrl))
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }

        try (InputStream in = response.body();
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
