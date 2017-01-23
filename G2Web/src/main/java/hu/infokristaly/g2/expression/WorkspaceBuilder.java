package hu.infokristaly.g2.expression;

import java.io.InvalidClassException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import hu.infokristaly.g2.exceptions.InvalidOrderExpression;
import hu.infokristaly.g2.exceptions.InvalidTokenException;
import hu.infokristaly.g2.model.And;
import hu.infokristaly.g2.model.BipolarClassParser;
import hu.infokristaly.g2.model.BoolNegation;
import hu.infokristaly.g2.model.ClassParser;
import hu.infokristaly.g2.model.Div;
import hu.infokristaly.g2.model.EQ;
import hu.infokristaly.g2.model.ExpOrValue;
import hu.infokristaly.g2.model.GT;
import hu.infokristaly.g2.model.GTorEQ;
import hu.infokristaly.g2.model.IfRule;
import hu.infokristaly.g2.model.LT;
import hu.infokristaly.g2.model.LTorEQ;
import hu.infokristaly.g2.model.Minus;
import hu.infokristaly.g2.model.Mod;
import hu.infokristaly.g2.model.Multiple;
import hu.infokristaly.g2.model.NumNegation;
import hu.infokristaly.g2.model.Or;
import hu.infokristaly.g2.model.Parentheses;
import hu.infokristaly.g2.model.ParsedObject;
import hu.infokristaly.g2.model.PlusOrConcat;
import hu.infokristaly.g2.model.Power;
import hu.infokristaly.g2.model.Reference;
import hu.infokristaly.g2.model.Rule;
import hu.infokristaly.g2.model.SetterConclusion;
import hu.infokristaly.g2.model.SubResult;
import hu.infokristaly.g2.model.TwoArgOperator;
import hu.infokristaly.g2.model.Unary;
import hu.infokristaly.g2.model.Unnamed;
import hu.infokristaly.g2.model.UnnamedConstant;
import hu.infokristaly.g2.model.Value;
import hu.infokristaly.g2.model.Workspace;
import hu.infokristaly.g2.model.WorkspaceObjects;

public class WorkspaceBuilder {
    private Workspace wp = new Workspace();

    public final static String REGEXP_STRING = new String("^\"");
    public final static String REGEXP_SPACE = new String("^\\s+");
    public final static String REGEXP_DIGITS = new String("^[\\d]+\\.{0,1}[\\d]*");
    public final static String REGEXP_ESCAPE = new String("^\\\\");

    private static Class<?>[] XMLClasses = { Rule.class, ExpOrValue.class, SubResult.class, PlusOrConcat.class, Minus.class, LT.class, LTorEQ.class, GT.class, GTorEQ.class, EQ.class, Div.class, Mod.class, Multiple.class, Power.class, And.class, Or.class,
            BoolNegation.class, NumNegation.class, SetterConclusion.class, TwoArgOperator.class, UnnamedConstant.class, Workspace.class, WorkspaceObjects.class, Value.class, Reference.class, Parentheses.class, BoolNegation.class };
    private static Class<?>[] EXPRESSION_CLASSES = { PlusOrConcat.class, Minus.class, LT.class, LTorEQ.class, GT.class, GTorEQ.class, EQ.class, Div.class, Mod.class, Multiple.class, Power.class, And.class, Or.class, Reference.class, Parentheses.class,
            BoolNegation.class };

    private List<ParsedObject> parsedObjectList = new LinkedList<ParsedObject>();
    private Stack<ExpOrValue> objectStack = new Stack<ExpOrValue>();
    private Stack<Stack<ExpOrValue>> parenthesesStack = new Stack<Stack<ExpOrValue>>();
    private Stack<ParsedObject> parserParenthesesStack = new Stack<ParsedObject>();
    private HashMap<String, ParsedObject> foundMap = new HashMap<String, ParsedObject>();
    private int stringPatternStartIdx = 0;
    private int stringPatternEndIdx = 0;
    private String input;

    private HashMap<String, ClassParser> regexpMap = new HashMap<String, ClassParser>();

    public String generateXML() {
        String result = null;
        StringWriter writer = new StringWriter();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLClasses, null);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            marshaller.marshal(wp, writer);
            result = writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        wp.resolveReferences();
        return result;
    }

    public ExpOrValue buildExpTree() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, InvalidOrderExpression {
        ExpTreeMapper mapper = new ExpTreeMapper(wp, parsedObjectList);
        mapper.buildTreeMap();
        ExpOrValue result = mapper.getResult();
        return result;
    }

    public void runWorkspace() {
        wp.start();
    }

    public void readBackWorkspace() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLClasses, null);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            wp = (Workspace) unmarshaller.unmarshal(this.getClass().getResourceAsStream("/workspace.xml"));
            wp.resolveReferences();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private int getMaxLength() {
        int result = 0;
        Iterator<String> iter = foundMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            ParsedObject parsedObject = foundMap.get(key);
            if (parsedObject.mRef.end() > result) {
                result = parsedObject.mRef.end();
            }
        }
        return result;
    }

    private void parseLine() {
        Iterator<String> iter = regexpMap.keySet().iterator();
        foundMap.clear();
        while (iter.hasNext()) {
            String key = iter.next();
            ClassParser classParser = regexpMap.get(key);
            Pattern pRef = null;
            Matcher mRef = null;
            boolean isOpener = false;
            boolean found = false;
            if (classParser instanceof BipolarClassParser) {
                pRef = ((BipolarClassParser) classParser).openRegExpRepresentation;
                mRef = pRef.matcher(input);
                found = mRef.find();
                isOpener = found;
                if (!found) {
                    pRef = ((BipolarClassParser) classParser).closeRegExpRepresentation;
                    mRef = pRef.matcher(input);
                    found = mRef.find();
                }
            } else {
                pRef = classParser.openRegExpRepresentation;
                mRef = pRef.matcher(input);
                found = mRef.find();
            }

            if (found) {
                if ((mRef.start() == 0)) {
                    if (mRef.end() > getMaxLength()) {
                        foundMap.clear();
                    }
                    ParsedObject parsedObject = new ParsedObject();
                    parsedObject.mRef = mRef;
                    parsedObject.className = classParser.className;
                    parsedObject.isOpener = isOpener;
                    foundMap.put(key, parsedObject);
                }
            }
        }

    }

    public void regExpProcess() throws InvalidClassException, InvalidTokenException {
        while (!input.isEmpty()) {
            parseLine();
            Iterator<String> found = foundMap.keySet().iterator();
            if (foundMap.size() == 0) {
                throw new InvalidTokenException(input);
            }
            int end = 0;
            List<ParsedObject> parsedMultiple = new LinkedList<ParsedObject>();
            while (found.hasNext()) {
                String key = found.next();
                Matcher mRef = (foundMap.get(key)).mRef;
                String parsedString = input.substring(mRef.start(), mRef.end());
                ParsedObject parsedObject = null;
                String parsedClassName = foundMap.get(key).className;
                if ("java.lang.String".equals(parsedClassName)) {
                    stringPatternStartIdx = mRef.start();
                    StringBuffer constant = new StringBuffer();
                    int mStrEnd = getStringPatternEnd(input.substring(mRef.end()));
                    if ((mStrEnd > 0) && (mStrEnd <= input.length())) {
                        stringPatternEndIdx = mStrEnd;
                        String stringValue = input.substring(stringPatternStartIdx + 1, stringPatternEndIdx);
                        constant.append(stringValue);
                        parsedString = constant.toString();
                        parsedObject = new ParsedObject();
                        parsedObject.value = parsedString;
                        parsedObject.isOpener = foundMap.get(key).isOpener;
                        parsedObject.className = parsedClassName;
                        parsedObjectList.add(parsedObject);
                        input = input.substring(mStrEnd + 1);
                    } else {
                        throw new InvalidClassException("unclosed apostrophe");
                    }
                } else {
                    if (!("null".equals(parsedClassName) || "hu.infokristaly.g2.model.Parentheses".equals(parsedClassName))) {
                        parsedObject = new ParsedObject();
                        parsedObject.value = parsedString;
                        parsedObject.className = (foundMap.get(key)).className;
                        parsedObject.mRef = (foundMap.get(key)).mRef;
                        parsedMultiple.add(parsedObject);
                    } else if ("hu.infokristaly.g2.model.Parentheses".equals(parsedClassName)) {
                        parsedObject = new ParsedObject();
                        parsedObject.className = (foundMap.get(key)).className;
                        parsedObject.value = parsedString;
                        parsedObject.mRef = (foundMap.get(key)).mRef;
                        parsedObject.value = parsedString;
                        parsedObject.isOpener = foundMap.get(key).isOpener;
                        parsedObjectList.add(parsedObject);
                        if ("(".equals(parsedString)) {
                            parserParenthesesStack.push(parsedObject);
                        } else if (")".equals(parsedString)) {
                            if (parserParenthesesStack.isEmpty()) {
                                throw new InvalidClassException("parentheses counter dropped to negative");
                            } else {
                                parsedObject = parserParenthesesStack.pop();
                            }
                        }
                    }
                    if (mRef.end() > end) {
                        end = mRef.end();
                    }
                }
            }
            ParsedObject parsedObject = findPriority(parsedMultiple);
            if (parsedObject != null) {
                parsedObjectList.add(parsedObject);
            }
            input = input.substring(end);
        }

        if (!parserParenthesesStack.isEmpty()) {
            throw new InvalidClassException("parentheses counter is not null at end of process");
        }

    }

    private ParsedObject findPriority(List<ParsedObject> parsedMultiple) {
        ParsedObject result = parsedMultiple.size() > 0 ? parsedMultiple.get(0) : null;
        for (ParsedObject a : parsedMultiple) {
            if (ExpTreeMapper.isKeyword(a.className)) {
                result = a;
            }
        }
        return result;
    }

    private static int getStringPatternEnd(String sub) {
        Pattern pStr = Pattern.compile(REGEXP_STRING);
        Pattern pEsc = Pattern.compile(REGEXP_ESCAPE);
        Matcher m = null;
        int idx = 0;
        while (sub.length() > 0) {
            m = pEsc.matcher(sub);
            if (m.find()) {
                idx += (m.end() + 1);
                sub = sub.substring(m.end() + 1);
            } else {
                m = pStr.matcher(sub);
                if (m.find()) {
                    idx++;
                    break;
                } else {
                    idx++;
                    sub = sub.substring(1);
                }
            }
        }
        return idx;
    }

    public void regExpBuilder() {
        for (Class<?> item : ExpTreeMapper.RULE_CLASSES) {
            Field regexpField = null;
            ClassParser classParser = null;
            try {
                regexpField = item.getDeclaredField("regexp_representation");

                classParser = new ClassParser();
                classParser.openRepresentation = (String) item.getDeclaredField("representation").get(item);
                classParser.openRegExpRepresentation = Pattern.compile("^" + regexpField.get(item));
                classParser.className = item.getName();
                classParser.keywords = (String[])item.getDeclaredField("keywords_representation").get(item);
                regexpMap.put(classParser.openRepresentation, classParser);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                System.out.println(item.getName());
                e.printStackTrace();
            }
        }
        for (Class<?> item : EXPRESSION_CLASSES) {
            Field regexpField = null;
            ClassParser classParser = null;

            try {
                regexpField = item.getDeclaredField("regexp_representation");
                classParser = new ClassParser();
                classParser.openRepresentation = (String) item.getDeclaredField("representation").get(item);
                classParser.openRegExpRepresentation = Pattern.compile("^" + regexpField.get(item));
                classParser.className = item.getName();
                regexpMap.put(classParser.openRepresentation, classParser);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
                try {
                    regexpField = item.getDeclaredField("representation");

                    classParser = new ClassParser();
                    classParser.openRepresentation = (String) regexpField.get(item);
                    classParser.openRegExpRepresentation = Pattern.compile("^" + regexpField.get(item));
                    classParser.className = item.getName();
                    regexpMap.put(classParser.openRepresentation, classParser);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e2) {
                    try {
                        Field openRepresentation = item.getDeclaredField("openRepresentation");
                        Field closeRepresentation = item.getDeclaredField("closeRepresentation");

                        Field openRegExpRepresentation = item.getDeclaredField("openRegExpRepresentation");
                        Field closeRegExpRepresentation = item.getDeclaredField("closeRegExpRepresentation");

                        classParser = new BipolarClassParser((String) openRepresentation.get(item), (String) closeRepresentation.get(item));

                        classParser.openRepresentation = (String) openRepresentation.get(item);
                        ((BipolarClassParser) classParser).closeRepresentation = (String) closeRepresentation.get(item);

                        classParser.openRegExpRepresentation = Pattern.compile((String) openRegExpRepresentation.get(item));
                        ((BipolarClassParser) classParser).closeRegExpRepresentation = Pattern.compile((String) closeRegExpRepresentation.get(item));

                        classParser.className = item.getName();
                        regexpMap.put(classParser.openRepresentation + ((BipolarClassParser) classParser).closeRepresentation, classParser);

                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                        System.out.println(item.getName());
                        e.printStackTrace();
                    }
                }
            }

        }

        appendStaticPattern("\"", Pattern.compile(REGEXP_STRING, Pattern.UNICODE_CHARACTER_CLASS), "java.lang.String");
        appendStaticPattern("space", Pattern.compile(REGEXP_SPACE, Pattern.UNICODE_CHARACTER_CLASS), "null");
        appendStaticPattern("digits", Pattern.compile(REGEXP_DIGITS, Pattern.UNICODE_CHARACTER_CLASS), "java.math.BigDecimal");
    }

    private void appendStaticPattern(String key, Pattern value, String className) {
        ClassParser classParser = new ClassParser();
        classParser.openRegExpRepresentation = value;
        classParser.className = className;
        regexpMap.put(key, classParser);
    }

    public void setRefObjects(HashMap<String, Object> objects) {
        wp.setObjects(objects);
    }

    public HashMap<String, Object> getRefObjects() {
        return wp.getObjects();
    }

    public void addRule(Rule r) {
        wp.getObjects().put(r.getName(), r);
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void clearBuilder() {
        parsedObjectList.clear();
        objectStack.clear();
    }

    public void replaceRule(Rule r1) {
        removeRule(r1.getName());
        addRule(r1);
        wp.resolveReferences();
    }

    private void removeRule(String name) {
        wp.getObjects().remove(name);
    }

    public Workspace getWp() {
        return wp;
    }

    public void processRules() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, InvalidOrderExpression, InvalidClassException,
            InvalidTokenException {
        regExpProcess();
        ExpOrValue exp = buildExpTree();
    }
}
