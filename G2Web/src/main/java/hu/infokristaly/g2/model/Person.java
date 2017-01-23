/**
 * 
 */
package hu.infokristaly.g2.model;

/**
 * @author pzoli
 *
 */
public class Person {
    private int id;
    private byte age;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getAge() {
        return age;
    }

    public void setAge(byte age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "id:" + id + "; age:" + age + "; name:" + name;
    }
}
