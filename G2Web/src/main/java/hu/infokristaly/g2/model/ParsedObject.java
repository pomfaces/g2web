package hu.infokristaly.g2.model;

import java.util.regex.Matcher;

public class ParsedObject {
    public String value;
    public String className;
    public Matcher mRef;
    public boolean isOpener;
    public ExpOrValue<?> object;
}
