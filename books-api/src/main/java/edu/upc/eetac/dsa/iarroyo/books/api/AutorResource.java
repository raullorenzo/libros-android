package edu.upc.eetac.dsa.iarroyo.books.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import edu.upc.eetac.dsa.iarroyo.books.api.model.Autor;
import edu.upc.eetac.dsa.iarroyo.books.api.model.AutorCollection;

@Path("/authors")
public class AutorResource {
	private DataSource ds = DataSourceSPA.getInstance().getDataSource();
	/* GET DE UN AUTOR POR SU ID */
	private String GET_AUTHOR_BY_ID_QUERY = "select * from autor where autor.aid=?";

	@GET
	@Path("/{aid}")
	@Produces(MediaType.AUTHORS_API_AUTHOR)
	public Autor getAutor(@PathParam("aid") String aid) {
		Autor autor = new Autor();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(GET_AUTHOR_BY_ID_QUERY);
			stmt.setInt(1, Integer.valueOf(aid));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				autor.setAid(rs.getInt("aid"));
				autor.setNombre(rs.getString("nombre"));
			} else {
				throw new NotFoundException("No hay ningún autor con id= "
						+ aid);
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
		return autor;
	}

	/* GET DE TODOS LOS AUTORES */
	private String GET_AUTHORS_QUERY = "select * from autor";

	@GET
	@Produces(MediaType.AUTHORS_API_AUTHOR_COLLECTION)
	public AutorCollection getAuthors() {
		AutorCollection authors = new AutorCollection();
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(GET_AUTHORS_QUERY);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Autor autor = new Autor();
				autor.setAid(rs.getInt("aid"));
				autor.setNombre(rs.getString("nombre"));
				authors.addAuthor(autor);
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
		return authors;
	}

	/* CREAR UN AUTOR */
	private String INSERT_AUTHOR_QUERY = "insert into autor (aid, nombre) values (?, ?)";

	@POST
	@Consumes(MediaType.AUTHORS_API_AUTHOR)
	@Produces(MediaType.AUTHORS_API_AUTHOR)
	public Autor createAuthor(Autor author) {
		// validateBook(book);
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(INSERT_AUTHOR_QUERY,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, Integer.toString(author.getAid()));
			stmt.setString(2, author.getNombre());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				int id = rs.getInt(1);
				author = getAutor(Integer.toString(id));
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
		return author;
	}

	/* ACTUALIZAR UN AUTOR */
	private String UPDATE_AUTHOR_QUERY = "update autor set nombre=ifnull(?, nombre) where aid=?";

	@PUT
	@Path("/{aid}")
	@Consumes(MediaType.AUTHORS_API_AUTHOR)
	@Produces(MediaType.AUTHORS_API_AUTHOR)
	public Autor updateAuthor(@PathParam("aid") String aid, Autor author) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(UPDATE_AUTHOR_QUERY);
			stmt.setString(1, author.getNombre());
			stmt.setInt(2, Integer.valueOf(aid));
			int rows = stmt.executeUpdate();
			if (rows == 1)
				author = getAutor(aid);
			else {
				throw new NotFoundException("There's no author with id=" + aid);
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
		return author;
	}

	/* DELETE DE UN AUTOR */
	private String DELETE_AUTHOR_QUERY = "delete from autor where autor.aid=?";

	@DELETE
	@Path("/{aid}")
	public void deleteAuthor(@PathParam("aid") String aid) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(DELETE_AUTHOR_QUERY);
			stmt.setInt(1, Integer.valueOf(aid));
			int rows = stmt.executeUpdate();
			if (rows == 0)
				throw new NotFoundException("No hay ningún autor con id= "
						+ aid);
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
}
