package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Multiple extends TwoArgOperator<ExpOrValue<BigDecimal>, ExpOrValue<BigDecimal>, BigDecimal> {

    public final static int precedence = 4;
    public final static String representation = new String("*");
    public final static String regexp_representation = new String("\\*");
    
    public Multiple() {
       super(null,null); 
    }
    
    public Multiple(ExpOrValue<BigDecimal> left, ExpOrValue<BigDecimal> right) {
        super(left, right);
    }

    @Override
    public BigDecimal getValue() {
        return getLeft().getValue().multiply(getRight().getValue());
    }

    @Override
    public String toString() {
        return " " + getLeft() + " * " + getRight() + " ";
    }

    /* (non-Javadoc)
     * @see model.TwoArgBigDecimalOperator#getPrecedence()
     */
    @Override
    public int getPrecedence() {
        return precedence;
    }
}
