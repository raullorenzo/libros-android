package edu.upc.eetac.dsa.iarroyo.books.api.model;

import java.util.ArrayList;
import java.util.List;

public class AutorCollection {

	private List<Autor> authors;
	 


	public AutorCollection() {
	super();
	authors = new ArrayList<>();
	}
	 
	public List<Autor> getAuthors() {
		return authors;
	}



	public void setAuthors(List<Autor> authors) {
		this.authors = authors;
	}


	 
	public void addAuthor(Autor autor) {
	authors.add(autor);
	}
	
	
}
