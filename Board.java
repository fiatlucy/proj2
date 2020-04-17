package tablut;
import java.util.Formatter;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;
import java.util.List;
import static tablut.Piece.*;
import static tablut.Square.*;

/** The state of a Tablut Game.
 *  @author Lucy Chen
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Return the locations of all pieces on SIDE. */
    private Square[] _thrones = { sq(4, 4), sq(4, 5), sq(4, 3),
            sq(4, 3), sq(3, 4), sq(5, 4) };

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        _board = new Piece[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                put(model.board()[i][j], sq(i, j));
            }
        }
        _moveCount = model._moveCount;
        _history = copyHistory(model._history);
        _winner = null;
        _moveLim = model._moveLim;
        _kingPosition = sq(model.kingPosition().col(),
                model.kingPosition().row());
        _turn = model._turn;
        _repeated = model._repeated;
    }

    /** Undo one move.  Has no effect on the initial board.
     * @param h dfdbdfb
     * @return Stack<String> eggfv*/
    Stack<String> copyHistory(Stack<String> h) {
        Stack<String> now =  new Stack<String>();

        for (int i = 0; i < h.size(); i++) {
            now.push(h.get(i));
        }
        return now;
    }

    /** Clears the board to the initial position. */
    void init() {
        _board = new Piece[SIZE][SIZE];
        _legalMoves = new ArrayList<Move>();
        _history = new Stack<String>();
        _winner = null;
        _kingPosition = THRONE;
        _moveCount = 0;
        _repeated = false;
        _moveLim = Integer.MAX_VALUE;
        _turn = BLACK;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (Arrays.asList(INITIAL_ATTACKERS).contains(sq(i, j))) {
                    _board[i][j] = BLACK;
                } else if (Arrays.asList(INITIAL_DEFENDERS).contains(sq(i, j))
                        || Arrays.asList(INITIAL_DEFENDERS).contains(sq(i, j))
                        || Arrays.asList(_thrones).contains(sq(i, j))) {
                    if (sq(i, j).equals(kingPosition())) {
                        _board[i][j] = KING;
                    } else {
                        _board[i][j] = WHITE;
                    }
                } else {
                    _board[i][j] = EMPTY;
                }
            }
        }
        _shistory = new ArrayList<String>();
        _shistory.add(encodedBoard().substring(1));
    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * @param n */
    void setMoveLimit(int n) {
        _moveLim = n;
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        String now = encodedBoard();
        if (checkhistory(now)) {
            _repeated = true;
            _winner = _turn;
        }
        _history.push(encodedBoard());
    }

    /** Undo one move.  Has no effect on the initial board.
     * @param s dfdfv
     * @return boolean dfgdfvd*/
    boolean checkhistory(String s) {
        for (int i = 0; i < _history.size(); i++) {
            if (_history.get(i).equals(s)) {
                return true;
            }
        }
        return false;
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        return _kingPosition;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _board[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(row - '1', col - 'a');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        _board[s.col()][s.row()] = p;
        if (p == KING) {
            _kingPosition = s;
            if (s.isEdge()) {
                _winner = WHITE;
            }
        }

    }

    /** Undo one move.  Has no effect on the initial board.
     * @param i dfvfdv*/
    void addMoveCount(int i) {
        _moveCount = i;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        put(p, s);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        if (from.isRookMove(to) && get(to) == EMPTY) {
            int dir = from.direction(to);
            SqList L = ROOK_SQUARES[from.index()][dir];
            for (int i = 1; i < L.size(); i++) {
                Square check = from.rookMove(dir, i);
                if (check.equals(to) && get(to) == EMPTY) {
                    break;
                } else if (get(check) != EMPTY) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /** Undo one move.  Has no effect on the initial board. */
    void addHistory() {
        _history.push(encodedBoard());
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        boolean check = (get(from) == _turn);
        if (get(from) == KING) {
            return _turn == WHITE;
        }
        return check;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        if (isLegal(from)) {
            if (isUnblockedMove(from, to)) {
                if (get(from) != KING && to == THRONE) {
                    return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        boolean check = isLegal(move.from(), move.to());
        return check;
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        Piece f = _board[from.col()][from.row()];
        checkRepeated();
        _moveCount += 1;
        _posCapture = new ArrayList<Square>();
        _board[from.col()][from.row()] = EMPTY;
        revPut(f, to);
        HashSet<Square> here = pieceLocations(get(to));
        for (int i = 0; i < 4; i++) {
            Square check = to.rookMove(i, 2);
            if ((here.contains(check) || check == THRONE)
                    && get(to.between(check)) != EMPTY
                    && checkCapture(to, check)) {
                capture(to, check);
                if (get(to.between(check)) == KING) {
                    _winner = BLACK;
                }
            }
        }
        _turn = f.opponent();
        if (checkhistory(encodedBoard())) {
            _winner = _turn;
        }
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** True when current board is a repeated position (ending the game).
     * @param sq0 bkj,
     * @param sq2 ;k,l'
     * @return boolean*/
    private boolean checkCapture(Square sq0, Square sq2) {
        Square s1 = sq0.between(sq2);
        if (get(s1) != KING) {
            if (isHostile(sq0, get(s1)) && isHostile(sq2, get(s1))) {
                return true;
            } else if (get(s1) == WHITE && THRONE == _kingPosition) {
                if ((sq0 == THRONE && get(sq2).side() == BLACK
                        && get(sq2.diag1(sq0)) == BLACK
                        && get(sq2.diag2(sq0)) == BLACK)
                        || (sq2 == THRONE && get(sq0).side() == BLACK
                        && get(sq0.diag1(sq2)) == BLACK
                        && get(sq0.diag2(sq2)) == BLACK)) {
                    return true;
                }
            }
        } else {
            if (Arrays.asList(_thrones).contains(kingPosition())
                    && isHostile(kingPosition().rookMove(0, 1), WHITE)
                    && isHostile(kingPosition().rookMove(1, 1), WHITE)
                    && isHostile(kingPosition().rookMove(2, 1), WHITE)
                    && isHostile(kingPosition().rookMove(3, 1), WHITE)) {
                return true;
            } else if (!Arrays.asList(_thrones).contains(kingPosition())
                    && isHostile(sq0, get(s1)) && isHostile(sq2, get(s1))) {
                return true;
            }
        }
        return false;
    }

    /** Move FROM-TO, assuming this is a legal move.
     * @param s dfdf
     * @param p dfdf
     * @return boolean */
    private boolean isHostile(Square s, Piece p) {
        if ((s == THRONE && THRONE != kingPosition())
                || get(s).side() == p.opponent()) {
            return true;
        }
        return false;
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Square s1 = sq0.between(sq2);
        if (get(s1) == KING) {
            _winner = BLACK;
        }
        _board[s1.col()][s1.row()] = EMPTY;
    }

    /** Undo one move.  Has no effect on the initial board.
     * @return Stack<String> fgb*/
    Stack<String> history() {
        return _history;
    }

    /** Undo one move.  Has no effect on the initial board. */
    void setTurn() {
        _turn = _turn.opponent();
    }

    /** Undo one move.  Has no effect on the initial board.
     * dfdf.
     * @return int gfbv*/
    int size() {
        return _history.size();
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            boolean king = false;
            String prev = _history.pop();
            _moveCount -= 1;
            _board = new Piece[SIZE][SIZE];
            _winner = null;
            _turn = getPiece(Character.toString(prev.charAt(0)));
            for (int i = 0; i < prev.length() - 1; i++) {
                String symbol = Character.toString(prev.charAt(i + 1));
                if (symbol.equals("K")) {
                    _kingPosition = sq(i % SIZE, i / SIZE);
                    king = true;
                }
                put(getPiece(symbol), sq(i % SIZE, i / SIZE));
            }
            if (!king) {
                _winner = BLACK;
                return;
            }
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        if (!_repeated || _moveCount > 0) {
            _history.pop();
        }
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        Iterator<Square> check = pieceLocations(side).iterator();
        List<Move> here = new ArrayList<Move>();
        while (check.hasNext()) {
            Square now = check.next();
            for (int i = 0; i < 4; i++) {
                int idx = now.index();
                Move.MoveList save = Move.ROOK_MOVES[idx][i];
                for (int j = 0; j < save.size(); j++) {
                    if (isLegal(save.get(j))) {
                        here.add(save.get(j));
                    }
                }
            }
        }
        return here;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        if (legalMoves(side).size() != 0) {
            return true;
        }
        _winner = side.opponent();
        return false;
    }

    /** Undo one move.  Has no effect on the initial board. */
    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE.
     * @param n jkn*/
    Piece getPiece(String n) {
        if (n.equals("W")) {
            return WHITE;
        } else if (n.equals("B")) {
            return BLACK;
        } else if (n.equals("K")) {
            return KING;
        } else {
            return EMPTY;
        }
    }

    /** Undo one move.  Has no effect on the initial board.
     * @param side rgerg.
     * @return dfvdfv*/
    int getPieceLocations(Piece side) {
        return pieceLocations(side).size();
    }

    /** Return the locations of all pieces on SIDE. */
    private HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> now = new HashSet<Square>();
        for (int i = 0; i < SQUARE_LIST.size(); i++) {
            if (get(SQUARE_LIST.get(i)).side() == side.side()
                    || (get(SQUARE_LIST.get(i)).side() == KING
                    && side.side() == WHITE)) {
                now.add(SQUARE_LIST.get(i));
            }
        }

        return now;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Undo one move.  Has no effect on the initial board.
     * @return Piece[][]*/
    Piece[][] board() {
        return _board;
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;
    /** True when current board is a repeated position (ending the game). */
    private int _moveLim;
    /** True when current board is a repeated position (ending the game). */
    private Square _kingPosition;
    /** True when current board is a repeated position (ending the game). */
    private HashSet<Square> _sideLocations;
    /** True when current board is a repeated position (ending the game). */
    private List<Move> _legalMoves;
    /** True when current board is a repeated position (ending the game). */
    private Stack<String> _history;
    /** True when current board is a repeated position (ending the game). */
    private ArrayList<Square> _posCapture;
    /** True when current board is a repeated position (ending the game). */
    private Piece[][] _board;
    /** Undo one move.  Has no effect on the initial board. */
    private ArrayList<String> _shistory;
}
