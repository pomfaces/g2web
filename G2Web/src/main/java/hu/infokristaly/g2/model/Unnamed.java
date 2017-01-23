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
public abstract class Unnamed<T> extends ExpOrValue<T>{

    protected T value;

}
