import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class PacManGame extends JPanel implements ActionListener, KeyListener {

    Timer timer;
    BufferedImage pacmanSprite;
    int pacmanX = 32, pacmanY = 32;
    int pacmanDir = 0; // 0=right, 2=left, 4=up, 6=down
    int tileSize = 16;

    int[][] maze = new int[20][20]; // 1 = wall, 0 = path
    int[][] dotMap = new int[20][20]; // 0 = none, 1 = dot, 2 = power pellet

    int score = 0;
    boolean scaredMode = false;
    int scaredTimer = 0;

    public PacManGame() {
        setPreferredSize(new Dimension(320, 320));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        loadResources();
        loadMaze();
        initDots();

        timer = new Timer(40, this);
        timer.start();
    }

    void loadResources() {
        try {
            pacmanSprite = ImageIO.read(new File("PacMan.png")); // Sprite order: L, R, U, D
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadMaze() {
        for (int i = 0; i < 20; i++) {
            maze[0][i] = 1;
            maze[19][i] = 1;
            maze[i][0] = 1;
            maze[i][19] = 1;
        }
        for (int i = 5; i < 15; i++) {
            maze[10][i] = 1;
        }
    }

    void initDots() {
        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 20; col++) {
                if (maze[row][col] == 0) {
                    dotMap[row][col] = 1;
                }
            }
        }
        dotMap[1][1] = 2;
        dotMap[1][18] = 2;
        dotMap[18][1] = 2;
        dotMap[18][18] = 2;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    void updateGame() {
        int newX = pacmanX;
        int newY = pacmanY;

        if (pacmanDir == 0) newX -= 2;        // Left
        else if (pacmanDir == 2) newX += 2;   // Right
        else if (pacmanDir == 4) newY -= 2;   // Up
        else if (pacmanDir == 6) newY += 2;   // Down

        int row = newY / tileSize;
        int col = newX / tileSize;

        if (maze[row][col] == 0) {
            pacmanX = newX;
            pacmanY = newY;
        }

        row = pacmanY / tileSize;
        col = pacmanX / tileSize;

        if (dotMap[row][col] == 1) {
            dotMap[row][col] = 0;
            score += 10;
        } else if (dotMap[row][col] == 2) {
            dotMap[row][col] = 0;
            score += 50;
            scaredMode = true;
            scaredTimer = 500;
        }

        if (scaredMode) {
            scaredTimer--;
            if (scaredTimer <= 0) scaredMode = false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 20; col++) {
                if (maze[row][col] == 1) {
                    g.setColor(Color.BLUE);
                    g.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);
                }
            }
        }

        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 20; col++) {
                int x = col * tileSize;
                int y = row * tileSize;

                if (dotMap[row][col] == 1) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + 6, y + 6, 4, 4);
                } else if (dotMap[row][col] == 2) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + 2, y + 2, 12, 12);
                }
            }
        }

        // Map direction to correct sprite frame
        int frame = 0;
        if (pacmanDir == 0) frame = 1;  // Left
        else if (pacmanDir == 2) frame = 3;  // Right
        else if (pacmanDir == 4) frame = 5;  // Up
        else if (pacmanDir == 6) frame = 7;  // Down

        g.drawImage(pacmanSprite.getSubimage(frame * 16, 0, 16, 16), pacmanX, pacmanY, null);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT)  pacmanDir = 0;
        if (key == KeyEvent.VK_RIGHT) pacmanDir = 2;
        if (key == KeyEvent.VK_UP)    pacmanDir = 4;
        if (key == KeyEvent.VK_DOWN)  pacmanDir = 6;
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("PacMan Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new PacManGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
