package vsd.co.za.sambugapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import android.widget.GridView;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import vsd.co.za.sambugapp.DataAccess.SpeciesDAO;
import vsd.co.za.sambugapp.DomainModels.Species;

/**
 * This activity class is the third viewable screen when interacting with the App in order
 * to capture scouting data.
 * <p/>
 * This activity makes use of the default camera handler app on the device.
 */
public class IdentificationActivity extends AppCompatActivity {

    public static final int REQUEST_TAKE_PHOTO = 1;
    private static final String ACTIVITY_STARTED = "za.co.vsd.firs_activity";
    public static final String FIELD_BITMAP = "za.co.vsd.field_bitmap";
    public static final String IDENTIFICATION_SPECIES="za.co.vsd.identification_species";
    public static final String BUG_COUNT = "za.co.vsd.bug_count";
    private static final String CURRENT_SPECIES = "za.co.vsd.current_entry";
    private ImageView mImageView = null;
    private Bitmap bitmap = null;
    private Species currentEntry = null;
    private boolean activityStarted;

    /**
     * Main responsibilities:
     * ------------------------------------------
     * Handles the saved state bundle
     * Loads the gallery by initialising the grid and loading compressed images
     * Checks on first startup if the database Species
     * @param savedInstanceState State of the previous running version of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            if (savedInstanceState != null) {
                bitmap = savedInstanceState.getParcelable(FIELD_BITMAP);
                activityStarted = savedInstanceState.getBoolean(ACTIVITY_STARTED);
                currentEntry = (Species) savedInstanceState.getSerializable(CURRENT_SPECIES);
            } else {
                activityStarted = false;
            }

        //Checks/loads species data into the Species table of the database
        if (!activityStarted) {
                SpeciesDAO speciesDAO = new SpeciesDAO(getApplicationContext());
                try {
                    speciesDAO.open();
                    if (speciesDAO.isEmpty()) {
                        speciesDAO.loadPresets();
                    }

                    speciesDAO.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                dispatchTakePictureIntent();
            activityStarted = true;
            }

            setContentView(R.layout.activity_identification);

            GridView gridview = (GridView) findViewById(R.id.gvIdentification_gallery);
            gridview.setAdapter(new ImageAdapter(this));

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    SpeciesDAO speciesDAO = new SpeciesDAO(getApplicationContext());
                    try {
                        speciesDAO.open();
                        currentEntry = speciesDAO.getSpeciesByID(position + 1);
                        Toast.makeText(getApplicationContext(), "You chose " + currentEntry.getSpeciesName() + " at instar " + currentEntry.getLifestage(), Toast.LENGTH_SHORT).show();
                        speciesDAO.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

            mImageView = (ImageView) findViewById(R.id.ivFieldPicture);
            if (bitmap != null) mImageView.setImageBitmap(bitmap);
    }

    /**
     * Saves the current state of the activity for future activities being restarted
     *
     * @param savedInstanceState The saved activity state of the currently running activity
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //activityStarted is used to only start the camera once
        savedInstanceState.putBoolean(ACTIVITY_STARTED, activityStarted);
        savedInstanceState.putParcelable(FIELD_BITMAP, bitmap);
        savedInstanceState.putSerializable(CURRENT_SPECIES, currentEntry);
    }

    public void doAutomaticClassification(View view) {
        GridView gv = (GridView) findViewById(R.id.gvIdentification_gallery);
        gv.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                Log.d("MOTION", event.toString());
                return false;
            }
        });
        gv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TOUCH", event.toString());
                return false;
            }
        });
        //gv.setNumColumns(3);
        //Toast.makeText(getApplicationContext(), "This feature is currently in development", Toast.LENGTH_SHORT).show();
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

    /**
     * The case where the photo is returned from the external camera app is handled here
     * @param requestCode The identification code of a specific
     * @param resultCode The code indicating the outcome of the request
     * @param data Data received from another activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        InputStream stream = null;
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {

            try {
                //Recycle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }
                stream = getContentResolver().openInputStream(data.getData());
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(stream, null, options);

                // Calculate inSampleSize
                options.inSampleSize = ImageAdapter.calculateInSampleSize(options, 150, 150);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream, null, options);

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
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_CANCELED) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sambug_logo);
            mImageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Starts a new intent to take a picture with the device's camera
     */
    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }

    }

    public void showDialogNumberOfBugs(View v) {
        if (currentEntry != null) {
            NumberPicker np;
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title("Number of Bugs")
                    .positiveText("Finish")
                    .titleGravity(GravityEnum.CENTER)
                    .customView(R.layout.dialog_number_picker, false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            NumberPicker np = (NumberPicker) dialog.getCustomView().findViewById(R.id.dlgNumBugs);
                            sendResultBack(np.getValue());
                        }
                    })
                    .show();
            np = (NumberPicker) dialog.getCustomView().findViewById(R.id.dlgNumBugs);
            np.setMinValue(0);
            np.setMaxValue(100);
            np.setWrapSelectorWheel(false);
        } else {
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title("Error")
                    .content("You need to select a species or perform automatic classification.")
                    .positiveText("OK")
                    .titleGravity(GravityEnum.CENTER)
                    .show();
        }
    }

    /**
     * This function puts the current Species entry as well as the field picture taken
     * into a bundle which is then returned to the enterDataActivity
     */
    public void sendResultBack(int numBugs) {
        Intent output = new Intent();
        Bundle bundle = new Bundle();

        currentEntry.setIdealPicture(null);
        bundle.putSerializable(IDENTIFICATION_SPECIES, currentEntry
        );
        if (bitmap==null){
            Log.e("PROB","IT~S NULL HERE");
        }
        Bitmap currentPicture = bitmap;
        currentPicture = Bitmap.createScaledBitmap(currentPicture, 50, 50, true);
        bundle.putParcelable(FIELD_BITMAP, currentPicture);
        bundle.putInt(BUG_COUNT, numBugs);
        output.putExtras(bundle);
        setResult(RESULT_OK, output);

        finish();
    }



}
