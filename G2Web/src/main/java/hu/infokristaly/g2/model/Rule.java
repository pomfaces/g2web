package hu.infokristaly.g2.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder={"name", "enabled", "mayCauseForwardChain", "condition", "conclusion", "conditionReferences", "conclusionReferences"})
public class Rule {
    
    private String name;
    
    private ExpOrValue<Boolean> condition;
    
    private Conclusion conclusion;

    private List<String> conditionReferences = new ArrayList<String>();
    
    private List<String> conclusionReferences = new ArrayList<String>();
    
    private boolean enabled = true;
    
    private boolean mayCauseForwardChain = true;

    public Rule() {
    }
    
    public Rule(String name, ExpOrValue<Boolean> condition, Conclusion conclusion) {
        this.setName(name);
        this.condition = condition;
        this.conclusion = conclusion;
    }
    
    public boolean evaluate() {
        boolean result = condition.getValue();
        if (result) {
            conclusion.execute();
        }
        return result;
    }

    @Override
    public String toString() {
        return "if " + condition + " then " + conclusion;
    }
    
    @XmlElementWrapper(name="conditionalrefs")
    public List<String> getConditionReferences() {
        return conditionReferences;
    }

    public void setConditionReferences(List<String> references) {
        this.conditionReferences.addAll(references);
    }

    public boolean isConclusionReferencedTo(String ref) {
        boolean result = false;
        if (conclusionReferences != null) {
            for (String name : conclusionReferences) {
                if (name.equalsIgnoreCase(ref)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isConditionReferencedTo(String ref) {
        boolean result = false;
        if (conditionReferences != null) {
            for (String name : conditionReferences) {
                if (name.equalsIgnoreCase(ref)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
    @XmlElementWrapper(name="conclusionrefs")
    public List<String> getConclusionReferences() {
        return conclusionReferences;
    }

    public void setConclusionReferences(List<String> conclusionReferences) {
        this.conclusionReferences.addAll(conclusionReferences);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isMayCauseForwardChain() {
        return mayCauseForwardChain;
    }

    public void setMayCauseForwardChain(boolean mayCauseForwardChain) {
        this.mayCauseForwardChain = mayCauseForwardChain;
    }

    public ExpOrValue<Boolean> getCondition() {
        return condition;
    }

    public void setCondition(ExpOrValue<Boolean> condition) {
        this.condition = condition;
    }

    public Conclusion getConclusion() {
        return conclusion;
    }

    public void setConclusion(Conclusion conclusion) {
        this.conclusion = conclusion;
    }

}
