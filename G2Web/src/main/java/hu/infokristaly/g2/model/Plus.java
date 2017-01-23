package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Plus extends TwoArgOperator<ExpOrValue<BigDecimal>, ExpOrValue<BigDecimal>, BigDecimal> {

    public final static int precedence = 5;
    public final static String representation = new String("+");
    public final static String regexp_representation = new String("\\+");
    
    public Plus() {
        super(null,null);
    }
    
    public Plus(ExpOrValue<BigDecimal> left, ExpOrValue<BigDecimal> right) {
        super(left, right);
    }

    @Override
    public BigDecimal getValue() {
        BigDecimal lValue = getLeft().getValue();
        BigDecimal rValue = getRight().getValue();
        return lValue.add(rValue);
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
