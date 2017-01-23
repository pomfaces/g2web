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
public class BoolNegation extends Unary<Boolean>{

    public final static int precedence = 1;
    public final static String representation = "!";
        
    @Override
    public int getPrecedence() {
        return precedence;
    }
    
    public String toString() {
        return "!" + super.getValue();
    }

}
