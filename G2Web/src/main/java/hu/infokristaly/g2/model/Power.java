package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Power extends TwoArgOperator<ExpOrValue<BigDecimal>, ExpOrValue<BigDecimal>, BigDecimal> {

    public final static int precedence = 3;
    public final static String representation = new String("^");
    public final static String regexp_representation = new String("\\^");
    
    public Power(){
        super(null,null);
    }
    
    public Power(ExpOrValue<BigDecimal> left, ExpOrValue<BigDecimal> right) {
        super(left, right);
    }

    @Override
    public BigDecimal getValue() {
        return getLeft().getValue().pow(getRight().getValue().intValue());
    }

    @Override
    public String toString() {
        return "(" + getLeft() + " ^ " + getRight() + ")";
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
}
