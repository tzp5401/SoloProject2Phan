import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

// Ghost class
class Ghost {
    BufferedImage sprite;
    int x, y, dir;

    public Ghost(String name, String spritePath, int x, int y) {
        this.x = x;
        this.y = y;
        this.dir = 0;
        try {
            sprite = ImageIO.read(new File(spritePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void move(int[][] maze) {
        int speed = 2;
        int newX = x, newY = y;
        if (dir == 0) newX -= speed;
        else if (dir == 1) newX += speed;
        else if (dir == 2) newY -= speed;
        else if (dir == 3) newY += speed;

        int row = newY / 20;
        int col = newX / 20;

        if (maze[row][col] == 0) {
            x = newX;
            y = newY;
        } else {
            dir = (int)(Math.random() * 4);
        }
    }

    public void draw(Graphics g, boolean scared) {
        int frame = scared ? 1 : 0;
        g.drawImage(sprite.getSubimage(frame * 16, 0, 16, 16), x, y, null);
    }
}

public class PacManGame extends JPanel implements ActionListener, KeyListener {

    Timer timer;
    BufferedImage pacmanSprite;
    int pacmanX = 20, pacmanY = 20;
    int pacmanDir = 0; // 0=left, 2=right, 4=up, 6=down
    int tileSize = 20;

    int[][] maze = new int[23][23];
    int[][] dotMap = new int[23][23];

    int score = 0;
    int level = 1;
    int lives = 3;
    boolean scaredMode = false;
    int scaredTimer = 0;

    boolean inStartMenu = true;  // <-- NEW: Start menu flag

    Ghost blinky, pinky, inky, clyde;

    public PacManGame() {
        setPreferredSize(new Dimension(460, 460));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        loadResources();
        loadMaze();
        initDots();

        blinky = new Ghost("Blinky", "red ghost.png", 180, 180);
        pinky  = new Ghost("Pinky",  "pink ghost.png", 200, 180);
        inky   = new Ghost("Inky",   "blue ghost.png", 220, 180);
        clyde  = new Ghost("Clyde",  "orange ghost.png", 240, 180);

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
        if (!inStartMenu) {
            updateGame();
        }
        repaint();
    }

    void updateGame() {
        int newX = pacmanX;
        int newY = pacmanY;

        if (pacmanDir == 0) newX -= 3;
        else if (pacmanDir == 2) newX += 3;
        else if (pacmanDir == 4) newY -= 3;
        else if (pacmanDir == 6) newY += 3;

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
            playSound(new File("pacman_chomp.wav"));
        } else if (dotMap[row][col] == 2) {
            dotMap[row][col] = 0;
            score += 50;
            playSound(new File("pacman_chomp.wav"));
            scaredMode = true;
            scaredTimer = 500;
        }

        if (scaredMode) {
            scaredTimer--;
            if (scaredTimer <= 0) scaredMode = false;
        }

        blinky.move(maze);
        pinky.move(maze);
        inky.move(maze);
        clyde.move(maze);

        checkCollision(blinky);
        checkCollision(pinky);
        checkCollision(inky);
        checkCollision(clyde);
    }

    void checkCollision(Ghost ghost) {
        Rectangle pacmanRect = new Rectangle(pacmanX, pacmanY, 16, 16);
        Rectangle ghostRect  = new Rectangle(ghost.x, ghost.y, 16, 16);

        if (pacmanRect.intersects(ghostRect)) {
            if (scaredMode) {
                ghost.x = 180;
                ghost.y = 180;
                score += 200;
                playSound(new File("pacman_eatghost.wav"));
            } else {
                lives--;
                if (lives <= 0) {
                    playDeathSounds(new File("pacman_death1.wav"), new File("pacman_death2.wav"));
                    JOptionPane.showMessageDialog(this, "Game Over!\nScore: " + score);
                    System.exit(0);
                } else {
                    playDeathSounds(new File("pacman_death1.wav"), new File("pacman_death2.wav"));
                    pacmanX = 20;
                    pacmanY = 20;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (inStartMenu) {
            // Draw start menu screen
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String title = "PACMAN";
            int titleWidth = g.getFontMetrics().stringWidth(title);
            g.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 2 - 50);

            g.setFont(new Font("Arial", Font.BOLD, 18));
            String prompt = "Press SPACE to start";
            int promptWidth = g.getFontMetrics().stringWidth(prompt);
            g.drawString(prompt, (getWidth() - promptWidth) / 2, getHeight() / 2);

            return;
        }

        // Draw maze walls
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[row][col] == 1) {
                    g.setColor(Color.BLUE);
                    g.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);
                }
            }
        }

        // Draw dots
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

        // Draw PacMan
        int frame = 0;
        if (pacmanDir == 0) frame = 1;
        else if (pacmanDir == 2) frame = 3;
        else if (pacmanDir == 4) frame = 5;
        else if (pacmanDir == 6) frame = 7;

        g.drawImage(pacmanSprite.getSubimage(frame * 16, 0, 16, 16), pacmanX, pacmanY, null);

        // Draw ghosts
        blinky.draw(g, scaredMode);
        pinky.draw(g, scaredMode);
        inky.draw(g, scaredMode);
        clyde.draw(g, scaredMode);

        // Draw HUD
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 14));

        g.drawString("Score: " + score, 10, 15);

        String levelText = "Level: " + level;
        int levelTextWidth = g.getFontMetrics().stringWidth(levelText);
        g.drawString(levelText, (getWidth() - levelTextWidth) / 2, 15);

        String livesText = "Lives: " + lives;
        int livesTextWidth = g.getFontMetrics().stringWidth(livesText);
        g.drawString(livesText, getWidth() - livesTextWidth - 10, 15);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (inStartMenu && e.getKeyCode() == KeyEvent.VK_SPACE) {
            inStartMenu = false; // <-- START GAME!
        } else {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) pacmanDir = 0;
            if (key == KeyEvent.VK_RIGHT) pacmanDir = 2;
            if (key == KeyEvent.VK_UP) pacmanDir = 4;
            if (key == KeyEvent.VK_DOWN) pacmanDir = 6;
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    private void playSound(File soundFile) {
        new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(soundFile));
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void playDeathSounds(File firstSound, File secondSound) {
        new Thread(() -> {
            try {
                Clip clip1 = AudioSystem.getClip();
                clip1.open(AudioSystem.getAudioInputStream(firstSound));
                clip1.start();
                Thread.sleep(clip1.getMicrosecondLength() / 1000);

                Clip clip2 = AudioSystem.getClip();
                clip2.open(AudioSystem.getAudioInputStream(secondSound));
                clip2.start();
                Thread.sleep(clip2.getMicrosecondLength() / 1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

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
