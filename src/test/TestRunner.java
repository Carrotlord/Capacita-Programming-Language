package test;

import java.io.UnsupportedEncodingException;
import capacita.Capacita;
import capacita.StringPrintStream;

/**
 * Runs unit tests.
 * @author Jiangcheng Oliver Chu
 */
public class TestRunner {
    private TestGroup[] allTests;
    
    public TestRunner(TestGroup[] allTestsToRun) {
        allTests = allTestsToRun;
    }
    
    public void runTests() {
        StringPrintStream strOut = null;
        boolean isStrOutWorking = true;

        try {
            strOut = new StringPrintStream();
            Capacita.IO.setPrintStream(strOut);
        } catch (UnsupportedEncodingException ex) {
            isStrOutWorking = false;
        }
        boolean allTestsPassed = true;
        for (TestGroup test : allTests) {
            if (!test.run()) {
                allTestsPassed = false;
                break;
            }
        }
        String inspected = strOut.toString();
        Capacita.IO.setSystemPrintStream();
        if (isStrOutWorking) {
            Capacita.IO.debugln("TEST OUTPUT:\n" + inspected);
        }
        Capacita.IO.debugln("All tests passed? " +
                    (allTestsPassed ? "Yes" : "No, stopped on first failure"));
    }
}
