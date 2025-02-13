package com.anthosmasher.snek;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Random;

public class Main extends Application {

    private Random r = new Random();
    private int counter = 3;
    private Rectangle[] rect = new Rectangle[100];
    private Rectangle food;
    private int[] xc = new int[100];
    private int[] yc = new int[100];
    private int dir = 3;
    private Pane root = new Pane();
    private Text gameOverText;
    private boolean isDone = false;
    private int grow = 0;
    private int[] foodloc, headloc;

    private Rectangle initRect() {
        Rectangle res = new Rectangle(45, 45);
        res.setFill(Color.GREEN);
        res.setStroke(Color.BLACK);
        res.setVisible(false);
        return res;
    }

    @Override
    public void start(Stage primaryStage) {
        headloc = new int[]{xc[0], yc[0]};
        Thread game;
        food = initRect();
        food.setFill(Color.RED);
        food.setStroke(Color.RED);
        food.setVisible(true);
        food.setTranslateX(r.nextInt(10) * 50);
        food.setTranslateY(r.nextInt(10) * 50);

        root.getChildren().add(food);

        Scene scene = new Scene(root, 514, 543);

        gameOverText = new Text("Game Over");
        gameOverText.setFill(Color.WHITE);
        gameOverText.setStroke(Color.BLACK);
        gameOverText.setStrokeWidth(3);
        gameOverText.setFont(Font.font("Consolas", FontWeight.BOLD,70));
        gameOverText.setTextAlignment(TextAlignment.CENTER);
        gameOverText.setVisible(false);
        root.getChildren().add(gameOverText);
        gameOverText.setX(88);
        gameOverText.setY(250);
        gameOverText.toFront();

        for (int i = 0; i < 3; i++) {
            rect[i] = initRect();
            rect[i].setTranslateX(50 + 50 * i);
            rect[i].setTranslateY(50 + 50);
            rect[i].setVisible(true);
            root.getChildren().add(rect[i]);
        }

        for (int i = 3; i < 100; i++) {
            rect[i] = initRect();
            rect[i].setTranslateX(50 + 50 * i);
            rect[i].setTranslateY(50 + 50);
            root.getChildren().add(rect[i]);
        }
        rect[0].setFill(Color.WHITE);

        game = new Thread(() -> {
            while (!isDone) {
                move();
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        game.start();

        scene.setOnKeyPressed(event -> {
            KeyCode k = event.getCode();
            switch (k) {
                case D:
                    if (dir != 1 && ((dir == 2 || dir == 0) && headloc[1] != yc[0])) dir = 3;
                    break;
                case A:
                    if (dir != 3 && ((dir == 2 || dir == 0) && headloc[1] != yc[0])) dir = 1;
                    break;
                case S:
                    if (dir != 2 && ((dir == 3 || dir == 1) && headloc[0] != xc[0])) dir = 0;
                    break;
                case W:
                    if (dir != 0 && ((dir == 3 || dir == 1) && headloc[0] != xc[0])) dir = 2;
                    break;
            }
            headloc = new int[]{xc[0], yc[0]};
        });
        root.setStyle("-fx-background-color: black;");

        primaryStage.setTitle("Snek");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("/serpent un peu moche.png"));
        primaryStage.setOnCloseRequest(event -> isDone = true);
        primaryStage.show();
    }

    public void move() {
        for (int i = counter; i > 0; i--) {
            xc[i] = xc[i - 1];
            yc[i] = yc[i - 1];
            if (grow > 0 && xc[counter - 1] == foodloc[0] && yc[counter - 1] == foodloc[1]) {
                rect[counter - 1].setVisible(true);
                grow--;
            }
        }

        switch (dir) {
            case 0 -> {
                if (yc[0] + 50 <= 450) yc[0] += 50; else yc[0] = 0;
            }
            case 1 -> {
                if (xc[0] - 50 >= 0) xc[0] -= 50; else xc[0] = 450;
            }
            case 2 -> {
                if (yc[0] - 50 >= 0) yc[0] -= 50; else yc[0] = 450;
            }
            case 3 -> {
                if (xc[0] + 50 <= 450) xc[0] += 50; else xc[0] = 0;
            }
        }

        if (intersects(xc[0], yc[0])) {
            isDone = true;
            Platform.runLater(() -> {
                gameOverText.setVisible(true);
                gameOverText.toFront();
            });
        } else {
            for (int i = 0; i < counter; i++) {
                rect[i].setTranslateX(xc[i]);
                rect[i].setTranslateY(yc[i]);
            }

            if (rect[0].getBoundsInParent().intersects(food.getBoundsInParent())) {
                boolean isChanged = false;
                foodloc = new int[]{(int) food.getTranslateX(), (int) food.getTranslateY()};
                while (!isChanged) {
                    int newX = r.nextInt(10) * 50;
                    int newY = r.nextInt(10) * 50;

                    boolean valid = true;
                    for (int i = 0; i < counter; i++) {
                        if (xc[i] == newX && yc[i] == newY) {
                            valid = false; // Prevent spawning food inside the snake
                            break;
                        }
                    }
                    if (valid) {
                        food.setTranslateX(newX);
                        food.setTranslateY(newY);
                        foodloc = new int[]{newX, newY};
                        isChanged = true;
                    }
                }
                xc[counter] = xc[counter - 1];
                yc[counter] = yc[counter - 1];
                rect[counter].setTranslateX(xc[counter]);
                rect[counter].setTranslateY(yc[counter]);
                rect[counter].setVisible(true);
                counter++;
                grow++;
            }
        }
    }

    public boolean intersects(int x, int y) {
        int i = 0;
        for (Rectangle part : rect) {
            if (part != rect[0] && i > 0 && part.isVisible() && x == xc[i] && y == yc[i]) {
                return true;
            }
            i++;
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}