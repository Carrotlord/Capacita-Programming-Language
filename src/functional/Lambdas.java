package functional;

/**
 * Functions with some number of arguments.
 * @author Jiangcheng Oliver Chu
 */
public class Lambdas {
    public static abstract class Lambda<R> implements Function {
        public abstract R run();
    }

    public static abstract class OneArgLambda<R, A> implements Function {
        public abstract R run(A a);
    }
    
    public static abstract class TwoArgLambda<R, A, B> implements Function {
        public abstract R run(A a, B b);
    }
    
    public static abstract class VarArgLambda<R, T extends TupleCell>
           implements Function {
        public abstract R run(T t);
    }
}
