package model;

/**
 * Created by Ott Konstantin on 15.03.2016.
 */
public class Facet implements Comparable<Facet>{
    public String typ;
    public String token;

    public Facet(String type, String token) {
        this.token=token;
        this.typ=type;
    }

    @Override
    public int compareTo(Facet o) {
        return 0;
    }
}
