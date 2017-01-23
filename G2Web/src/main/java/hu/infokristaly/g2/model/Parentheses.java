/**
 * 
 */
package hu.infokristaly.g2.model;

import javax.xml.bind.annotation.XmlType;

/**
 * @author pzoli
 *
 */
@XmlType
public class Parentheses<T> extends Unary<T>{

    public final static int precedence = 1;
    public final static String openRepresentation = "(";
    public final static String closeRepresentation =")";
    public final static String openRegExpRepresentation = "^\\(";
    public final static String closeRegExpRepresentation = "^\\)";
    
    @Override
    public String toString() {
        return "("+getObject().toString()+")";
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
}
