package de.jose.chess;

import java.lang.reflect.Constructor;

public interface MatSignature extends Cloneable
{
    Object clone();
    MatSignature cloneReversed();

    boolean canReach(MatSignature sig);

    void init(long wshiteSignature, long blackSignature);
    void clear();

    void setBoard(Board board);
    void update(Board board, Move move);
    void setInitial();

    long getWhiteSignature();
    long getBlackSignature();


    /**
     * factory method
     * @return
     */
    static MatSignature newMatSignature(Class clazz)
    {
        try {
            Constructor ctor = clazz.getConstructor();
            return (MatSignature)ctor.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
