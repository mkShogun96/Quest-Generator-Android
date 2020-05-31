package com.carlmeyer.questgeneratordemo.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carlmeyer.questgeneratordemo.R;
import com.carlmeyer.questgeneratordemo.questgenerator.models.Enemy;
import com.carlmeyer.questgeneratordemo.questgenerator.models.Item;
import com.carlmeyer.questgeneratordemo.questgenerator.models.Location;
import com.carlmeyer.questgeneratordemo.ui.adapters.EnemiesAdapter;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

public class EnemiesFragment extends Fragment {

    private Realm realm;
    private RecyclerView rvEnemies;
    private Button btnAddEnemy;
    private OrderedRealmCollection<Enemy> enemies;
    private OrderedRealmCollection<Location> locations;
    private List<String> locationsNames;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_enemies, container, false);
        // Find views
        rvEnemies = root.findViewById(R.id.rvEnemies);
        realm = Realm.getDefaultInstance();
        btnAddEnemy = root.findViewById(R.id.btnAddEnemy);
        rvEnemies = root.findViewById(R.id.rvEnemies);
        // Get data
        getEnemies();
        getLocations();
        // UI Setup
        setUpUI();
        setUpRecyclerView();
        return root;
    }

    /**
     * Set up RecyclerView
     */
    private void setUpRecyclerView() {
        // Create and set layoutManager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvEnemies.setLayoutManager(layoutManager);
        // Initialize and set enemiesAdapter with list of enemies
        EnemiesAdapter enemiesAdapter = new EnemiesAdapter(enemies);
        rvEnemies.setAdapter(enemiesAdapter);
    }

    /**
     * Set up UI onclick listeners etc
     */
    private void setUpUI() {
        btnAddEnemy.setOnClickListener(v -> {
            showAddEnemyDialog();
        });
    }

    /**
     * Setup, Configure and Show the add location dialog
     */
    private void showAddEnemyDialog() {
        // set up the dialog
        LovelyCustomDialog dialog = new LovelyCustomDialog(getContext())
                .setView(R.layout.dialog_add_enemy)
                .setTopColorRes(R.color.colorPrimary)
                .setTitle(R.string.add_enemy)
                .setIcon(R.drawable.skull_light);
        // config txtLocation
        dialog.configureView(v -> {
            EditText txtEnemyName = v.findViewById(R.id.txtAddEnemyName);
            EditText txtEnemyLocation = v.findViewById(R.id.txtAddEnemyLocation);
            Button btnDialogAddEnemy = v.findViewById(R.id.btnDialogAddEnemy);
            txtEnemyLocation.setKeyListener(null);
            txtEnemyLocation.setOnFocusChangeListener((v1, hasFocus) -> {
                // when location edit text is clicked and gains focus display a choice dialog of locations
                if (hasFocus) {
                    new LovelyChoiceDialog(getContext())
                            .setTopColorRes(R.color.colorPrimary)
                            .setTitle(R.string.locations)
                            .setIcon(R.drawable.google_maps_light)
                            .setMessage(R.string.choose_a_location)
                            .setItems(locationsNames, (position, location) -> {
                                // when a location is selected, set the location txt of the enemy and dismiss
                                txtEnemyLocation.setText(location);
                                // clear focus so that you can click on it again once dialog closes
                                txtEnemyLocation.clearFocus();
                            })
                            .show();
                }

            });
//             Set Add Enemy Listener
            btnDialogAddEnemy.setOnClickListener(v1 -> {
                // validate data before adding to DB
                if(txtEnemyName.getText().toString().isEmpty()){
                    // Show error dialog
                    new LovelyStandardDialog(getContext(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.alert_box_light)
                            .setTitle(R.string.error)
                            .setMessage(R.string.enemy_name_may_not_be_empty)
                            .setPositiveButton(android.R.string.ok,v2 -> {})
                            .show();

                }
                else if(txtEnemyLocation.getText().toString().isEmpty()){
                    // Show error dialog
                    new LovelyStandardDialog(getContext(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.alert_box_light)
                            .setTitle(R.string.error)
                            .setMessage(R.string.enemy_location_may_not_be_empty)
                            .setPositiveButton(android.R.string.ok,v2 -> {})
                            .show();

                }else{
                    // Data valid, add location to database
                    addEnemy(txtEnemyName.getText().toString(), txtEnemyLocation.getText().toString());
                    dialog.dismiss();
                }
            });

        });

        // show the dialog
        dialog.show();
    }

    /**
     * Get the enemies from the DB and sort them alphabetically
     */
    private void getEnemies(){
        enemies = realm.where(Enemy.class).findAll();
        enemies = enemies.sort("name");
    }

    /**
     * Add a new location to the database and update the list of enemies
     *
     * @param enemyName - the new enemy's name
     */
    private void addEnemy(String enemyName, String locationName) {

        // first add the enemy to the database
        realm.executeTransaction(r -> {
            // unfortunately since realm does not support auto increment ID's yet we need to
            // get the next id before adding the new enemy
            long nextID = (long) (r.where(Enemy.class).max("id")) + 1;
            Enemy enemy = r.createObject(Enemy.class, nextID);
            enemy.setName(enemyName);
            Location location = realm.where(Location.class).equalTo("name", locationName).findFirst();
            enemy.setLocation(location);
        });

        // get the position of the item that has been inserted alphabetically into the list
        int position = 0;
        for (Enemy enemy : enemies){
            if(enemy.getName().equals(enemyName)){
                break;
            }
            position++;
        }
        // scroll to that position
        rvEnemies.smoothScrollToPosition(position);

    }

    /**
     * Get the locations from the DB and sort them alphabetically
     */
    private void getLocations(){
        locations = realm.where(Location.class).findAll();
        locations = locations.sort("name");
        // set locationNames list to use in choice dialog
        setLocationNames();
    }

    /**
     * Make a string list of location names to use in locations choice dialog
     * I used to do this each time when you open the dialog but now I am rather doing it once and
     * passing the reference to the dialog instead
     */
    private void setLocationNames() {

        locationsNames = new ArrayList<>();

        for (Location location : locations) {
            locationsNames.add(location.getName());
        }

    }

    /*
     * It is good practice to null the reference from the view to the adapter when it is no longer needed.
     * Because the <code>RealmRecyclerViewAdapter</code> registers itself as a <code>RealmResult.ChangeListener</code>
     * the view may still be reachable if anybody is still holding a reference to the <code>RealmResult>.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        rvEnemies.setAdapter(null);
        realm.close();
    }
}
