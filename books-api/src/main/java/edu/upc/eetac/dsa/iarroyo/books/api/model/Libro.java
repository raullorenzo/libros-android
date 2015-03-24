package edu.upc.eetac.dsa.iarroyo.books.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

import edu.upc.eetac.dsa.iarroyo.books.api.BookResource;
import edu.upc.eetac.dsa.iarroyo.books.api.MediaType;

public class Libro {
	@InjectLinks({
			@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "libro", title = "Consulta ultimos libros", type = MediaType.BOOKS_API_BOOK_COLLECTION),
			@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "self edit", title = "Accede a este libro", type = MediaType.BOOKS_API_BOOK, method = "getBook", bindings = @Binding(name = "id", value = "${instance.id}")),
			@InjectLink(value = "/books/reviews/{id}", style = Style.ABSOLUTE, rel = "focus", title = "Reviews Libro", type = MediaType.BOOKS_API_BOOK, bindings = { @Binding(name = "id", value = "${instance.id}") }) })
	private List<Link> links;
	private int id;
	private String titulo;
	private String lengua;
	private String edicion;
	private long fecha_edicion;
	private long fecha_impresion;
	private String editorial;
	private List<Review> reviews;

	public Libro() {
		super();
		reviews = new ArrayList<>();
	}

	public long getFecha_edicion() {
		return fecha_edicion;
	}

	public void setFecha_edicion(long fecha_edicion) {
		this.fecha_edicion = fecha_edicion;
	}

	public long getFecha_impresion() {
		return fecha_impresion;
	}

	public void setFecha_impresion(long fecha_impresion) {
		this.fecha_impresion = fecha_impresion;
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

	private long lastModified;

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getLengua() {
		return lengua;
	}

	public void setLengua(String lengua) {
		this.lengua = lengua;
	}

	public String getEdicion() {
		return edicion;
	}

	public void setEdicion(String edicion) {
		this.edicion = edicion;
	}

	public String getEditorial() {
		return editorial;
	}

	public void setEditorial(String editorial) {
		this.editorial = editorial;
	}

}
