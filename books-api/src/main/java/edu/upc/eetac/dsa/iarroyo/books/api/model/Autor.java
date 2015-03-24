package edu.upc.eetac.dsa.iarroyo.books.api.model;

import java.util.List;

import javax.ws.rs.core.Link;

public class Autor {

	private List<Link> links;
	private int aid;
	private String nombre;

	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	public int getAid() {
		return aid;
	}
	public void setAid(int aid) {
		this.aid = aid;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
