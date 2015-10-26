package net.ldvsoft.warofviruses;

import java.util.ArrayList;

/**
 * Created by Сева on 21.10.2015.
 */
public class GameLogic {
    public static final int BOARD_SIZE = 10;

    public enum CellType {CROSS, ZERO, DEAD_CROSS, DEAD_ZERO, EMPTY}
    public enum PlayerFigure {CROSS, ZERO, NONE}
    public enum GameState {NOT_RUNNING, RUNNING, DRAW, CROSS_WON, ZERO_WON}

    public static class Cell {
        private CellType cellType = CellType.EMPTY;
        private boolean canMakeTurn = false;
        private boolean isActive = false;

        public Cell() {
        }

        public Cell(CellType cellType, boolean canMakeTurn) {
            this.cellType = cellType;
            this.canMakeTurn = canMakeTurn;
        }

        public Cell(Cell cell) {
            if (cell == null) {
                return;
            }

            cellType = cell.cellType;
            canMakeTurn = cell.canMakeTurn;
            isActive = cell.isActive;
        }

        public PlayerFigure getOwner() {
            if (cellType == CellType.ZERO || cellType == CellType.DEAD_CROSS) {
                return PlayerFigure.ZERO;
            } else if (cellType == CellType.CROSS || cellType == CellType.DEAD_ZERO) {
                return PlayerFigure.CROSS;
            } else {
                return PlayerFigure.NONE;
            }
        }

        public boolean isActive() {
            return isActive;
        }

        public void setOwner(PlayerFigure newOwner) {
            if (newOwner == PlayerFigure.CROSS) {
                cellType = CellType.CROSS;
            } else if (newOwner == PlayerFigure.ZERO) {
                cellType = CellType.ZERO;
            } else {
                cellType = CellType.EMPTY;
            }
        }

        public boolean isDead() {
            return cellType == CellType.DEAD_CROSS || cellType == CellType.DEAD_ZERO;
        }

        public CellType getCellType() {
            return cellType;
        }

        public boolean canMakeTurn() {
            return canMakeTurn;
        }

        public void setOwnerOnDead(PlayerFigure newOwner) {
            if (newOwner == PlayerFigure.CROSS) {
                cellType = CellType.DEAD_ZERO;
            } else {
                cellType = CellType.DEAD_CROSS;
            }
        }
    }

    static final int[][] ADJACENT_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

    private Cell board[][] = new Cell[BOARD_SIZE][BOARD_SIZE];

    private GameState currentGameState = GameState.NOT_RUNNING;
    private PlayerFigure curPlayerFigure = PlayerFigure.CROSS;

    private boolean previousTurnSkipped = false;
    int currentTurn = 0;
    int currentMiniturn = 0;

    public PlayerFigure getCurPlayerFigure() {
        return curPlayerFigure;
    }

    public GameLogic() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell();
            }
        }
    }

    public GameLogic(GameLogic logic) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell(logic.board[i][j]);
            }
        }
        currentMiniturn = logic.currentMiniturn;
        currentTurn = logic.currentTurn;
        previousTurnSkipped = logic.previousTurnSkipped;
        currentGameState = logic.currentGameState;
        curPlayerFigure = logic.curPlayerFigure;
    }

    public void newGame() {
        currentTurn = 0;
        currentMiniturn = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell(CellType.EMPTY, false);
            }
        }
        previousTurnSkipped = false;
        curPlayerFigure = PlayerFigure.CROSS;
        currentGameState = GameState.RUNNING;
        updateGameState();
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    private void updateAdjacentCells(int x, int y) {
        board[x][y].isActive = true;
        for (int[] adjacentDirection : ADJACENT_DIRECTIONS) {
            int dx = x + adjacentDirection[0], dy = y + adjacentDirection[1];
            if (dx >= 0 && dx < BOARD_SIZE && dy >= 0 && dy < BOARD_SIZE) {
                if ((board[dx][dy].getOwner() == getOpponent(curPlayerFigure) && !board[dx][dy].isDead()) ||
                        board[dx][dy].cellType == CellType.EMPTY) {
                    board[dx][dy].canMakeTurn = true;
                }

                if (!board[dx][dy].isActive) {
                    if (board[dx][dy].isDead() && board[dx][dy].getOwner() == curPlayerFigure) {
                        updateAdjacentCells(dx, dy);
                    }
                }
            }
        }
    }

    public Cell getCellAt(int x, int y) {
        return board[x][y];
    }

    public PlayerFigure getOpponent(PlayerFigure curPlayerFigure) {
        switch (curPlayerFigure) {
            case CROSS:
                return PlayerFigure.ZERO;
            case ZERO:
                return PlayerFigure.CROSS;
            default:
                return PlayerFigure.NONE;
        }
    }

    private void updateGameState() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j].canMakeTurn = false;
                board[i][j].isActive = false;
            }
        }

        boolean isCrossAlive = false, isZeroAlive = false;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (!board[i][j].isDead() && board[i][j].cellType != CellType.EMPTY) {
                    if (board[i][j].getOwner() == PlayerFigure.CROSS) {
                        isCrossAlive = true;
                    } else {
                        isZeroAlive = true;
                    }
                }
            }
        }

        if (currentTurn > 1) {
            if (!isCrossAlive) {
                zeroWon();
            }
            if (!isZeroAlive) {
                crossWon();
            }
        }

        //cycle below will work incorrectly in case of curPlayerFigure==NONE
        if (currentGameState != GameState.RUNNING) {
            return;
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (!board[i][j].isActive && board[i][j].getOwner() == curPlayerFigure && !board[i][j].isDead()) {
                    updateAdjacentCells(i, j);
                }
            }
        }

        if (currentTurn == 0 && currentMiniturn == 0) {
            board[0][0].canMakeTurn = true;
        }
        if (currentTurn == 1 && currentMiniturn == 0) {
            board[BOARD_SIZE - 1][BOARD_SIZE - 1].canMakeTurn = true;
        }

    }

    //returns true is can pass turn (it's beginning of player's turn), false otherwise
    //probably it's better just to not allow to press 'pass' button

    public boolean skipTurn() {
        if (currentMiniturn != 0 || currentTurn < 2) {
            return false;
        }

        if (previousTurnSkipped) {
            draw();
            return true;
        }

        previousTurnSkipped = true;
        currentMiniturn = 2;
        passTurn();
        return true;
    }

    //returns true if turn was correct, false otherwise
    public boolean doTurn(int x, int y) {
        if (!board[x][y].canMakeTurn || currentGameState != GameState.RUNNING) {
            return false;
        }

        previousTurnSkipped = false;

        if (board[x][y].cellType != CellType.EMPTY) {
            board[x][y].setOwnerOnDead(curPlayerFigure);
        } else {
            board[x][y].setOwner(curPlayerFigure);
        }

        passTurn();
        return true;
    }

    public boolean giveUp() {
        switch (curPlayerFigure) {
            case CROSS:
                zeroWon();
                return true;
            case ZERO:
                crossWon();
                return true;
            default:
                return false;
        }
    }

    private void draw() {
        currentGameState = GameState.DRAW;
        curPlayerFigure = PlayerFigure.NONE;
        updateGameState();
    }

    private void crossWon() {
        currentGameState = GameState.CROSS_WON;
        curPlayerFigure = PlayerFigure.NONE;
        updateGameState();
    }

    private void zeroWon() {
        currentGameState = GameState.ZERO_WON;
        curPlayerFigure = PlayerFigure.NONE;
        updateGameState();
    }

    private void passTurn() {
        currentMiniturn++;
        if (currentMiniturn == 3 ) {
            currentTurn++;
            currentMiniturn = 0;
            curPlayerFigure = getOpponent(curPlayerFigure);
        }

        updateGameState();

        //to avoid recursion when game is over and both of the players pass turn because they can't make any move
        if (currentGameState != GameState.RUNNING) {
            return;
        }

        //if player can't move at the beginning of his turn, he should manually press "skip turn" button
        if (!canMove() && currentMiniturn != 0) {
            currentMiniturn = 2;
            passTurn();
        }
    }

    //should be used very carefully, as it might broke some game logic.
    //It's public for easier AI implementations
    public void setCurrentPlayerToOpponent() {
        curPlayerFigure = getOpponent(curPlayerFigure);
        updateGameState();
    }

    public static class CoordinatePair {
        int x, y;
        CoordinatePair(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    ArrayList<CoordinatePair> getMoves(){
        ArrayList<CoordinatePair> moves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].canMakeTurn) {
                    moves.add(new CoordinatePair(i, j));
                }
            }
        }
        return moves;
    }

    public boolean canMove() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].canMakeTurn) {
                    return true;
                }
            }
        }
        return false;
    }
}
