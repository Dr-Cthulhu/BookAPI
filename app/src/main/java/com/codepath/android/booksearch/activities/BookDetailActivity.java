package com.codepath.android.booksearch.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.android.booksearch.R;
import com.codepath.android.booksearch.models.Book;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BookDetailActivity extends AppCompatActivity {
    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private Toolbar mActionBarToolbar;
    private ShareActionProvider miShareAction;
    Intent shareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        // Fetch views
        ivBookCover = (ImageView) findViewById(R.id.ivBookCover);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvAuthor = (TextView) findViewById(R.id.tvAuthor);
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);

        // Extract book object from intent extras
        Book book = (Book) Parcels.unwrap(getIntent().getParcelableExtra("book"));

        // Use book object to populate data into views

        String author = book.getAuthor();
        String title = book.getTitle();

        tvAuthor.setText(author);
        tvTitle.setText(title);
        String url = book.getCoverUrl();

        Glide.with(this)
                .load(url)
                .listener(new RequestListener<String, GlideDrawable>() {
                              @Override
                              public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                  prepareShareIntent(((GlideBitmapDrawable) resource).getBitmap());
                                  attachShareIntentAction();
                                  // Let Glide handle resource load
                                  return false;
                              }
                          }
                )

                .into(ivBookCover);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        mActionBarToolbar.setTitle(title);
        setSupportActionBar(mActionBarToolbar);

    }

    // Method when launching drawable within Glide
    public Uri getBitmapFromDrawable(Bitmap bmp){

        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

            // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            bmpUri = FileProvider.getUriForFile(BookDetailActivity.this, "com.codepath.fileprovider", file);  // use this version for API >= 24

            // **Note:** For API < 24, you may use bmpUri = Uri.fromFile(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    // Can be triggered by a view event such as a button press
    public void onShareItem(MenuItem v) {


        // Get access to bitmap image from view
        ImageView ivImage = (ImageView) findViewById(R.id.ivBookCover);
        BitmapDrawable drawable = (BitmapDrawable) ivImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        // Get access to the URI for the bitmap
        Uri bmpUri = getBitmapFromDrawable(bitmap);
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } else {
            // ...sharing failed, handle error
            Log.d("BookDetailActivity", "oops");
        }
    }

//    // Returns the URI path to the Bitmap displayed in specified ImageView
//    public Uri getLocalBitmapUri(ImageView imageView) {
//        // Extract Bitmap from ImageView drawable
//        Drawable drawable = imageView.getDrawable();
//        Bitmap bmp = null;
//        if (drawable instanceof BitmapDrawable){
//            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//        } else {
//            return null;
//        }
//        // Store image to default external storage directory
//        Uri bmpUri = null;
//        try {
//            // Use methods on Context to access package-specific directories on external storage.
//            // This way, you don't need to request external read/write permission.
//            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
//            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
//            FileOutputStream out = new FileOutputStream(file);
//            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
//            out.close();
//            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
//            bmpUri = Uri.fromFile(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bmpUri;
//    }


    // Gets the image URI and setup the associated share intent to hook into the provider
    public void prepareShareIntent(Bitmap drawableImage) {

        // Fetch Bitmap Uri locally
        Uri bmpUri = getBitmapFromDrawable(drawableImage);// see previous remote images section and notes for API > 23
        // Construct share intent as described above based on bitmap
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.setType("image/*");
    }

    // Attaches the share intent to the share menu item provider
    public void attachShareIntentAction() {
        if (miShareAction != null && shareIntent != null)
            miShareAction.setShareIntent(shareIntent);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch reference to the share action provider
        miShareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        attachShareIntentAction(); // call here in case this method fires second

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
