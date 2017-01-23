package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Mod extends TwoArgOperator<ExpOrValue<BigDecimal>, ExpOrValue<BigDecimal>,BigDecimal> {

    public final static int precedence = 4;
    public final static String representation = new String("%");
    
    public Mod(){
        super(null,null);
    }
    
    public Mod(ExpOrValue<BigDecimal> left, ExpOrValue<BigDecimal> right) {
        super(left, right);
    }

    @Override
    public BigDecimal getValue() {
        return getLeft().getValue().remainder(getRight().getValue());
    }

    @Override
    public String toString() {
        return "(" + getLeft() + " % " + getRight() + ")";
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
}
