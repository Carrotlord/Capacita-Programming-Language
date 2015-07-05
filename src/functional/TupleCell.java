package functional;

/**
 * A single cell of a tuple.
 * @author Jiangcheng Oliver Chu
 * @param <A>
 */
public abstract class TupleCell<A extends TupleCell<A>> {
    public static final EmptyCell EMPTY_TUPLE = new EmptyCell();
    public static class EmptyCell extends TupleCell<EmptyCell> {}
};
