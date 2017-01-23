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
public abstract class Unary<T> extends ExpOrValue<T>{
    
    protected ExpOrValue<T> object;
    
    @Override
    public T getValue() {
        return object.getValue();
    }    

    public ExpOrValue<T> getObject() {
        return object;
    }
    
    public void setObject(ExpOrValue<T> obj) {
        this.object = obj;
    }

}
