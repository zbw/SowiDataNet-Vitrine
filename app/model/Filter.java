package model;

/**
 * Created by Ott Konstantin on 23.03.2016.
 */
public class Filter {
    public int id;
    public String typ;
    public String filter;
    public String relationalOperator;
    public String queryWithoutFilter;


    @Override
    public boolean equals(Object o) {
       if (o instanceof Filter) {
           Filter cf = (Filter) o;
           if (this.id == cf.id) {
               return true;
           }
       }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean isEmpty() {
        if (filter == null || filter.equals("")) {
            return true;
        } else {
            return false;
        }
    }

}
