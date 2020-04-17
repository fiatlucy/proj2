package tablut;


import static java.lang.Math.*;
import static tablut.Piece.*;
import java.util.ArrayList;
import java.util.List;

/** A Player that automatically generates moves.
 *  @author Lucy Chen
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    /** Return either a String denoting either a legal move for me
     *  or another command (which may be invalid).  Always returns the
     *  latter if board().turn() is not myPiece() or if board.winner()
     *  is not null. */
    String myMove() {
        Move m = findMove();
        _controller.reportMove(m);
        if (board().turn() != myPiece() || board().winner() != null) {
            _controller.reportMove(m);
        }
        return m.toString();
    }

    /** Return a heuristic value for BOARD. */
    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        int depth = maxDepth(b);
        int sense;
        _lmove = null;
        if (this.myPiece() == WHITE) {
            sense = 1;
            findMove(new Board(b), depth, true, sense, -INFTY, INFTY);
        } else {
            sense = -1;
            findMove(new Board(b), depth, true, sense, -INFTY, INFTY);
        }

        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH COPY levels.
     * @return int dgdfg
     * Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board copy, int depth,
            boolean saveMove, int sense, int alpha, int beta) {
        Piece now;
        if (sense == 1) {
            now = WHITE;
        } else {
            now = BLACK;
        }
        if (depth == 0 || copy.winner() != null) {
            return staticScore(copy);
        }
        int value = 0;
        if (sense == 1) {
            Board c = new Board(copy);
            List<Move> m = copy.legalMoves(WHITE);
            value = -INFTY;
            for (int i = 0; i < m.size(); i++) {
                if (saveMove) {
                    _lmove = m.get(i);
                }
                Board j = new Board(c);
                j.makeMove(m.get(i));
                int fmove = findMove(j, depth - 1,
                        false, 0 - sense, alpha, beta);
                value = Math.max(value, fmove);
                alpha = Math.max(alpha, value);
                if (fmove >= value && now == myPiece() && saveMove) {
                    _lastFoundMove = m.get(i);
                }
                if (alpha >= beta) {
                    break;
                }
            }
        } else {
            value = INFTY;
            Board c = new Board(copy);
            List<Move> m = c.legalMoves(BLACK);
            for (int i = 0; i < m.size(); i++) {
                Board j = new Board(c);
                if (saveMove) {
                    _lmove = m.get(i);
                }
                j.makeMove(m.get(i));
                int fmove = findMove(j, depth - 1,
                        false, 0 - sense, alpha, beta);
                if (fmove <= value && now == myPiece() && saveMove) {
                    _lastFoundMove = m.get(i);
                }
                value = Math.min(value, fmove);
                beta = min(beta, value);
                j.undo();
                if (alpha >= beta) {
                    break;
                }
            }
        }
        if (saveMove && _lastFoundMove == null) {
            _lastFoundMove = _lmove;
        }
        return value;
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        return 3;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        int white = board.getPieceLocations(WHITE);
        int black = board.getPieceLocations(BLACK);
        if (board.winner() == BLACK || !board.hasMove(WHITE)) {
            return -INFTY;
        } else if (board.winner() == WHITE || !board.hasMove(BLACK)) {
            return INFTY;
        }
        int kingpos = Math.abs(4
                - board.kingPosition().col())
                * Math.abs(4 - board.kingPosition().row());
        return (white - black) * kingpos;
    }

    /** Return a heuristic value for BOARD. */
    private Move _lmove;
    /** Return a heuristic value for BOARD. */
    private ArrayList<ArrayList<Board>> _boards;
    /** Return a heuristic value for BOARD. */
    private ArrayList<ArrayList<Integer>> _ss;
    /** Return a heuristic value for BOARD. */
    private int _currdepth = 0;
    /** Return a heuristic value for BOARD. */
    private int _maxDepth;
}
