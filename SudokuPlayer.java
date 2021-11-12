import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Queue;
import java.text.DecimalFormat;

public class SudokuPlayer implements Runnable, ActionListener {

    // final values must be assigned in vals[][]
    int[][] vals = new int[9][9];
    Board board = null;



    /// --- AC-3 Constraint Satisfication --- ///
   
    
    // Useful but not required Data-Structures;
    ArrayList<Integer>[] globalDomains = new ArrayList[81];
    ArrayList<Integer>[] neighbors = new ArrayList[81];
    Queue<Arc> globalQueue = new LinkedList<Arc>();
        

    /*
     * This method sets up the data structures and the initial global constraints
     * (by calling allDiff()) and makes the initial call to backtrack().
     */

    @SuppressWarnings("unchecked")
    private final void init(){
        //Do NOT remove these 3 lines (required for the GUI)
        board.Clear();
        ops = 0;
        recursions = 0;


        /**
         *  YOUR CODE HERE:
         *  Create Data structures ( or populate the ones defined above ).
         *  These will be the data structures necessary for AC-3.
         **/
        //Sets up neighbors and globalQueue
        allDiff(vals);
        
        //initiates storage arrays
        ArrayList<Integer> temp = new ArrayList<Integer>();
        ArrayList<Integer> empty = new ArrayList<Integer>();
        
        //Initiates domains for empty squares w/ 1-9
        for (int q = 1; q < vals.length + 1; q++){
            empty.add(q);
        }
        
        //sets feasible domains for squares
        for (int i = 0; i < vals.length; i ++){
            for (int k = 0; k < vals[0].length; k++){
                
                int tile = i*vals.length + k;
                
                if (vals[i][k] == 0){
                    temp.clear();
                    temp.addAll(empty);
                    for (int j = 0; j < neighbors[tile].size(); j ++){
                        temp.remove((Object)vals[neighbors[tile].get(j)/9][neighbors[tile].get(j)%9]);
                    }
                    globalDomains[tile] = (ArrayList<Integer>) temp.clone();
                }
                
                else{
                globalDomains[tile] = new ArrayList<Integer>();
                    globalDomains[tile].add(vals[i][k]);
                }
            }
        }
        
         
         // Initial call to backtrack() on cell 0 (top left)
        boolean success = backtrack(0,globalDomains);

        // Prints evaluation of run
        Finished(success);

    }

    

    // This defines constraints between a set of variables
    // This is discussed in the book. You may change this method header.
    @SuppressWarnings("unchecked")
    private final void allDiff(int[][] all){
        // YOUR CODE HERE
        ArrayList<Integer> col = new ArrayList<Integer>();
        ArrayList<Integer> row = new ArrayList<Integer>();
        ArrayList<Integer>[] box = new ArrayList[3];
        ArrayList<Integer> temp = new ArrayList<Integer>();
        
        int tile;
        
        for (int i = 0; i < vals.length; i++){
            //clears all lists to avoid improper neighbor assignment
            row.clear();
            col.clear();
            if (i%3 == 0){
                for (int r = 0; r < box.length; r ++){
                    box[r] = new ArrayList<Integer>();
                }
            }
            
            //for loop that adds all tiles with values into row, column, and box
            for (int j = 0; j < vals.length; j++){
                int r_tile = i*9 + j;
                int c_tile = i + j*9;
                row.add(r_tile);
                col.add(c_tile);
                
                if (j%3 == 0 && i%3 == 0){
                    for (int n = i; n < i + 3; n++){
                        for (int c = j; c < j + 3; c++){
                            box[(j)/3].add(n*9 + c);
                            
                        }
                    }
                }
            }
            
            //assigns neighbors to each tile
            for (int k = 0; k < vals[0].length; k++){
                
                //works down the columns
                tile = (k*vals[0].length) + i;
                temp.clear();
                temp.addAll(col);
                
                //keeps old neighbors of each tile
                if (neighbors[tile] != null){
                    temp.addAll(neighbors[tile]);
                }
                
                neighbors[tile] = (ArrayList<Integer>) temp.clone();
                
                //works across the rows and boxes
                tile = k + (i*vals.length);
                temp.clear();
                temp.addAll(row);
                temp.addAll(box[(k)/3]);
                
                //keeps old neighbors of each tile
                if (neighbors[tile] != null){
                    temp.addAll(neighbors[tile]);
                }
                
                neighbors[tile] = (ArrayList<Integer>) temp.clone();

                
                
            }
        }
        
        for (int i = 0; i < neighbors.length; i++) {
            Set<Integer> hs = new HashSet<> ();
            hs.addAll(neighbors[i]);
            neighbors[i].clear();
            neighbors[i].addAll(hs);
            for (int k = 0; k < neighbors[i].size(); k++){
                if (i != neighbors[i].get(k)){
                    globalQueue.add(new Arc(i, neighbors[i].get(k)));
                }
            }
        }
    }

    // This is the Recursive AC3.  ( You may change this method header )
    private final boolean backtrack(int cell, ArrayList<Integer>[] Domains) {

        recursions += 1;
        // YOUR CODE HERE
        int[] board = new int[81];
        int dSize = 0;
        int last_cell = 80;
        
        ArrayList<Integer>[] tempDom = new ArrayList[81];
        for(int i = 0; i < Domains.length; i ++){
            tempDom[i] = new ArrayList<Integer>();
            tempDom[i].addAll(Domains[i]);

        }

        
        if (cell > 80){
            return true;
        }
        if (tempDom[cell].size() == 1){
            vals[cell/9][cell%9] = tempDom[cell].get(0);
            dSize = 10;
            backtrack(cell+1, tempDom);
        }
       
        if (!AC3(Domains)){
            return false;
        }
        
        
        while (dSize < Domains[cell].size()){
            board[cell] = Domains[cell].get(dSize);
            tempDom[cell].clear();
            //passes a single-valued domain for cell into child nodes
            tempDom[cell].add(board[cell]);
            vals[cell/9][cell%9] = board[cell];
            backtrack(cell+1, tempDom);
            dSize++;
            if (!rowContains(8,0)){
                dSize = 10;
            }
            
        }
        //finds the last empty cell
        while (Domains[last_cell].size() != 1 && cell == 0){
            last_cell--;
        }
        //updates the last empty cell at the end
        if (cell == last_cell){
            for (int i = 1; i < Domains[last_cell].size(); i ++){
                int val = vals[last_cell/9][last_cell%9];
                System.out.println(val);
                vals[last_cell/9][last_cell%9] = 0;
                System.out.println(val);
                if (!valid(last_cell/9, last_cell%9, val)){
                    vals[last_cell/9][last_cell%9] = Domains[last_cell].get(i);
                }
                else{
                    vals[last_cell/9][last_cell%9] = val;
                    i = Domains[last_cell].size();
                }
                
            }
        }
        
        
        return true;

    }

    // This is the actual AC-3 Algorithm ( You may change this method header)
    private final boolean AC3(ArrayList<Integer>[] Domains) {
        // YOUR CODE HERE
        Queue<Arc> q = new LinkedList<Arc>();
        q.addAll(globalQueue);
        Arc temp;
        ArrayList<Integer>[] tempDom = new ArrayList[81];
        for(int i = 0; i < Domains.length; i ++){
            tempDom[i] = new ArrayList<Integer>();
            if (Domains[i] != null){
                tempDom[i].addAll(Domains[i]);
            }

        }
        ArrayList<Integer>[] tempNei = new ArrayList[81];
        for(int i = 0; i < neighbors.length; i ++){
            tempNei[i] = new ArrayList<Integer>();
            if (neighbors[i] != null){
                tempNei[i].addAll(neighbors[i]);
            }

        }
        //using tempdom here prevents editing of parent domains
        while(!q.isEmpty()){
            temp = q.poll();
            if (Revise(temp, tempDom)){
                if (tempDom[temp.Xi].size() == 0){
                    return false;
                }
                tempNei[temp.Xi].remove((Object)temp.Xj);
                
                int jSize = tempNei[temp.Xi].size();
                for (int i = 0; i < jSize; i ++){
                    if (tempNei[temp.Xi].get(i) != temp.Xi){
                        q.add(new Arc(tempNei[temp.Xi].get(i), temp.Xi));
                 
                    }
                }
               
                
            }
            
        }
        return true;
    }
    
    

 // This is the Revise() method defined in the book
     // ( You may change this method header )
     private final boolean Revise(Arc t, ArrayList<Integer>[] Domains){
         ops += 1;
         // YOUR CODE HERE
         boolean revised = false;
         if (Domains[t.Xj].size() == 1 && Domains[t.Xi].contains(Domains[t.Xj].get(0))){
             Domains[t.Xi].remove(Domains[t.Xj].get(0));
             revised = true;
         }
         return revised;
     }

   
    
    private final void customSolver(){
           
           //’success’ should be set to true if a successful board    
           //is found and false otherwise.
           boolean success = true;
           board.Clear();
            
            System.out.println("Running custom algorithm");

            //-- Your Code Here --
     
            
           Finished(success);
               
        }


    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    public final boolean valid(int x, int y, int val){
        ops +=1;
        if (vals[x][y] == val)
            return true;
        if (rowContains(x,val))
            return false;
        if (colContains(y,val))
            return false;
        if (blockContains(x,y,val))
            return false;
        return true;
    }

    public final boolean blockContains(int x, int y, int val){
        int block_x = x / 3;
        int block_y = y / 3;
        for(int r = (block_x)*3; r < (block_x+1)*3; r++){
            for(int c = (block_y)*3; c < (block_y+1)*3; c++){
                if (vals[r][c] == val)
                    return true;
            }
        }
        return false;
    }

    public final boolean colContains(int c, int val){
        for (int r = 0; r < 9; r++){
            if (vals[r][c] == val)
                return true;
        }
        return false;
    }

    public final boolean rowContains(int r, int val) {
        for (int c = 0; c < 9; c++)
        {
            if(vals[r][c] == val)
                return true;
        }
        return false;
    }

    private void CheckSolution() {
        // If played by hand, need to grab vals
        board.updateVals(vals);

        /*for(int i=0; i<9; i++){
            for(int j=0; j<9; j++)
                System.out.print(vals[i][j]+" ");
            System.out.println();
        }*/
        
        for (int v = 1; v <= 9; v++){
            // Every row is valid
            for (int r = 0; r < 9; r++)
            {
                if (!rowContains(r,v))
                {
                    board.showMessage("Value "+v+" missing from row: " + (r+1));// + " val: " + v);
                    return;
                }
            }
            // Every column is valid
            for (int c = 0; c < 9; c++)
            {
                if (!colContains(c,v))
                {
                    board.showMessage("Value "+v+" missing from column: " + (c+1));// + " val: " + v);
                    return;
                }
            }
            // Every block is valid
            for (int r = 0; r < 3; r++){
                for (int c = 0; c < 3; c++){
                    if(!blockContains(r, c, v))
                    {
                        return;
                    }
                }
            }
        }
        board.showMessage("Success!");
    }

    

    /// ---- GUI + APP Code --- ////
    /// ----   DO NOT EDIT  --- ////
    enum algorithm {
        AC3, Custom
    }
    class Arc implements Comparable<Object>{
        int Xi, Xj;
        public Arc(int cell_i, int cell_j){
            if (cell_i == cell_j){
                try {
                    throw new Exception(cell_i+ "=" + cell_j);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            Xi = cell_i;      Xj = cell_j;
        }

        public int compareTo(Object o){
            return this.toString().compareTo(o.toString());
        }

        public String toString(){
            return "(" + Xi + "," + Xj + ")";
        }
    }

    enum difficulty {
        easy, medium, hard, random
    }

    public void actionPerformed(ActionEvent e){
        String label = ((JButton)e.getSource()).getText();
        if (label.equals("AC-3"))
            init();
        else if (label.equals("Clear"))
            board.Clear();
        else if (label.equals("Check"))
            CheckSolution();
            //added
        else if(label.equals("Custom"))
            customSolver();
    }

    public void run() {
        board = new Board(gui,this);
        
        long start=0, end=0;
       
        while(!initialize());
        if (gui)
            board.initVals(vals);
        else {
            board.writeVals();
            System.out.println("Algorithm: " + alg);
            switch(alg) {
                default:
                case AC3:
                    start = System.currentTimeMillis();
                    init();
                    end = System.currentTimeMillis();
                    break;
                case Custom: //added
                    start = System.currentTimeMillis();
                    customSolver();
                    end = System.currentTimeMillis();
                    break;
            }
            
            CheckSolution();
            
            if(!gui)
                System.out.println("time to run: "+(end-start));
        }
    }

    public final boolean initialize(){
        switch(level) {
            case easy:
                vals[0] = new int[] {0,0,0,1,3,0,0,0,0};
                vals[1] = new int[] {7,0,0,0,4,2,0,8,3};
                vals[2] = new int[] {8,0,0,0,0,0,0,4,0};
                vals[3] = new int[] {0,6,0,0,8,4,0,3,9};
                vals[4] = new int[] {0,0,0,0,0,0,0,0,0};
                vals[5] = new int[] {9,8,0,3,6,0,0,5,0};
                vals[6] = new int[] {0,1,0,0,0,0,0,0,4};
                vals[7] = new int[] {3,4,0,5,2,0,0,0,8};
                vals[8] = new int[] {0,0,0,0,7,3,0,0,0};
                break;
            case medium:
                vals[0] = new int[] {0,4,0,0,9,8,0,0,5};
                vals[1] = new int[] {0,0,0,4,0,0,6,0,8};
                vals[2] = new int[] {0,5,0,0,0,0,0,0,0};
                vals[3] = new int[] {7,0,1,0,0,9,0,2,0};
                vals[4] = new int[] {0,0,0,0,8,0,0,0,0};
                vals[5] = new int[] {0,9,0,6,0,0,3,0,1};
                vals[6] = new int[] {0,0,0,0,0,0,0,7,0};
                vals[7] = new int[] {6,0,2,0,0,7,0,0,0};
                vals[8] = new int[] {3,0,0,8,4,0,0,6,0};
                break;
            case hard:
                vals[0] = new int[] {1,2,0,4,0,0,3,0,0};
                vals[1] = new int[] {3,0,0,0,1,0,0,5,0};  
                vals[2] = new int[] {0,0,6,0,0,0,1,0,0};  
                vals[3] = new int[] {7,0,0,0,9,0,0,0,0};    
                vals[4] = new int[] {0,4,0,6,0,3,0,0,0};    
                vals[5] = new int[] {0,0,3,0,0,2,0,0,0};    
                vals[6] = new int[] {5,0,0,0,8,0,7,0,0};    
                vals[7] = new int[] {0,0,7,0,0,0,0,0,5};    
                vals[8] = new int[] {0,0,0,0,0,0,0,9,8};  
                break;
            case random:
            default:
                ArrayList<Integer> preset = new ArrayList<Integer>();
                while (preset.size() < numCells)
                {
                    int r = rand.nextInt(81);
                    if (!preset.contains(r))
                    {
                        preset.add(r);
                        int x = r / 9;
                        int y = r % 9;
                        if (!assignRandomValue(x, y))
                            return false;
                    }
                }
                break;
        }
        return true;
    }

    public final boolean assignRandomValue(int x, int y){
        ArrayList<Integer> pval = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));

        while(!pval.isEmpty()){
            int ind = rand.nextInt(pval.size());
            int i = pval.get(ind);
            if (valid(x,y,i)) {
                vals[x][y] = i;
                return true;
            } else
                pval.remove(ind);
        }
        System.err.println("No valid moves exist.  Recreating board.");
        for (int r = 0; r < 9; r++){
            for(int c=0;c<9;c++){
                vals[r][c] = 0;
            }    }
        return false;
    }

    private void Finished(boolean success){
        if(success) {
            board.writeVals();
            board.showMessage("Solved in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
        } else {
            board.showMessage("No valid configuration found in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
        }
    }

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

        char c='*';


        while(c!='e'&& c!='m'&&c!='n'&&c!='h'&&c!='r'){
            c = scan.nextLine().charAt(0);

            if(c=='e')
                level = difficulty.valueOf("easy");
            else if(c=='m')
                level = difficulty.valueOf("medium");
            else if(c=='h')
                level = difficulty.valueOf("hard");
            else if(c=='r')
                level = difficulty.valueOf("random");
            else{
                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
            }
            //System.out.println("2: "+c+" "+level);
        }

        System.out.println("Gui? y or n ");
        c=scan.nextLine().charAt(0);

        if (c=='n')
            gui = false;
        else
            gui = true;

        //System.out.println("c: "+c+", Difficulty: " + level);

        //System.out.println("Difficulty: " + level);

        if(!gui){
            System.out.println("Algorithm? AC3 (1) or Custom (2)");
            if(scan.nextInt()==1)
                alg = algorithm.valueOf("AC3");
            else
                alg = algorithm.valueOf("Custom");
        }

        SudokuPlayer app = new SudokuPlayer();
       
        app.run();
      
    }


    class Board {
        GUI G = null;
        boolean gui = true;

        public Board(boolean X, SudokuPlayer s) {
            gui = X;
            if (gui)
                G = new GUI(s);
        }

        public void initVals(int[][] vals){
            G.initVals(vals);
        }

        public void writeVals(){
            if (gui)
                G.writeVals();
            else {
                for (int r = 0; r < 9; r++) {
                    if (r % 3 == 0)
                        System.out.println(" ----------------------------");
                    for (int c = 0; c < 9; c++) {
                        if (c % 3 == 0)
                            System.out.print (" | ");
                        if (vals[r][c] != 0) {
                            System.out.print(vals[r][c] + " ");
                        } else {
                            System.out.print("_ ");
                        }
                    }
                    System.out.println(" | ");
                }
                System.out.println(" ----------------------------");
            }
        }

        public void Clear(){
            if(gui)
                G.clear();
        }

        public void showMessage(String msg) {
            if (gui)
                G.showMessage(msg);
            System.out.println(msg);
        }

        public void updateVals(int[][] vals){
            if (gui)
                G.updateVals(vals);
        }

    }

    class GUI {
        // ---- Graphics ---- //
        int size = 40;
        JFrame mainFrame = null;
        JTextField[][] cells;
        JPanel[][] blocks;

        public void initVals(int[][] vals){
            // Mark in gray as fixed
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (vals[r][c] != 0) {
                        cells[r][c].setText(vals[r][c] + "");
                        cells[r][c].setEditable(false);
                        cells[r][c].setBackground(Color.lightGray);
                    }
                }
            }
        }

        public void showMessage(String msg){
            JOptionPane.showMessageDialog(null,
                    msg,"Message",JOptionPane.INFORMATION_MESSAGE);
        }

        public void updateVals(int[][] vals) {

           // System.out.println("calling update");
            for (int r = 0; r < 9; r++) {
                for (int c=0; c < 9; c++) {
                    try {
                        vals[r][c] = Integer.parseInt(cells[r][c].getText());
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        showMessage("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        return;
                    }
                }
            }
        }

        public void clear() {
            for (int r = 0; r < 9; r++){
                for (int c = 0; c < 9; c++){
                    if (cells[r][c].isEditable())
                    {
                        cells[r][c].setText("");
                        vals[r][c] = 0;
                    } else {
                        cells[r][c].setText("" + vals[r][c]);
                    }
                }
            }
        }

        public void writeVals(){
            for (int r=0;r<9;r++){
                for(int c=0; c<9; c++){
                    cells[r][c].setText(vals[r][c] + "");
                }   }
        }

        public GUI(SudokuPlayer s){

            mainFrame = new javax.swing.JFrame();
            mainFrame.setLayout(new BorderLayout());
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel gamePanel = new javax.swing.JPanel();
            gamePanel.setBackground(Color.black);
            mainFrame.add(gamePanel, BorderLayout.NORTH);
            gamePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gamePanel.setLayout(new GridLayout(3,3,3,3));

            blocks = new JPanel[3][3];
            for (int i = 0; i < 3; i++){
                for(int j =2 ;j>=0 ;j--){
                    blocks[i][j] = new JPanel();
                    blocks[i][j].setLayout(new GridLayout(3,3));
                    gamePanel.add(blocks[i][j]);
                }
            }

            cells = new JTextField[9][9];
            for (int cell = 0; cell < 81; cell++){
                int i = cell / 9;
                int j = cell % 9;
                cells[i][j] = new JTextField();
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setSize(new java.awt.Dimension(size, size));
                cells[i][j].setPreferredSize(new java.awt.Dimension(size, size));
                cells[i][j].setMinimumSize(new java.awt.Dimension(size, size));
                blocks[i/3][j/3].add(cells[i][j]);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout());
            mainFrame.add(buttonPanel, BorderLayout.SOUTH);
            //JButton DFS_Button = new JButton("DFS");
            //DFS_Button.addActionListener(s);
            JButton AC3_Button = new JButton("AC-3");
            AC3_Button.addActionListener(s);
            JButton Clear_Button = new JButton("Clear");
            Clear_Button.addActionListener(s);
            JButton Check_Button = new JButton("Check");
            Check_Button.addActionListener(s);
            //buttonPanel.add(DFS_Button);
            JButton Custom_Button = new JButton("Custom");
            Custom_Button.addActionListener(s);
            //added
            buttonPanel.add(AC3_Button);
            buttonPanel.add(Custom_Button);
            buttonPanel.add(Clear_Button);
            buttonPanel.add(Check_Button);






            mainFrame.pack();
            mainFrame.setVisible(true);

        }
    }

    Random rand = new Random();

    // ----- Helper ---- //
    static algorithm alg = algorithm.AC3;
    static difficulty level = difficulty.easy;
    static boolean gui = true;
    static int ops;
    static int recursions;
    static int numCells = 15;
    static DecimalFormat myformat = new DecimalFormat("###,###");
}