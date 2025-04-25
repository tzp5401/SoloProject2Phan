import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class PacManGame extends JPanel implements ActionListener, KeyListener {

    Timer timer;
    BufferedImage pacmanSprite;
    int pacmanX = 20, pacmanY = 20;
    int pacmanDir = 0; // 0=left, 2=right, 4=up, 6=down
    int tileSize = 20;

    int[][] maze = new int[23][23]; // 23 rows x 23 columns for 460x460
    int[][] dotMap = new int[23][23];

    int score = 0;
    boolean scaredMode = false;
    int scaredTimer = 0;

    public PacManGame() {
        setPreferredSize(new Dimension(460, 460));
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
            pacmanSprite = ImageIO.read(new File("PacMan.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadMaze() {
        for (int i = 0; i < 23; i++) {
            maze[0][i] = 1;
            maze[22][i] = 1;
            maze[i][0] = 1;
            maze[i][22] = 1;
        }

        // Sample internal wall
        for (int i = 5; i < 18; i++) {
            maze[11][i] = 1;
        }
    }

    void initDots() {
        for (int row = 0; row < 23; row++) {
            for (int col = 0; col < 23; col++) {
                if (maze[row][col] == 0) {
                    dotMap[row][col] = 1;
                }
            }
        }

        dotMap[1][1] = 2;
        dotMap[1][21] = 2;
        dotMap[21][1] = 2;
        dotMap[21][21] = 2;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    void updateGame() {
        int newX = pacmanX;
        int newY = pacmanY;

        if (pacmanDir == 0) newX -= 2;
        else if (pacmanDir == 2) newX += 2;
        else if (pacmanDir == 4) newY -= 2;
        else if (pacmanDir == 6) newY += 2;

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

        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[row][col] == 1) {
                    g.setColor(Color.BLUE);
                    g.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);
                }
            }
        }

        for (int row = 0; row < dotMap.length; row++) {
            for (int col = 0; col < dotMap[0].length; col++) {
                int x = col * tileSize;
                int y = row * tileSize;

                if (dotMap[row][col] == 1) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + 8, y + 8, 4, 4);
                } else if (dotMap[row][col] == 2) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + 4, y + 4, 12, 12);
                }
            }
        }

        int frame = 0;
        if (pacmanDir == 0) frame = 1;
        else if (pacmanDir == 2) frame = 3;
        else if (pacmanDir == 4) frame = 5;
        else if (pacmanDir == 6) frame = 7;

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
