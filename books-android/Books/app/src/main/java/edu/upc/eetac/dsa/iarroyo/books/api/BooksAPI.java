package edu.upc.eetac.dsa.iarroyo.books.api;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by nacho on 25/04/15.
 */
public class BooksAPI {

    private final static String TAG = BooksAPI.class.getName();
    private static BooksAPI instance = null;
    private URL url;

    private BooksRootAPI rootAPI = null;

    private BooksAPI(Context context) throws IOException, AppException {
        super();

        AssetManager assetManager = context.getAssets();
        Properties config = new Properties();
        config.load(assetManager.open("config.properties"));
        String urlHome = config.getProperty("books.home");
        url = new URL(urlHome);

        Log.d("LINKS", url.toString());
        getRootAPI();
    }

    public final static BooksAPI getInstance(Context context) throws AppException {
        if (instance == null)
            try {
                instance = new BooksAPI(context);
            } catch (IOException e) {
                throw new AppException(
                        "Can't load configuration file");
            }
        return instance;
    }

    private void getRootAPI() throws AppException {
        Log.d(TAG, "getRootAPI()");
        rootAPI = new BooksRootAPI();
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
        } catch (IOException e) {
            throw new AppException(
                    "Can't connect to Books API Web Service");
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray jsonLinks = jsonObject.getJSONArray("links");
            parseLinks(jsonLinks, rootAPI.getLinks());
        } catch (IOException e) {
            throw new AppException(
                    "Can't get response from Books API Web Service");
        } catch (JSONException e) {
            throw new AppException("Error parsing Book Root API");
        }

    }

    public LibroCollection getBooks() throws AppException {
        Log.d(TAG, "getBooks()");
        LibroCollection books = new LibroCollection();

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(rootAPI.getLinks()
                    .get("books").getTarget()).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
        } catch (IOException e) {
            throw new AppException(
                    "Can't connect to Books API Web Service");
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray jsonLinks = jsonObject.getJSONArray("links");
            parseLinks(jsonLinks, books.getLinks());


            books.setNewestTimestamp(jsonObject.getLong("newestTimestamp"));
            books.setOldestTimestamp(jsonObject.getLong("oldestTimestamp"));
            JSONArray jsonBooks = jsonObject.getJSONArray("books");
            for (int i = 0; i < jsonBooks.length(); i++) {
                Libro book = new Libro();
                JSONObject jsonBook = jsonBooks.getJSONObject(i);
                book.setId(jsonBook.getInt("id"));
                book.setTitulo(jsonBook.getString("titulo"));
                book.setLengua(jsonBook.getString("lengua"));
                book.setEdicion(jsonBook.getString("edicion"));
                book.setFecha_edicion(jsonBook.getLong("fecha_edicion"));
                book.setFecha_impresion(jsonBook.getLong("fecha_impresion"));
                book.setEditorial(jsonBook.getString("editorial"));
                jsonLinks = jsonBook.getJSONArray("links");
                parseLinks(jsonLinks, book.getLinks());
                books.getBooks().add(book);
            }
        } catch (IOException e) {
            throw new AppException(
                    "Can't get response from Books API Web Service");
        } catch (JSONException e) {
            throw new AppException("Error parsing Books Root API");
        }

        return books;
    }
/*
    public ReviewCollection getReviews() throws AppException {
        Log.d(TAG, "getReviews()");
        ReviewCollection reviews = new ReviewCollection();

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(rootAPI.getLinks()
                    .get("reviews").getTarget()).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
        } catch (IOException e) {
            throw new AppException(
                    "Can't connect to Books API Web Service");
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray jsonLinks = jsonObject.getJSONArray("links");
            parseLinks(jsonLinks, reviews.getLinks());
            JSONArray jsonReviews = jsonObject.getJSONArray("reviews");

;
            for (int i = 0; i < jsonReviews.length(); i++) {
                Review review = new Review();

                JSONObject jsonReview = jsonReviews.getJSONObject(i);
                review.setLibroid(jsonReview.getInt("libroid"));
                review.setReseñaid(jsonReview.getInt("reseñaid"));
                review.setUsername(jsonReview.getString("username"));
                review.setName(jsonReview.getString("name"));
                review.setUltima_fecha_hora(jsonReview.getLong("ultima_fecha_hora"));
                review.setTexto(jsonReview.getString("texto"));
                jsonLinks = jsonReview.getJSONArray("links");
                parseLinks(jsonLinks, review.getLinks());
                reviews.getReviews().add(review);
            }
        } catch (IOException e) {
            throw new AppException(
                    "Can't get response from Books API Web Service");
        } catch (JSONException e) {
            throw new AppException("Error parsing Books Root API");
        }

        return reviews;
    }
*/

    private void parseLinks(JSONArray jsonLinks, Map<String, Link> map)
            throws AppException, JSONException {
        for (int i = 0; i < jsonLinks.length(); i++) {
            Link link = null;
            try {
                link = SimpleLinkHeaderParser
                        .parseLink(jsonLinks.getString(i));
            } catch (Exception e) {
                throw new AppException(e.getMessage());
            }
            String rel = link.getParameters().get("rel");
            String rels[] = rel.split("\\s");
            for (String s : rels)
                map.put(s, link);
        }
    }



}
