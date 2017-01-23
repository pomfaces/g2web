package hu.infokristaly.g2.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class UnnamedConstant<T> extends Unnamed<T> {

    public UnnamedConstant() {
        super();
    }

    public UnnamedConstant(T value) {
        this.value = value;
    }

    @XmlElement
    @Override
    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        this.value = newValue;
    }
    
    @Override
    public String toString() {
        return " " + getValue() + " ";
    }

    public int getPrecedence() {
        return 0;
    }
}
