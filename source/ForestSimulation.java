import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.Timer;

public class ForestSimulation implements KeyListener {

    private Forest forest;
    private Fire fire;
    private Water water;
    //private Wind wind;

    private ForestPanel panel;
    private JFrame frame;
    private Timer fireTimer;

    // How often the fire advances, in milliseconds. Slower than a redraw
    // tick so the spread is watchable rather than instant.
    private static final int FIRE_STEP_MS = 800;

    public ForestSimulation(){
        //forest
        forest = new Forest(6, 6);
        //objects
        fire = new Fire();
        water = new Water(0, 0);
        //wind = new Wind();

        //GUI
        panel = new ForestPanel(forest, water);
        frame = new JFrame("Forest Simulation");

        frame.add(panel);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addKeyListener(this);
        frame.setFocusable(true);
        frame.setVisible(true);

        // Advance the fire on a timer rather than only when a key is
        // pressed, so it spreads on its own while the player is moving
        // water around.
        fireTimer = new Timer(FIRE_STEP_MS, e -> {
            fire.spread(forest);
            panel.repaint();
        });
        fireTimer.start();
    }

    public void startRandomFire() {
        // Start a random fire in the forest
        int row = (int) (Math.random() * forest.getRows());
        int col = (int) (Math.random() * forest.getColumns());

        try {
            Tree tree = forest.getTree(row, col);
            fire.startFire(tree);
            forest.reportBurning(tree);
            System.out.println("Random fire started at (" + row + ", " + col + ")");
        } catch (InvalidGridPositionException e) {
            // Should not happen since row/col are generated from the
            // forest's own dimensions, but handled defensively in case
            // that generation logic ever changes.
            System.out.println("Could not start fire: " + e.getMessage());
        }

        panel.repaint();
    }

    //WATER
    public void useWater(){
        int row = water.getRow();
        int col = water.getCol();

        try {
            Tree tree = forest.getTree(row, col);
            water.extinguish(tree);
            forest.reportExtinguished(tree);
        } catch (InvalidGridPositionException e) {
            System.out.println("Could not use water: " + e.getMessage());
        }
    }

    public void moveWater(String direction){
        if(direction.equals("UP")){
            water.moveUp();
        } else if(direction.equals("DOWN")){
            water.moveDown(forest.getRows());
        } else if(direction.equals("LEFT")){
            water.moveLeft();
        } else if(direction.equals("RIGHT")){
            water.moveRight(forest.getColumns());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()== KeyEvent.VK_UP) {
            moveWater("UP");
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            moveWater("DOWN");
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveWater("LEFT");
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveWater("RIGHT");
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            useWater();
        }

        panel.repaint();
    }
    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    //MAIN SIMULATION
    public void runSimulation() {
        System.out.println("Forest Simulation Started");
        forest.displayForest();

        startRandomFire();
        forest.displayForest();

    }
    public static void main(String[] args){

        ForestSimulation simulation = new ForestSimulation();
        simulation.runSimulation();

    }
}
