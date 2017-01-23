package hu.infokristaly.g2.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public abstract class TwoArgOperator<L,R,T> extends ExpOrValue<T> {
    private L left;
    private R right;
    
    public TwoArgOperator(L left, R right) {
        this.left = left;
        this.right = right;
    }
    
    @XmlElement
    public L getLeft() {
        return left;
    }
    
    public void setLeft(L left) {
        this.left = left;
    }
    
    @XmlElement
    public R getRight() {
        return right;
    }
    
    public void setRight(R right) {
        this.right = right;
    }
}
