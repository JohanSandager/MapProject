package view;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import model.*;
import java.io.FileNotFoundException;
import java.util.List;

public class UIView extends model.ResourceLoader {
    StackPane menuPane;
    ScrollPane navigationScrollPane, searchResultScrollPane;
    private final VBox elements, searchResultList, navigationList, navigationView, debugConsoleView;
    private final Label titleLabel, fromLabel, toLabel, fromAddressLabel, toAddressLabel, nearestNeighborLabel,
            fpsLabel, fpsValueLabel, zoomLabel, zoomValueLabel;
    private final TextField inputBar, debugConsoleInputBar;
    private final TextArea debugConsoleTextArea;
    private final StringBuilder printableNavigationText;
    private final Button searchButton, toggleModeButton, carButton, bikeButton, walkButton, navigationPrintButton,
            clearNavigationButton, poiButton;
    private ImageView searchImageView;
    private Image searchImage, searchImageInverted;
    private final Image leftTurnDark, leftTurnLight, rightTurnDark, rightTurnLight, goStraightDark, goStraightLight,
            destinationReachedDark, destinationReachedLight;
    private final ValueChangeSubject<Integer> curAddress, fromAddress, toAddress;
    private final ValueChangeSubject<Graph.TravelPermission> curPermission;
    private final Canvas canvas;
    private final MapView mapView;
    private final AddressRegistry addressRegistry;

    /**
     * GUI used for showing searching, navigation and utility
     * 
     * @param masterPane The workspace for the entire GUI
     * @param canvas     Where the map is drawn
     * @param scene      Current scene for the entire GUI
     * @param drawer     The MapDrawer used to draw on canvas
     * @param mapView    MapView used for showing the map
     */
    public UIView(BorderPane masterPane, Canvas canvas, Scene scene, MapView mapView, MapDrawer drawer,
            AddressRegistry addressRegistry) throws FileNotFoundException {
        this.canvas = canvas;
        this.menuPane = new StackPane();
        this.mapView = mapView;
        this.addressRegistry = addressRegistry;

        // Initialize subject
        curPermission = new ValueChangeSubject<>(Graph.TravelPermission.drivable);
        curAddress = new ValueChangeSubject<>(null);
        fromAddress = new ValueChangeSubject<>(null);
        toAddress = new ValueChangeSubject<>(null);

        // Initialize Vertical Boxes
        elements = createVBox(20, 250, 600, 350, 1000, Pos.TOP_LEFT);
        navigationView = createVBox(10, 320, 200, 320, 800, Pos.TOP_LEFT);
        navigationList = createVBox(2, 300, 200, 300, 16000, Pos.TOP_LEFT);
        searchResultList = createVBox(5, 310, 60, 310, 600, Pos.TOP_LEFT);
        debugConsoleView = createVBox(1, 320, 400, 320, 400, Pos.TOP_LEFT);

        // Initialize Horizontal Boxes
        HBox infoBar = createHBox(5, 350, 20, 350, 20, Pos.TOP_LEFT);
        HBox titleBar = createHBox(5, 350, 20, 350, 20, Pos.TOP_LEFT);
        HBox transportMethodBar = createHBox(5, 200, 20, 200, 20, Pos.TOP_LEFT);
        HBox searchUtilityBar = createHBox(5, 350, 15, 350, 15, Pos.TOP_LEFT);
        HBox fromResultBar = createHBox(5, 300, 20, 350, 20, Pos.TOP_LEFT);
        HBox toResultBar = createHBox(5, 300, 20, 350, 20, Pos.TOP_LEFT);
        HBox searchBar = createHBox(5, 350, 20, 350, 20, Pos.TOP_LEFT);
        HBox zoomBar = createHBox(0, 80, 20, 80, 20, Pos.TOP_LEFT);
        HBox fpsBar = createHBox(0, 80, 20, 80, 20, Pos.TOP_LEFT);

        // Initialize labels
        fromLabel = createLabel("From:", 40, 25, 40, 25, 12);
        fromAddressLabel = createLabel("Nothing selected", 250, 25, 250, 25, 12);
        toLabel = createLabel("To:", 40, 25, 40, 25, 12);
        toAddressLabel = createLabel("Nothing selected", 250, 25, 250, 25, 12);
        titleLabel = createLabel("Where do you want to go?", 170, 25, 170, 25, 14);
        nearestNeighborLabel = createLabel("N/A", 180, 25, 150, 25, 12);
        fpsLabel = createLabel("FPS:", 30, 25, 30, 25, 12);
        fpsValueLabel = createLabel("N/A", 30, 25, 30, 25, 12);
        zoomLabel = createLabel("Zoom:", 50, 25, 50, 25, 12);
        zoomValueLabel = createLabel("N/A", 30, 25, 30, 25, 12);

        // Initialize Buttons
        clearNavigationButton = createButton("Clear", 60, 25, 60, 25);
        toggleModeButton = createButton("Darkmode", 80, 25, 60, 25);
        navigationPrintButton = createButton("Print", 50, 25, 50, 25);
        searchButton = createButton("Search", 37, 26, 37, 26);
        carButton = createButton("Car", 50, 25, 50, 25);
        bikeButton = createButton("Bike", 50, 25, 50, 25);
        walkButton = createButton("Walk", 50, 25, 50, 25);
        poiButton = createButton("POI off", 70, 25, 70, 25);
        setUpClearNavigationButton();
        setUpNavigationPrintButton();
        setUpPOIButton();
        setUpSearchButton();
        setUpToggleModeButton();
        setUpTransportMethodButtons();

        // Initialize Text fields and areas
        inputBar = new TextField();
        setUpInputBar();
        debugConsoleInputBar = new TextField();
        debugConsoleTextArea = DebugConsole.getInstance().getTextArea();
        setUpDebugConsole();

        // Initialize instructions
        printableNavigationText = new StringBuilder();

        // Initialize scroll panes
        navigationScrollPane = new ScrollPane();
        navigationScrollPane.setContent(navigationList);
        searchResultScrollPane = new ScrollPane();
        setUpSearchResultScrollPane();
        setUpNavigationScrollPane();

        // Chaining everything together
        masterPane.setLeft(menuPane);
        menuPane.setAlignment(Pos.TOP_LEFT);
        menuPane.getChildren().add(elements);
        elements.getChildren().addAll(titleBar, searchBar, searchUtilityBar, fromResultBar, toResultBar,
                searchResultScrollPane, navigationView, infoBar);
        elements.setPadding(new Insets(5, 5, 5, 5));

        titleBar.getChildren().addAll(titleLabel, toggleModeButton, poiButton);
        searchBar.getChildren().addAll(inputBar, searchButton);
        searchUtilityBar.getChildren().addAll(transportMethodBar, clearNavigationButton);
        transportMethodBar.getChildren().addAll(carButton, bikeButton, walkButton);
        fromResultBar.getChildren().addAll(fromLabel, fromAddressLabel);
        toResultBar.getChildren().addAll(toLabel, toAddressLabel);
        navigationView.getChildren().addAll(navigationScrollPane, navigationPrintButton);

        infoBar.getChildren().addAll(nearestNeighborLabel, zoomBar, fpsBar);
        zoomBar.getChildren().addAll(zoomLabel, zoomValueLabel);
        fpsBar.getChildren().addAll(fpsLabel, fpsValueLabel);
        FPSCounter.getInstance().setFPSViewer(fpsValueLabel);

        debugConsoleView.getChildren().addAll(debugConsoleTextArea, debugConsoleInputBar);

        // SceneSizeChangeListener for change of window size
        SceneSizeChangeListener sizeListener = new SceneSizeChangeListener(scene, 1920, 1080, canvas, elements,
                mapView);
        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);

        // Listener for nearest road
        RTree rTree = drawer.getTree();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setOnMouseMoved(event -> {
            Affine trans = gc.getTransform();
            try {
                Point2D point = trans.inverseTransform(event.getX(), event.getY());
                SerializablePoint2D serPoint = new SerializablePoint2D(point.getX(), point.getY());
                if (rTree.getNearestRoad(serPoint) != null) {
                    String roadName = rTree.getNearestRoad(serPoint).getName();
                    nearestNeighborLabel.setText(roadName);
                }
            } catch (NonInvertibleTransformException e) {
                throw new RuntimeException(e);
            }
        });

        // Initializing images src\main\resources\images
        goStraightDark = new Image(getResourceStream("goStraightDark.png"));
        rightTurnDark = new Image(getResourceStream("rightTurnDark.png"));
        leftTurnDark = new Image(getResourceStream("leftTurnDark.png"));
        destinationReachedDark = new Image(getResourceStream("destinationReachedDark.png"));
        goStraightLight = new Image(getResourceStream("goStraightLight.png"));
        rightTurnLight = new Image(getResourceStream("rightTurnLight.png"));
        leftTurnLight = new Image(getResourceStream("leftTurnLight.png"));
        destinationReachedLight = new Image(getResourceStream("destinationReachedLight.png"));

        // Init toggle Darkmode
        toggleDarkmode(GlobalConfig.getInstance().getOption(GlobalConfig.Options.TOGGLE_DARKMODE));
    }

    /**
     * Creates a button with given parameters
     *
     * @param text      Text on the button
     * @param minWidth  Minimum width
     * @param minHeight Minimum height
     * @param maxWidth  Maximum width
     * @param maxHeight Maximum height
     * @return Button
     */
    private Button createButton(String text, int minWidth, int minHeight, int maxWidth, int maxHeight) {
        Button button = new Button();
        button.setText(text);
        button.setMinSize(minWidth, minHeight);
        button.setMaxSize(maxWidth, maxHeight);
        button.setFont(new Font("Arial", 12));
        return button;
    }

    /**
     * Creates a label with given parameters
     * 
     * @param text      Text on the label
     * @param minWidth  Minimum width
     * @param minHeight Minimum height
     * @param maxWidth  Maximum width
     * @param maxHeight Maximum height
     * @param fontSize  Fontsize on for text
     * @return Label
     */
    private Label createLabel(String text, int minWidth, int minHeight, int maxWidth, int maxHeight, int fontSize) {
        Label label = new Label();
        label.setText(text);
        label.setMinSize(minWidth, minHeight);
        label.setMaxSize(maxWidth, maxHeight);
        label.setFont(new Font("Arial", fontSize));
        return label;
    }

    /**
     * Creates an HBox with given parameters
     * 
     * @param spacing   Distance between elements
     * @param minWidth  Minimum width
     * @param minHeight Minimum height
     * @param maxWidth  Maximum width
     * @param maxHeight Maximum height
     * @param alignment ALignment of elements
     * @return HBox
     */
    private HBox createHBox(int spacing, int minWidth, int minHeight, int maxWidth, int maxHeight, Pos alignment) {
        HBox hBox = new HBox();
        hBox.setSpacing(spacing);
        hBox.setMinSize(minWidth, minHeight);
        hBox.setMaxSize(maxWidth, maxHeight);
        hBox.setAlignment(alignment);
        return hBox;
    }

    /**
     * Creates an VBox with given parameters
     * 
     * @param spacing   Distance between elements
     * @param minWidth  Minimum width
     * @param minHeight Minimum height
     * @param maxWidth  Maximum width
     * @param maxHeight Maximum height
     * @param alignment ALignment of elements
     * @return VBox
     */
    private VBox createVBox(int spacing, int minWidth, int minHeight, int maxWidth, int maxHeight, Pos alignment) {
        VBox vBox = new VBox();
        vBox.setSpacing(spacing);
        vBox.setMinSize(minWidth, minHeight);
        vBox.setMaxSize(maxWidth, maxHeight);
        vBox.setAlignment(alignment);
        return vBox;
    }

    /**
     * Creating a popup for displaying directions as printable plain text
     */
    private void popupForPrint() {
        Stage navigationPopup = new Stage();
        VBox popupContent = createVBox(15, 500, 800, 500, 800, Pos.CENTER);
        Scene popupScene = new Scene(popupContent);
        Label printText = createLabel(printableNavigationText.toString(), 480, 600, 500, 16000, 12);
        ScrollPane popupPane = new ScrollPane(printText);
        popupPane.setMaxSize(500, 800);
        popupPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Button printButton = createButton("Copy to Clipboard", 150, 25, 150, 25);
        printButton.setOnMouseClicked(event1 -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(printText.getText());
            clipboard.setContent(content);
            printButton.setText("Copied");
            // canvas.requestFocus();
        });

        if (GlobalConfig.getInstance().getOption(GlobalConfig.Options.TOGGLE_DARKMODE)) {
            printButton.setStyle(
                    "-fx-background-color: black; -fx-border-color: white; -fx-text-fill: white; -fx-background-radius: 30, 30, 10, 10; -fx-border-radius: 30, 30, 10, 10");
            popupPane.setStyle("-fx-background: transparent; -fx-background-color: black; ");
            printText.setStyle("-fx-text-fill: white; -fx-background-color: black");
            popupContent.setStyle("-fx-background-color: black");
        } else {
            printButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-text-fill: black; -fx-background-radius: 30, 30, 10, 10; -fx-border-radius: 30, 30, 10, 10");
            popupPane.setStyle("-fx-background: transparent; -fx-background-color: white");
            printText.setStyle("-fx-text-fill: black; -fx-background-color: white");
            popupContent.setStyle("-fx-background-color: white");
        }

        popupContent.getChildren().addAll(popupPane, printButton);
        navigationPopup.setScene(popupScene);
        navigationPopup.show();
    }

    /**
     * Setting up searchResultScrollPane's layout
     */
    private void setUpSearchResultScrollPane() {
        searchResultScrollPane.setContent(searchResultList);
        searchResultScrollPane.setMinHeight(100);
        searchResultScrollPane.setPadding(new Insets(10, 10, 10, 10));
        searchResultScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    /**
     * Setting up navigationScrollPane's layout
     */
    private void setUpNavigationScrollPane() {
        navigationScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navigationScrollPane.setMinWidth(320);
        navigationScrollPane.setMinHeight(400);
        navigationScrollPane.setPrefHeight(900);
    }

    /**
     * Setting up button for "Point of Interest"-mode
     */
    private void setUpPOIButton() {
        poiButton.setOnAction(event -> {
            boolean mode = !mapView.getPOIMode();
            mapView.setPOIMode(mode);
            poiButton.setText(mode ? "POI on" : "POI off");
            canvas.requestFocus();
        });
    }

    /**
     * Setting up Button for clearing navigation input
     */
    private void setUpClearNavigationButton() {
        clearNavigationButton.setOnMouseClicked(event -> {
            fromAddressLabel.setText("Nothing selected");
            toAddressLabel.setText("Nothing selected");
            fromAddress.setValue(null);
            toAddress.setValue(null);
            showNavigationList();
        });
    }

    /**
     * Setting up button for printing navigation to popup
     */
    private void setUpNavigationPrintButton() {
        navigationPrintButton.setOnMouseClicked(event -> popupForPrint());
    }

    /**
     * Setting up button for switching theme
     */
    private void setUpToggleModeButton() {
        toggleModeButton.setOnMouseClicked(event -> {
            if (toggleModeButton.getText().equals("Lightmode")) {
                toggleModeButton.setText("Darkmode");
                GlobalConfig.getInstance().setOption(GlobalConfig.Options.TOGGLE_DARKMODE, true);
                toggleDarkmode(true);
            } else {
                toggleModeButton.setText("Lightmode");
                GlobalConfig.getInstance().setOption(GlobalConfig.Options.TOGGLE_DARKMODE, false);
                toggleDarkmode(false);
            }
            canvas.requestFocus();
        });
    }

    /**
     * Setting up inputbar for searching
     */
    private void setUpInputBar() {
        inputBar.setPromptText("Byvolden 1 1. 103, 4000 Roskilde");
        inputBar.setPrefWidth(280);
        inputBar.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                doSearch(inputBar.getText());
                inputBar.clear();
                canvas.requestFocus();
            }
        });
    }

    /**
     * Setting up the debug console
     */
    private void setUpDebugConsole() {
        setConsoleVisibility(GlobalConfig.getInstance().getOption(GlobalConfig.Options.DEBUG_CONSOLE));

        debugConsoleTextArea.setMinSize(340, 200);
        debugConsoleTextArea.setMaxSize(340, 200);
        debugConsoleTextArea.setStyle("-fx-background-color: white");
        debugConsoleTextArea.setEditable(false);

        debugConsoleInputBar.setStyle("-fx-background-color: white; -fx-border-color: black");
        debugConsoleInputBar.setMinWidth(340);
        debugConsoleInputBar.setPromptText("Input here");
        debugConsoleInputBar.setOnKeyPressed((event) -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                addressRegistry.findAddresses(debugConsoleInputBar.getText());
                debugConsoleInputBar.clear();
                canvas.requestFocus();
            }
        });
    }

    /**
     * Setting up button for searching
     */
    private void setUpSearchButton() throws FileNotFoundException {
        searchButton.setOnMouseClicked(event -> {
            doSearch(inputBar.getText());
            inputBar.clear();
            canvas.requestFocus();
        });
        searchImage = new Image(getResourceStream("searchLoopInverted.png"));
        searchImageInverted = new Image(getResourceStream("searchLoopInverted.png"));
        searchImageView = new ImageView();
        searchImageView.setFitHeight(20);
        searchImageView.setFitWidth(20);
        searchButton.setGraphic(searchImageView);
    }

    /**
     * Setting up buttons for changing transport method
     */
    private void setUpTransportMethodButtons() {
        walkButton.setOnMouseClicked(event -> {
            curPermission.setValue(Graph.TravelPermission.walkable);
            walkButton.setText("Chosen");
            bikeButton.setText("Bike");
            carButton.setText("Car");
            canvas.requestFocus();
        });
        bikeButton.setText("Bike");
        bikeButton.setMinSize(50, 25);
        bikeButton.setOnMouseClicked(event -> {
            curPermission.setValue(Graph.TravelPermission.cyclable);
            walkButton.setText("Walk");
            bikeButton.setText("Chosen");
            carButton.setText("Car");
            canvas.requestFocus();
        });
        carButton.setText("Car");
        carButton.setMinSize(50, 25);
        carButton.setOnMouseClicked(event -> {
            curPermission.setValue(Graph.TravelPermission.drivable);
            walkButton.setText("Walk");
            bikeButton.setText("Bike");
            carButton.setText("Chosen");
            canvas.requestFocus();
        });
    }

    /**
     * Search and find adresses
     * 
     * @param query Search input used for finding addresses
     */
    private void doSearch(String query) {
        clearSearchResultList();
        curAddress.setValue(null);
        List<Address> addresses = addressRegistry.findAddresses(query);
        if (addresses.isEmpty()) {
            Label noResults = new Label();
            noResults.setPrefSize(200, 50);
            noResults.setText("No result for query: " + query);
            noResults.setFont(new Font("Arial", 12));
            noResults.setWrapText(true);
            searchResultList.getChildren().add(noResults);
            return;
        }
        if (addresses.size() == 1) {
            curAddress.setValue(addresses.get(0).getGraphNode());
        } else if (addresses.size() > 8) {
            addresses = addresses.subList(0, 8);
        }
        addresses.forEach(this::addToSearchResultList);
    }

    /**
     * Adding search result to list of search results
     * 
     * @param address Address to show in search result list
     */
    public void addToSearchResultList(Address address) {

        Label view = createLabel(address.toString("standard"), 170, 60, 170, 60, 12);

        view.setOnMouseClicked(event -> {
            curAddress.setValue(address.getGraphNode());
            clearSearchResultList();
            addToSearchResultList(address);
            fromAddressLabel.setText(address.toString("oneLine"));
            canvas.requestFocus();
        });

        // Label time = createLabel(420 + " min", 70, 15, 70, 15, 12);
        // Label distance = createLabel(69 + " km", 70, 15, 70, 15, 12);

        Button fromAddressResultButton = createButton("From", 50, 25, 50, 25);
        fromAddressResultButton.setOnMouseClicked(event -> {
            printableNavigationText.setLength(0);
            fromAddressLabel.setText(address.toString("oneLine"));
            fromAddress.setValue(address.getGraphNode());
            showNavigationList();
            canvas.requestFocus();
        });
        Button toAddressResultButton = createButton("To", 50, 25, 50, 25);
        toAddressResultButton.setOnMouseClicked(event -> {
            printableNavigationText.setLength(0);
            toAddressLabel.setText(address.toString("oneLine"));
            toAddress.setValue(address.getGraphNode());
            showNavigationList();
            canvas.requestFocus();
        });
        VBox searchResultSelectionButtons = createVBox(1, 70, 50, 70, 50, Pos.CENTER);
        searchResultSelectionButtons.getChildren().add(fromAddressResultButton);
        searchResultSelectionButtons.getChildren().add(toAddressResultButton);

        if (GlobalConfig.getInstance().getOption(GlobalConfig.Options.TOGGLE_DARKMODE)) {
            view.setStyle("-fx-text-fill: white");
            // time.setStyle("-fx-text-fill: white");
            // distance.setStyle("-fx-text-fill: white");
            fromAddressResultButton.setStyle(
                    "-fx-background-color: black; -fx-border-color: white; -fx-text-fill: white; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            toAddressResultButton.setStyle(
                    "-fx-background-color: black; -fx-border-color: white; -fx-text-fill: white; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
        } else {
            view.setStyle("-fx-text-fill: black");
            // time.setStyle("-fx-text-fill: black");
            // distance.setStyle("-fx-text-fill: black");
            fromAddressResultButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            toAddressResultButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
        }

        HBox listElementHBox = createHBox(0, 280, 70, 280, 70, Pos.CENTER_LEFT);
        // VBox timeAndDistanceVBox = createVBox(1, 60, 50, 60, 50, Pos.CENTER);

        // timeAndDistanceVBox.getChildren().add(time);
        // timeAndDistanceVBox.getChildren().add(distance);
        listElementHBox.getChildren().add(view);
        // listElementHBox.getChildren().add(timeAndDistanceVBox);
        listElementHBox.getChildren().add(searchResultSelectionButtons);
        searchResultList.getChildren().add(listElementHBox);
    }

    /**
     * Shows navigationList if there are directions to show. Otherwise hides it.
     */
    public void showNavigationList() {
        mapView.draw();
        if (fromAddress.getValue() != null && toAddress.getValue() != null) {
            navigationView.setVisible(true);
        } else {
            navigationView.setVisible(false);
        }
        canvas.requestFocus();
    }

    /**
     * Adding direction to navigation panel
     * 
     * @param direction Single word that tells the direction for next intersection
     * @param context   Text the user should see as direction
     * @param time      Time in minutes until next direction
     * @param distance  Distance in kilometers until next direction
     */
    public void addToNavigationList(String direction, String context, int time, double distance) {
        HBox viewHBox = createHBox(10, 280, 25, 280, 25, Pos.CENTER_LEFT);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);

        Label view = createLabel(context, 280, 25, 280, 25, 12);

        HBox timeAndDistanceHBox = createHBox(5, 60, 25, 60, 25, Pos.TOP_LEFT);
        Label nextTime = createLabel(time + " min.", 50, 25, 50, 25, 12);
        Label nextDistance = createLabel("(" + distance + " km)", 50, 25, 50, 25, 12);
        Label startStamp = createLabel("     ---------- ", 80, 25, 80, 25, 12);
        Label endStamp = createLabel(" ----------", 80, 25, 80, 25, 12);
        if (direction.equals("destination")) {
            printableNavigationText.append("\n" + context);
        } else {
            if (printableNavigationText.length() != 0) {
                printableNavigationText.append("\n");
            }
            printableNavigationText.append(context + "\n" + startStamp.getText() + nextTime.getText() + " "
                    + nextDistance.getText() + endStamp.getText());
        }

        if (GlobalConfig.getInstance().getOption(GlobalConfig.Options.TOGGLE_DARKMODE)) {
            if (direction.equals("straight")) {
                imageView.setImage(goStraightDark);
            } else if (direction.equals("right")) {
                imageView.setImage(rightTurnDark);
            } else if (direction.equals("left")) {
                imageView.setImage(leftTurnDark);
            } else if (direction.equals("destination")) {
                imageView.setImage(destinationReachedDark);
            }
            view.setStyle("-fx-text-fill: white");
            nextTime.setStyle("-fx-text-fill: white");
            nextDistance.setStyle("-fx-text-fill: white");
            startStamp.setStyle("-fx-text-fill: white");
            endStamp.setStyle("-fx-text-fill: white");
        } else {
            if (direction.equals("straight")) {
                imageView.setImage(goStraightLight);
            } else if (direction.equals("right")) {
                imageView.setImage(rightTurnLight);
            } else if (direction.equals("left")) {
                imageView.setImage(leftTurnLight);
            } else if (direction.equals("destination")) {
                imageView.setImage(destinationReachedLight);
            }
            view.setStyle("-fx-text-fill: black");
            nextTime.setStyle("-fx-text-fill: black");
            nextDistance.setStyle("-fx-text-fill: black");
            startStamp.setStyle("-fx-text-fill: black");
            endStamp.setStyle("-fx-text-fill: black");
        }

        viewHBox.getChildren().addAll(imageView, view);
        timeAndDistanceHBox.getChildren().addAll(startStamp, nextTime, nextDistance, endStamp);
        navigationList.getChildren().add(viewHBox);
        if (!direction.equals("destination")) {
            navigationList.getChildren().add(timeAndDistanceHBox);
        }
    }

    /**
     * Clearing list of search results
     */
    public void clearSearchResultList() {
        searchResultList.getChildren().clear();
    }

    /**
     * Sets all styles when theme changes
     * 
     * @param darkmode Determines whether Darkmode should be on or not
     */
    private void toggleDarkmode(boolean darkmode) {
        GlobalConfig.getInstance().setOption(GlobalConfig.Options.TOGGLE_DARKMODE, darkmode);
        if (darkmode) {
            menuPane.setStyle("-fx-background-color: black; -fx-border-color: white");
            elements.setStyle("-fx-background-color: black");
            toggleModeButton.setStyle(
                    "-fx-border-color: white; -fx-background-color: black; -fx-text-fill: white; -fx-border-radius: 10, 10, 10, 10; -fx-background-radius: 10, 10, 10, 10");
            poiButton.setStyle(
                    "-fx-border-color: white; -fx-background-color: black; -fx-text-fill: white; -fx-border-radius: 10, 10, 10, 10; -fx-background-radius: 10, 10, 10, 10");
            inputBar.setStyle(
                    "-fx-border-color: white; -fx-background-color: black; -fx-text-fill: white; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            searchButton.setStyle(
                    "-fx-background-color: black; -fx-border-color: white; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            clearNavigationButton.setStyle(
                    "-fx-background-color: black; -fx-border-color: white; -fx-text-fill: white; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            walkButton.setStyle(
                    "-fx-background-color: black; -fx-border-color: white; -fx-text-fill: white; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            bikeButton.setStyle(
                    "-fx-background-color: black; -fx-border-color: white; -fx-text-fill: white; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            carButton.setStyle(
                    "-fx-background-color: black; -fx-border-color: white; -fx-text-fill: white; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            titleLabel.setStyle("-fx-text-fill: white");
            fromLabel.setStyle("-fx-text-fill: white");
            fromAddressLabel.setStyle("-fx-text-fill: white");
            toLabel.setStyle("-fx-text-fill: white");
            toAddressLabel.setStyle("-fx-text-fill: white");
            searchImageView.setImage(searchImageInverted);
            searchResultScrollPane
                    .setStyle("-fx-background: transparent; -fx-background-color: black; -fx-border-color: white");
            navigationScrollPane.setStyle("-fx-background: transparent; -fx-background-color: black; ");
            navigationPrintButton.setStyle(
                    "-fx-background-color: black; -fx-border-color: white; -fx-text-fill: white; -fx-background-radius: 30, 30, 10, 10; -fx-border-radius: 30, 30, 10, 10");
            nearestNeighborLabel.setStyle("-fx-text-fill: white");
            zoomLabel.setStyle("-fx-text-fill: white");
            zoomValueLabel.setStyle("-fx-text-fill: white");
            fpsLabel.setStyle("-fx-text-fill: white");
            fpsValueLabel.setStyle("-fx-text-fill: white");
            GlobalConfig.getInstance().setBackgroundColor(GlobalConfig.BackgroundColor.CANVAS_BACKGROUND,
                    Color.rgb(10, 10, 10));
        } else {
            elements.setStyle("-fx-background-color: white");
            menuPane.setStyle("-fx-background-color: white; -fx-border-color: black");
            toggleModeButton.setStyle(
                    "-fx-border-color: black; -fx-background-color: white; -fx-text-fill: black; -fx-border-radius: 10, 10, 10, 10; -fx-background-radius: 10, 10, 10, 10");
            poiButton.setStyle(
                    "-fx-border-color: black; -fx-background-color: white; -fx-text-fill: black; -fx-border-radius: 10, 10, 10, 10; -fx-background-radius: 10, 10, 10, 10");
            inputBar.setStyle(
                    "-fx-border-color: black; -fx-background-color: white; -fx-text-fill: black; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            searchButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            clearNavigationButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-text-fill: black; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            walkButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-text-fill: black; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            bikeButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-text-fill: black; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            carButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-text-fill: black; -fx-border-radius: 30, 30, 10, 10; -fx-background-radius: 30, 30, 10, 10");
            titleLabel.setStyle("-fx-text-fill: black");
            fromLabel.setStyle("-fx-text-fill: black");
            fromAddressLabel.setStyle("-fx-text-fill: black");
            toLabel.setStyle("-fx-text-fill: black");
            toAddressLabel.setStyle("-fx-text-fill: black");
            searchImageView.setImage(searchImage);
            searchResultScrollPane
                    .setStyle("-fx-background: transparent; -fx-background-color: white; -fx-border-color: black");
            navigationScrollPane.setStyle("-fx-background: transparent; -fx-background-color: white");
            navigationPrintButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-text-fill: black; -fx-background-radius: 30, 30, 10, 10; -fx-border-radius: 30, 30, 10, 10");
            nearestNeighborLabel.setStyle("-fx-text-fill: black");
            zoomLabel.setStyle("-fx-text-fill: black");
            zoomValueLabel.setStyle("-fx-text-fill: black");
            fpsLabel.setStyle("-fx-text-fill: black");
            fpsValueLabel.setStyle("-fx-text-fill: black");
            GlobalConfig.getInstance().setBackgroundColor(GlobalConfig.BackgroundColor.CANVAS_BACKGROUND,
                    Color.LIGHTGREY);
        }
        showNavigationList();
        clearSearchResultList();
    }

    /**
     * Setting visibility of the debug console
     * 
     * @param visibility Determines visibility
     */
    public void setConsoleVisibility(boolean visibility) {
        debugConsoleView.setVisible(visibility);
        if (visibility) {
            elements.getChildren().add(debugConsoleView);
        } else {
            elements.getChildren().remove(debugConsoleView);
        }
    }

    /**
     * Getting label for showing zoom level
     */
    public Label getZoomValueLabel() {
        return zoomValueLabel;
    }

    /**
     * Getting ValueChangeSubject of the from-address for navigation
     */
    public ValueChangeSubject<Integer> getFromAddrSubject() {
        return fromAddress;
    }

    /**
     * Getting ValueChangeSubject of the to-address for navigation
     */
    public ValueChangeSubject<Integer> getToAddrSubject() {
        return toAddress;
    }

    /**
     * Getting ValueChangeSubject of current permission for navigation
     */
    public ValueChangeSubject<Graph.TravelPermission> getCurPermission() {
        return curPermission;
    }

    public VBox getNavigationList() {
        return navigationList;
    }
}