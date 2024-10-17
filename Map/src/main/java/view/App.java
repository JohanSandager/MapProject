package view;

import java.io.File;
import java.io.IOException;
import java.util.List;

import controller.KeyboardInputController;
import controller.PanZoomController;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;

import javax.xml.stream.XMLStreamException;

public class App extends Application {
    private Canvas canvas;
    private GraphicsContext gc;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("MapLoader");
        BorderPane contentPane = new BorderPane();
        Scene scene = new Scene(contentPane);
        VBox contentBox = new VBox();
        contentBox.setAlignment(Pos.CENTER);
        Button fileChooserButton = new Button("Choose file");
        Button defaultFile = new Button("Load Default");
        contentBox.getChildren().add(defaultFile);
        contentBox.getChildren().add(fileChooserButton);
        contentPane.setCenter(contentBox);
        fileChooserButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Regular osm", "*.osm"),
                    new FileChooser.ExtensionFilter("Binary osm", "*.osm.obj"),
                    new FileChooser.ExtensionFilter("Zipped osm", "*.osm.zip"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    program(primaryStage, FileLoader.load(file.toString()));
                } catch (IOException | ClassNotFoundException | XMLStreamException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        defaultFile.setOnAction(event -> {
            try {
                program(primaryStage, FileLoader.loadResourceFile("default.obj"));
            } catch (IOException | ClassNotFoundException | XMLStreamException e) {
                throw new RuntimeException(e);
            }
        });
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void program(Stage primaryStage, FileLoader loader)
            throws IOException, XMLStreamException, ClassNotFoundException {
        Benchmarking.startTimer();
        canvas = new Canvas(1570, 1050);
        gc = canvas.getGraphicsContext2D();
        canvas.setFocusTraversable(true);
        Group root = new Group();
        primaryStage.setTitle("Map Application");
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(720);
        primaryStage.setMaximized(true);

        StackPane middlePane = new StackPane(canvas);
        BorderPane masterPane = new BorderPane(middlePane);
        root.getChildren().add(masterPane);
        Scene scene = new Scene(root);
        List<MapObject> objects = loader.getObjects();
        Graph graph = loader.getGraph();
        AddressRegistry addressRegistry = loader.getAddressRegistry();
        SerializableRectangle2D bounds = loader.getBounds();
        loader = null;
        System.out.println("STARTING MAPDRAWER(& RTREE) CONSTRUCTION");
        MapDrawer drawer = new MapDrawer(gc, objects, bounds, graph.getXCoords(), graph.getYCoords(), graph.getEdges());
        objects = null;
        System.out.println("MAPDRAWER(& RTREE) CONSTRUCTION DONE\n FINAL TOUCHES:");
        MapView mapView = new MapView(primaryStage, scene, canvas, gc, drawer);
        UIView uiView = new UIView(masterPane, canvas, scene, mapView, drawer, addressRegistry);

        Observer doSearchLambda = () -> {
            Integer fromNode = uiView.getFromAddrSubject().getValue();
            Integer toNode = uiView.getToAddrSubject().getValue();
            drawer.removePointOfInterest();
            if (fromNode == null && toNode == null) {
                return;
            } else if (toNode == null) {
                drawer.setPointOfInterest(fromNode);
                mapView.draw();
                return;
            } else if (fromNode == null) {
                drawer.setPointOfInterest(toNode);
                mapView.draw();
                return;
            } else {
                drawer.setPointOfInterest(fromNode);
                drawer.setPointOfInterest(toNode);
            }
            Graph.TravelPermission perm = uiView.getCurPermission().getValue();
            int maxSpeed = 130;
            if (perm == Graph.TravelPermission.walkable) {
                maxSpeed = 5;
            } else if (perm == Graph.TravelPermission.cyclable) {
                maxSpeed = 15;
            }

            AStar search = new AStar(graph, fromNode, toNode, maxSpeed, perm);
            drawer.setPath(search.getPath());

            uiView.getNavigationList().getChildren().clear();

            for (String instruction : TextualDescription.getTextualDescription(graph, search.getPath())) {
                String[] instructions = instruction.split(":");
                uiView.addToNavigationList(instructions[0], instructions[1], 0, Double.parseDouble(instructions[2]));
            }
            mapView.draw();
        };

        Observer updateZoomLambda = () -> {
            uiView.getZoomValueLabel().setText(Math.round(drawer.getZoomLevel().getValue() * 100) + "%");
        };

        drawer.getZoomLevel().addObserver(updateZoomLambda);

        uiView.getFromAddrSubject().addObserver(doSearchLambda);
        uiView.getToAddrSubject().addObserver(doSearchLambda);
        uiView.getCurPermission().addObserver(doSearchLambda);

        PanZoomController con = new PanZoomController(mapView, graph);
        KeyboardInputController kbCon = new KeyboardInputController(canvas, uiView, mapView);
        Benchmarking.endTime();
    }
}
