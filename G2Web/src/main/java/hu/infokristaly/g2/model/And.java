package hu.infokristaly.g2.model;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class And extends TwoArgOperator<ExpOrValue<Boolean>, ExpOrValue<Boolean>, Boolean> {

    public final static int precedence = 6;
    public final static String representation = new String("&&");
    
    public And() {
        super(null,null);
    }
    
    public And(ExpOrValue<Boolean> left, ExpOrValue<Boolean> right) {
        super(left, right);
    }

    @Override
    public Boolean getValue() {
        return getLeft().getValue() && getRight().getValue();
    }
    
    @Override
    public String toString() {
        return "(" + getLeft() + " && " + getRight() + ")";
    }

    /* (non-Javadoc)
     * @see model.TwoArgBooleanOperator#getPrecedence()
     */
    @Override
    public int getPrecedence() {
        return precedence;
    }
}
