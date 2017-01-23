package hu.infokristaly.g2.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class Value<T> extends Unnamed<T> {

    private String name;

    public Value() {
        
    }
    
    public Value(String name) {
        this.setName(name);
    }

    public Value(String name, T value) {
        this.setName(name);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return " " + getName() + ":" + getValue() + " ";
    }

    public int getPrecedence() {
        return 0;
    }
}
