package hu.infokristaly.g2.front.manager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.xnio.streams.ReaderInputStream;

import hu.infokristaly.g2.exceptions.InvalidOrderExpression;
import hu.infokristaly.g2.exceptions.InvalidTokenException;
import hu.infokristaly.g2.expression.WorkspaceBuilder;
import hu.infokristaly.g2.model.Value;
import hu.infokristaly.g2.model.Conclusion;
import hu.infokristaly.g2.model.ExpOrValue;
import hu.infokristaly.g2.model.NumNegation;
import hu.infokristaly.g2.model.Parentheses;
import hu.infokristaly.g2.model.Reference;
import hu.infokristaly.g2.model.Rule;
import hu.infokristaly.g2.model.SetterConclusion;
import hu.infokristaly.g2.model.TwoArgOperator;
import hu.infokristaly.g2.model.Unary;
import hu.infokristaly.g2.model.Unnamed;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

@Named
@SessionScoped
public class WorkspaceManager implements Serializable {

    private static final long serialVersionUID = 400715849825322789L;

    private HashMap<String, Object> objects = new HashMap<String, Object>();
    private List<Object> objectList = new LinkedList<Object>();

    private DefaultDiagramModel model;

    private String newObjectName;
    private String newObjectValue;

    private String expressions;

    private String result;

    private String valueToSet = "3";

    private WorkspaceBuilder wpBuilder;

    private TreeNode root;

    private StreamedContent file;

    @PostConstruct
    public void init() {
        wpBuilder = new WorkspaceBuilder();
        wpBuilder.regExpBuilder();
        
        expressions = "-2 * ( -this.value1 ) -1 <= 100 * ( this.value2 ) + 3";
        ExpOrValue value1 = new Value<BigDecimal>("value1", new BigDecimal(500));
        objects.put("value1", value1);
        ExpOrValue value2 = new Value<BigDecimal>("value2", new BigDecimal(10));
        objects.put("value2", value2);

        ExpOrValue result = new Value<BigDecimal>("result", new BigDecimal(0));
        objects.put("result", result);

        wpBuilder.setRefObjects(objects);
        objectList.addAll(objects.values());
        model = new DefaultDiagramModel();
    }

    public String getExpressions() {
        return expressions;
    }

    public void setExpressions(String expressions) {
        this.expressions = expressions;
    }

    public void process() {
        wpBuilder.setInput(expressions);
        ExpOrValue exp = null;
        try {
            wpBuilder.clearBuilder();
            wpBuilder.regExpProcess();
            exp = wpBuilder.buildExpTree();
            Object resultObj = exp.getValue();
            if (resultObj instanceof Boolean && ((Boolean) resultObj).booleanValue()) {
                ExpOrValue value = (ExpOrValue) wpBuilder.getRefObjects().get("result");

                wpBuilder.getRefObjects().remove("valueToSet");

                ExpOrValue setterValue = new Value<BigDecimal>("valueToSet", new BigDecimal(valueToSet));
                objects.put("valueToSet", setterValue);

                Reference resultRef = new Reference<ExpOrValue>();
                resultRef.setObjSpace(wpBuilder.getWp());
                resultRef.setRefName("this.result");

                Reference setterValueRef = new Reference<ExpOrValue>();
                setterValueRef.setObjSpace(wpBuilder.getWp());
                setterValueRef.setRefName("this.valueToSet");

                Conclusion c = new SetterConclusion(resultRef, setterValueRef);
                Rule r1 = new Rule("rule1", exp, c);
                wpBuilder.replaceRule(r1);
                r1.evaluate();

            }
            model.clear();
            if (resultObj != null) {
                buildDiagram(exp);
                TreeNode root = new DefaultTreeNode("Root", null);
                setRoot(root);
                buildTreeNodes(exp, root, 1);

                result = resultObj.toString();
                FacesMessage msg = new FacesMessage("Értelmezve", exp.toString());
                FacesContext.getCurrentInstance().addMessage(null, msg);
            } else {
                result = null;
                setRoot(null);
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Értelmezési hiba", "expression value is null");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        } catch (InvalidClassException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | InvalidOrderExpression
                | NullPointerException | InvalidTokenException e) {
            result = null;
            setRoot(null);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Értelmezési hiba", e.getMessage() + (exp != null ? " exp: " + exp.toString() : ""));
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    private void buildTreeNodes(ExpOrValue exp, TreeNode parent, int count) {
        IntStream.range(0, count).forEach(i -> {
            System.out.print("-");
        });
        if (exp instanceof TwoArgOperator) {
            TreeNode newRoot = new DefaultTreeNode(exp.getClass().getSimpleName(), parent);
            System.out.println(exp.getClass().getSimpleName());

            ExpOrValue left = (ExpOrValue) ((TwoArgOperator) exp).getLeft();
            buildTreeNodes(left, newRoot, count + 1);

            ExpOrValue right = (ExpOrValue) ((TwoArgOperator) exp).getRight();
            buildTreeNodes(right, newRoot, count + 1);
        } else if (exp instanceof Reference) {
            TreeNode refNode = new DefaultTreeNode(((Reference) exp).getRefName(), parent);
            System.out.println(((Reference) exp).toString());
        } else if (exp instanceof Unary) {
            TreeNode unaryNode = new DefaultTreeNode(exp.getClass().getSimpleName(), parent);
            System.out.println(exp.getClass().getSimpleName());
            buildTreeNodes(((Unary) exp).getObject(), unaryNode, count + 1);
        } else if (exp instanceof Unnamed) {
            TreeNode unnamed = new DefaultTreeNode(exp.getValue(), parent);
            System.out.println(exp.getValue());
        }
    }

    private void buildDiagram(ExpOrValue exp) {
        model.setMaxConnections(-1);

        Element elementA = new Element("A", "20em", "6em");
        elementA.addEndPoint(new DotEndPoint(EndPointAnchor.BOTTOM));

        Element elementB = new Element("B", "10em", "18em");
        elementB.addEndPoint(new DotEndPoint(EndPointAnchor.TOP));

        Element elementC = new Element("C", "40em", "18em");
        elementC.addEndPoint(new DotEndPoint(EndPointAnchor.TOP));

        model.addElement(elementA);
        model.addElement(elementB);
        model.addElement(elementC);

        model.connect(new Connection(elementA.getEndPoints().get(0), elementB.getEndPoints().get(0)));
        model.connect(new Connection(elementA.getEndPoints().get(0), elementC.getEndPoints().get(0)));
    }

    public void addNew() {
        if ((newObjectName!=null) && !newObjectName.trim().isEmpty()) {
            ExpOrValue value;
            try {
                value = new Value<BigDecimal>(newObjectName, new BigDecimal(newObjectValue));
            } catch (Exception e) {
                value = new Value<String>(newObjectName, newObjectValue);
            }
            objects.put(newObjectName, value);
            newObjectName = null;
            newObjectValue = null;
            objectList.add(value);
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Értelmezési hiba", "Üres név");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void removeObject(String name) {
        objects.remove(name);
        objectList.clear();
        objectList.addAll(objects.values());
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getNewObjectName() {
        return newObjectName;
    }

    public void setNewObjectName(String newObjectName) {
        this.newObjectName = newObjectName;
    }

    public String getNewObjectValue() {
        return newObjectValue;
    }

    public void setNewObjectValue(String newObjectValue) {
        this.newObjectValue = newObjectValue;
    }

    public List<Object> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<Object> objectList) {
        this.objectList = objectList;
    }

    public DefaultDiagramModel getModel() {
        return model;
    }

    public void setModel(DefaultDiagramModel model) {
        this.model = model;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public String getWorkspaceXML() {
        return wpBuilder.generateXML();
    }

    public StreamedContent getWorkspaceFile() {
        String value = getWorkspaceXML();
        InputStream stream = new ByteArrayInputStream(value.getBytes());
        file = new DefaultStreamedContent(stream, "text/xml", "workspace.xml");
        return file;
    }

    public String getValueToSet() {
        return valueToSet;
    }

    public void setValueToSet(String valueToSet) {
        this.valueToSet = valueToSet;
    }

}
