package hu.infokristaly.g2.model;

import java.util.regex.Pattern;

public class BipolarClassParser extends ClassParser {
    public String openRepresentation;
    public String closeRepresentation;
    
    public BipolarClassParser(String openRepresentation, String closeRepresentation) {
        this.openRepresentation = openRepresentation;
        this.closeRepresentation = closeRepresentation;
    }
    
    public Pattern closeRegExpRepresentation;
}
