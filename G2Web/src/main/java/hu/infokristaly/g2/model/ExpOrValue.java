package hu.infokristaly.g2.model;

public abstract class ExpOrValue<T> {
    
    public abstract T getValue();
    public abstract int getPrecedence();
}
