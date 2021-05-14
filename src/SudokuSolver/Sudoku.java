package SudokuSolver;

import java.util.*;

public class Sudoku {

    /**
     * Instance variables
     */

    //Size is the dimension of 1 subgrid, N is the dimension of the whole puzzle ( = SIZE*SIZE)
    //For example, a 9x9 grid has SIZE = 3 and N = 9
    public int SIZE, N;

    //The sudoku grid we are trying to solve
    public int grid[][];

    //ArratList that stores all the solutions if there are multiple
    public ArrayList<Sudoku> solutions = new ArrayList<>();

    // Field that stores the number masks of the numbers that can fit in the sudoku (from 1 to N)
    // The element at index i corresponds to the bitmask of the number i+1
    private static int[] numberMasks;

    // Matrix that stores which numbers are allowed in the matrix for each position in the form of a bitfield
    // Each position has a bitfield which states which numbers are allowed (eg if 000010101 -> 1 allowed, 2 not, 3 allowed...)
    // Note that the bits represent the numbers from N to 1 if we read from left to right -> start reading at the right for 1 to N
    private int[][] possibilitiesMatrix;


    /**
     * Public methods (Solve, constructor)
     */

    //Public solve method that takes in a boolean depending on whether we want every solution or not
    //It stores all the solutions in the "solutions" ArrayList if passed true (Calls separate helper methods for each case)
    public void solve(boolean allSolutions) {
        initPossibilitiesMatrix(); //Initializing the possibilities matrix
        if(allSolutions){
            solveMultiple(grid, possibilitiesMatrix);
            if(solutions.size() > 0) this.grid = solutions.get(0).grid; //If we found at least 1 solution, assign the first one to the grid
        }
        else solveUnique(grid, possibilitiesMatrix);
    }

    //Public constructor that initializes the grid to all 0s, creates the number masks, and intializes the possibilties matrix to hold all ones
    public Sudoku(int size) {

        //Initializing the size variables
        SIZE = size;
        N = size*size;

        //Initializing the grid variable
        grid = new int[N][N];
        for( int i = 0; i < N; i++ )
            for( int j = 0; j < N; j++ )
                grid[i][j] = 0;

        //Initializing the number masks array
        numberMasks = new int[N];
        int allOnes = 0;
        for (int i = 0; i < N; i++) {
            numberMasks[i] = (1 << i);
            allOnes += (1 << i); //A number that has N 1s to represent that all numbers can be put in that spot
        }

        //Initializing the possibilities matrix with all numbers possible
        possibilitiesMatrix = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                possibilitiesMatrix[i][j] = allOnes;
            }
        }
    }

    /**
     * Private helper methods
     */

    //Private constructor that creates a new ChessSudoku object given a grid (deep copy)
    //Used to store new solutions we reach in the solutions ArrayList in case we want all solutions
    private Sudoku(int SIZE, int[][] grid){

        this.SIZE = SIZE;
        this.N = SIZE*SIZE;

        this.grid = new int[N][N];
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                this.grid[i][j] = grid[i][j];
            }
        }
    }

    //Method that initializes the possibilities matrix according to what is already in the grid at the beginning
    private void initPossibilitiesMatrix() {

        for (int i = 0; i < N; i++) { //Checking pre-set numbers and modifying the possibilities matrix accordingly
            for (int j = 0; j < N; j++) {
                if (grid[i][j] != 0) {
                    applyMask(grid[i][j], i, j, possibilitiesMatrix); //Modifying possibilities matrix accordingly
                    possibilitiesMatrix[i][j] = 0; //No more values allowed in that square
                }
            }
        }

    }

    //Method that applies the mask of the input number to a given row, column, subgrid
    private void applyMask(int value, int row, int column, int[][] possibilitiesMatrix) {

        //Creating the mask
        int mask = ~numberMasks[value - 1];

        //Applying the mask to the row / column
        for (int i = 0; i < N; i++) {
            possibilitiesMatrix[i][column] &= mask;
            possibilitiesMatrix[row][i] &= mask;
        }

        //Applying the mask to the subgrid
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                possibilitiesMatrix[row / SIZE * SIZE + i][column / SIZE * SIZE + j] &= mask;
            }
        }

    }

    //Method that uses iteration as well as recursion to solve for a unique solution (stops when it reaches one)
    private boolean solveUnique(int[][] grid, int[][] possibilitiesMatrix) {

        //Variable that keeps track of how many changes we performed in the iterative part of the method
        int numOfChanges;

        //Iterative part of the method that keeps on checking for unique possibility spots or spots that have their neighbors
        //which all cannot take on a designated number and adds them without recursion to save a lot of time and memory. It keeps
        //going until the iterations change less than a designated threshold of numbers, after which recursion becomes more efficient
        //Note that 3 of the 4 tests have shown that they slow down the solving process on 3x3s (overkill) so only used for bigger matrices
        do {
            numOfChanges = 0;
            numOfChanges += insertUniquePossibilities(grid, possibilitiesMatrix);
            if(SIZE>3) {
                numOfChanges += insertNoOtherSpotSubmatrix(grid, possibilitiesMatrix);
                numOfChanges += insertNoOtherSpotRowCol(grid, possibilitiesMatrix);
                filterRowCol(possibilitiesMatrix);
                filterPairs(possibilitiesMatrix);
            }
        } while (numOfChanges >= 4);

        //Recursive part of the method that adds the next legal number at the next empty spots with minimum possibilities left.
        //This is the so called brute force approach with backtracking. Note that we create 2 new matrices to copy the possibilities
        //matrix and grid to them. If the last bruteforced number leads to a solution, we assign the instance variable grid that
        //solution and let the parent know by returning true. However, if it leads to a dead end, we just discard that matrix (here
        //we see how creating a new matrix saved us the time of undoing all the changes we made to the possibilities matrix)
        int[] bestSpot = getMinimumPossibilities(grid, possibilitiesMatrix); //Finding the best spot

        if (bestSpot[0] == -2) { //If all spots are taken, it means we have a solution -> assign it and let the parent know
            this.grid = grid;
            return true;
        }

        else if (bestSpot[0] == -1) { //If some spots have no possibilities, it means we have a dead end -> let the parent know
            return false;
        }

        else { //If we found a best spot (no solution / dead end), we try the first legal number on that spot and recurse

            for (int number = 1; number <= N; number++) {

                if ((possibilitiesMatrix[bestSpot[0]][bestSpot[1]] & numberMasks[number - 1]) > 0) { //If legal to put that value there

                    //Making copies of the grid and possibilities matrix
                    int[][] gridCopy = copyGrid(grid);
                    int[][] possibilitiesMatrixCopy = copyGrid(possibilitiesMatrix);

                    //Changing the number
                    assignNumber(number, bestSpot[0], bestSpot[1], gridCopy, possibilitiesMatrixCopy);

                    //If that leads to a solution let the parent know
                    if (solveUnique(gridCopy, possibilitiesMatrixCopy))
                        return true;
                }

            }
            return false; //The only way to get here is by reaching a dead end (empty spot but nothing to fill it with as every number led to a dead end)
        }
    }

    //Method that uses iteration as well as recursion to solve for multiple solutions (keeps going)
    private void solveMultiple(int[][] grid, int[][] possibilitiesMatrix) {

        //Variable that keeps track of how many changes we performed in the iterative part of the method
        int numOfChanges;

        //Iterative part of the method that keeps on checking for unique possibility spots or spots that have their neighbors
        //which all cannot take on a designated number and adds them without recursion to save a lot of time and memory. It keeps
        //going until the iterations change less than a designated threshold of numbers, after which recursion becomes more efficient
        //Note that 3 of the 4 tests have shown that they slow down the solving process on 3x3s (overkill) so only used for bigger matrices
        do {
            numOfChanges = 0;
            numOfChanges += insertUniquePossibilities(grid, possibilitiesMatrix);
            if(SIZE>3) {
                numOfChanges += insertNoOtherSpotSubmatrix(grid, possibilitiesMatrix);
                numOfChanges += insertNoOtherSpotRowCol(grid, possibilitiesMatrix);
                filterRowCol(possibilitiesMatrix);
                filterPairs(possibilitiesMatrix);
            }
        } while (numOfChanges >= 4);

        //Recursive part of the method that adds the next legal number at the next empty spots with minimum possibilities left.
        //This is the so called brute force approach with backtracking. Note that we create 2 new matrices to copy the possibilites
        //matrix and grid to them. If the last bruteforced number leads to a solution, we add that solution to the ArrayList of solutions
        //and try other possibilities (can't stop like unique). However, if it leads to a dead end, we just discard that matrix (here
        //we see how creating a new matrix saved us the time of undoing all the changes we made to the possibilities matrix)
        int[] bestSpot = getMinimumPossibilities(grid, possibilitiesMatrix); //Finding the best spot

        if (bestSpot[0] == -2) { //If all spots are taken, it means we have a solution -> add it to ArrayList and let the parent know
            Sudoku solution = new Sudoku(SIZE, grid);
            solutions.add(solution);
            return;
        }

        else if (bestSpot[0] == -1) { //If some spots have no possibilities, it means we have a dead end -> no adding the solution
            return;
        }

        else { //If we found a best spot (no solution / dead end), we try the first legal number on that spot and recurse

            for (int number = 1; number <= N; number++) {

                if ((possibilitiesMatrix[bestSpot[0]][bestSpot[1]] & numberMasks[number - 1]) > 0) { //If legal to put that value there

                    //Making copies of the grid and possibilities matrix
                    int[][] gridCopy = copyGrid(grid);
                    int[][] possibilitiesMatrixCopy = copyGrid(possibilitiesMatrix);

                    //Changing the number
                    assignNumber(number, bestSpot[0], bestSpot[1], gridCopy, possibilitiesMatrixCopy);

                    //Recurse
                    solveMultiple(gridCopy, possibilitiesMatrixCopy);
                }
            }
            return; //The only way to get here is by reaching a dead end -> nothing to do as we want all solutions so just discard matrix
        }
    }

    //Method that checks for spots that have unique solutions and adds them to the grid passed as input
    private int insertUniquePossibilities(int[][] grid, int[][] possibilitiesMatrix){

        int numOfChanges = 0;

        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                int uniqueNumber = checkUniqueOne(possibilitiesMatrix[i][j]);
                if(uniqueNumber != -1){
                    assignNumber(uniqueNumber, i, j, grid, possibilitiesMatrix);
                    numOfChanges++;
                }
            }
        }

        return numOfChanges;
    }

    //Method that checks for spots that have a number which cannot be put elsewhere in the same submatrix
    //It thus must belong to the only spot in which we can add it (called during the iterative part of the algo)
    private int insertNoOtherSpotSubmatrix(int[][] grid, int[][] possibilitiesMatrix){

        //To keep track of the number of changes
        int numOfChanges = 0;

        //Iterating over every subsection
        for(int sectionX = 0; sectionX < SIZE; sectionX++){
            for(int sectionY = 0; sectionY < SIZE; sectionY++){

                //For every subsection, iterating over every number
                for(int number = 1; number <= N; number++){

                    int mask = numberMasks[number-1]; //Mask to check if the number we are at is a possibility
                    int chosenX = -1, chosenY = -1; //To keep track of when we find a unique spot

                    submatrix:
                    //For every subsection and every number, iterating over every spot in the subsection
                    for(int x = 0; x < SIZE; x++){
                        for(int y = 0; y < SIZE; y++) {

                            //Checking if it is possible to put the number there
                            if((possibilitiesMatrix[sectionX * SIZE + x][sectionY * SIZE + y] & mask) > 0){
                                if(chosenX < 0){
                                    chosenX = x;
                                    chosenY = y;
                                }
                                else {
                                    chosenX = -1; //Spot possible in more than 1 place in the same submatrix
                                    break submatrix; //Breaking both loops
                                }
                            }
                        }
                    }
                    //If the number can only be added at a unique place (and if we reached a unique place), we add it
                    if(chosenX != -1){
                        assignNumber(number, sectionX * SIZE + chosenX, sectionY * SIZE + chosenY, grid, possibilitiesMatrix);
                        numOfChanges++;
                    }
                }
            }
        }
        return numOfChanges;
    }

    //Method similar to the above one, but for row / column checks: If no other spot in the same row AND column can take on
    //a value that a spot can take on, then that spot MUST take on that value. Again used in the iterative part of the algo.
    private int insertNoOtherSpotRowCol(int[][] grid, int[][] possibilitiesMatrix){

        //To keep track of the number of changes
        int numOfChanges = 0;

        //Iterating over every combination of rows and columns
        for(int row = 0; row < N; row++){
            for(int col = 0; col < N; col++){

                //For every combinations of rows and columns, iterating over every number
                for(int number = 1; number <= N; number++){

                    int mask = numberMasks[number-1]; //Mask to check if the number we are at is a possibility
                    int uniqueX = -1, uniqueY = -1; //To keep track of when we find a unique spot

                    //For every combination of rows and columns and every number, iterating over every spot possible
                    for(int i = 0; i < N; i++){

                        //Checking if it is possible to put the number in the row
                        if((possibilitiesMatrix[row][i] & mask) > 0){
                            if(uniqueX < 0){
                                uniqueX = row;
                                uniqueY = i;
                            }
                            else {
                                uniqueX = -1; //Spot possible in more than 1 place in the same submatrix
                                break; //Breaking both loops
                            }
                        }

                        //Checking if it is possible to put the number in the column
                        if((possibilitiesMatrix[i][col] & mask) > 0){
                            if(uniqueX < 0){
                                uniqueX = i;
                                uniqueY = col;
                            }
                            else {
                                uniqueX = -1; //Spot possible in more than 1 place in the same row-col combination
                                break; //Breaking the loop
                            }
                        }
                    }

                    if(uniqueX != -1){
                        assignNumber(number, uniqueX, uniqueY, grid, possibilitiesMatrix);
                        numOfChanges++;
                    }
                }
            }
        }
        return numOfChanges;
    }

    //Method that checks for a row / column that is the only one in a submatrix that can take on a given value
    //If so, that means that number will be going in that row / col of the submatrix, and we can remove it from the rest of the row / col
    private void filterRowCol(int[][] possibilitiesMatrix){

        //Iterating over every number
        for(int number = 1; number <= N; number++){

            int mask = numberMasks[number-1]; //Mask to check if the number we are at is a possibility

            //For every number, iterating over every submatrix
            for(int sectionX = 0; sectionX < SIZE; sectionX++){
                for(int sectionY = 0; sectionY < SIZE; sectionY++){

                    int uniqueX = -1, uniqueY = -1; //To keep track of when we find a unique number in a row

                    submatrix:
                    //For every number and every submatrix, iterating over every entry and checking for the condition
                    for(int x = 0; x < SIZE; x++){
                        for(int y = 0; y < SIZE; y++){

                            if((possibilitiesMatrix[sectionX*SIZE + x][sectionY*SIZE + y] & mask) > 0){ //If the number is possible there

                                if(uniqueX == -1){ //If no row has been registered yet for the current number
                                    uniqueX = x;
                                }
                                else if(uniqueX != x){ //If the current number was already found in a different row
                                    uniqueX = -2;
                                }

                                if(uniqueY == -1){ //If no column has been registered yet for the current number
                                    uniqueY = y;
                                }
                                else if(uniqueY != y){ //If the current number was already found in a different column
                                    uniqueY = -2;
                                }

                                if(uniqueX == -2 && uniqueY == -2){ //Number found in multiple rows and cols
                                    break submatrix;
                                }
                            }
                        }
                    }

                    if(uniqueX >= 0) { //If we found a unique row in the subsection with that number
                        for (int y = 0; y < N; y++) //Iterate over every y that is not in the subsection
                            if (!(y >= sectionY*SIZE && y < sectionY*SIZE + SIZE)) //Make sure it isn't in the subsection
                                possibilitiesMatrix[sectionX*SIZE + uniqueX][y] &= ~mask; //Apply the mask to remove that number
                    }

                    if(uniqueY >= 0) { //If we found a unique column in the subsection with that number
                        for (int x = 0; x < N; x++) //Iterate over every x that is not in the subsection
                            if (!(x >= sectionX*SIZE && x < sectionX*SIZE + SIZE)) //Make sure it isn't in the subsection
                                possibilitiesMatrix[x][sectionY*SIZE + uniqueY] &= ~mask; //Apply the mask to remove that number
                    }
                }
            }
        }
    }

    //Method that finds pairs in a given row or column for which the same exact 2 numbers are possible
    //If so, it removes those possibilities from all the other cells in that row or column as the number must belong there
    private void filterPairs(int[][] possibilitiesMatrix){

        //Going through the matrix to find such pairs
        for(int row = 0; row < N; row++){
            for(int column = 0; column < N; column++){

                //Extracting the possible values of the current cell
                int bitfield = possibilitiesMatrix[row][column];

                //If we find a given cell with exactly 2 possibilities
                if(countNumOfOnes(bitfield) == 2){

                    //Going through the rest of the row to find another one (can't be before or would have found it already)
                    for(int y = column+1; y < N; y++) {
                        //If we find another cell with the matching 2 values, we remove that value from every cell in the current row
                        if(possibilitiesMatrix[row][y] == bitfield) {
                            for(int k = 0; k < N && k != column && k != y; k++) { //Going through the whole row again (except for the pair of cells found)
                                possibilitiesMatrix[row][k] &= ~bitfield;
                            }
                            break; //If we find a pair, that means that we won't find a third cell in the same row with the same possibilities
                        }
                    }

                    //Going through the rest of the column to find another one (can't be before or would have found it already)
                    for(int x = row+1; x < N; x++){
                        //If we find another cell with the matching 2 values, we remove that value from every cell in the current column
                        if(possibilitiesMatrix[x][column] == bitfield) {
                            for(int k = 0; k < N && k != row && k != x; k++) { //Going through the whole column again (except for the pair of cells found)
                                possibilitiesMatrix[k][column] &= ~bitfield;
                            }
                            break; //If we find a pair, that means that we won't find a third cell in the same column with the same possibilities
                        }
                    }
                }
            }
        }
    }

    // Method that finds the spot in the possibilities matrix with the minimum non zero amount of possibilities left
    // If it finds an empty spot with 0 possibilities left, it returns {-1, -1} indicating that we have a dead end
    private int[] getMinimumPossibilities(int[][] grid, int[][] possibilitiesMatrix){

        //To keep track of the best overall scores
        int bestNumOfPossibilities = Integer.MAX_VALUE;
        int[] bestSpot = {-2, -2}; //Format {x,y} -> Initialized to {-2,-2} so that we return just that if no empty spots and solution found

        findingBest:
        //Looping over the whole possibilities matrix
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){

                //Checking that the current number is 0 as will have 0 possibilities if already assigned
                if(grid[i][j] == 0) {

                    int currentNumOfPossibilities = countNumOfOnes(possibilitiesMatrix[i][j]); //Computing num of possibilities

                    if(currentNumOfPossibilities == 0){ //If we have 0 possibilities, it means that this matrix is a dead end
                        bestSpot[0] = -1;
                        bestSpot[1] = -1;
                        break findingBest; //We set {x,y} to be {-1,-1} and stop here for caller to know and not go deeper
                    }

                    if(currentNumOfPossibilities < bestNumOfPossibilities){ //If we have more than 0 possibilities, update best spot
                        bestNumOfPossibilities = currentNumOfPossibilities;
                        bestSpot[0] = i;
                        bestSpot[1] = j;
                    }

                }
            }
        }
        //Returns the best spot -> If {-2,-2}, it means that we found a solution // If {-1,-1}, it means that we reached a dead end
        return bestSpot;
    }

    //Method that takes a bitfield as input and checks if it has a unique one
    //It returns the unique number possible for that field if that is the case
    private int checkUniqueOne(int bitfield){

        int lastOneNumber = -1;
        int currentNumber = 1; //Starts at 1 to return a number and not an index (index+1)

        while(bitfield > 0){
            if((bitfield & 1) > 0){
                if(lastOneNumber != -1) return -1; //We already had another one
                lastOneNumber = currentNumber;
            }
            bitfield >>>= 1;
            currentNumber++;
        }

        return lastOneNumber;
    }

    //Method that takes a bitfield as input and counts the number of ones that bitfield has
    private int countNumOfOnes(int bitfield){

        int numOfOnes = 0;

        while(bitfield > 0){
            if((bitfield & 1) > 0) numOfOnes++;
            bitfield >>>= 1;
        }

        return numOfOnes;

    }

    //Method that deep copies a grid to a new 2D array and returns it
    private int[][] copyGrid(int[][] grid){

        int[][] gridCopy = new int[N][N];

        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                gridCopy[i][j] = grid[i][j];
            }
        }

        return gridCopy;
    }

    //Method that assigns a new number to a spot in the grid passed as input and modifies the possibilities table passed accordingly
    private void assignNumber(int number, int i, int j, int[][] grid, int[][] possibilitiesMatrix){
        applyMask(number, i, j, possibilitiesMatrix);
        grid[i][j] = number;
        possibilitiesMatrix[i][j] = 0; //No more values are allowed in that square
    }

}
