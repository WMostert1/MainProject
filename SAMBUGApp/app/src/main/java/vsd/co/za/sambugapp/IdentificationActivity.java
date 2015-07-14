package vsd.co.za.sambugapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vsd.co.za.sambugapp.DataAccess.DBHelper;
import vsd.co.za.sambugapp.DataAccess.SpeciesDAO;
import vsd.co.za.sambugapp.DomainModels.Species;


public class IdentificationActivity extends AppCompatActivity {
    static final int REQUEST_TAKE_PHOTO = 1;
    private ImageView mImageView;
    private Bitmap bitmap;
    private Species currentEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        // dispatchTakePictureIntent();
        mImageView = (ImageView) findViewById(R.id.ivFieldPicture);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_identification, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        InputStream stream = null;
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK)

            try {
                // recyle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }
                stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                Log.e("BMap", bitmap.toString());
                //TODO: Rotate the image

                mImageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (stream != null)
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
        }
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    public void speciesSelectionClick(View view) {

        Species identification = new Species();
        String c = "Coconut";
        String gV = "Green Vegetable";
        String tS = "Two Spotted";
        String yE = "Yellow Edged";
        identification.setIsPest(true);

        SpeciesDAO dao = new SpeciesDAO(getApplicationContext());
        try {
            dao.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO: Replace this with proper dynamic code when DB is up
        switch (view.getId()) {
            case R.id.coconut_1:
                identification.setSpeciesName(c);
                identification.setLifestage(1);
                Drawable drawable = getResources().getDrawable(R.drawable.coconut_inst_1);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                identification.setIdealPicture(stream.toByteArray());
                try {
                    String date = DateFormat.getDateTimeInstance().format(new Date());
                    identification.setTMStamp(DateFormat.getDateTimeInstance().parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                    identification.setTMStamp(null);
                }
                identification.setLastModifiedID(1);
                //identification.setIdealPicture(R.);


                dao.insert(identification);

                break;
            case R.id.coconut_2:
                identification.setSpeciesName(c);
                identification.setLifestage(2);
                List<Species> entries = dao.getAllSpecies();
                break;
            case R.id.coconut_3:
                identification.setSpeciesName(c);
                identification.setLifestage(3);
                break;
            case R.id.coconut_4:
                identification.setSpeciesName(c);
                identification.setLifestage(4);
                break;

            case R.id.green_veg_1:
                identification.setSpeciesName(gV);
                identification.setLifestage(1);
                break;

            case R.id.green_veg_2:
                identification.setSpeciesName(gV);
                identification.setLifestage(2);
                break;

            case R.id.green_veg_3:
                identification.setSpeciesName(gV);
                identification.setLifestage(3);
                break;

            case R.id.green_veg_4:
                identification.setSpeciesName(gV);
                identification.setLifestage(4);
                break;

            case R.id.two_spot_1:
                identification.setSpeciesName(tS);
                identification.setLifestage(1);
                break;

            case R.id.two_spot_2:
                identification.setSpeciesName(tS);
                identification.setLifestage(2);
                break;

            case R.id.two_spot_3:
                identification.setSpeciesName(tS);
                identification.setLifestage(3);
                break;

            case R.id.two_spot_4:
                identification.setSpeciesName(tS);
                identification.setLifestage(4);
                break;

            case R.id.yellow_edged_1:
                identification.setSpeciesName(yE);
                identification.setLifestage(1);
                break;

            case R.id.yellow_edged_2:
                identification.setSpeciesName(yE);
                identification.setLifestage(2);
                break;

            case R.id.yellow_edged_3:
                identification.setSpeciesName(yE);
                identification.setLifestage(3);
                break;

            case R.id.yellow_edged_4:
                identification.setSpeciesName(yE);
                identification.setLifestage(4);
                break;
        }

        dao.close();
    }

    public void sendResultBack(View view) {

        Intent output = new Intent();
        Bundle b = new Bundle();
        Species species = new Species();
        species.setSpeciesName("Keagan a bitch ;)");
        b.putSerializable("Species", species);

        Bitmap cp = mImageView.getDrawingCache();
        if (cp == null) {
            Log.e("Look", "Bitch");
        }
        b.putParcelable("Image", cp);
        output.putExtras(b);
        setResult(RESULT_OK, output);
        finish();

    }



}
