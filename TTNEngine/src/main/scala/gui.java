import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import scala.collection.immutable.Vector;

public class gui {
    static String currentPlayer = "";
    public static TicTacState state;
    public static int players;
    private static StandardMCTSAgent agent1, agent2;
    private static JFrame frame;
    private static String r,b;
    static JButton[][] buttons_grid;

    private static void print_board(){
        int dim = state.board().length()-1;
        for(int row = 0; row <= dim; row++){
            for(int entry = 0; entry <= dim; entry++){
                System.out.print(state.board().apply(row).apply(entry) + " ");
            }
            System.out.println();
        }
    }

    private static void addComponentsForResult(Container pane, int n) {
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        String display = "e";
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (state.board().apply(i).apply(j).equals('r')) {
                    display = "X";
                }
                else if (state.board().apply(i).apply(j).equals('b')) {
                    display = "O";
                }
                else if (state.board().apply(i).apply(j).equals('e')) {
                    display = "";
                }
                final JButton button = new JButton(display);
                button.setName(i + " " + j);
                button.setPreferredSize(new Dimension(50, 50));
                button.setFont(new Font("Arial", Font.PLAIN, 40));

                c.gridx = i;
                c.gridy = j;
                pane.add(button, c);
            }
        }

        final JButton restart = new JButton("Restart Game");
        restart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                restart();
            }
        });
        pane.add(restart);
    }

    private static void displayCVCResult(int n) {
        //Create and set up the window.
        frame = new JFrame("CVCResult");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        addComponentsForResult(frame.getContentPane(), n);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private static void computerVcomputer() {
        displayCVCResult(state.dim()+1);
        while(state.winner().isEmpty()) {
            if (state.current_player() == 'r') {
                state = (TicTacState) agent1.make_move(state);
                print_board();
            }
            else {
                state = (TicTacState) agent2.make_move(state);
                print_board();
            }
            frame.dispose();
            displayCVCResult(state.dim()+1);
            checkWin();
        }
    }

    private static void restart() {
        main(null);
    }

    private static void checkWin() {
        char winner;
        String message;

        if (state.winner().nonEmpty()) {
            winner = (char) state.winner().get();
            message = "";

            if (winner == 't') {
                message = "The game has ended in a tie.";
            }
            else if (winner == 'r') {
                message = r + " won.";
            }
            else if (winner == 'b') {
                message = b + " won.";
            }

            if (players == 0) {
                displayCVCResult(state.dim()+1);
            }

            System.out.println(message);
            JOptionPane.showMessageDialog(frame, message);

            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                    "Would you like to start a new game?", "Game Over", JOptionPane.YES_NO_OPTION)) {
                frame.dispose();
                restart();
            }
        }

    }

    private static void moveState(int x, int y) {
        state = (TicTacState) state.move(x, y);
    }

    private static void computerTurn() {
        if (state.winner().nonEmpty()) {
            return;
        }
        state = (TicTacState)agent1.make_move(state);
        print_board();
        int x = (int)state.last_move()._1();
        int y = (int)state.last_move()._2();


        JButton thisGuy = buttons_grid[x][y];

        if (state.current_player() == 'r') {
            thisGuy.setForeground(Color.BLUE);
            thisGuy.setText("X");
        }
        else {
            thisGuy.setForeground(Color.RED);
            thisGuy.setText("O");
        }

        checkWin();
    }

    private static void addComponentsToPane(Container pane, int n) {
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        buttons_grid = new JButton[n][n];
        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++) {
                final JButton button = new JButton("");
                button.setName(i + " " + j);
                button.setPreferredSize(new Dimension(50, 50));
                button.setFont(new Font("Arial", Font.PLAIN, 40));
                button.setForeground(Color.WHITE);
                buttons_grid[i][j] = button;
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String name = button.getName();
                        int x = Integer.parseInt(name.substring(0, name.indexOf(" ")));
                        int y = Integer.parseInt(name.substring(name.indexOf(" ")+1));
                        if (state.is_move_valid(x,y)) {
                            if (players == 1){
                                moveState(x, y);
                                if (state.current_player() == 'r') {
                                    button.setForeground(Color.BLUE);
                                    button.setText("X");
                                }
                                else {
                                    button.setForeground(Color.RED);
                                    button.setText("O");
                                }
                                checkWin();
                                currentPlayer = "c1";
                                computerTurn();
                            }
                            else if (players == 2) {
                                if (currentPlayer.equals("p1")) {
                                    moveState(x, y);
                                    currentPlayer = "p2";
                                    button.setForeground(Color.BLUE);
                                    button.setText("X");
                                    checkWin();
                                }
                                else {
                                    moveState(x, y);
                                    currentPlayer = "p1";
                                    button.setForeground(Color.RED);
                                    button.setText("O");
                                    checkWin();
                                }
                            }
                        }
                    }
                });
                c.gridx = i;
                c.gridy = j;
                pane.add(button, c);
            }
        }

        final JButton restart = new JButton("Restart Game");
        restart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                restart();
            }
        });
        pane.add(restart);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGui(int n) {
        //Create and set up the window.
        frame = new JFrame("TTNBoard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        addComponentsToPane(frame.getContentPane(), n);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private static int getN() {
        String input = "";
        int n = 0;
        boolean err = true;

        do {
            try {
                input = JOptionPane.showInputDialog("Enter board size N x N:");
                n = Integer.parseInt(input);
                if (n > 0) {
                    err = false;
                }
            }
            catch(NumberFormatException e) {}
        } while (err);

        return n;
    }

    private static int getK(int n) {
        String input = "";
        int k = 0;
        boolean err = true;

        do {
            try {
                input = JOptionPane.showInputDialog("Enter number of winning connections:");
                k = Integer.parseInt(input);
                if (k <= n && k > 0) {
                    err = false;
                }
            }
            catch(NumberFormatException e) {}
        } while (err);

        return k;
    }

    private static int getPlayers() {
        String input = "";
        int players = 0;
        boolean err = true;

        do {
            try {
                input = JOptionPane.showInputDialog("Enter number of players(0,1,2):");
                players = Integer.parseInt(input);
                if (players >= 0 && players <= 2) {
                    err = false;
                }
            }
            catch(NumberFormatException e) {}
        } while (err);

        return players;
    }

    private static int getTurn() {
        String input = "";
        int turn = 0;
        boolean err = true;

        do {
            try {
                input = JOptionPane.showInputDialog("Type '1' to go first or '2' to go second:");
                turn = Integer.parseInt(input);
                if (turn == 1 || turn == 2) {
                    err = false;
                }
            }
            catch(NumberFormatException e) {}
        } while (err);

        return turn;
    }

    private static double getTime(String player) {
        String input = "";
        double time = 0;
        boolean err = true;

        do {
            try {
                input = JOptionPane.showInputDialog(player +": Enter number of seconds MCTS can run:");
                time = Double.parseDouble(input);

                if (time > 0) {
                    err = false;
                }
            }
            catch(NumberFormatException e) {}
        } while (err);

        return time;
    }

    private static double getC(String player) {
        String input = "";
        double a = 0;
        double b = 0;
        boolean err = true;

        do {
            try {
                input = JOptionPane.showInputDialog(player + ": Enter the a value for a^b:");
                a = Double.parseDouble(input);

                if (a > 0) {
                    err = false;
                }
            } catch(NumberFormatException e) {}
        } while (err);

        err = true;

        do {
            try {
                input = JOptionPane.showInputDialog(player + ": Enter the b value for a^b:");
                b = Double.parseDouble(input);

                if (b > 0) {
                    err = false;
                }
            }catch(NumberFormatException e) {}
        } while (err);

        return Math.pow(a, b);
    }

    /*private static int getInputInt(String message, Integer lowerBound, Integer upperBound) {
        String input = "";
        int num = 0;
        boolean err = true;

        do {
            try {
                input = JOptionPane.showInputDialog(message);
                num = Integer.parseInt(input);

                if (lowerBound != null && upperBound != null) {
                    if
                }
                err = false;
            }
            catch(NumberFormatException e) {}
        } while (err);

        return num;
    }*/

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int n = getN();
                int k = getK(n);
                double thinkingTime1, thinkingTime2;


                players = getPlayers();

                //agent1 = new StandardMCTSAgent(thinkingTime1, Math.pow(2, 0.5));

                state = TicTacState.create_new_game_state(n, k, 'r');
                //System.out.println("Winner right now is " + state.winner());
                if (players > 0) {
                    createAndShowGui(n);
                }
                if (players == 0) {
                    currentPlayer = "c1"; //c1 is r, c2 is b
                    r = currentPlayer;
                    b = "c2";
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                            "Use default values for the agents? t=2s c=2^.5", "Autofill", JOptionPane.YES_NO_OPTION)) {
                        agent1 = new StandardMCTSAgent(2, Math.pow(2, 0.5));
                        agent2 = new StandardMCTSAgent(2, Math.pow(2, 0.5));
                    }
                    else {
                        agent1 = new StandardMCTSAgent(getTime(r), getC(r));
                        agent2 = new StandardMCTSAgent(getTime(b), getC(b));
                    }

                    computerVcomputer();
                }
                else if (players == 1) {
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                            "Use default values for the agent? t=2s c=2^.5", "Autofill", JOptionPane.YES_NO_OPTION)) {
                        agent1 = new StandardMCTSAgent(2, Math.pow(2, 0.5));
                    }
                    else {
                        agent1 = new StandardMCTSAgent(getTime(r), getC(r));
                    }

                    if (getTurn() == 1) {
                        currentPlayer = "p1"; //p1 is r, c1 is b
                        r = currentPlayer;
                        b = "c1";
                    }
                    else {
                        currentPlayer = "c1"; //c1 is r, p1 is b
                        r = currentPlayer;
                        b = "p1";
                        computerTurn();
                    }

                }
                else if (players == 2) {
                    currentPlayer = "p1"; // p1 is r, p2 is b
                    r = currentPlayer;
                    b = "p2";
                }
            }
        });
    }
}

