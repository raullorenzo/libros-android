package edu.upc.eetac.dsa.iarroyo.books.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nacho on 25/04/15.
 */
public class BooksRootAPI {

    private Map<String, Link> links;

    public BooksRootAPI() {
        links = new HashMap<String, Link>();
    }

    public Map<String, Link> getLinks() {
        return links;
    }
}
