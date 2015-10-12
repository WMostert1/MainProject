package vsd.co.za.sambugapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;

import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import vsd.co.za.sambugapp.DataAccess.ScoutStopDAO;
import vsd.co.za.sambugapp.DataAccess.WebAPI;
import vsd.co.za.sambugapp.DomainModels.*;


public class ScoutTripActivity extends AppCompatActivity {


    //Variables for proper communication between activities
    public static final String SCOUT_STOP="za.co.vsd.scout_stop";
    private final String UPDATE_INDEX="za.co.vsd.update_index";
    public static final String USER_FARM="za.co.vsd.user_blocks";
    public final String SCOUT_STOP_LIST="za.co.vsd.scout_stop_list";
    public final String HAS_STOPS = "za.co.vsd.has_stops";
    private final int NEW_STOP=0;
    private final String TAG="ScoutTripActivity";
    private boolean hasStops = false;

    public ScoutTrip scoutTrip;
    private RecyclerView rvScoutStops;
    private RecyclerView rvPestsPerTree;
    private Spinner spnFarms;
    private List<Farm> farms;

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

        //set toolbar (ActionBar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        Spinner spinner = (Spinner) toolbar.findViewById(R.id.spnFarms);

       /* ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                scoutTrip.getStopList().remove(pos);
                rvScoutStops.getAdapter().notifyItemRemoved(pos);
            }
        };
        ItemTouchHelper swipeHelper = new ItemTouchHelper(callback);*/

        rvScoutStops = (RecyclerView) findViewById(R.id.rvScoutStops);
        rvScoutStops.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvScoutStops.setAdapter(new RVScoutStopAdapter(scoutTrip.getStopList()));
        rvScoutStops.setHasFixedSize(true);
        //swipeHelper.attachToRecyclerView(rvScoutStops);

        rvPestsPerTree = (RecyclerView) findViewById(R.id.rvPestsPerTree);
        rvPestsPerTree.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvPestsPerTree.setAdapter(new RVPestsPerTreeAdapter(scoutTrip.getStopList()));
        rvPestsPerTree.setHasFixedSize(true);

        if (!hasStops && scoutTrip.getNumStops() == 0) {
            addDefaultStop();
        }

        acceptFarms(getIntent());
        spnFarms = (Spinner) toolbar.findViewById(R.id.spnFarms);
        ArrayAdapter<Farm> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFarms.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(UPDATE_INDEX, updateIndex);
        savedInstanceState.putBoolean(HAS_STOPS, hasStops);
        savedInstanceState.putSerializable(SCOUT_STOP_LIST, scoutTrip);
    }

    /**
     * Start EnterDataActivity to create new ScoutStop object.
     * @param v Button view that is clicked
     */
    public void addStopActivityStart(View v){
        Intent intent=new Intent(this,enterDataActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(USER_FARM, (Farm) spnFarms.getSelectedItem());
        intent.putExtras(b);
        startActivityForResult(intent, NEW_STOP);
    }

    public void addStop(ScoutStop stop){
        if (!hasStops) {
            scoutTrip.getStopList().remove(0);
            rvScoutStops.getAdapter().notifyItemRemoved(0);
            rvPestsPerTree.getAdapter().notifyItemRemoved(0);
        }
        scoutTrip.getStopList().add(0,stop);
        hasStops = true;
        int pos=0;
        rvScoutStops.getAdapter().notifyItemInserted(pos);
        rvPestsPerTree.getAdapter().notifyItemInserted(pos);
    }

    public void addDefaultStop(){
        ScoutStop tempStop = new ScoutStop();
        Block tempBlock = new Block();
        tempBlock.setBlockName("No stops added yet. Click '+'");
        tempStop.setBlock(tempBlock);
        scoutTrip.addStop(tempStop);
        int pos=rvScoutStops.getChildCount() - 1;
        rvScoutStops.getAdapter().notifyItemInserted(pos);
        rvPestsPerTree.getAdapter().notifyItemInserted(pos);
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
    public void acceptFarms(Intent intent) {
        Bundle b=intent.getExtras();
        farms = new ArrayList<>((HashSet<Farm>) b.getSerializable(LoginActivity.USER_FARMS));
    }

    /**
     * End ScoutTripActivity.
     * @param v Button view that is clicked.
     */
    public void finishTrip(View v){
        persistData();
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
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
                Log.d("ADDING", rvScoutStops.getChildCount()+"");
            }
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

    private class RVScoutStopAdapter extends RecyclerView.Adapter<RVScoutStopAdapter.ScoutStopViewHolder> {

        List<ScoutStop> scoutStops;

        public RVScoutStopAdapter(List<ScoutStop> scoutStops) {
            this.scoutStops = scoutStops;
        }

        public class ScoutStopViewHolder extends RecyclerView.ViewHolder {
            SwipeLayout slScoutStop;
            LinearLayout llDraggedMenu;
            LinearLayout llBugInfo;
            CheckedTextView tvBlockName;
            CheckedTextView tvTreeCount;

            ScoutStopViewHolder(View itemView) {
                super(itemView);
                slScoutStop=(SwipeLayout) itemView.findViewById(R.id.swiper);
                llDraggedMenu=(LinearLayout)itemView.findViewById(R.id.draggedMenu);
                tvBlockName = (CheckedTextView) itemView.findViewById(R.id.tvBlockName);
                tvTreeCount = (CheckedTextView) itemView.findViewById(R.id.tvTreeCount);
                llBugInfo = (LinearLayout) itemView.findViewById(R.id.llBugInfo);
            }
        }

        @Override
        public int getItemCount() {
            return scoutStops.size();
        }

        @Override
        public ScoutStopViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_scout_stop, viewGroup, false);
            ScoutStopViewHolder scoutStopViewHolder = new ScoutStopViewHolder(v);
            return scoutStopViewHolder;
        }

        @Override
        public void onBindViewHolder(final ScoutStopViewHolder scoutStopViewHolder, int i) {
            if (i==scoutStopViewHolder.getAdapterPosition()) {
                ScoutStop stop = scoutStops.get(i);
                scoutStopViewHolder.tvBlockName.setText(stop.getBlock().getBlockName());
                if (hasStops) {
                    for (ScoutBug bug : stop.getScoutBugs()) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bug.getFieldPicture(), 0, bug.getFieldPicture().length);
                        bitmap = Bitmap.createScaledBitmap(bitmap, 75, 75, true);
                        View view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.bug_info, null);
                        ((ImageView) view.findViewById(R.id.bugInfoImage)).setImageBitmap(bitmap);
                        ((TextView) view.findViewById(R.id.bugInfoText)).setText(bug.getNumberOfBugs() + "");
                        scoutStopViewHolder.llBugInfo.addView(view);
                    }
                    scoutStopViewHolder.tvTreeCount.setText(stop.getNumberOfTrees() + "");
                    scoutStopViewHolder.tvTreeCount.setTextSize(36);
                } else {
                    scoutStopViewHolder.tvTreeCount.setText("");
                    scoutStopViewHolder.tvTreeCount.setTextSize(0);
                }

                scoutStopViewHolder.llDraggedMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos=scoutStopViewHolder.getAdapterPosition();
                        scoutStops.remove(pos);
                        rvScoutStops.getAdapter().notifyItemRemoved(pos);
                        rvPestsPerTree.getAdapter().notifyItemRemoved(pos);
                        if (scoutStops.size()==0) {
                            hasStops=false;
                            addDefaultStop();
                        }
                    }
                });

                scoutStopViewHolder.slScoutStop.setShowMode(SwipeLayout.ShowMode.LayDown);
                scoutStopViewHolder.slScoutStop.addDrag(SwipeLayout.DragEdge.Right, scoutStopViewHolder.llDraggedMenu);
                scoutStopViewHolder.slScoutStop.addSwipeListener(new SwipeLayout.SwipeListener() {
                    @Override
                    public void onClose(SwipeLayout layout) {
                        //when the SurfaceView totally cover the BottomView.
                        Log.e("SWIPE", "1");
                    }

                    @Override
                    public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                        //you are swiping.
                        Log.e("SWIPE", "2");
                    }

                    @Override
                    public void onStartOpen(SwipeLayout layout) {
                        Log.e("SWIPE", "3");
                    }

                    @Override
                    public void onOpen(SwipeLayout layout) {
                        //when the BottomView totally show.
                        Log.e("SWIPE", "4");
                    }

                    @Override
                    public void onStartClose(SwipeLayout layout) {
                        Log.e("SWIPE", "5");
                    }

                    @Override
                    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                        //when user's hand released.
                        Log.e("SWIPE", "6");
                    }
                });
            }
        }
    }

    private class RVPestsPerTreeAdapter extends RecyclerView.Adapter<RVPestsPerTreeAdapter.PestsPerTreeViewHolder> {

        List<ScoutStop> scoutStops;

        public RVPestsPerTreeAdapter(List<ScoutStop> scoutStops) {
            this.scoutStops = scoutStops;
        }

        public class PestsPerTreeViewHolder extends RecyclerView.ViewHolder {
            CheckedTextView tvBlockName;
            CheckedTextView tvPestsPerTree;

            PestsPerTreeViewHolder(View itemView) {
                super(itemView);
                tvBlockName = (CheckedTextView) itemView.findViewById(R.id.tvBlockName);
                tvPestsPerTree = (CheckedTextView) itemView.findViewById(R.id.tvPestsPerTree);
            }
        }

        @Override
        public int getItemCount() {
            return scoutStops.size();
        }

        @Override
        public PestsPerTreeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_pests_per_tree, viewGroup, false);
            PestsPerTreeViewHolder pestsPerTreeViewHolder = new PestsPerTreeViewHolder(v);
            return pestsPerTreeViewHolder;
        }

        @Override
        public void onBindViewHolder(PestsPerTreeViewHolder pestsPerTreeViewHolder, int i) {
            ScoutStop stop = scoutStops.get(i);
            pestsPerTreeViewHolder.tvBlockName.setText(stop.getBlock().getBlockName());
            pestsPerTreeViewHolder.tvPestsPerTree.setText(hasStops ? String.format("%.2f", stop.getPestsPerTree()) : "");
        }
    }
}
