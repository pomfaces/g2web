package hu.infokristaly.g2.model;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class NotEQ extends TwoArgOperator<ExpOrValue<?>, ExpOrValue<?>, Boolean> {
    public final static int precedence = 8;
    public final static String representation = new String("!=");
    
    public NotEQ() {
        super(null,null);
    }
    
    public NotEQ(ExpOrValue<?> left, ExpOrValue<?> right) {
        super(left, right);
    }

    @Override
    public Boolean getValue() {
        return !getLeft().getValue().equals(getRight().getValue());
    }

    @Override
    public String toString() {
        return getLeft() + " != " + getRight();
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
}
