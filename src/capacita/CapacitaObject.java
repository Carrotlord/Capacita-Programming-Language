package capacita;

import java.util.HashMap;

/**
 * Capacita object representing both user objects and environment frames.
 * @author Jiangcheng Oliver Chu
 */
class CapacitaObject {
    private HashMap<String, CapacitaValue> bindings;
    
    public CapacitaObject() {
        bindings = new HashMap<String, CapacitaValue>();
    }
    
    public void assign(String variable, CapacitaValue value) {
        bindings.put(variable, value);
    }
    
    public CapacitaValue get(String variable) throws CapacitaException {
        if (bindings.containsKey(variable)) {
            return bindings.get(variable);
        } else {
            throw new CapacitaException("Variable " + variable +
                                    " used before assignment.");
        }
    }
    
    /**
     * Deletes the key variable and the value associated with it.
     * @param variable the string name of the variable
     * @return false iff variable was not defined before deletion.
     */
    public boolean delete(String variable) {
        CapacitaValue removed = bindings.remove(variable);
        return removed == null;
    }
    
    public void deleteAll() {
        bindings.clear();
    }
}
