package edu.upc.eetac.dsa.iarroyo.books.api.model;

import java.util.ArrayList;
import java.util.List;

public class ReviewCollection {

	private List<Review> reviews;

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
