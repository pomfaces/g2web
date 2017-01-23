package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Concat extends TwoArgOperator<ExpOrValue<?>, ExpOrValue<?>, Object> {

    public final static int precedence = 5;
    public final static String representation = new String("+");
    public final static String regexp_representation = new String("\\+");
    
    public Concat() {
        super(null,null);
    }
    
    public Concat(ExpOrValue<?> left, ExpOrValue<?> right) {
        super(left, right);
    }

    @Override
    public Object getValue() {
        Object lValue = getLeft().getValue();
        Object rValue = getRight().getValue();
        return String.valueOf(lValue) + String.valueOf(rValue);
    }

    @Override
    public String toString() {
        return " " + getLeft() + " + " + getRight() + " ";
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
}
