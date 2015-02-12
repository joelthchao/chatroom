import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class EditHTML {
	private static HTMLEditor htmlEditor = null;
	
	public EditHTML()
	{
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            initAndShowGUI();
            }
        });
	}
	
    private static void initAndShowGUI() {
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("HTML Editor");
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(550, 520);
        frame.setResizable(false);
        frame.setVisible(true);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            initFX(fxPanel);
            }
       });
    }

    private static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }

    private static Scene createScene() {
    	/*
    	 * Enter code here
    	 */
        String INITIAL_TEXT = "<html><body>NMLab Rocks!!!</body></html>";
    	Group root = new Group();
        VBox vRoot = new VBox();

        vRoot.setPadding(new Insets(8, 8, 8, 8));
        vRoot.setSpacing(5);

        htmlEditor = new HTMLEditor();
        htmlEditor.setPrefSize(500, 245);
        htmlEditor.setHtmlText(INITIAL_TEXT);
        vRoot.getChildren().add(htmlEditor);

        final Label htmlLabel = new Label();
        htmlLabel.setMaxWidth(500);
        htmlLabel.setWrapText(true);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("noborder-scroll-pane");
        scrollPane.setContent(htmlLabel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(180);

        Button showHTMLButton = new Button("Show the HTML below");
        vRoot.setAlignment(Pos.CENTER);
        showHTMLButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                htmlLabel.setText(htmlEditor.getHtmlText());
            }
        });

        vRoot.getChildren().addAll(showHTMLButton, scrollPane);
        root.getChildren().addAll(vRoot);
        return new Scene(root);
    }
}