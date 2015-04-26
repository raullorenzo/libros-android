package edu.upc.eetac.dsa.iarroyo.books.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nacho on 25/04/15.
 */
public class ReviewCollection {


    private List<Review> reviews;

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    private Map<String, Link> links = new HashMap<String, Link>();

    public ReviewCollection(){

        super();
        reviews = new ArrayList<>();
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }


    public void addReview(Review review) {
        reviews.add(review);
    }
}
