package edu.upc.eetac.dsa.iarroyo.books.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import edu.upc.eetac.dsa.iarroyo.books.api.model.Libro;
import edu.upc.eetac.dsa.iarroyo.books.api.model.LibroCollection;
import edu.upc.eetac.dsa.iarroyo.books.api.model.Review;
import edu.upc.eetac.dsa.iarroyo.books.api.model.ReviewCollection;

@Path("/books")
public class BookResource {
	@Context
	private SecurityContext security;
	private DataSource ds = DataSourceSPA.getInstance().getDataSource();

	private String GET_BOOK_BY_ID_QUERY = "select * from libro where libro.id=?";

	/*
	 * 
	 * 
	 * RECURSO CACHEABLE
	 */

	private Libro getBookFromDatabase(String id) {
		Libro book = new Libro();

		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(GET_BOOK_BY_ID_QUERY);
			stmt.setInt(1, Integer.valueOf(id));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				book.setId(rs.getInt("id"));
				book.setTitulo(rs.getString("titulo"));
				book.setLengua(rs.getString("lengua"));
				book.setEdicion(rs.getString("edicion"));
				book.setFecha_edicion(rs.getTimestamp("fecha_edicion")
						.getTime());
				book.setFecha_impresion(rs.getTimestamp("fecha_impresion")
						.getTime());
				book.setEditorial(rs.getString("editorial"));
			} else {
				throw new NotFoundException("There's no book with id= " + id);
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}

		return book;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.BOOKS_API_BOOK)
	public Response getBook(@PathParam("id") String id, @Context Request request) {
		// Create CacheControl
		CacheControl cc = new CacheControl();

		Libro book = getBookFromDatabase(id);

		// Calculate the ETag on last modified date of user resource
		EntityTag eTag = new EntityTag(Long.toString(book.getLastModified()));

		// Verify if it matched with etag available in http request
		Response.ResponseBuilder rb = request.evaluatePreconditions(eTag);

		// If ETag matches the rb will be non-null;
		// Use the rb to return the response without any further processing
		if (rb != null) {
			return rb.cacheControl(cc).tag(eTag).build();
		}

		// If rb is null then either it is first time request; or resource is
		// modified
		// Get the updated representation and return with Etag attached to it
		rb = Response.ok(book).cacheControl(cc).tag(eTag);

		return rb.build();
	}

	/* GET DE TODOS LOS BOOOKSSS */

	// private String GET_BOOKS_QUERY = "select * from libro";
	private String GET_LIBROS_QUERY = "select lib.*, rev.*, rel.autorid, aut.nombre from libro lib, reviews rev, relLibroAutor rel, autor aut  where lib.id=rel.libroid and rel.autorid=aut.aid and lib.id=rev.libroid and lib.fecha_edicion < ifnull(?, now()) order by fecha_impresion desc limit ?";
	private String GET_LIBROS_QUERY_FROM_LAST = " select lib.*, rev.*, rel.autorid, aut.nombre from libro lib, reviews rev, relLibroAutor rel, autor aut where lib.id=rel.libroid and rel.autorid=aut.aid and lib.id=rev.libroid and lib.fecha_edicion > ? order by lib.id";

	@GET
	@Produces(MediaType.BOOKS_API_BOOK_COLLECTION)
	public LibroCollection getBooks(@QueryParam("length") int length,
			@QueryParam("before") long before, @QueryParam("after") long after) {
		LibroCollection books = new LibroCollection();

		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {

			boolean updateFromLast = after > 0;
			stmt = updateFromLast ? conn
					.prepareStatement(GET_LIBROS_QUERY_FROM_LAST) : conn
					.prepareStatement(GET_LIBROS_QUERY);
			if (updateFromLast) {
				stmt.setTimestamp(1, new Timestamp(after));
			} else {
				if (before > 0)
					stmt.setTimestamp(1, new Timestamp(before));
				else
					stmt.setTimestamp(1, null);
				length = (length <= 0) ? 5 : length;// si lenght menor a 0 coge
				// valor a 5 sino coge valor
				// por defecto de lenght
				stmt.setInt(2, length);
			}

			ResultSet rs = stmt.executeQuery();
			boolean first = true;
			long oldestTimestamp = 0;

			while (rs.next()) {

				Libro book;
				if (books.getLibro(rs.getInt("libroid")) == null) {
					book = new Libro();
					book.setId(rs.getInt("id"));
					book.setTitulo(rs.getString("titulo"));
					book.setLengua(rs.getString("lengua"));
					book.setEdicion(rs.getString("edicion"));
					book.setFecha_edicion(rs.getTimestamp("fecha_edicion")
							.getTime());
					oldestTimestamp = rs.getTimestamp("fecha_impresion")
							.getTime();
					book.setFecha_impresion(oldestTimestamp);
					book.setEditorial(rs.getString("editorial"));
					books.addBook(book);
					if (first) {
						first = false;
						books.setNewestTimestamp(book.getFecha_impresion());
					}

				} else {

					book = books.getLibro(rs.getInt("libroid"));
				}

				Review review = new Review();
				review.setReseñaid(rs.getInt("reseñaid"));
				review.setLibroid(rs.getInt("libroid"));
				review.setUsername(rs.getString("username"));
				review.setName(rs.getString("name"));
				review.setUltima_fecha_hora(rs
						.getTimestamp("ultima_fecha_hora").getTime());
				review.setTexto(rs.getString("texto"));
				book.addReview(review);

				if (books.getLibro(rs.getInt("libroid")) == null) {
					books.addBooks(book);
				}
			}
			books.setOldestTimestamp(oldestTimestamp);

		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}

		return books;
	}
	
	private String GET_LIBROS_QUERY_BY_TITLE = "select lib.*, aut.nombre from libro lib, autor aut, relLibroAutor rel where rel.libroid=lib.id and rel.autorid=aut.aid and lib.fecha_edicion < ifnull(?, now()) and lib.titulo=? order by fecha_edicion desc limit ?";
	private String GET_LIBROS_QUERY_BY_TITLE_FROM_LAST = "select lib.*, aut.nombre, rel.autorid from libro lib, autor aut, relLibroAutor rel where rel.autorid=aut.aid and lib.fecha_edicion > ? and lib.titulo=? order by fecha_edicion desc";
	
	@GET
	@Path("/search")
	@Produces(MediaType.BOOKS_API_BOOK_COLLECTION)
	public LibroCollection getBooksParameters(
			@QueryParam("titulo") String titulo,
			@QueryParam("length") int length,
			@QueryParam("before") long before, @QueryParam("after") long after) {
		LibroCollection books = new LibroCollection();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			boolean updateFromLast = after > 0;
			boolean whichstmt = titulo != null;
			if (whichstmt) {

				stmt = updateFromLast ? conn
						.prepareStatement(GET_LIBROS_QUERY_BY_TITLE_FROM_LAST)
						: conn.prepareStatement(GET_LIBROS_QUERY_BY_TITLE);
				if (updateFromLast) {
					stmt.setTimestamp(1, new Timestamp(after));
				} else {
					if (before > 0)
						stmt.setTimestamp(1, new Timestamp(before));
					else
						stmt.setTimestamp(1, null);
					length = (length <= 0) ? 5 : length;// si lenght menor a 0
					// coge valor a 5 sino
					// coge valor por
					// defecto de lenght
					stmt.setInt(3, length);
				}
				stmt.setString(2, titulo);
			}
			ResultSet rs = stmt.executeQuery();
			boolean first = true;
			long oldestTimestamp = 0;
			while (rs.next()) {
				Libro book = new Libro();
				book.setId(rs.getInt("id"));
				book.setTitulo(rs.getString("titulo"));
				book.setLengua(rs.getString("lengua"));
				book.setEdicion(rs.getString("edicion"));
				book.setFecha_edicion(rs.getTimestamp("fecha_edicion")
						.getTime());
				oldestTimestamp = rs.getTimestamp("fecha_impresion").getTime();
				book.setFecha_impresion(oldestTimestamp);
				book.setEditorial(rs.getString("editorial"));

				if (first) {
					first = false;
					books.setNewestTimestamp(book.getFecha_impresion());
				}
				books.addBooks(book);
			}
			books.setOldestTimestamp(oldestTimestamp);
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		return books;
	}

	/* AÑADIR LIBRO A LA COLECCION DE LIBROS */
	private String INSERT_BOOK_QUERY = "insert into libro (id, titulo, lengua, edicion, editorial) values (?, ?, ?, ?, ?)";

	@POST
	@Consumes(MediaType.BOOKS_API_BOOK)
	@Produces(MediaType.BOOKS_API_BOOK)
	public Libro createBook(Libro book) {
		
		System.out.println("hi");
		validateBook(book);
		validateAdmin();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(INSERT_BOOK_QUERY,
					Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1, Integer.toString(book.getId()));
			stmt.setString(2, book.getTitulo());
			stmt.setString(3, book.getLengua());
			stmt.setString(4, book.getEdicion());
			stmt.setString(5, book.getEditorial());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				int id = rs.getInt(1);

				book = getBookFromDatabase(Integer.toString(id));
			} else {
				// Something has failed...
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}

		return book;
	}

	private void validateBook(Libro book) {
		if (book.getTitulo() == null)
			throw new BadRequestException("Titulo can't be null.");
		if (book.getLengua() == null)
			throw new BadRequestException("Lengua can't be null.");
		if (book.getEdicion() == null)
			throw new BadRequestException("Edicion can't be null.");

		if (book.getEditorial() == null)
			throw new BadRequestException("Editorial can't be null.");
		if (book.getTitulo().length() > 20)
			throw new BadRequestException(
					"Titulo can't be greater than 100 characters.");
		if (book.getLengua().length() > 10)
			throw new BadRequestException(
					"Content can't be greater than 500 characters.");
		if (book.getEdicion().length() > 100)
			throw new BadRequestException(
					"Edicion can't be greater than 100 characters.");

		if (book.getEditorial().length() > 20)
			throw new BadRequestException(
					"Editorial can't be greater than 500 characters.");
	}

	/* BORRAR LIBRO A LA COLECCION DE LIBROS */

	private String DELETE_BOOK_QUERY = "delete from libro where libro.id=?";

	@DELETE
	@Path("/{id}")
	public void deleteBook(@PathParam("id") String id) {
		validateAdmin();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(DELETE_BOOK_QUERY);
			stmt.setInt(1, Integer.valueOf(id));

			int rows = stmt.executeUpdate();
			if (rows == 0)
				throw new NotFoundException("No hay ningún libro con id= " + id);
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

	/* UPDATE DE UN LIBRO */

	private String UPDATE_BOOK_QUERY = "update libro set titulo=ifnull(?, titulo), lengua=ifnull(?, lengua), edicion=ifnull(?, edicion), editorial=ifnull(?, editorial) where id=?";

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.BOOKS_API_BOOK)
	@Produces(MediaType.BOOKS_API_BOOK)
	public Libro updateBook(@PathParam("id") String id, Libro book) {
		validateAdmin();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(UPDATE_BOOK_QUERY);
			stmt.setString(1, book.getTitulo());
			stmt.setString(2, book.getLengua());
			stmt.setString(3, book.getEdicion());
			stmt.setString(4, book.getEditorial());
			stmt.setInt(5, Integer.valueOf(id));

			int rows = stmt.executeUpdate();
			if (rows == 1)
				book = getBookFromDatabase(id);
			else {
				throw new NotFoundException("There's no book with id=" + id);
			}

		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}

		return book;
	}

	/*
	 * 
	 * 
	 * 
	 * Reviewss
	 */

	/* GET DE UNA REVIEW POR SU ID */

	private String GET_REVIEW_BY_ID_QUERY = "select * from reviews where reviews.libroid=?";

	public Review getReviewLibroId(String libroid) {
		Review review = new Review();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(GET_REVIEW_BY_ID_QUERY);
			stmt.setInt(1, Integer.valueOf(libroid));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				review.setReseñaid(rs.getInt("reseñaid"));
				review.setLibroid(rs.getInt("libroid"));
				review.setUsername(rs.getString("username"));
				review.setName(rs.getString("name"));
				review.setUltima_fecha_hora(rs
						.getTimestamp("ultima_fecha_hora").getTime());
				review.setTexto(rs.getString("texto"));
			} else {
				throw new NotFoundException("No hay ningún libro con id= "
						+ libroid);
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		return review;
	}

	private String GET_REVIEW_BY_REVIEWID_QUERY = "select * from reviews where reviews.reseñaid=?";

	public Review getReviewReseñaId(String reseñaid) {
		Review review = new Review();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(GET_REVIEW_BY_REVIEWID_QUERY);
			stmt.setInt(1, Integer.valueOf(reseñaid));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				review.setReseñaid(rs.getInt("reseñaid"));
				review.setLibroid(rs.getInt("libroid"));
				review.setUsername(rs.getString("username"));
				review.setName(rs.getString("name"));
				review.setUltima_fecha_hora(rs
						.getTimestamp("ultima_fecha_hora").getTime());
				review.setTexto(rs.getString("texto"));
			} else {
				throw new NotFoundException("No hay ninguna reseña con id= "
						+ reseñaid);
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		return review;
	}

	/* GET DE TODAS LAS REVIEWS */
	private String GET_REVIEWS_QUERY = "select * from reviews";

	@GET
	@Path("/reviews")
	@Produces(MediaType.REVIEWS_API_REVIEW_COLLECTION)
	public ReviewCollection getReviews() {
		ReviewCollection reviews = new ReviewCollection();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(GET_REVIEWS_QUERY);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Review review = new Review();
				review.setReseñaid(rs.getInt("reseñaid"));
				review.setLibroid(rs.getInt("libroid"));
				review.setUsername(rs.getString("username"));
				review.setName(rs.getString("name"));
				review.setUltima_fecha_hora(rs
						.getTimestamp("ultima_fecha_hora").getTime());
				review.setTexto(rs.getString("texto"));
				reviews.addReview(review);
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		return reviews;
	}

	/* GET DE TODAS LAS REVIEWS DE UN LIBRO */

	String GET_REVIEW_QUERY_FROM_ID = "select * from reviews where reviews.libroid=?";

	@GET
	@Path("/reviews/{libroid}")
	@Produces(MediaType.REVIEWS_API_REVIEW_COLLECTION)
	public ReviewCollection getReviewLibro(@PathParam("libroid") String libroid) {

		ReviewCollection reviews = new ReviewCollection();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(GET_REVIEW_QUERY_FROM_ID);
			stmt.setInt(1, Integer.valueOf(libroid));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Review review = new Review();
				review.setReseñaid(rs.getInt("reseñaid"));
				review.setLibroid(rs.getInt("libroid"));
				review.setUsername(rs.getString("username"));
				review.setName(rs.getString("name"));
				review.setUltima_fecha_hora(rs
						.getTimestamp("ultima_fecha_hora").getTime());
				review.setTexto(rs.getString("texto"));
				reviews.addReview(review);

			}

		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		return reviews;
	}

	/* AÑADIR RESEÑA A LA COLECCION DE RESEÑAS DE UN LIBRO */
	private String INSERT_REVIEW_QUERY = "insert into reviews (libroid, username, name, texto) values (?, ?, ?, ?)";

	@POST
	@Path("/reviews/{libroid}")
	@Consumes(MediaType.REVIEWS_API_REVIEW)
	@Produces(MediaType.REVIEWS_API_REVIEW)
	public Review createReview(@PathParam("libroid") String libroid,
			Review review) {

		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(INSERT_REVIEW_QUERY,
					Statement.RETURN_GENERATED_KEYS);

			// stmt.setString(1, Integer.toString(review.getReseñaid()));

			stmt.setString(1, Integer.toString(review.getLibroid()));
			stmt.setString(2, security.getUserPrincipal().getName());
			stmt.setString(3, getNameOfUsername(security.getUserPrincipal()
					.getName()));
			stmt.setString(4, review.getTexto());

			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			/*
			 * if (rs.next()) {
			 * 
			 * review = getReviewLibroId(libroid);
			 * 
			 * } else { // Something has failed... }
			 */
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		System.out.println(review.getReseñaid());
		return review;
	}

	/* BORRAR REVIEW DE UN LIBRO */

	private String DELETE_REVIEW_QUERY = "delete from reviews where reviews.reseñaid=?";

	@DELETE
	@Path("/reviews/{libroid}/{reseñaid}")
	public void deleteReview(@PathParam("libroid") String libroid,
			@PathParam("reseñaid") String reseñaid) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		Review review = new Review();
		review = getReviewReseñaId(reseñaid);
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(DELETE_REVIEW_QUERY);
			stmt.setInt(1, Integer.valueOf(reseñaid));
			int rows = stmt.executeUpdate();
			if (rows == 0)
				throw new NotFoundException("No hay ninguna review con id= "
						+ reseñaid);
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

	/* UPDATE DE UN LIBRO */
	private String UPDATE_REVIEW_QUERY = "update reviews set  texto=ifnull(?, texto) where reseñaid=?";

	@PUT
	@Path("/reviews/{libroid}/{reseñaid}")
	@Consumes(MediaType.REVIEWS_API_REVIEW)
	@Produces(MediaType.REVIEWS_API_REVIEW)
	public Review updateReview(@PathParam("libroid") String libroid,
			@PathParam("reseñaid") String reseñaid, Review review) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(UPDATE_REVIEW_QUERY);

			stmt.setString(1, review.getTexto());
			stmt.setInt(2, Integer.valueOf(reseñaid));
			int rows = stmt.executeUpdate();
			if (rows == 1)
				review = getReviewReseñaId(reseñaid);
			else {
				throw new NotFoundException("There's no review with id="
						+ reseñaid);
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		return review;
	}

	private String GET_NUM_OF_TIMES_USED = "select * from reviews where username=? and libroid=?";

	private Boolean validateOnetimePerUser(String username, int idlibro) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		int count = 0;
		boolean turn;
		try {
			stmt = conn.prepareStatement(GET_NUM_OF_TIMES_USED);
			stmt.setString(1, username);
			stmt.setInt(2, idlibro);
			System.out.println("meto en la base de datos ---" + username
					+ "----" + idlibro);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				count++;
			}
			System.out.println("count == " + count);
			if (count == 0) {
				turn = true;
			} else {
				turn = false;
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		System.out.println("return " + turn);
		return turn;
	}

	/*
	 * private void validateUser(String id) { Review review =
	 * getBookFromDatabase(id); String username = review.getUsername(); if
	 * (!security.getUserPrincipal().getName().equals(username)) throw new
	 * ForbiddenException( "You are not allowed to modify this review.");
	 * 
	 * }
	 */

	/*
	 * 
	 * 
	 * Security
	 */

	private void validateAdmin() { // VALIDATE ADMIN
		if (!security.isUserInRole("administrator"))
			throw new ForbiddenException("This function is only for admins.");
	}

	private void validateOneReviewPerUser(String username, int idlibro) { // VALIDATE
		// AUTOR
		// REGISTERED
		if (validateOnetimePerUser(username, idlibro) == false)
			throw new ForbiddenException(
					"Solo puedes crear una reseña por libro i persona registrada");
	}

	private String GET_NAME_BY_USERNAME = "select name from reviews where username=?";

	private String getNameOfUsername(String username) {
		String Name = null;
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(GET_NAME_BY_USERNAME);
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				Name = rs.getString("name");
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		System.out.println("return " + Name);
		return Name;
	}

}
