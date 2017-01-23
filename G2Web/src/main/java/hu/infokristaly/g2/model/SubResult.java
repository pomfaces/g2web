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
public class SubResult<T> extends Unary<T>{

    public final static int precedence = 1;
    
    @Override
    public String toString() {
        return object.toString();
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
}
