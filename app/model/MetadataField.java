package model;

/**
 * Created by IntelliJ IDEA.
 * User: peterdietz
 * Date: 8/11/12
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetadataField implements Comparable<MetadataField>{
    public String key, value;
    public int order;
    
    public MetadataField(String key, String value, int order) {
        this.key = key;
        this.value = value;
        this.order = order;
    }
    
    public String getKey() {
        return key;
        //return schema + "." + element + ((qualifier != null) ? "." + qualifier : "");
    }
    
    public String getValue() {
        return value;
    }
    
    public String toString() {
        return getKey() + " : " + getValue();
    }

    @Override
    public int compareTo(MetadataField o) {
        if (this.order < o.order) {
            return -1;
        } else if (this.order == o.order) {
            return 0;
        } else {
            return 1;
        }

    }
}
