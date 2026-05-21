package com.app.uangku.component;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SvgIcon extends StackPane {
    private final StringProperty source = new SimpleStringProperty(this, "source");
    private final StringProperty iconStyleClass = new SimpleStringProperty(this, "iconStyleClass", "");
    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", 24);

    private final Group iconGroup = new Group();

    public SvgIcon() {
        getChildren().add(iconGroup);

        source.addListener((obs, oldValue, newValue) -> reloadIcon());
        iconStyleClass.addListener((obs, oldValue, newValue) -> reloadIcon());
        size.addListener((obs, oldValue, newValue) -> applyScale());

        prefWidthProperty().bind(size);
        prefHeightProperty().bind(size);
        minWidthProperty().bind(size);
        minHeightProperty().bind(size);
        maxWidthProperty().bind(size);
        maxHeightProperty().bind(size);
    }

    public final String getSource() {
        return source.get();
    }

    public final void setSource(String source) {
        this.source.set(source);
    }

    public final StringProperty sourceProperty() {
        return source;
    }

    public final String getIconStyleClass() {
        return iconStyleClass.get();
    }

    public final void setIconStyleClass(String iconStyleClass) {
        this.iconStyleClass.set(iconStyleClass);
    }

    public final StringProperty iconStyleClassProperty() {
        return iconStyleClass;
    }

    public final double getSize() {
        return size.get();
    }

    public final void setSize(double size) {
        this.size.set(size);
    }

    public final DoubleProperty sizeProperty() {
        return size;
    }

    private void reloadIcon() {
        iconGroup.getChildren().clear();

        if (getSource() == null || getSource().isBlank()) {
            return;
        }

        try (InputStream inputStream = getClass().getResourceAsStream(resolvePath(getSource()))) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Icon resource not found: " + getSource());
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document document = factory.newDocumentBuilder().parse(inputStream);
            Element root = document.getDocumentElement();

            List<Shape> shapes = parseShapes(root);
            iconGroup.getChildren().setAll(shapes);
            applyScale();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load SVG icon: " + getSource(), exception);
        }
    }

    private List<Shape> parseShapes(Element root) {
        List<Shape> shapes = new ArrayList<>();
        NodeList childNodes = root.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);
            if (!(node instanceof Element element)) {
                continue;
            }

            Shape shape = switch (element.getTagName()) {
                case "path" -> createSvgPath(element);
                case "rect" -> createRectangle(element);
                case "circle" -> createCircle(element);
                default -> null;
            };

            if (shape != null) {
                applyShapeStyle(shape);
                shapes.add(shape);
            }
        }

        return shapes;
    }

    private SVGPath createSvgPath(Element element) {
        SVGPath path = new SVGPath();
        path.setContent(element.getAttribute("d"));
        return path;
    }

    private Rectangle createRectangle(Element element) {
        Rectangle rectangle = new Rectangle(
                parseDouble(element, "width", 0),
                parseDouble(element, "height", 0)
        );
        rectangle.setX(parseDouble(element, "x", 0));
        rectangle.setY(parseDouble(element, "y", 0));
        double rx = parseDouble(element, "rx", 0);
        rectangle.setArcWidth(rx * 2);
        rectangle.setArcHeight(rx * 2);
        return rectangle;
    }

    private Circle createCircle(Element element) {
        Circle circle = new Circle(parseDouble(element, "r", 0));
        circle.setCenterX(parseDouble(element, "cx", 0));
        circle.setCenterY(parseDouble(element, "cy", 0));
        return circle;
    }

    private void applyShapeStyle(Shape shape) {
        shape.getStyleClass().clear();
        if (getIconStyleClass() != null && !getIconStyleClass().isBlank()) {
            shape.getStyleClass().add(getIconStyleClass());
        }
    }

    private void applyScale() {
        double scale = getSize() / 24.0;
        iconGroup.setScaleX(scale);
        iconGroup.setScaleY(scale);
    }

    private double parseDouble(Element element, String attributeName, double fallback) {
        String value = element.getAttribute(attributeName);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Double.parseDouble(value);
    }

    private String resolvePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }
}
