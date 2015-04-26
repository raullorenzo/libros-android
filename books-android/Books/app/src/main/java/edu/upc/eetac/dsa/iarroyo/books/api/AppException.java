package edu.upc.eetac.dsa.iarroyo.books.api;

/**
 * Created by nacho on 25/04/15.
 */
public class AppException extends Exception{

    public AppException() {
        super();
    }

    public AppException(String detailMessage) {
        super(detailMessage);
    }

}
