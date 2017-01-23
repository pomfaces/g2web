/**
 * 
 */
package hu.infokristaly.g2.model;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.sun.tools.corba.se.logutil.StringUtil;

/**
 * @author pzoli
 * @param <T>
 *
 */
@XmlType
public class Reference<T> extends ExpOrValue<T> {

    public final static String regexp_representation = new String("[\\p{L}_]{1}[\\p{L}\\d_]*(\\.[\\p{L}_]{1}[\\p{L}\\d_]*)*");
    public final static String representation = new String("reference");
    
    private Workspace objSpace;
    private ExpOrValue<T> value;

    private String refName;

    public Reference() {
    }

    public Reference(String refName) {
        this.refName = refName;
    }

    @XmlElement
    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    @XmlTransient
    public Workspace getObjSpace() {
        return objSpace;
    }

    public void setObjSpace(Workspace objSpace) {
        this.objSpace = objSpace;
        if ((refName != null) && (objSpace != null)) {
            String[] objNameSpace = refName.split("\\.");
            if ((value == null) && (objNameSpace != null) && (objNameSpace.length > 1)) {
                String[] objName = Arrays.copyOfRange(objNameSpace,1,objNameSpace.length);
                String name = String.join(".",objName);
                value = objSpace.getObjectByName(name);
            }
        }
    }

    @Override
    public T getValue() {
        return (value != null ? value.getValue() : null);
    }

    public ExpOrValue<T> getObject() {
        return value;
    }

    @Override
    public String toString() {
        String result = " " + refName + ":" + (value != null ? value.toString() : "null") + " ";
        return result;
    }
    
    public int getPrecedence() {
        return 0;
    }
}
