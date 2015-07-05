package tools;

/**
 * Command line argument parsing.
 * @author Jiangcheng Oliver Chu
 */
public class ArgParser {
    private final String[] arguments;
    private final Argument[] allowed = {
        new Argument(
            "--syn-highlight",
            new String[]{"inputFile", "outputFile"},
            "Syntax highlights source code files."
        ),
        new Argument(
            "help",
            null,
            "Shows documentation for all commands."
        )
    };
    
    public ArgParser(String[] args) {
        arguments = args;
    }
    
    public void parse() {
        
    }
}
