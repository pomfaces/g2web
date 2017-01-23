package hu.infokristaly.g2.expression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import hu.infokristaly.g2.exceptions.InvalidOrderExpression;
import hu.infokristaly.g2.model.ExpOrValue;
import hu.infokristaly.g2.model.IfRule;
import hu.infokristaly.g2.model.Minus;
import hu.infokristaly.g2.model.NumNegation;
import hu.infokristaly.g2.model.Parentheses;
import hu.infokristaly.g2.model.ParsedObject;
import hu.infokristaly.g2.model.Reference;
import hu.infokristaly.g2.model.TwoArgOperator;
import hu.infokristaly.g2.model.Unary;
import hu.infokristaly.g2.model.Unnamed;
import hu.infokristaly.g2.model.UnnamedConstant;
import hu.infokristaly.g2.model.Workspace;

public class ExpTreeMapper {

    public static Class<?>[] RULE_CLASSES = { IfRule.class };

    private ExpOrValue valueObj = null;
    private Class classObj = null;
    private ParsedObject lastParseditem;
    private Workspace wp;
    private Stack<ExpOrValue> objectStack = new Stack<ExpOrValue>();
    private Stack<Stack<ExpOrValue>> parenthesesStack = new Stack<Stack<ExpOrValue>>();
    private ExpOrValue result;

    private List<ParsedObject> parsedObjectList;
    private Iterator<ParsedObject> iter;

    public ExpTreeMapper(Workspace wp, List<ParsedObject> parsedObjectList) {
        this.parsedObjectList = parsedObjectList;
        this.wp = wp;
    }

    public static boolean isKeyword(String a) {
        boolean result = false;
        for (Class<?> classItem : RULE_CLASSES) {
            if (classItem.getName().equals(a)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void setTreeElement() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, InvalidOrderExpression {
        if ("java.math.BigDecimal".equals(lastParseditem.className)) {
            classObj = UnnamedConstant.class;
            valueObj = new UnnamedConstant<BigDecimal>(BigDecimal.valueOf(Double.valueOf(lastParseditem.value)));
        } else if ("java.lang.String".equals(lastParseditem.className)) {
            classObj = UnnamedConstant.class;
            valueObj = new UnnamedConstant<String>(lastParseditem.value);
            Method setValueMethod = classObj.getMethod("setValue", Object.class);
            setValueMethod.invoke(valueObj, lastParseditem.value);
        } else if ("hu.infokristaly.g2.model.Reference".equals(lastParseditem.className)) {
            classObj = Class.forName(lastParseditem.className);
            valueObj = (ExpOrValue) classObj.newInstance();
            Method setValueMethod = classObj.getMethod("setRefName", String.class);
            setValueMethod.invoke(valueObj, lastParseditem.value);
            ((Reference) valueObj).setObjSpace(wp);
        } else if (!isKeyword(lastParseditem.className)) {
            classObj = Class.forName(lastParseditem.className);
            valueObj = (ExpOrValue) classObj.newInstance();
        } else {
            classObj = Class.forName(lastParseditem.className);
            valueObj = null;
        }
    }

    private void setFirstElementInStack() throws InvalidOrderExpression {
        if (valueObj instanceof Parentheses) {
            if (lastParseditem.isOpener) {
                objectStack.push(valueObj);
                parenthesesStack.push(objectStack);
                objectStack = new Stack<ExpOrValue>();
            } else {
                throw new InvalidOrderExpression("Not opener for pharentheses (" + valueObj.getClass().getName() + ")");
            }
        } else {
            if (valueObj instanceof Minus) {
                valueObj = new NumNegation();
            }
            objectStack.push(valueObj);
        }
    }

    private boolean isUnnamedOrReference(ExpOrValue peek) {
        boolean result = false;
        if (peek instanceof Unnamed || peek instanceof Reference) {
            result = true;
        }
        return result;
    }

    private static boolean isUnary(ExpOrValue peek) {
        Boolean result = (peek instanceof Unary);
        return result;
    }

    private void setLeftValueOrGetBackFromParentheses() throws InvalidOrderExpression {
        if (valueObj instanceof TwoArgOperator) {
            // akkor a balértéket beállítjuk és push
            setLeftAndPush();
        } else if (valueObj instanceof Parentheses) {
            if (!lastParseditem.isOpener) {
                getBackFromParentheses();
            } else {
                throw new InvalidOrderExpression("Opener found instaid closer parentheses (" + valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName() + ")");
            }
        } else {
            throw new InvalidOrderExpression(valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName());
        }
    }

    private void getBackFromParentheses() throws InvalidOrderExpression {
        ExpOrValue pInner = objectStack.firstElement();
        Stack<ExpOrValue> popParentheses = parenthesesStack.pop();
        if (popParentheses != null) {
            objectStack = popParentheses;
            ExpOrValue parentheses;
            if (objectStack.size() > 1) {
                parentheses = objectStack.pop();
            } else {
                parentheses = objectStack.peek();
            }
            if (parentheses instanceof Parentheses) {
                ((Parentheses) parentheses).setObject(pInner);
            } else {
                throw new InvalidOrderExpression("can't put inner expression to pharenthese");
            }
        } else {
            throw new InvalidOrderExpression("parenthesesStack is empty");
        }
    }

    private void setPeekOrGetBackFromParentheses() throws InvalidOrderExpression {
        if (((Unary) objectStack.peek()).getObject() == null) {
            ((Unary) objectStack.peek()).setObject((ExpOrValue) valueObj);
            if (valueObj instanceof Parentheses) {
                if (lastParseditem.isOpener) {
                    objectStack.push(valueObj);
                    parenthesesStack.push(objectStack);
                    objectStack = new Stack<ExpOrValue>();
                } else {
                    throw new InvalidOrderExpression("Not opener for pharentheses (" + valueObj.getClass().getName() + ")");
                }
            }
        } else if (valueObj instanceof Parentheses) {
            if (!lastParseditem.isOpener) {
                getBackFromParentheses();
            } else {
                throw new InvalidOrderExpression("Opener found instaid closer parentheses (" + valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName() + ")");
            }
        } else {
            throw new InvalidOrderExpression("Not null value in unary operator");
        }
    }

    private void setIfPeekNotNumNegation() throws InvalidOrderExpression {
        if (((Unary) objectStack.peek()).getObject() == null) {
            if (objectStack.peek() instanceof NumNegation) {
                throw new InvalidOrderExpression(valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName());
            } else {
                valueObj = new NumNegation();
                ((Unary) objectStack.peek()).setObject((ExpOrValue) valueObj);
            }
        } else {
            throw new InvalidOrderExpression("Not null value in unary operator");
        }
    }

    private void setIfPeekNumNegation() throws InvalidOrderExpression {

        if (((Unary) objectStack.peek()).getObject() == null) {
            if (objectStack.peek() instanceof NumNegation) {
                if (objectStack.size() > 1) {
                    ((NumNegation) objectStack.pop()).setObject(valueObj);
                } else {
                    ((NumNegation) objectStack.peek()).setObject(valueObj);
                }
            } else {
                ((Unary) objectStack.peek()).setObject((ExpOrValue) valueObj);
            }
        }
    }

    private void setLeftAndPush() throws InvalidOrderExpression {
        if (valueObj instanceof TwoArgOperator) {
            ((TwoArgOperator) valueObj).setLeft(objectStack.pop());
            objectStack.push(valueObj);
        } else {
            throw new InvalidOrderExpression(valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName());
        }
    }

    private void setRightValue() throws InvalidOrderExpression {
        TwoArgOperator lastOp = (TwoArgOperator) objectStack.peek();
        if (isUnary(valueObj) || isUnnamedOrReference(valueObj)) {
            lastOp.setRight(valueObj);
            if (valueObj instanceof Parentheses) {
                if (lastParseditem.isOpener) {
                    objectStack.push(valueObj);
                    parenthesesStack.push(objectStack);
                    objectStack = new Stack<ExpOrValue>();
                } else {
                    throw new InvalidOrderExpression(valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName());
                }
            }
        } else if (valueObj instanceof Minus) {
            valueObj = new NumNegation();
            lastOp.setRight(valueObj);
            objectStack.push(valueObj);
        } else {
            throw new InvalidOrderExpression(valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName());
        }
    }

    private void changeRightValue() throws InvalidOrderExpression {
        TwoArgOperator lastOp = (TwoArgOperator) objectStack.peek();
        if (valueObj instanceof TwoArgOperator) {
            // Ha valueObj prioritása nagyobb mint peek
            if (valueObj.getPrecedence() < lastOp.getPrecedence()) {
                Object right = lastOp.getRight();
                lastOp.setRight(valueObj);
                ((TwoArgOperator) valueObj).setLeft(right);
                objectStack.push(valueObj);
            } else {
                // Ha lastOp prioritása nagyobb
                ExpOrValue popOp = objectStack.pop();
                // popOp prioritása kisebb
                while ((objectStack.size() > 0) && (objectStack.peek().getPrecedence() <= valueObj.getPrecedence())) {
                    popOp = objectStack.pop();
                }
                ((TwoArgOperator) valueObj).setLeft(popOp);
                if (objectStack.size() > 0) {
                    ((TwoArgOperator) objectStack.peek()).setRight(valueObj);
                }
                objectStack.push(valueObj);
            }
        } else if (valueObj instanceof Parentheses) {
            if (!lastParseditem.isOpener) {
                getBackFromParentheses();
            } else {
                throw new InvalidOrderExpression("Opener found instaid closer parentheses (" + valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName() + ")");
            }
        } else {
            throw new InvalidOrderExpression("invalid order (" + valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName() + ")");
        }
    }

    public void buildTreeMap() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, InvalidOrderExpression {
        iter = parsedObjectList.iterator();
        while (iter.hasNext()) {
            lastParseditem = iter.next();
            setTreeElement();
            if (objectStack.isEmpty()) {
                if (valueObj != null) {
                    setFirstElementInStack();
                }
            } else {
                if ((objectStack.peek() instanceof Unnamed) || (objectStack.peek() instanceof Reference)) {
                    setLeftValueOrGetBackFromParentheses();
                } else if (objectStack.peek() instanceof Parentheses) {
                    setLeftAndPush();
                } else if (objectStack.peek() instanceof Unary) {
                    if (isUnary(valueObj)) {
                        setPeekOrGetBackFromParentheses();
                    } else if (valueObj instanceof Minus) {
                        setIfPeekNotNumNegation();
                    } else if (isUnnamedOrReference(valueObj)) {
                        setIfPeekNumNegation();
                    } else if (valueObj instanceof TwoArgOperator) {
                        setLeftAndPush();
                    } else {
                        throw new InvalidOrderExpression(valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName());
                    }
                } else if (objectStack.peek() instanceof TwoArgOperator) {
                    TwoArgOperator lastOp = (TwoArgOperator) objectStack.peek();
                    if (lastOp.getRight() == null) {
                        setRightValue();
                    } else {
                        changeRightValue();
                    }
                } else {
                    throw new InvalidOrderExpression(valueObj.getClass().getName() + " after " + objectStack.peek().getClass().getName());
                }
            }
        }
        result = objectStack.firstElement();
    }

    public ExpOrValue getResult() {
        return this.result;
    }
}
