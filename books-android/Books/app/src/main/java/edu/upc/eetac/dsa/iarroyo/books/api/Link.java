package edu.upc.eetac.dsa.iarroyo.books.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nacho on 25/04/15.
 */
public class Link {

    private String target;
    private Map<String, String> parameters;

    public Link() {
        parameters = new HashMap<String, String>();
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
