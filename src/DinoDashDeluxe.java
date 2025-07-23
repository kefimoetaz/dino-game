import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class DinoDashDeluxe extends JPanel implements KeyListener {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private static final int GROUND_Y = 500;
    private static final int DINO_START_Y = 450;
    private static final int CLOUD_WIDTH = 100;

    private static final Color SKY_BLUE = new Color(135, 206, 235);
    private static final Color GROUND_GREEN = new Color(34, 139, 34);
    private static final Color DINO_RED = new Color(220, 20, 60); // Crimson red
    private static final Color MOUNTAIN_COLOR = new Color(139, 137, 137);
    private static final Color SUN_COLOR = new Color(255, 215, 0);
    private static final Color GRASS_COLOR = new Color(50, 205, 50);
    private static final Color CLOUD_COLOR = new Color(255, 255, 255, 200);
    private static final Color CACTUS_DARK = new Color(0, 100, 0);
    private static final Color CACTUS_LIGHT = new Color(0, 150, 0);
    private static final Color PTERODACTYL_COLOR = new Color(165, 42, 42);
    private static final Color PTERODACTYL_WING = new Color(139, 69, 19);

    private static final Font HUD_FONT = new Font("Comic Sans MS", Font.BOLD, 24);
    private static final Font GAME_OVER_FONT = new Font("Impact", Font.BOLD, 48);

    private static final int MOVE_SPEED = 5;
    private static final int FAST_FALL_SPEED = 15;
    private static final int MIN_DINO_X = 50;
    private static final int MAX_DINO_X = PANEL_WIDTH - 100;

    private int dinoX = 150;
    private int dinoY = DINO_START_Y;
    private final int dinoSize = 60;
    private boolean jumping = false;
    private boolean ducking = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private int jumpVelocity = 0;
    private int dinoFrame = 0;

    private final int[] cactusX = {800, 1200, 1600};
    private final int cactusY = GROUND_Y - 40;
    private final int[] cactusWidths = {30, 45, 25};
    private final int[] cactusHeights = {50, 50, 50};

    private boolean isGameOver = false;
    private boolean isPaused = false;
    private int score = 0;
    private int highScore = 0;
    private int gameSpeed = 5;

    private final int GRAVITY = 1;
    private final int JUMP_STRENGTH = -18;

    private final int[] cloudX = {200, 450, 700};
    private final int[] cloudY = {100, 150, 80};

    // Flying obstacle arrays
    private final int[] flyingObstacleX = {800, 1200, 1600};
    private final int[] flyingObstacleY = {300, 350, 400};
    private final boolean[] flyingObstacleActive = {false, false, false};
    private int consecutiveObstacleCount = 0;
    private int lastObstacleType = 0;
    private static final int MAX_CONSECUTIVE_SAME_TYPE = 3;

    private final Random random = new Random();
    private final javax.swing.Timer gameTimer;
    private final javax.swing.Timer animationTimer;

    // Add static map for sound caching
    private static final Map<String, Clip> soundCache = new HashMap<>();

    // Add new fields for power-ups and double jump
    private boolean canDoubleJump = false;
    private boolean hasShield = false;
    private int shieldDuration = 0;
    private final int SHIELD_TIME = 300;
    private int lastScoreFlashTime = 0;
    private String scoreMessage = "";
    private int doubleJumpCount = 0;
    
    // Add to existing fields section
    private final Color SHIELD_COLOR = new Color(64, 224, 208, 120);

    // Game constants
    private static final int MIN_CACTUS_SPACING = 300;
    private static final int MAX_CACTUS_SPACING = 400;
    private static final int INITIAL_SPACING = 500;
    private static final int BASE_SPEED = 8; // Base speed (1.0x)
    private static final double INITIAL_SPEED_MULTIPLIER = 0.5; // Start at 0.5x
    private static final double MAX_SPEED_MULTIPLIER = 1.0; // Max at 1.0x (normal speed)
    private static final int SPEED_INCREASE_INTERVAL = 20; // Every 20 points
    private static final double SPEED_INCREASE_AMOUNT = 0.1; // 0.1x increase
    private static final int FLYING_OBSTACLES_START_SCORE = 5;
    private static final int FLYING_OBSTACLE_MIN_Y = 250;
    private static final int FLYING_OBSTACLE_MAX_Y = 400;
    private static final int FLYING_OBSTACLE_HEIGHT = 40;
    private static final int FLYING_OBSTACLE_WIDTH = 60;
    private static final int COLLISION_MARGIN = 10;

    private double currentSpeedMultiplier = INITIAL_SPEED_MULTIPLIER;

    // Animation
    private int sunPosition = 0;
    private double cloudOffset = 0;
    private double grassOffset = 0;

    // Particle system
    private static class Particle {
        double x, y;
        double vx, vy;
        double life;
        Color color;
        float alpha;
        float size;

        Particle(double x, double y, double vx, double vy, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = 1.0;
            this.alpha = 1.0f;
            this.size = 5;
        }

        void update() {
            x += vx;
            y += vy;
            vy += 0.1; // gravity
            life -= 0.02;
            alpha = (float)life;
            size *= 0.99;
        }

        boolean isDead() {
            return life <= 0;
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle> dustParticles = new ArrayList<>();
    private final Random particleRandom = new Random();

    // Enhanced visual effects
    private static final Color DUST_COLOR = new Color(210, 180, 140);
    private static final Color SHIELD_PARTICLE = new Color(64, 224, 208);
    private static final Color JUMP_PARTICLE = new Color(255, 223, 186);
    private double windEffect = 0;
    private int timeOfDay = 0; // 0-360 for day/night cycle

    // Game constants
    private static final int CYCLE_LENGTH = 38; // Complete cycle length in score points
    private static final float TRANSITION_SPEED = 0.02f; // Lower = slower transitions
    
    // Time of day score thresholds
    private static final int EARLY_MORNING_END = 6;
    private static final int MID_MORNING_END = 12;
    private static final int NOON_END = 19;
    private static final int AFTERNOON_END = 25;
    private static final int SUNSET_END = 30;
    private static final int NIGHT_END = 38;

    // Add transition tracking
    private float currentTimeProgress = 0;
    private float targetTimeProgress = 0;

    public DinoDashDeluxe() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        setBackground(SKY_BLUE);

        gameTimer = new javax.swing.Timer(16, e -> {
            if (!isPaused && !isGameOver) {
                updateGame();
                repaint();
            }
        });

        animationTimer = new javax.swing.Timer(100, e -> {
            dinoFrame = (dinoFrame + 1) % 4;
            repaint();
        });

        gameTimer.start();
        animationTimer.start();

        // Initialize clouds
        for (int i = 0; i < cloudX.length; i++) {
            cloudX[i] = random.nextInt(PANEL_WIDTH);
            cloudY[i] = 50 + random.nextInt(150);
        }

        // Initialize obstacles off-screen with more spacing
        for (int i = 0; i < cactusX.length; i++) {
            cactusX[i] = PANEL_WIDTH + ((i + 2) * INITIAL_SPACING); // Start further away
            cactusWidths[i] = 35 + random.nextInt(15);
            flyingObstacleX[i] = PANEL_WIDTH + ((i + 3) * INITIAL_SPACING); // Even further
            flyingObstacleY[i] = FLYING_OBSTACLE_MIN_Y + random.nextInt(FLYING_OBSTACLE_MAX_Y - FLYING_OBSTACLE_MIN_Y);
            flyingObstacleActive[i] = false; // All flying obstacles start inactive
        }
    }

    private void updateGame() {
        moveCacti();
        moveFlyingObstacles();
        moveClouds();
        handleJump();
        detectCollision();
        updateDifficulty();
        updatePowerUps();
        updateParticles();
    }

    private void moveCacti() {
        for (int i = 0; i < cactusX.length; i++) {
            cactusX[i] -= gameSpeed;
            if (cactusX[i] < -cactusWidths[i]) {
                // Find the rightmost cactus only
                int maxX = Math.max(cactusX[0], Math.max(cactusX[1], cactusX[2]));
                
                // Only allow flying obstacles after score threshold
                boolean canSpawnFlying = score >= FLYING_OBSTACLES_START_SCORE;
                boolean shouldSpawnFlying = canSpawnFlying && random.nextInt(3) == 0;

                int obstacleType;
                if (shouldSpawnFlying) {
                    obstacleType = 1; // Flying
                    consecutiveObstacleCount = 0;
                } else {
                    obstacleType = 0; // Cactus
                    consecutiveObstacleCount++;
                }

                if (obstacleType == 0) {
                    // Place new cactus with consistent spacing
                    cactusX[i] = maxX + MIN_CACTUS_SPACING + random.nextInt(MAX_CACTUS_SPACING - MIN_CACTUS_SPACING);
                    cactusWidths[i] = 35 + random.nextInt(15);
                    flyingObstacleActive[i] = false;
                } else {
                    // Place flying obstacle
                    flyingObstacleX[i] = PANEL_WIDTH + MIN_CACTUS_SPACING;
                    flyingObstacleY[i] = FLYING_OBSTACLE_MIN_Y + random.nextInt(FLYING_OBSTACLE_MAX_Y - FLYING_OBSTACLE_MIN_Y);
                    flyingObstacleActive[i] = true;
                    cactusX[i] = PANEL_WIDTH + MAX_CACTUS_SPACING * 2; // Move cactus far away
                }

                lastObstacleType = obstacleType;
                score++;

                // Show message when flying obstacles start appearing
                if (score == FLYING_OBSTACLES_START_SCORE) {
                    scoreMessage = "FLYING ENEMIES INCOMING!";
                    lastScoreFlashTime = score;
                }
            }
        }
    }

    private void moveFlyingObstacles() {
        for (int i = 0; i < flyingObstacleX.length; i++) {
            if (flyingObstacleActive[i]) {
                // Move flying obstacles
                flyingObstacleX[i] -= gameSpeed;
                
                // Add vertical movement to flying obstacles
                flyingObstacleY[i] += Math.sin(flyingObstacleX[i] * 0.02) * 2;
                
                // Keep within bounds
                if (flyingObstacleY[i] < FLYING_OBSTACLE_MIN_Y) {
                    flyingObstacleY[i] = FLYING_OBSTACLE_MIN_Y;
                } else if (flyingObstacleY[i] > FLYING_OBSTACLE_MAX_Y) {
                    flyingObstacleY[i] = FLYING_OBSTACLE_MAX_Y;
                }
            }
        }
    }

    private void moveClouds() {
        for (int i = 0; i < cloudX.length; i++) {
            cloudX[i] -= gameSpeed / 2;
            if (cloudX[i] < -CLOUD_WIDTH) {
                cloudX[i] = PANEL_WIDTH;
                cloudY[i] = 50 + random.nextInt(150);
            }
        }
    }

    private void handleJump() {
        if (jumping) {
            dinoY += jumpVelocity;
            jumpVelocity += GRAVITY;

            // Add jump particles
            if (jumpVelocity < 0 && particleRandom.nextInt(2) == 0) {
                double spread = 2;
                particles.add(new Particle(
                    dinoX + dinoSize/2,
                    dinoY + dinoSize,
                    -1 + particleRandom.nextDouble() * 2,
                    jumpVelocity * 0.2,
                    JUMP_PARTICLE
                ));
            }

            // Apply fast fall when ducking in air
            if (ducking && dinoY < DINO_START_Y) {
                jumpVelocity = FAST_FALL_SPEED;
                // Add fast fall particles
                if (particleRandom.nextInt(2) == 0) {
                    particles.add(new Particle(
                        dinoX + particleRandom.nextInt(dinoSize),
                        dinoY + dinoSize/2,
                        0,
                        -2,
                        DUST_COLOR
                    ));
                }
            }

            if (dinoY >= DINO_START_Y) {
                dinoY = DINO_START_Y;
                jumping = false;
                jumpVelocity = 0;
                doubleJumpCount = 0;
                
                // Landing particles
                for (int i = 0; i < 10; i++) {
                    double vx = -2 + particleRandom.nextDouble() * 4;
                    particles.add(new Particle(
                        dinoX + particleRandom.nextInt(dinoSize),
                        DINO_START_Y + dinoSize,
                        vx,
                        -2 - particleRandom.nextDouble() * 2,
                        DUST_COLOR
                    ));
                }
            }
        }

        // Handle horizontal movement
        if (movingLeft && dinoX > MIN_DINO_X) {
            dinoX -= MOVE_SPEED;
            if (!jumping && particleRandom.nextInt(3) == 0) {
                // Movement particles
                particles.add(new Particle(
                    dinoX + dinoSize,
                    DINO_START_Y + dinoSize - 5,
                    2 + particleRandom.nextDouble() * 2,
                    -particleRandom.nextDouble(),
                    DUST_COLOR
                ));
            }
        }
        if (movingRight && dinoX < MAX_DINO_X) {
            dinoX += MOVE_SPEED;
            if (!jumping && particleRandom.nextInt(3) == 0) {
                // Movement particles
                particles.add(new Particle(
                    dinoX,
                    DINO_START_Y + dinoSize - 5,
                    -2 - particleRandom.nextDouble() * 2,
                    -particleRandom.nextDouble(),
                    DUST_COLOR
                ));
            }
        }
    }

    private void detectCollision() {
        if (hasShield) return;
        
        // Create collision box for dino based on ducking state
        Rectangle dinoRect;
        if (ducking) {
            // When ducking, make the collision box shorter and wider
            dinoRect = new Rectangle(
                dinoX - 10, // Extend hitbox slightly to the left
                dinoY + dinoSize/2, // Start from middle of dino
                dinoSize + 20, // Make hitbox wider
                dinoSize/2 - COLLISION_MARGIN // Make hitbox shorter
            );
        } else {
            dinoRect = new Rectangle(
                dinoX + COLLISION_MARGIN,
                dinoY + COLLISION_MARGIN,
                dinoSize - (2 * COLLISION_MARGIN),
                dinoSize - (2 * COLLISION_MARGIN)
            );
        }

        // Check cactus collisions
        for (int i = 0; i < cactusX.length; i++) {
            Rectangle cactusRect = new Rectangle(
                cactusX[i] + COLLISION_MARGIN,
                cactusY + COLLISION_MARGIN,
                cactusWidths[i] - (2 * COLLISION_MARGIN),
                cactusHeights[i] - (2 * COLLISION_MARGIN)
            );

            if (dinoRect.intersects(cactusRect)) {
                handleGameOver();
                return;
            }
        }

        // Check flying obstacle collisions
        for (int i = 0; i < flyingObstacleX.length; i++) {
            if (flyingObstacleActive[i]) {
                Rectangle flyingRect = new Rectangle(
                    flyingObstacleX[i] + COLLISION_MARGIN,
                    flyingObstacleY[i] + COLLISION_MARGIN,
                    FLYING_OBSTACLE_WIDTH - (2 * COLLISION_MARGIN),
                    FLYING_OBSTACLE_HEIGHT - (2 * COLLISION_MARGIN)
                );

                if (dinoRect.intersects(flyingRect)) {
                    handleGameOver();
                    return;
                }
            }
        }
    }

    private void handleGameOver() {
        isGameOver = true;
        gameTimer.stop();
        animationTimer.stop();
        if (score > highScore) highScore = score;
        playSound("game_over.wav");
    }

    private void updateDifficulty() {
        if (score % SPEED_INCREASE_INTERVAL == 0 && score != 0) {
            // Increase speed multiplier by 0.1, but don't exceed 1.0x
            currentSpeedMultiplier = Math.min(currentSpeedMultiplier + SPEED_INCREASE_AMOUNT, MAX_SPEED_MULTIPLIER);
            // Update actual game speed
            gameSpeed = (int)(BASE_SPEED * currentSpeedMultiplier);
            if (currentSpeedMultiplier < MAX_SPEED_MULTIPLIER) {
                scoreMessage = String.format("Speed: %.1fx!", currentSpeedMultiplier);
                lastScoreFlashTime = score;
            }
        }

        // Update time of day based on score within cycle
        int cycleScore = score % CYCLE_LENGTH;
        float newTargetProgress;
        
        if (cycleScore <= EARLY_MORNING_END) {
            newTargetProgress = 0f;
        } else if (cycleScore <= MID_MORNING_END) {
            newTargetProgress = 0.2f;
        } else if (cycleScore <= NOON_END) {
            newTargetProgress = 0.4f;
        } else if (cycleScore <= AFTERNOON_END) {
            newTargetProgress = 0.6f;
        } else if (cycleScore <= SUNSET_END) {
            newTargetProgress = 0.8f;
        } else if (cycleScore <= NIGHT_END) {
            newTargetProgress = 1.0f;
        } else {
            newTargetProgress = 0f;
        }

        // Smooth transition to target
        if (targetTimeProgress != newTargetProgress) {
            targetTimeProgress = newTargetProgress;
            if (Math.abs(targetTimeProgress - currentTimeProgress) > 0.5f) {
                // If the difference is too large (like transitioning from night to morning),
                // just set it directly to avoid long transitions
                currentTimeProgress = targetTimeProgress;
            }
        }

        // Gradually move current time towards target
        if (currentTimeProgress < targetTimeProgress) {
            currentTimeProgress = Math.min(currentTimeProgress + TRANSITION_SPEED, targetTimeProgress);
        } else if (currentTimeProgress > targetTimeProgress) {
            currentTimeProgress = Math.max(currentTimeProgress - TRANSITION_SPEED, targetTimeProgress);
        }

        // Convert progress to time of day (0-360 degrees)
        timeOfDay = (int)(currentTimeProgress * 360);
    }

    private void updatePowerUps() {
        if (hasShield) {
            shieldDuration--;
            if (shieldDuration <= 0) {
                hasShield = false;
            }
        }
        if (score > 0 && score % 50 == 0 && lastScoreFlashTime != score) {
            scoreMessage = "+" + (score >= 100 ? "DOUBLE JUMP!" : "SHIELD!");
            lastScoreFlashTime = score;
            if (score >= 100) {
                canDoubleJump = true;
            } else {
                hasShield = true;
                shieldDuration = SHIELD_TIME;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawSky(g2d);
        drawClouds(g2d);
        drawGround(g2d);
        drawCacti(g2d);
        drawFlyingObstacles(g2d);
        drawParticles(g2d);
        drawDino(g2d);
        drawHUD(g2d);

        if (isGameOver) drawGameOverScreen(g2d);
        if (isPaused) drawPauseScreen(g2d);

        updateParticles();
    }

    private void drawSky(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Use currentTimeProgress for smooth transitions
        Color skyTop, skyBottom;
        
        if (currentTimeProgress < 0.2f) { // Dawn
            float t = currentTimeProgress * 5;
            skyTop = interpolateColor(new Color(25, 25, 112), new Color(135, 206, 235), t);
            skyBottom = interpolateColor(new Color(70, 70, 170), new Color(173, 216, 230), t);
        } else if (currentTimeProgress < 0.4f) { // Morning
            float t = (currentTimeProgress - 0.2f) * 5;
            skyTop = interpolateColor(new Color(135, 206, 235), new Color(135, 206, 235), t);
            skyBottom = interpolateColor(new Color(173, 216, 230), new Color(173, 216, 230), t);
        } else if (currentTimeProgress < 0.6f) { // Noon
            float t = (currentTimeProgress - 0.4f) * 5;
            skyTop = interpolateColor(new Color(135, 206, 235), new Color(135, 206, 255), t);
            skyBottom = interpolateColor(new Color(173, 216, 230), new Color(173, 216, 250), t);
        } else if (currentTimeProgress < 0.8f) { // Afternoon/Sunset
            float t = (currentTimeProgress - 0.6f) * 5;
            skyTop = interpolateColor(new Color(135, 206, 255), new Color(255, 160, 100), t);
            skyBottom = interpolateColor(new Color(173, 216, 250), new Color(255, 190, 140), t);
        } else { // Night
            float t = (currentTimeProgress - 0.8f) * 5;
            skyTop = interpolateColor(new Color(255, 160, 100), new Color(25, 25, 112), t);
            skyBottom = interpolateColor(new Color(255, 190, 140), new Color(70, 70, 170), t);
        }

        // Sky gradient
        GradientPaint skyGradient = new GradientPaint(0, 0, skyTop, 0, GROUND_Y, skyBottom);
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, PANEL_WIDTH, GROUND_Y);

        // Stars (visible during night)
        if (currentTimeProgress >= 0.8f || currentTimeProgress < 0.2f) {
            float starBrightness = currentTimeProgress >= 0.8f ? 
                Math.min((currentTimeProgress - 0.8f) * 5, 1.0f) : 
                Math.max(1.0f - currentTimeProgress * 5, 0.0f);
            drawStars(g2d, starBrightness);
        }

        // Sun/Moon
        int celestialY = 100 + (int)(Math.sin(Math.toRadians(timeOfDay)) * 200);
        if (currentTimeProgress >= 0.2f && currentTimeProgress < 0.8f) {
            // Sun
            drawSun(g2d, celestialY);
        } else {
            // Moon
            drawMoon(g2d, celestialY);
        }

        // Mountains with parallax
        drawMountains(g2d);
    }

    private Color interpolateColor(Color c1, Color c2, float t) {
        float[] comp1 = c1.getRGBComponents(null);
        float[] comp2 = c2.getRGBComponents(null);
        return new Color(
            comp1[0] + (comp2[0] - comp1[0]) * t,
            comp1[1] + (comp2[1] - comp1[1]) * t,
            comp1[2] + (comp2[2] - comp1[2]) * t,
            1f
        );
    }

    private void drawStars(Graphics2D g2d, float brightness) {
        g2d.setColor(new Color(1f, 1f, 1f, brightness));
        for (int i = 0; i < 50; i++) {
            int x = (particleRandom.nextInt(PANEL_WIDTH) + (int)(timeOfDay * 0.5)) % PANEL_WIDTH;
            int y = particleRandom.nextInt(GROUND_Y - 100);
            int size = 1 + particleRandom.nextInt(2);
            g2d.fillOval(x, y, size, size);
        }
    }

    private void drawSun(Graphics2D g2d, int y) {
        // Sun glow
        RadialGradientPaint sunGlow = new RadialGradientPaint(
            new Point(130, y + 30),
            80f,
            new float[]{0f, 0.7f, 1f},
            new Color[]{
                new Color(1f, 1f, 0.8f, 0.4f),
                new Color(1f, 1f, 0.8f, 0.1f),
                new Color(1f, 1f, 0.8f, 0f)
            }
        );
        g2d.setPaint(sunGlow);
        g2d.fillOval(50, y - 50, 160, 160);

        // Sun body
        g2d.setColor(SUN_COLOR);
        g2d.fillOval(100, y, 60, 60);

        // Sun rays
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 + timeOfDay);
            int rayLength = 30 + (int)(Math.sin(timeOfDay * 0.1 + i) * 10);
            g2d.drawLine(
                130, y + 30,
                (int)(130 + Math.cos(angle) * rayLength),
                (int)(y + 30 + Math.sin(angle) * rayLength)
            );
        }
    }

    private void drawMoon(Graphics2D g2d, int y) {
        // Moon glow
        RadialGradientPaint moonGlow = new RadialGradientPaint(
            new Point(130, y + 30),
            80f,
            new float[]{0f, 0.7f, 1f},
            new Color[]{
                new Color(0.9f, 0.9f, 1f, 0.3f),
                new Color(0.9f, 0.9f, 1f, 0.1f),
                new Color(0.9f, 0.9f, 1f, 0f)
            }
        );
        g2d.setPaint(moonGlow);
        g2d.fillOval(50, y - 50, 160, 160);

        // Moon body
        g2d.setColor(new Color(230, 230, 250));
        g2d.fillOval(100, y, 60, 60);

        // Moon craters
        g2d.setColor(new Color(200, 200, 220));
        g2d.fillOval(110, y + 10, 15, 15);
        g2d.fillOval(130, y + 25, 10, 10);
        g2d.fillOval(125, y + 40, 12, 12);
    }

    private void drawMountains(Graphics2D g2d) {
        // Three layers of mountains for parallax effect
        Color[] mountainColors = {
            new Color(70, 90, 100),
            new Color(90, 110, 120),
            new Color(110, 130, 140)
        };

        for (int layer = 0; layer < 3; layer++) {
            g2d.setColor(mountainColors[layer]);
            int offset = (int)(timeOfDay * (layer + 1) * 0.5) % PANEL_WIDTH;
            
            for (int i = -1; i <= PANEL_WIDTH/200 + 1; i++) {
                int baseX = i * 200 - offset;
                int height = 150 + layer * 50 + (int)(Math.sin(i * 2.5) * 50);
                
                int[] xPoints = {baseX, baseX + 200, baseX + 100};
                int[] yPoints = {GROUND_Y, GROUND_Y, GROUND_Y - height};
                g2d.fillPolygon(xPoints, yPoints, 3);
            }
        }
    }

    private void drawClouds(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        cloudOffset += 0.5;
        g2d.setColor(CLOUD_COLOR);
        
        for (int i = 0; i < cloudX.length; i++) {
            double yOffset = Math.sin((cloudOffset + i * 100) * 0.02) * 10;
            int cloudY = this.cloudY[i] + (int)yOffset;
            
            // Draw fluffy cloud
            g2d.fillOval(cloudX[i], cloudY, 60, 40);
            g2d.fillOval(cloudX[i] + 20, cloudY - 10, 50, 40);
            g2d.fillOval(cloudX[i] + 40, cloudY, 60, 30);
            g2d.fillOval(cloudX[i] + 15, cloudY + 10, 40, 30);
        }
    }

    private void drawGround(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Main ground
        GradientPaint groundGradient = new GradientPaint(
            0, GROUND_Y, GROUND_GREEN,
            0, PANEL_HEIGHT, new Color(20, 100, 20)
        );
        g2d.setPaint(groundGradient);
        g2d.fillRect(0, GROUND_Y, PANEL_WIDTH, PANEL_HEIGHT - GROUND_Y);

        // Animated grass
        grassOffset += gameSpeed * 0.1;
        g2d.setColor(GRASS_COLOR);
        for (int i = 0; i < PANEL_WIDTH; i += 15) {
            int grassHeight = 10 + (int)(Math.sin((i + grassOffset) * 0.05) * 5);
            g2d.drawLine(i, GROUND_Y, i + 7, GROUND_Y - grassHeight);
        }
    }

    private void drawDino(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shield effect if active
        if (hasShield) {
            g2d.setColor(SHIELD_COLOR);
            for (int i = 0; i < 3; i++) {
                int size = dinoSize + 10 + (int)(Math.sin(dinoFrame * 0.3) * 5);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f - (i * 0.1f)));
                g2d.fillOval(dinoX - 5 - i*2, dinoY - 5 - i*2, size + i*4, size + i*4);
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // Body
        g2d.setColor(DINO_RED);
        if (ducking) {
            // Ducking pose
            g2d.fillRoundRect(dinoX, dinoY + dinoSize/2, dinoSize + 10, dinoSize/2, 20, 20);
        } else {
            // Running/jumping pose
            g2d.fillRoundRect(dinoX, dinoY, dinoSize, dinoSize, 20, 20);
            
            // Arms
            int armOffset = (int)(Math.sin(dinoFrame * 0.5) * 5);
            g2d.fillRoundRect(dinoX + 15, dinoY + 20, 20, 8, 5, 5);
        }

        // Eyes
        if (dinoFrame % 4 != 3 || !jumping) {
            g2d.setColor(Color.WHITE);
            g2d.fillOval(dinoX + 35, dinoY + 15, 18, 18);
            g2d.setColor(Color.BLACK);
            g2d.fillOval(dinoX + 42, dinoY + 18, 8, 8);
            
            // Eyebrow
            g2d.setStroke(new BasicStroke(2));
            g2d.drawArc(dinoX + 35, dinoY + 10, 18, 10, 0, 180);
        }

        // Mouth
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        if (ducking) {
            g2d.drawArc(dinoX + 45, dinoY + dinoSize/2 + 5, 20, 10, 180, 180);
        } else {
            g2d.drawArc(dinoX + 15, dinoY + 35, 30, 15, 180, 180);
        }

        // Legs
        if (!ducking) {
            int legOffset = (dinoFrame % 2) * 5;
            g2d.setColor(DINO_RED.darker());
            g2d.fillRoundRect(dinoX + 10, dinoY + dinoSize - 15, 12, 20, 5, 5);
            g2d.fillRoundRect(dinoX + 40, dinoY + dinoSize - 15 + legOffset, 12, 20, 5, 5);
        }
    }

    private void drawCacti(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < cactusX.length; i++) {
            // Base
            g2d.setColor(CACTUS_DARK);
            g2d.fillRoundRect(cactusX[i], cactusY, cactusWidths[i], cactusHeights[i], 10, 10);

            // Highlights
            g2d.setColor(CACTUS_LIGHT);
            g2d.fillRoundRect(cactusX[i] + cactusWidths[i]/4, cactusY, cactusWidths[i]/2, cactusHeights[i], 5, 5);

            // Arms
            if (cactusWidths[i] > 30) {
                g2d.setColor(CACTUS_DARK);
                g2d.fillRoundRect(cactusX[i] - 10, cactusY + 20, 15, 8, 5, 5);
                g2d.fillRoundRect(cactusX[i] + cactusWidths[i] - 5, cactusY + 30, 15, 8, 5, 5);
            }

            // Texture
            g2d.setColor(new Color(0, 120, 0));
            for (int y = cactusY + 10; y < cactusY + cactusHeights[i] - 10; y += 10) {
                g2d.drawLine(cactusX[i] + 5, y, cactusX[i] + cactusWidths[i] - 5, y);
            }
        }
    }

    private void drawFlyingObstacles(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < flyingObstacleX.length; i++) {
            if (flyingObstacleActive[i]) {
                // Body
                g2d.setColor(PTERODACTYL_COLOR);
                g2d.fillOval(flyingObstacleX[i], flyingObstacleY[i], FLYING_OBSTACLE_WIDTH, FLYING_OBSTACLE_HEIGHT);

                // Wings with animation
                g2d.setColor(PTERODACTYL_WING);
                double wingAngle = Math.sin(dinoFrame * 0.5) * 0.5;
                
                // Create transformed graphics for wing rotation
                Graphics2D g2dWing = (Graphics2D) g2d.create();
                g2dWing.translate(flyingObstacleX[i] + FLYING_OBSTACLE_WIDTH/2, 
                                flyingObstacleY[i] + FLYING_OBSTACLE_HEIGHT/2);
                
                // Left wing
                g2dWing.rotate(-wingAngle);
                g2dWing.fillOval(-40, -15, 40, 25);
                
                // Right wing
                g2dWing.rotate(wingAngle * 2);
                g2dWing.fillOval(0, -15, 40, 25);
                
                g2dWing.dispose();

                // Head and beak
                g2d.setColor(PTERODACTYL_COLOR.darker());
                g2d.fillOval(flyingObstacleX[i] + FLYING_OBSTACLE_WIDTH - 15, 
                           flyingObstacleY[i] + 5, 25, 20);
                g2d.setColor(Color.BLACK);
                
                // Draw beak using polygon
                int[] xPoints = {
                    flyingObstacleX[i] + FLYING_OBSTACLE_WIDTH + 5,
                    flyingObstacleX[i] + FLYING_OBSTACLE_WIDTH + 20,
                    flyingObstacleX[i] + FLYING_OBSTACLE_WIDTH + 10
                };
                int[] yPoints = {
                    flyingObstacleY[i] + 15,
                    flyingObstacleY[i] + 15,
                    flyingObstacleY[i] + 20
                };
                g2d.fillPolygon(xPoints, yPoints, 3);

                // Eye
                g2d.setColor(Color.WHITE);
                g2d.fillOval(flyingObstacleX[i] + FLYING_OBSTACLE_WIDTH - 5, 
                           flyingObstacleY[i] + 10, 8, 8);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(flyingObstacleX[i] + FLYING_OBSTACLE_WIDTH - 3, 
                           flyingObstacleY[i] + 12, 4, 4);
            }
        }
    }

    private void drawParticles(Graphics2D g2d) {
        // Draw dust particles
        for (Particle p : dustParticles) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha * 0.3f));
            g2d.setColor(p.color);
            g2d.fillOval((int)p.x, (int)p.y, (int)p.size, (int)p.size);
        }

        // Draw regular particles
        for (Particle p : particles) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha));
            g2d.setColor(p.color);
            g2d.fillOval((int)p.x, (int)p.y, (int)p.size, (int)p.size);
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawHUD(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Score and high score with shadow
        g2d.setFont(HUD_FONT);
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.drawString("Score: " + score, 22, 42);
        g2d.drawString("High Score: " + highScore, 22, 72);
        
        g2d.setColor(Color.BLACK);
        g2d.drawString("Score: " + score, 20, 40);
        g2d.drawString("High Score: " + highScore, 20, 70);

        // Speed indicator with gradient
        String speedText = String.format("Speed: %.1fx", currentSpeedMultiplier);
        FontMetrics fm = g2d.getFontMetrics();
        int speedWidth = fm.stringWidth(speedText);
        
        GradientPaint speedGradient = new GradientPaint(
            PANEL_WIDTH - 150, 0,
            new Color(0, 100, 0),
            PANEL_WIDTH - 150 + speedWidth, 0,
            new Color(0, 150, 0)
        );
        g2d.setPaint(speedGradient);
        g2d.drawString(speedText, PANEL_WIDTH - 150, 40);

        // Power-up status with effects
        if (hasShield) {
            g2d.setColor(SHIELD_COLOR);
            String shieldText = "SHIELD ACTIVE!";
            int shieldWidth = fm.stringWidth(shieldText);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                0.7f + (float)Math.sin(dinoFrame * 0.2) * 0.3f));
            g2d.drawString(shieldText, PANEL_WIDTH - shieldWidth - 20, 100);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        if (canDoubleJump) {
            g2d.setColor(new Color(255, 215, 0));
            String jumpText = "DOUBLE JUMP READY!";
            int jumpWidth = fm.stringWidth(jumpText);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                0.7f + (float)Math.sin(dinoFrame * 0.2) * 0.3f));
            g2d.drawString(jumpText, PANEL_WIDTH - jumpWidth - 20, 130);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // Score message with animation
        if (scoreMessage.length() > 0 && lastScoreFlashTime == score) {
            g2d.setFont(GAME_OVER_FONT);
            FontMetrics msgFm = g2d.getFontMetrics();
            int msgWidth = msgFm.stringWidth(scoreMessage);
            
            // Message shadow
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(scoreMessage, PANEL_WIDTH/2 - msgWidth/2 + 2, PANEL_HEIGHT/2 + 2);
            
            // Animated message color
            float hue = (float)(Math.sin(dinoFrame * 0.1) * 0.1 + 0.3);
            g2d.setColor(Color.getHSBColor(hue, 0.8f, 1.0f));
            g2d.drawString(scoreMessage, PANEL_WIDTH/2 - msgWidth/2, PANEL_HEIGHT/2);
        }

        // Pause reminder
        if (!isGameOver && !isPaused) {
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString("Press P to pause", PANEL_WIDTH - 180, 70);
        }
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setFont(GAME_OVER_FONT);
        g.setColor(Color.RED);
        g.drawString("GAME OVER", 250, 250);

        g.setFont(HUD_FONT);
        g.setColor(Color.WHITE);
        g.drawString("Final Score: " + score, 320, 300);
        if (score == highScore) {
            g.setColor(Color.YELLOW);
            g.drawString("NEW HIGH SCORE!", 300, 340);
        }
        g.setColor(Color.WHITE);
        g.drawString("Press R to restart or Q to quit", 250, 380);
    }

    private void drawPauseScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setFont(GAME_OVER_FONT);
        g.setColor(Color.YELLOW);
        g.drawString("PAUSED", 320, 250);

        g.setFont(HUD_FONT);
        g.setColor(Color.WHITE);
        g.drawString("Press P to continue", 300, 300);
        g.drawString("Press R to restart", 310, 340);
        g.drawString("Press Q to quit", 330, 380);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_UP:
                if (!isGameOver && !isPaused) {
                    if (!jumping) {
                        jumping = true;
                        jumpVelocity = JUMP_STRENGTH;
                        playSound("jump.wav");
                    } else if (canDoubleJump && doubleJumpCount == 0) {
                        jumpVelocity = JUMP_STRENGTH;
                        doubleJumpCount = 1;
                        playSound("jump.wav");
                    }
                }
                break;
            case KeyEvent.VK_DOWN:
                if (!isGameOver && !isPaused) {
                    ducking = true;
                }
                break;
            case KeyEvent.VK_LEFT:
                if (!isGameOver && !isPaused) {
                    movingLeft = true;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (!isGameOver && !isPaused) {
                    movingRight = true;
                }
                break;
            case KeyEvent.VK_R:
                if (isGameOver || isPaused) restartGame();
                break;
            case KeyEvent.VK_P:
                if (!isGameOver) togglePause();
                break;
            case KeyEvent.VK_Q:
                if (isGameOver || isPaused) System.exit(0);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
                ducking = false;
                break;
            case KeyEvent.VK_LEFT:
                movingLeft = false;
                break;
            case KeyEvent.VK_RIGHT:
                movingRight = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void restartGame() {
        dinoY = DINO_START_Y;
        dinoX = 150; // Reset X position
        score = 0;
        jumping = false;
        ducking = false;
        movingLeft = false;
        movingRight = false;
        jumpVelocity = 0;
        isGameOver = false;
        isPaused = false;
        currentSpeedMultiplier = INITIAL_SPEED_MULTIPLIER;
        gameSpeed = (int)(BASE_SPEED * currentSpeedMultiplier); // Start at 0.5x speed
        canDoubleJump = false;
        hasShield = false;
        shieldDuration = 0;
        doubleJumpCount = 0;
        scoreMessage = "";
        lastScoreFlashTime = 0;
        consecutiveObstacleCount = 0;
        lastObstacleType = 0;
        timeOfDay = 0;
        currentTimeProgress = 0;
        targetTimeProgress = 0;

        // Reset all obstacles with more spacing at start
        for (int i = 0; i < cactusX.length; i++) {
            cactusX[i] = PANEL_WIDTH + ((i + 2) * INITIAL_SPACING); // Start further away
            cactusWidths[i] = 35 + random.nextInt(15);
            flyingObstacleX[i] = PANEL_WIDTH + ((i + 3) * INITIAL_SPACING); // Even further
            flyingObstacleY[i] = FLYING_OBSTACLE_MIN_Y + random.nextInt(FLYING_OBSTACLE_MAX_Y - FLYING_OBSTACLE_MIN_Y);
            flyingObstacleActive[i] = false; // All flying obstacles start inactive
        }

        gameTimer.start();
        animationTimer.start();
        repaint();
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            animationTimer.stop();
            playSound("pause.wav");
        } else {
            animationTimer.start();
            playSound("resume.wav");
        }
        repaint();
    }

    private void playSound(String filename) {
        try {
            Clip clip = soundCache.get(filename);
            if (clip == null) {
                InputStream audioSrc = getClass().getResourceAsStream("/sounds/" + filename);
                if (audioSrc == null) throw new IOException("Sound file not found: " + filename);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(audioSrc));
                clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundCache.put(filename, clip);
            }
            clip.setFramePosition(0);
            clip.start();
        } catch (Exception ex) {
            System.err.println("Audio error: " + ex.getMessage());
        }
    }

    private void updateParticles() {
        // Update regular particles
        particles.removeIf(p -> {
            p.update();
            return p.isDead();
        });

        // Update dust particles
        dustParticles.removeIf(p -> {
            p.update();
            return p.isDead();
        });

        // Add dust particles while running
        if (!jumping && !ducking && !isGameOver) {
            if (dinoFrame % 2 == 0) {
                double vx = -2 - particleRandom.nextDouble() * 2;
                double vy = -particleRandom.nextDouble() * 2;
                dustParticles.add(new Particle(
                    dinoX + dinoSize/2,
                    DINO_START_Y + dinoSize - 5,
                    vx, vy, DUST_COLOR
                ));
            }
        }

        // Add shield particles
        if (hasShield && particleRandom.nextInt(3) == 0) {
            double angle = particleRandom.nextDouble() * Math.PI * 2;
            double speed = 1 + particleRandom.nextDouble() * 2;
            particles.add(new Particle(
                dinoX + dinoSize/2 + Math.cos(angle) * dinoSize,
                dinoY + dinoSize/2 + Math.sin(angle) * dinoSize,
                Math.cos(angle) * speed,
                Math.sin(angle) * speed,
                SHIELD_PARTICLE
            ));
        }

        // Update wind effect
        windEffect = Math.sin(System.currentTimeMillis() * 0.001) * 5;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🦖 Dino Dash Deluxe - Java Edition");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setIconImage(new ImageIcon("dino_icon.png").getImage());
            frame.add(new DinoDashDeluxe());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
