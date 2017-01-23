/**
 * 
 */
package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

/**
 * @author pzoli
 *
 */
@XmlType
public class NumNegation extends Unary<BigDecimal>{

    public final static int precedence = 2;
    public final static String representation = "-";
    public final static String regexp_representation = new String("\\-");
    
    @Override
    public BigDecimal getValue() {
        BigDecimal value = super.getValue();
        if (value == null) {
            throw new NullPointerException();
        }
        return value.multiply(BigDecimal.valueOf(-1));
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
    
    @Override
    public String toString() {
        if (getObject() != null) {
            return "-" + getObject().toString();
        } else {
            return "-null";
        }
    }
}
