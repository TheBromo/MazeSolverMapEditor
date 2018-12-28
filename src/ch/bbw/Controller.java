package ch.bbw;

import ch.bbw.model.fields.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    public TextField width;
    public TextField height;
    public BorderPane pane;
    public ToggleGroup blockType;
    public Canvas canvas;
    public RadioButton empty, goal, start, wall;
    private boolean dragging;
    private GraphicsContext gc;
    private int w, h, size = 40;
    private Field[][] fields;
    private double lastDragY;
    private double lastDragX;
    private double offsetX;
    private double offsetY;
    private Start startField;
    private Goal goalField;


    private void draw() {
        gc.setFill(Color.web("#eceff1"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.translate(offsetX, offsetY);
        for (int x = 0; x < fields.length; x++) {
            for (int y = 0; y < fields[x].length; y++) {
                Field field = fields[x][y];
                if (field instanceof Wall) {
                    gc.setFill(Color.web("#263238"));
                    gc.fillRect(x * size, y * size, size, size);
                } else if (field instanceof Goal) {
                    gc.setFill(Color.web("#00e676"));
                    gc.fillRect(x * size, y * size, size, size);
                } else if (field instanceof Start) {
                    gc.setFill(Color.web("#ffea00"));
                    gc.fillRect(x * size, y * size, size, size);
                }
                gc.setStroke(Color.web("#37474f"));
                gc.strokeRect(x * size, y * size, size, size);
            }
        }
        gc.translate(-offsetX, -offsetY);
        gc.restore();
    }

    public void handleGenerate() {
        if (isNumber(width.getText()) && isNumber(height.getText())) {
            w = Integer.parseInt(width.getText()) + 2;
            h = Integer.parseInt(height.getText()) + 2;
            fields = new Field[w][h];

            for (int column = 0; column < w; column++) {
                for (int row = 0; row < h; row++) {
                    if (column == 0 || column == w - 1 || row == 0 || row == h - 1) {
                        fields[column][row] = new Wall();
                    } else {
                        fields[column][row] = new Empty();
                    }
                    fields[column][row].setX(column);
                    fields[column][row].setY(row);
                }
            }
            externalDraw();
        } else {
            System.err.println("Not a number");
        }
    }

    private boolean isNumber(String input) {
        if (input.equals("")) return false;
        for (int i = 0; i < input.length(); i++) {
            if (!(input.charAt(i) >= 47 && input.charAt(i) <= 57)) {
                return false;
            }
        }
        return true;
    }

    private Field getFieldWithCoordinates(double x, double y) {
        x = resetDeformation(x, offsetX);
        y = resetDeformation(y, offsetY);
        if (x >= 0 && x < fields.length && y >= 0 && y < fields[(int) x].length) {
            return fields[(int) x][(int) y];
        } else {
            return null;
        }
    }

    private double resetDeformation(double value, double offset) {
        value -= offset;
        value = (value - (value % size)) / size;
        return value;
    }

    private Point2D getArrayCoordinates(double x, double y) {
        x = resetDeformation(x, offsetX);
        y = resetDeformation(y, offsetY);
        if (x >= 0 && x < fields.length && y >= 0 && y < fields[(int) x].length) {
            return new Point2D(x, y);
        } else {
            return null;
        }
    }

    private boolean inRange(int x, int y) {
        return x >= 1 && x <= w - 2 && y >= 1 && y <= h - 2;
    }

    private Field getSelectedBlockType() {
        if (goal.isSelected()) {
            return new Goal();
        } else if (empty.isSelected()) {
            return new Empty();
        } else if (wall.isSelected()) {
            return new Wall();
        } else {
            return new Start();
        }
    }

    private void save(int x, int y, Field field) {
        field.setY(y);
        field.setX(x);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        lastDragY = mouseEvent.getY();
        lastDragX = mouseEvent.getX();
    }

    private void externalDraw() {
        Platform.runLater(this::draw);
    }

    public void handleMouseDrag(MouseEvent mouseEvent) {
        offsetX += mouseEvent.getX() - lastDragX;
        offsetY += mouseEvent.getY() - lastDragY;
        lastDragX = mouseEvent.getX();
        lastDragY = mouseEvent.getY();
        dragging = true;
        externalDraw();
    }

    public void click(MouseEvent mouseEvent) {
        if (!dragging) {
            Point2D coor = getArrayCoordinates(mouseEvent.getX(), mouseEvent.getY());
            if (coor != null) {
                int x = (int) coor.getX();
                int y = (int) coor.getY();

                if (inRange(x, y)) {
                    if (mouseEvent.isPopupTrigger()) {
                        if (fields[x][y] instanceof Goal) {
                            goalField = null;
                        } else if (fields[x][y] instanceof Start) {
                            startField = null;
                        }
                        fields[x][y] = new Empty();
                    } else {
                        Field type = getSelectedBlockType();
                        if (type instanceof Wall) {
                            fields[x][y] = new Wall();
                        } else if (type instanceof Empty) {
                            fields[x][y] = new Empty();
                        } else if (type instanceof Start) {
                            if (startField != null) {
                                int oldx = startField.getX();
                                int oldy = startField.getY();
                                fields[oldx][oldy] = new Empty();
                                fields[x][y] = new Start();
                            } else {
                                fields[x][y] = new Start();
                            }
                            startField = (Start) fields[x][y];
                        } else if (type instanceof Goal) {
                            if (goalField != null) {
                                int oldx = goalField.getX();
                                int oldy = goalField.getY();
                                fields[oldx][oldy] = new Empty();
                                fields[x][y] = new Goal();

                            } else {
                                fields[x][y] = new Goal();
                            }
                            goalField = (Goal) fields[x][y];
                        }
                    }
                }
                save(x, y, fields[x][y]);
                externalDraw();
            }
        } else {
            dragging = false;
        }
    }

    public void handleExport(ActionEvent actionEvent) {
        try {
            new Map().exportMap(fields);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleImport(ActionEvent actionEvent) {
        try {
            fields = new Map().importMap();
            externalDraw();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
