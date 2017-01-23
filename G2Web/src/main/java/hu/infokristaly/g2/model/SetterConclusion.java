package hu.infokristaly.g2.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class SetterConclusion<T> extends Conclusion {
    @XmlElement
    private ExpOrValue<T> target;

    @XmlElement
    private ExpOrValue<T> value;

    public SetterConclusion() {
        super();
    }

    public SetterConclusion(ExpOrValue<T> target, ExpOrValue<T> value) {
        super();
        this.target = target;
        this.value = value;
    }

    @Override
    public void execute() {
        if (target != null) {
            if (target instanceof Value) {
                ((Value) target).setValue(value.getValue());
            } else if (target instanceof Reference) {
                Object refObj = ((Reference) target).getObject();
                if (refObj instanceof Value) {
                    ((Value) refObj).setValue(value.getValue());
                }
            }

        }
    }

    @Override
    public String toString() {
        String varName = "null";
        if (target instanceof Value) {
            varName = ((Value) target).getName();
        } else if (target instanceof Reference) {
            varName = ((Reference) target).getRefName();
        }
        return " set " + varName + ".value to " + (value != null ? value + "=" + value.getValue() : "null");
    }

    public ExpOrValue<T> getTarget() {
        return target;
    }

    public ExpOrValue<T> getValueExp() {
        return value;
    }
}
