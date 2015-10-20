package net.ldvsoft.warofviruses;

import android.util.Log;

/**
 * Created by Сева on 19.10.2015.
 */
public class Game {
    public static final int BOARD_SIZE = 10;

    public static enum CellType {CROSS, ZERO, DEAD_CROSS, DEAD_ZERO, EMPTY};

    public static enum PlayerFigure {CROSS, ZERO, NONE};

    private Cell board[][] = new Cell[BOARD_SIZE][BOARD_SIZE];
    static final int[][] adjacentDirections = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private PlayerFigure curPlayerFigure = PlayerFigure.CROSS;
    int currentTurn = 0;
    int currentMiniturn = 0;

    public static class Cell {
        private CellType cellType = CellType.EMPTY;
        private boolean canMove = false;

        public Cell(CellType cellType, boolean canMove) {
            this.cellType = cellType;
            this.canMove = canMove;
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

        public boolean getCanMove() {
            return canMove;
        }

        public void setOwnerOnDead(PlayerFigure newOwner) {
            if (newOwner == PlayerFigure.CROSS) {
                cellType = CellType.DEAD_ZERO;
            } else {
                cellType = CellType.DEAD_CROSS;
            }
        }
    }

    public PlayerFigure getCurPlayerFigure() {
        return curPlayerFigure;
    }

    public void newGame() {
        currentTurn = 0;
        currentMiniturn = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell(CellType.EMPTY, false);
            }
        }

        curPlayerFigure = PlayerFigure.CROSS;
        updateGameState();
    }

    private void updateAdjacentCells(int x, int y, boolean[][] processed) {
        processed[x][y] = true;
        for (int dir = 0; dir < 4; dir++) {
            int dx = x + adjacentDirections[dir][0], dy = y + adjacentDirections[dir][1];
            if (dx >= 0 && dx < BOARD_SIZE && dy >= 0 && dy < BOARD_SIZE) {
                if ((board[dx][dy].getOwner() == getOpponent(curPlayerFigure) && !board[dx][dy].isDead()) ||
                        board[dx][dy].cellType == CellType.EMPTY) {
                    board[dx][dy].canMove = true;
                }

                if (!processed[dx][dy]) {
                    if (board[dx][dy].isDead() && board[dx][dy].getOwner() == curPlayerFigure) {
                        updateAdjacentCells(dx, dy, processed);
                    }
                }
            }
        }
    }

    public Cell getCellAt(int x, int y) {
        return board[x][y]; //to avoid changing board cell outside of game class
    }

    public PlayerFigure getOpponent(PlayerFigure curPlayerFigure) {
        if (curPlayerFigure == PlayerFigure.CROSS) {
            return PlayerFigure.ZERO;
        } else if (curPlayerFigure == PlayerFigure.ZERO) {
            return PlayerFigure.CROSS;
        } else {
            return PlayerFigure.NONE;
        }
    }

    private void updateGameState() {
        boolean[][] processed = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j].canMove = false;
                processed[i][j] = false;
            }
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (!processed[i][j] && board[i][j].getOwner() == curPlayerFigure && !board[i][j].isDead()) {
                    updateAdjacentCells(i, j, processed);
                }
            }
        }

        if (currentTurn == 0 && currentMiniturn == 0) {
            board[BOARD_SIZE - 1][0].canMove = true;
        }
        if (currentTurn == 1 && currentMiniturn == 0) {
            board[0][BOARD_SIZE - 1].canMove = true;
        }

    }

    public void doTurn(int x, int y) {
        if (!board[x][y].canMove) {
            return;
        }

        if (board[x][y].cellType != CellType.EMPTY) {
            board[x][y].setOwnerOnDead(curPlayerFigure);
        } else {
            board[x][y].setOwner(curPlayerFigure);
        }

        currentMiniturn++;
        if (currentMiniturn == 3) {
            currentTurn++;
            currentMiniturn = 0;
            if (curPlayerFigure == PlayerFigure.CROSS) {
                curPlayerFigure = PlayerFigure.ZERO;
            } else {
                curPlayerFigure = PlayerFigure.CROSS;
            }
        }

        updateGameState();
    }
}
