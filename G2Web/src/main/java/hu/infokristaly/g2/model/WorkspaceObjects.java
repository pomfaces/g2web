/**
 * 
 */
package hu.infokristaly.g2.model;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author pzoli
 *
 */
@XmlType
public class WorkspaceObjects {
    private HashMap<String, Object> objects = new HashMap<String, Object>();

    public WorkspaceObjects(){
        super();
    }
    
    @XmlElement
    public HashMap<String, Object> getObjects() {
        return objects;
    }

    public void setObjects(HashMap<String, Object> objects) {
        this.objects = objects;
    }
}
