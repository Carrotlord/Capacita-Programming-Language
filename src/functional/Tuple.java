package functional;

/**
 * Represents a type (A, B, C, ...) where A, B, C may be distinct.
 * Thanks to:
 * <https://apocalisp.wordpress.com/2008/10/23/
 *  heterogeneous-lists-and-the-limits-of-the-java-type-system/>
 * @author Jiangcheng Oliver Chu
 * @param <A>
 * @param <B>
 */
public class Tuple<A, B extends TupleCell<B>>
             extends TupleCell<Tuple<A, B>> {
    private A first = null;
    private B rest = null;

    private Tuple(A a, B b) {
        first = a;
        rest = b;
    }

    public static <A, B extends TupleCell<B>> Tuple<A, B> cons(A a, B b) {
        return new Tuple<>(a, b);
    }
    
    public static <A> Tuple<A, EmptyCell> cons(A a) {
        return new Tuple<>(a, EMPTY_TUPLE);
    }

    public A car() {
        return first;
    }

    public B cdr() {
        return rest;
    }
}

