package tools;

import functional.Lambdas;

/**
 * Represents a command line argument.
 * @author Jiangcheng Oliver Chu
 */
public class Argument {
    private final String name;
    private final String[] subArgs;
    private final String doc;
    private final String[] NO_ARGS = new String[]{};
    
    public Argument(String name, String[] subArgs, String doc) {
        this.name = name;
        this.subArgs = subArgs == null ? NO_ARGS : subArgs;
        this.doc = doc;
    }

    public String getName() {
        return name;
    }
    
    public String[] getSubArgs() {
        return subArgs;
    }
    
    public boolean validate(String[] receivedArgs) {
        return numArgs() == receivedArgs.length;
    }
    
    public int numArgs() {
        return subArgs.length;
    }
    
    public String getDoc() {
        return doc;
    }
    
    public String getFullDoc() {
        return "Usage: " + getName() + " (with " + subArgs.length +
               " parameters)\n" + "    " + doc.replace("\n", "\n    ");
    }
}
