/**
 * 
 */
package hu.infokristaly.g2.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * @author pzoli
 *
 */
@XmlRootElement
@XmlType(propOrder = { "objects" })
public class Workspace {

    private Queue<Rule> ruleQueue = new LinkedList<Rule>();

    private WorkspaceObjects objects = new WorkspaceObjects();

    public Workspace() {
        super();
    }

    @XmlTransient
    public Queue<Rule> getRuleQueue() {
        return ruleQueue;
    }

    public void setRuleQueue(Queue<Rule> ruleQueue) {
        this.ruleQueue = ruleQueue;
    }

    public HashMap<String, Object> getObjects() {
        return objects.getObjects();
    }

    public void setObjects(HashMap<String, Object> objects) {
        this.objects.setObjects(objects);
    }

    /**
     * 
     */
    public void start() {
        while (!ruleQueue.isEmpty()) {
            Rule rule = ruleQueue.remove();
            System.out.printf("rule: %s \n", rule);
            boolean evaluated = rule.isEnabled() && rule.isMayCauseForwardChain() && rule.evaluate();
            if (evaluated) {
                System.out.println("evaluated : " + evaluated);
                if (rule.getConclusionReferences() != null) {
                    List<String> concludeRefs = rule.getConclusionReferences();
                    if (concludeRefs != null) {
                        for (String ref : concludeRefs) {
                            System.out.println(ref + " new value =" + objects.getObjects().get(ref).toString());
                        }
                    }
                }
                if (rule.getConclusionReferences() != null) {
                    List<String> concludeRefs = rule.getConclusionReferences();
                    if (concludeRefs != null) {
                        for (String ref : concludeRefs) {
                            for (Object obj : objects.getObjects().values()) {
                                if (obj instanceof Rule) {
                                    if (((Rule) obj).isConditionReferencedTo(ref) && ((Rule) obj).isEnabled()) {
                                        ruleQueue.add((Rule) obj);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("evaluated : " + evaluated);
            }
        }
    }

    /**
     * @param refName
     */
    public ExpOrValue getObjectByName(String refName) {
        return (ExpOrValue) objects.getObjects().get(refName);
    }

    /**
     * 
     */
    public void resolveReferences() {
        Iterator<String> iter = objects.getObjects().keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Object valueObj = objects.getObjects().get(key);
            if (valueObj instanceof TwoArgOperator) {
                resolveTwoArgOperator((TwoArgOperator) valueObj);
            } else if (valueObj instanceof Rule) {
                resolveRule((Rule) valueObj);
            }
        }
    }
    
    /**
     * @param valueObj
     */
    private void resolveTwoArgOperator(TwoArgOperator obj) {
        Object left = obj.getLeft();
        resolveExpOrValue(left);

        Object right = obj.getRight();
        resolveExpOrValue(right);
    }

    private void resolveExpOrValue(Object value) {
        if (value != null) {
            if (value instanceof Reference) {
                ((Reference)value).setObjSpace(this);
            } else if (value instanceof TwoArgOperator) {
                resolveTwoArgOperator((TwoArgOperator) value);
            }
        }

    }

    /**
     * @param valueObj
     */
    private void resolveRule(Rule valueObj) {
        Conclusion conclusion = valueObj.getConclusion();
        if (conclusion instanceof SetterConclusion) {
            ExpOrValue concl = ((SetterConclusion) conclusion).getTarget();
            resolveExpOrValue(concl);
            
            ExpOrValue conclValue = ((SetterConclusion) conclusion).getValueExp();
            resolveExpOrValue(conclValue);            
        }

        ExpOrValue cond = valueObj.getCondition();
        resolveExpOrValue(cond);
    }

}
