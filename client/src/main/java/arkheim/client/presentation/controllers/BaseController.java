package arkheim.client.presentation.controllers;

import arkheim.client.presentation.navigation.Navigator;
import arkheim.client.presentation.theme.ThemeMode;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseController {
    protected Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    protected Node createFormattedPostBody(String content, ThemeMode themeMode, Consumer<String> onHashtagClick) {
        if (content == null || content.isEmpty()) {
            Label label = new Label("");
            label.getStyleClass().add("post-body");
            return label;
        }

        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("post-body");
        textFlow.setMaxWidth(500.0);

        Pattern pattern = Pattern.compile("(#[a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(content);

        int lastIdx = 0;
        while (matcher.find()) {
            if (matcher.start() > lastIdx) {
                String plainText = content.substring(lastIdx, matcher.start());
                Text textNode = new Text(plainText);
                textNode.getStyleClass().add("post-body-text");
                textFlow.getChildren().add(textNode);
            }

            String hashtagStr = matcher.group(1);
            Text hashtagNode = new Text(hashtagStr);
            hashtagNode.setStyle("-fx-fill: #1D9BF0; -fx-font-weight: bold; -fx-cursor: hand;");
            if (onHashtagClick != null) {
                hashtagNode.setOnMouseClicked(e -> {
                    e.consume();
                    onHashtagClick.accept(hashtagStr);
                });
            }
            textFlow.getChildren().add(hashtagNode);
            lastIdx = matcher.end();
        }

        if (lastIdx < content.length()) {
            String remainingText = content.substring(lastIdx);
            Text textNode = new Text(remainingText);
            textNode.getStyleClass().add("post-body-text");
            textFlow.getChildren().add(textNode);
        }

        return textFlow;
    }
}
