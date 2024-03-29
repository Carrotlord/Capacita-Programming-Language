package capacita;

import java.io.IOException;

/**
 * Virtual machine that executes successor code and has access to console
 * and file IO on the actual system.
 * @author Jiangcheng Oliver Chu
 */
public class SuccessorVirtualMachine {
    private int[][] intHeapSegments;
    private double[][] doubleHeapSegments;
    private String[][] stringHeapSegments;
    private CapacitaObject[][] capacitaObjHeapSegments;
    private int numSegments;
    private int initialSlots;
    private char growthRate;
    private int latestActiveSegment;
    private int[] intRegs;
    private double[] doubleRegs;
    private String[] stringRegs;
    private CapacitaObject[] capacitaObjRegs;
    private int[] program;
    private boolean isHalted;
    private int[][] stackSegments;
    
    public static final char LINEAR_GROWTH = 0;
    public static final char QUADRATIC_GROWTH = 1;
    public static final char EXPONENTIAL_GROWTH = 2;
    
    private static final int DEFAULT_SEGMENTS = 14;
    private static final int DEFAULT_STACK_SEGMENTS = 16;
    private static final char DEFAULT_SLOTS = 2048;
    private static final char DEFAULT_GROWTH = EXPONENTIAL_GROWTH;
    
    private static final int NUM_REGISTERS = 64;
    
    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_EOF = -1;
    public static final int EXIT_FAILURE = 1;
    
    public static final int VM_EXIT = 0;
    public static final int VM_HALT = 1;
    public static final int VM_PRINT = 2;
    public static final int VM_PRINTLN = 3;
    public static final int VM_INPUT = 4;
    public static final int VM_READ_PROPERTY = 5;
    public static final int VM_WRITE_PROPERTY = 6;
    public static final int VM_CALL_METHOD = 7;
    public static final int VM_OTHER = 8;
    
    private static final int STACK_SEG_SIZE = 16384;
    private int stackStart;
    private static final int STACK_VIRTUAL_OFFSET = 0x6e000000;
    private static final int STACK_VIRTUAL_BOUNDARY = STACK_VIRTUAL_OFFSET -
                             STACK_SEG_SIZE * (DEFAULT_STACK_SEGMENTS - 1);
    private int currentStackSeg;
    
    private static final int rSP = 62;
    private static final int rBP = 63;
    
    public SuccessorVirtualMachine(int[] bytecode) throws InternalException {
        this(bytecode, DEFAULT_SEGMENTS, DEFAULT_SLOTS, DEFAULT_GROWTH);
    }
    
    public SuccessorVirtualMachine(int[] bytecode, int segments, int slots,
                                   char growth) throws InternalException {
        checkInitialConditions(segments, slots, growth);
        program = bytecode;
        if ((program.length & 1) == 1) {
            throw new InternalException("Program length is " + program.length +
                                        ", number of words should be even.");
        }
        numSegments = segments;
        initialSlots = slots;
        growthRate = growth;
        intHeapSegments = new int[numSegments][];
        doubleHeapSegments = new double[numSegments][];
        stringHeapSegments = new String[numSegments][];
        capacitaObjHeapSegments = new CapacitaObject[numSegments][];
        intHeapSegments[0] = new int[initialSlots];
        doubleHeapSegments[0] = new double[initialSlots];
        stringHeapSegments[0] = new String[initialSlots];
        capacitaObjHeapSegments[0] = new CapacitaObject[initialSlots];
        intRegs = new int[NUM_REGISTERS];
        doubleRegs = new double[NUM_REGISTERS];
        stringRegs = new String[NUM_REGISTERS];
        capacitaObjRegs = new CapacitaObject[NUM_REGISTERS];
        stackSegments = new int[DEFAULT_STACK_SEGMENTS][];
        stackSegments[DEFAULT_STACK_SEGMENTS - 1] = new int[STACK_SEG_SIZE];
        stackStart = STACK_VIRTUAL_OFFSET + STACK_SEG_SIZE - 1;
        setStackPointer(stackStart);
        setBasePointer(stackStart);
        isHalted = false;
        currentStackSeg = DEFAULT_STACK_SEGMENTS - 1;
    }
    
    private void setStackPointer(int address) {
        intRegs[rSP] = address;
    }
    
    private void setBasePointer(int address) {
        intRegs[rBP] = address;
    }
    
    public int execute() {
        for (int ip = 0; ip < program.length;) {
            int command = program[ip];
            int constant = program[ip + 1];
            int type = command >>> 30;
            int opcode = (command & 0x3ff80000) >>> 19;
            int rA = (command & 0xfc0) >> 6;
            int rB = command & 0x3f;
            int rC = (command & 0x3f000) >> 12;
            /* For efficiency reasons, avoid external method calls in
             * the following switch. */
            switch (type) {
            case Mnemonics.INT:
                switch (opcode) {
                case Mnemonics.SHLV:
                    intRegs[rA] <<= intRegs[rB] + constant;
                    ip += 2;
                    break;
                case Mnemonics.MOV:
                    intRegs[rA] = intRegs[rB] + constant;
                    ip += 2;
                    break;
                case Mnemonics.ADD:
                    intRegs[rA] += intRegs[rB] + constant;
                    ip += 2;
                    break;
                case Mnemonics.SUB:
                    intRegs[rA] -= intRegs[rB] + constant;
                    ip += 2;
                    break;
                case Mnemonics.MUL:
                    intRegs[rA] *= intRegs[rB] + constant;
                    ip += 2;
                    break;
                case Mnemonics.DIV:
                    intRegs[rA] /= intRegs[rB] + constant;
                    ip += 2;
                    break;
                case Mnemonics.MOD:
                    intRegs[rA] %= intRegs[rB] + constant;
                    ip += 2;
                    break;
                case Mnemonics.AND:
                    intRegs[rA] &= intRegs[rB] | constant;
                    ip += 2;
                    break;
                case Mnemonics.OR:
                    intRegs[rA] |= intRegs[rB] | constant;
                    ip += 2;
                    break;
                case Mnemonics.XOR:
                    intRegs[rA] ^= intRegs[rB] ^ constant;
                    ip += 2;
                    break;
                case Mnemonics.J:
                    ip = constant << 1;
                    break;
                case Mnemonics.JMP:
                    ip = intRegs[rA] << 1;
                    break;
                case Mnemonics.JEQ:
                    ip = intRegs[rA] == intRegs[rB] ? constant << 1 : ip + 2;
                    break;
                case Mnemonics.JNE:
                    ip = intRegs[rA] != intRegs[rB] ? constant << 1 : ip + 2;
                    break;
                case Mnemonics.JGE:
                    ip = intRegs[rA] >= intRegs[rB] ? constant << 1 : ip + 2;
                    break;
                case Mnemonics.JG:
                    ip = intRegs[rA] > intRegs[rB] ? constant << 1 : ip + 2;
                    break;
                case Mnemonics.JLE:
                    ip = intRegs[rA] <= intRegs[rB] ? constant << 1 : ip + 2;
                    break;
                case Mnemonics.JL:
                    ip = intRegs[rA] < intRegs[rB] ? constant << 1 : ip + 2;
                    break;
                case Mnemonics.CALL: {
                    intRegs[rSP]--;
                    int destination = intRegs[rSP] - STACK_VIRTUAL_OFFSET;
                    stackSegments[currentStackSeg][destination] = ip + 2;
                    ip = constant << 1;
                    break;
                } case Mnemonics.SYSCALL:
                    /* The following should be changed for other types of
                     * syscalls. */
                    switch (rA) {
                    case VM_EXIT:
                        return EXIT_SUCCESS;
                    case VM_PRINT:
                        Capacita.IO.print(intRegs[rB]);
                        ip += 2;
                        break;
                    case VM_PRINTLN:
                        Capacita.IO.println(intRegs[rB]);
                        ip += 2;
                        break;
                    case VM_INPUT:
                        try {
                            stringRegs[rB] = Capacita.IO.getln();
                        } catch (IOException ex) {
                            return EXIT_FAILURE;
                        }
                        ip += 2;
                        break;
                    case VM_READ_PROPERTY:
                        break;
                    case VM_WRITE_PROPERTY:
                        break;
                    case VM_CALL_METHOD:
                        break;
                    case VM_OTHER:
                        break;
                    case VM_HALT:
                    default:
                        return EXIT_FAILURE;
                    }
                    break;
                case Mnemonics.RET:
                    int returnAddress = stackSegments[currentStackSeg][
                                        intRegs[rSP] - STACK_VIRTUAL_OFFSET];
                    ip = returnAddress;
                    intRegs[rSP]++;
                    break;
                case Mnemonics.LOAD: {
                    int baseAddress = intRegs[rB] + intRegs[rC] + constant;
                    if (baseAddress >= STACK_VIRTUAL_BOUNDARY) {
                        intRegs[rA] = stackSegments[currentStackSeg][
                                      baseAddress - STACK_VIRTUAL_OFFSET];
                    } else {
                        // TODO: load int from heap
                        return EXIT_FAILURE;
                    }
                    ip += 2;
                    break;
                } case Mnemonics.SAVE: {
                    int baseAddress = intRegs[rB] + intRegs[rC] + constant;
                    if (baseAddress >= STACK_VIRTUAL_BOUNDARY) {
                        stackSegments[currentStackSeg][
                            baseAddress - STACK_VIRTUAL_OFFSET] = intRegs[rA];
                    } else {
                        // TODO: save int to heap
                        return EXIT_FAILURE;
                    }
                    ip += 2;
                    break;
                } case Mnemonics.PUSH: {
                    intRegs[rSP]--;
                    int destination = intRegs[rSP] - STACK_VIRTUAL_OFFSET;
                    stackSegments[currentStackSeg][destination] = constant;
                    ip += 2;
                    break;
                } case Mnemonics.POP:
                    int poppedValue = stackSegments[currentStackSeg][
                                      intRegs[rSP] - STACK_VIRTUAL_OFFSET];
                    intRegs[rA] = poppedValue;
                    intRegs[rSP]++;
                    ip += 2;
                    break;
                default:
                    return EXIT_FAILURE;
                }
                break;
            case Mnemonics.DBL:
                break;
            case Mnemonics.STR:
                break;
            case Mnemonics.CAPACITA_OBJ:
                break;
            default:
                return EXIT_FAILURE;
            }
            // TODO: finish this method
        }
        return EXIT_EOF;
    }
    
    public String stackToString() {
        String result = "";
        /* int stoppingPoint = Math.min(intRegs[rSP] - STACK_VIRTUAL_OFFSET,
                                     intRegs[rBP] - STACK_VIRTUAL_OFFSET); */
        int stoppingPoint = STACK_SEG_SIZE - 11;
        for (int i = STACK_SEG_SIZE - 1; i >= stoppingPoint; i--) {
            result = stackSegments[DEFAULT_STACK_SEGMENTS - 1][i] +
                     " | " + result;
        }
        return result;
    }
    
    public String inspectState() {
        String result = "Registers that are not 0:\n";
        for (int i = 0; i < NUM_REGISTERS; i++) {
            if (intRegs[i] != 0) {
                result += "r" + i + " = " + intRegs[i] + "\n";
            }
        }
        return result;
    }
    
    public void writeInt(int i, int address) {
        
    }
    
    public void writeDouble(double d, int address) {
        
    }
    
    public void writeString(String s, int address) {
        
    }
    
    void writeCapacitaObj(CapacitaObject m, int address) {
        
    }
    
    /**
     * Given a address, returns the segment index of that address.
     * @param address
     * @return 
     */
    public int getEffectiveSegment(int address) {
        switch (growthRate) {
            case LINEAR_GROWTH:
                return address / initialSlots;
            case QUADRATIC_GROWTH:
                return (int) Math.sqrt(address / initialSlots);
            case EXPONENTIAL_GROWTH:
                return log2OfPowerOf2(address / initialSlots);
            default:
                return -1;
        }
    }
    
    /**
     * Returns log base 2 of the argument, which must be a power of 2.
     * When given 0, returns 0.
     * @param powerOf2
     * @return log base 2 of the argument, only if the argument is a power of 2.
     */
    public static int log2OfPowerOf2(int powerOf2) {
        for (int i = 0; i < 32; i++) {
            if ((powerOf2 & 1) == 1) {
                return i;
            }
            powerOf2 >>= 1;
        }
        return 31;
    }
    
    /**
     * 
     * @param segments
     * @param slots
     * @param growth
     * @return maximum heap size under these conditions
     * @throws InternalException if heap would be too large given conditions
     */
    private static int checkInitialConditions(int segments, int slots,
            char growth) throws InternalException {
        int maxHeapSize;
        if (segments <= 0) {
            throw new InternalException("Number of segments is negative or 0.");
        } else if (slots <= 0) {
            throw new InternalException("Initial slots is negative or 0.");
        } else {
            switch (growth) {
                case LINEAR_GROWTH:
                    if (multiplyOverflows(segments, slots)) {
                        throw new InternalException(
                            "Linear growth rate will overflow.");
                    }
                    maxHeapSize = segments * slots;
                    break;
                case QUADRATIC_GROWTH:
                    if (multiplyOverflows(segments * segments,
                                          slots)) {
                        throw new InternalException(
                            "Quadratic growth rate will overflow.");
                    }
                    maxHeapSize = segments * segments * slots;
                    break;
                case EXPONENTIAL_GROWTH:
                    if (multiplyOverflows(1 << segments,
                                          slots)) {
                        throw new InternalException(
                            "Exponential growth rate will overflow.");
                    }
                    maxHeapSize = (1 << segments) * slots;
                    break;
                default:
                    throw new InternalException("Illegal growth rate.");
            }
        }
        return maxHeapSize;
    }
    
    public static String reportMaximumHeapSize(char growth, int slots) {
        int size;
        int previousSize = -1;
        if (slots == 0) {
            return "Not useful to have 0 slots per segment.";
        }
        for (int segments = 1; segments <= Integer.MAX_VALUE; segments++) {
            try {
                size = checkInitialConditions(segments, slots, growth);
            } catch (InternalException ex) {
                return "Max segments: " + (segments - 1) + ", Max size: " +
                       previousSize + " bytes";
            }
            previousSize = size;
        }
        return "Max segments: " + Integer.MAX_VALUE + ", Max size: " +
               Integer.MAX_VALUE;
    }
    
    private static boolean multiplyOverflows(int a, int b) {
        return a != 0 && (b > Integer.MAX_VALUE / a);
    }
}
