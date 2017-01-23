package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class GT extends TwoArgOperator<ExpOrValue, ExpOrValue, Boolean> {

    public final static int precedence = 8;
    public final static String representation = new String(">");
    
    public GT() {
        super(null,null);
    }
    
    public GT(ExpOrValue left, ExpOrValue right) {
        super(left, right);
    }

    public int compare(Object left, Object right) {
        int result = 0;
        if ((left instanceof BigDecimal) && (right instanceof BigDecimal)) {
            result = ((BigDecimal)left).compareTo((BigDecimal)right);
        }
        return result;
    }
    
    @Override
    public Boolean getValue() {
        return compare(getLeft().getValue() , getRight().getValue()) == 1;
    }

    @Override
    public String toString() {
        return " " + getLeft() + " > " + getRight() + " ";
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
}
