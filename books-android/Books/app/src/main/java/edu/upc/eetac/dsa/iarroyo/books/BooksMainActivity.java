package edu.upc.eetac.dsa.iarroyo.books;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;

import edu.upc.eetac.dsa.iarroyo.books.api.AppException;
import edu.upc.eetac.dsa.iarroyo.books.api.BooksAPI;
import edu.upc.eetac.dsa.iarroyo.books.api.Libro;
import edu.upc.eetac.dsa.iarroyo.books.api.LibroCollection;

public class BooksMainActivity extends ListActivity {




        private final static String TAG = BooksMainActivity.class.toString();
        private static final String[] items = { "lorem", "ipsum", "dolor", "sit",
                "amet", "consectetuer", "adipiscing", "elit", "morbi", "vel",
                "ligula", "vitae", "arcu", "aliquet", "mollis", "etiam", "vel",
                "erat", "placerat", "ante", "porttitor", "sodales", "pellentesque",
                "augue", "purus" };
        private ArrayAdapter<String> adapter;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_books_main);
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("admin", "admin"
                            .toCharArray());
                }
            });
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, items);
            setListAdapter(adapter);
            (new FetchStingsTask()).execute();
        }



    private class FetchStingsTask extends
            AsyncTask<Void, Void, LibroCollection> {
        private ProgressDialog pd;

        @Override
        protected LibroCollection doInBackground(Void... params) {
            LibroCollection books = null;
            try {
                books = BooksAPI.getInstance(BooksMainActivity.this)
                        .getBooks();
            } catch (AppException e) {
                e.printStackTrace();
            }
            return books;
        }

        @Override
        protected void onPostExecute(LibroCollection result) {
            ArrayList<Libro> books = new ArrayList<Libro>(result.getBooks());
            for (Libro b : books) {
                Log.d(TAG, b.getId() + "-" + b.getTitulo());
            }
            if (pd != null) {
                pd.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(BooksMainActivity.this);
            pd.setTitle("Searching...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

    }


}

