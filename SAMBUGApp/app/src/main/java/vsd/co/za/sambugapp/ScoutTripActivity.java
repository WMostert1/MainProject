package vsd.co.za.sambugapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import vsd.co.za.sambugapp.DataAccess.ScoutStopDAO;
import vsd.co.za.sambugapp.DataAccess.WebAPI;
import vsd.co.za.sambugapp.DomainModels.*;


public class ScoutTripActivity extends ActionBarActivity {


    //Variables for proper communication between activities
    public static final String SCOUT_STOP="za.co.vsd.scout_stop";
    private final String UPDATE_INDEX="za.co.vsd.update_index";
    public static final String USER_FARM="za.co.vsd.user_blocks";
    public final String SCOUT_STOP_LIST="za.co.vsd.scout_stop_list";
    public final String HAS_STOPS = "za.co.vsd.has_stops";
    private final int NEW_STOP=0;
    private final int UPDATE_STOP=1;
    private final String TAG="ScoutTripActivity";
    private boolean hasStops = false;

    public ScoutTrip scoutTrip;
    private ListView lstStops;
    private ListView lstPestsPerTree;
    private ScoutStopAdapter lstStopsAdapter;
    private PestsPerTreeAdapter lstPestsPerTreeAdapter;
    private Farm farm;

    public int updateIndex=-1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check if activity is already running
        if (savedInstanceState != null) {
            updateIndex = savedInstanceState.getInt(UPDATE_INDEX);
            scoutTrip=(ScoutTrip)savedInstanceState.getSerializable(SCOUT_STOP_LIST);
            hasStops = savedInstanceState.getBoolean(HAS_STOPS);
        } else {
            scoutTrip = new ScoutTrip();
        }
        setContentView(R.layout.activity_scout_trip);

        lstStops = (ListView) findViewById(R.id.lstStops);
        lstStops.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateStopActivityStart(position);
            }
        });
        lstStopsAdapter = new ScoutStopAdapter(scoutTrip.getStopList());
        lstStops.setAdapter(lstStopsAdapter);

        lstPestsPerTree = (ListView) findViewById(R.id.lstPestsPerTree);
        lstPestsPerTreeAdapter = new PestsPerTreeAdapter(scoutTrip.getStopList());
        lstPestsPerTree.setAdapter(lstPestsPerTreeAdapter);
        if (!hasStops) {
            ScoutStop tempStop = new ScoutStop();
            Block tempBlock = new Block();
            tempBlock.setBlockName("No stops added yet. Click '+'");
            tempStop.setBlock(tempBlock);
            scoutTrip.addStop(tempStop);
            lstStopsAdapter.notifyDataSetChanged();
        }

        acceptFarm(getIntent());

        setTitle(farm.getFarmName());

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(UPDATE_INDEX, updateIndex);
        savedInstanceState.putBoolean(HAS_STOPS, hasStops);
        savedInstanceState.putSerializable(SCOUT_STOP_LIST,scoutTrip);
    }

    /**
     * Start EnterDataActivity to create new ScoutStop object.
     * @param v Button view that is clicked
     */
    public void addStopActivityStart(View v){
        Intent intent=new Intent(this,enterDataActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(SCOUT_STOP,null);
        b.putSerializable(USER_FARM,farm);
        intent.putExtras(b);
        startActivityForResult(intent, NEW_STOP);
        //handle new stop object
    }

    public void addStop(ScoutStop stop){
        if (!hasStops)
            scoutTrip.getStopList().clear();
        scoutTrip.addStop(stop);
        hasStops = true;
    }

    /**
     * Start EnterDataActivity to update ScoutStop object.
     * @param position Index of ScoutStop in stop list.
     */
    public void updateStopActivityStart(int position){
        //Enter EnterDataActivity for editing the stop
        updateIndex=position;
        Intent intent=new Intent(this,enterDataActivity.class);
        Bundle bundle=new Bundle();
        bundle.putSerializable(SCOUT_STOP, scoutTrip.getStop(position));
        bundle.putSerializable(USER_FARM, farm);
        intent.putExtras(bundle);
        startActivityForResult(intent, UPDATE_STOP);
    }

    /**
     * Update ScoutStop object.
     * @param scoutStop ScoutStop object to update list.
     */
    public void updateStop(ScoutStop scoutStop) {
        scoutTrip.getStopList().set(updateIndex, scoutStop);
    }

    /**
     * Initialise farm object.
     * @param intent Intent passed in from LoginActivity.
     */
    public void acceptFarm(Intent intent){
        /*farm=new Farm();
        farm.setFarmID(1);
        farm.setFarmName("DEEZ NUTS");
        HashSet<Block> blocks=new HashSet<>();
        for (int j=1;j<=10;j++){
            Block obj=new Block();
            obj.setBlockID(j);
            obj.setBlockName("Block #" + j);
            blocks.add(obj);
        }
        farm.setBlocks(blocks);*/
        Bundle b=intent.getExtras();
        farm=(Farm)b.get(LoginActivity.USER_FARM);
    }

    /**
     * End ScoutTripActivity.
     * @param v Button view that is clicked.
     */
    public void finishTrip(View v){
        persistData();
        finish();
    }

    /**
     * Persist scout stops to the SQLite database.
     * @return True if data is successfully persisted, false otherwise.
     */
    public boolean persistData(){
        ScoutStopDAO scoutStopDAO=new ScoutStopDAO(getApplicationContext());
       // ScoutBugDAO scoutBugDAO=new ScoutBugDAO(getApplicationContext());
        try{
            scoutStopDAO.open();
            //persist each scout stop
            for (ScoutStop scoutStop : scoutTrip.getStopList()){
                long scoutStopID = scoutStopDAO.insert(scoutStop);
//                scoutBugDAO.open();
//                for (ScoutBug scoutBug : scoutStop.getScoutBugs()){
//                    scoutBug.setScoutStopID((int)scoutStopID);
//                    scoutBugDAO.insert(scoutBug);
//                }
//                scoutBugDAO.close();
            }

            scoutStopDAO.close();
        } catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        Toast.makeText(getApplicationContext(),"You are done. Go home.",Toast.LENGTH_LONG).show();

        WebAPI.attemptSyncCachedScoutingData(getApplicationContext());

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK) {
            Bundle bundle = data.getExtras();
            ScoutStop stop = (ScoutStop) bundle.get(SCOUT_STOP);
            if (requestCode == NEW_STOP) { //add new stop
                addStop(stop);
                Log.d(TAG, "Added");
            } else if (requestCode == UPDATE_STOP) { //update selected stop
                updateStop(stop);
                Log.d(TAG, "Updated");
            }
            lstStopsAdapter.notifyDataSetChanged();
            lstPestsPerTreeAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scout_trip, menu);
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

    //special adapter to create custom list items for scout stops
    private class ScoutStopAdapter extends ArrayAdapter<ScoutStop> {
        public ScoutStopAdapter(ArrayList<ScoutStop> stops) {
            super(getApplication().getApplicationContext(), 0, stops);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ScoutStop stop = getItem(position);
            if (hasStops) {
                convertView = getLayoutInflater()
                        .inflate(R.layout.list_scout_stop, null);
                TextView lblBlockName =
                        (TextView) convertView.findViewById(R.id.listscoutstop_lblBlockName);
                lblBlockName.setText(stop.Block.getBlockName());
                TextView lblTreeAmount =
                        (TextView) convertView.findViewById(R.id.lblTreeAmount);
                lblTreeAmount.setText(stop.getNumberOfTrees() + "");
                LinearLayout hscrollBugInfo = (LinearLayout) convertView.findViewById(R.id.hscrollBugInfo);
                hscrollBugInfo.removeAllViews();
                for (ScoutBug bug : stop.getScoutBugs()) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bug.getFieldPicture(), 0, bug.getFieldPicture().length);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                    View view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.bug_info, null);
                    ((ImageView) view.findViewById(R.id.bugInfoImage)).setImageBitmap(bitmap);
                    ((TextView) view.findViewById(R.id.bugInfoText)).setText(bug.getNumberOfBugs() + "");
                    hscrollBugInfo.addView(view);
                }
            } else {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                }
                TextView lblText = (TextView) convertView.findViewById(android.R.id.text1);
                lblText.setText(stop.getBlock().getBlockName());
            }
            return convertView;
        }
    }

    //special adapter to create custom list item for pests per tree display
    private class PestsPerTreeAdapter extends ArrayAdapter<ScoutStop> {
        public PestsPerTreeAdapter(ArrayList<ScoutStop> stops){
            super(getApplication().getApplicationContext(),0,stops);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ScoutStop stop = getItem(position);
            if (hasStops) {
                    convertView = getLayoutInflater().inflate(R.layout.list_pests_per_tree, null);
                TextView lblBlockName = (TextView) convertView.findViewById(R.id.lblBlockName);
                lblBlockName.setText(stop.getBlock().getBlockName());
                TextView lblPestsPerTree = (TextView) convertView.findViewById(R.id.lblPestsPerTree);
                lblPestsPerTree.setText(String.format("%.2f", stop.getPestsPerTree()));
                return convertView;
            } else {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                }
                TextView lblBlockName = (TextView) convertView.findViewById(android.R.id.text1);
                lblBlockName.setText(stop.getBlock().getBlockName());
            }
            return convertView;
        }
    }

}
