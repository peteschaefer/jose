package de.jose.book;

import de.jose.book.crafty.CraftyHashKey;
import de.jose.book.polyglot.PolyglotBook;
import de.jose.book.polyglot.PolyglotHashKey;
import de.jose.book.shredder.ShredderHashKey;
import de.jose.chess.Board;
import de.jose.chess.Position;

public class BookQueryArguments
{
    //  Query arguments. Don't use Position.
    public String fen;
    public PolyglotHashKey[] polyglotHashKeys;
    public CraftyHashKey[] craftyHashKeys;
    public ShredderHashKey[] shredderHashKeys;

    public BookQueryArguments(Position pos)
    {
        fen = pos.toString(Board.XFEN);
        polyglotHashKeys = PolyglotBook.computeHashKeys(pos);
        craftyHashKeys = CraftyBook.computeHashKeys(pos);
        shredderHashKeys = ShredderHashKey.computeHashKeys(pos);
    }
}
