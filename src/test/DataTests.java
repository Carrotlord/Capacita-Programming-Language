package test;

import capacita.DenseLinkedList;
import functional.Lambdas.VarArgLambda;
import functional.Tuple;
import functional.TupleCell.EmptyCell;

/**
 * Tests for custom data structures.
 * @author Jiangcheng Oliver Chu
 */
public class DataTests extends TestGroup {
    public DataTests() {
        super("data_tests");
        TestGroup[] tests = {
            new TestGroup("charqueue") {
                @Override
                protected boolean mainTest() {
                    String str = "This is a test.";
                    DenseLinkedList cq = new DenseLinkedList();
                    for (char c : str.toCharArray()) {
                        cq.push(c);
                    }
                    return cq.convertToString().equals("CharLinkedList[\"" +
                                                       str + "\"]");
                }
            },
            new TestGroup("charstack") {
                @Override
                protected boolean mainTest() {
                    String backwards = "\u266b sdrawkcaB";
                    String forwards = "Backwards \u266b";
                    DenseLinkedList cq = new DenseLinkedList();
                    for (char c : backwards.toCharArray()) {
                        cq.push(c);
                    }
                    return cq.convertToReversedString().equals(
                                   "CharLinkedList[\"" + forwards + "\"]");
                }
            },
            new TestGroup("tuple") {
                @Override
                protected boolean mainTest() {
                    Tuple<String, Tuple<Integer, EmptyCell>> x;
                    x = Tuple.cons("hello", Tuple.cons(15));
                    return x.cdr().car() == 15;
                }
            },
            new TestGroup("lambdas") {
                @Override
                protected boolean mainTest() {
                    VarArgLambda<Double, Tuple<String, Tuple<Boolean,
                                 Tuple<Double, EmptyCell>>>> lmbda;
                    lmbda = new VarArgLambda<Double, Tuple<String,
                                Tuple<Boolean, Tuple<Double, EmptyCell>>>>() {
                        @Override
                        public Double run(Tuple<String, Tuple<Boolean,
                                          Tuple<Double, EmptyCell>>> t) {
                            return t.cdr().cdr().car() + Math.PI;
                        }
                    };
                    Tuple<String, Tuple<Boolean,
                          Tuple<Double, EmptyCell>>> s;
                    s = Tuple.cons("a",
                        Tuple.cons(true,
                            Tuple.cons(Math.PI)
                        )
                    );
                    return lmbda.run(s) == 2 * Math.PI;
                }
            }
        };
        setSubtests(tests);
    }

    @Override
    protected boolean mainTest() {
        return true;
    }
}
