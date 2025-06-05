package de.jose.chess;

import java.lang.reflect.Constructor;

public interface MatSignature extends Cloneable
{
    boolean equals(Object sig);
    Object clone();
    MatSignature cloneReversed();

    boolean canReach(MatSignature sig);
    //@deprecated might be too epensive
    boolean canReachReversed(MatSignature sig);

    void init(long wshiteSignature, long blackSignature);
    void clear();

    void setBoard(Board board);
    void update(Board board, Move move);
    void setInitial();

    long getWhiteSignature();
    long getBlackSignature();

    default boolean isEmpty() {
        return getBlackSignature()==0 && getWhiteSignature()==0;
    }

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
