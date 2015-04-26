package edu.upc.eetac.dsa.iarroyo.books.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nacho on 25/04/15.
 */
public class Review {

    private int reseñaid;

    public int getLibroid() {
        return libroid;
    }


    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    private Map<String, Link> links = new HashMap<String, Link>();
    public void setLibroid(int libroid) {
        this.libroid = libroid;
    }

    public int getReseñaid() {
        return reseñaid;
    }

    public void setReseñaid(int reseñaid) {
        this.reseñaid = reseñaid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUltima_fecha_hora() {
        return ultima_fecha_hora;
    }

    public void setUltima_fecha_hora(long ultima_fecha_hora) {
        this.ultima_fecha_hora = ultima_fecha_hora;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    private int libroid;
    private String username;
    private String name;
    private long ultima_fecha_hora;
    private String texto;
}
