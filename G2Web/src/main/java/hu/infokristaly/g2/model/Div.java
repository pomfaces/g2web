package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Div extends TwoArgOperator<ExpOrValue<BigDecimal>, ExpOrValue<BigDecimal>,BigDecimal> {

    public final static int precedence = 4;
    public final static String representation = new String("/");
    
    public Div(){
        super(null,null);
    }
    
    public Div(ExpOrValue<BigDecimal> left, ExpOrValue<BigDecimal> right) {
        super(left, right);
    }

    @Override
    public BigDecimal getValue() {
        return getLeft().getValue().divide(getRight().getValue());
    }

    @Override
    public String toString() {
        return "(" + getLeft() + " / " + getRight() + ")";
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
}
