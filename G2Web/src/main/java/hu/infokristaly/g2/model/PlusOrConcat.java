package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class PlusOrConcat extends TwoArgOperator<ExpOrValue<?>, ExpOrValue<?>, Object> {

    public final static int precedence = 5;
    public final static String representation = new String("+");
    public final static String regexp_representation = new String("\\+");

    public PlusOrConcat() {
        super(null, null);
    }

    public PlusOrConcat(ExpOrValue<BigDecimal> left, ExpOrValue<BigDecimal> right) {
        super(left, right);
    }

    @Override
    public Object getValue() {
        Object lValue = getLeft().getValue();
        Object rValue = getRight().getValue();
        Object result = null;
        if (lValue instanceof BigDecimal && rValue instanceof BigDecimal) {
            result = ((BigDecimal) lValue).add((BigDecimal) rValue);
        } else if (lValue instanceof String || rValue instanceof String) {
            result = String.valueOf(lValue).concat(String.valueOf(rValue));
        }
        return result;
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
