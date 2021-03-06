package com.carlmeyer.questgeneratordemo.ui.fragments;

import android.os.Bundle;
import android.util.Log;
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
import com.carlmeyer.questgeneratordemo.questgenerator.models.Location;
import com.carlmeyer.questgeneratordemo.questgenerator.models.NPC;
import com.carlmeyer.questgeneratordemo.ui.adapters.NPCsAdapter;
import com.carlmeyer.questgeneratordemo.ui.viewholders.NPCViewHolder;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

public class NPCsFragment extends Fragment implements NPCViewHolder.OnNPCListener {

    private Realm realm;
    private RecyclerView rvNPCS;
    private Button btnAddNPC;
    private OrderedRealmCollection<NPC> npcs;
    private OrderedRealmCollection<Location> locations;
    private List<String> locationsNames;
    NPCsAdapter npcsAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_npcs, container, false);
        rvNPCS = root.findViewById(R.id.rvNPCS);
        realm = Realm.getDefaultInstance();
        btnAddNPC = root.findViewById(R.id.btnAddNPC);
        rvNPCS = root.findViewById(R.id.rvNPCS);
        getNPCS();
        // Get all the locations, we will need this later
        getLocations();
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
        rvNPCS.setLayoutManager(layoutManager);
        // Initialize and set locationsAdapter with list of locations
        npcsAdapter = new NPCsAdapter(npcs, this::onNPCClick);
        rvNPCS.setAdapter(npcsAdapter);
    }

    /**
     * Set up UI onclick listeners etc
     */
    private void setUpUI() {
        btnAddNPC.setOnClickListener(v -> {
            showAddNPCDialog();
        });
    }

    /**
     * Setup, Configure and Show the add location dialog
     */
    private void showAddNPCDialog() {
        // set up the dialog
        LovelyCustomDialog dialog = new LovelyCustomDialog(getContext())
                .setView(R.layout.dialog_add_npc)
                .setTopColorRes(R.color.colorPrimary)
                .setTitle(R.string.add_npc)
                .setIcon(R.drawable.human_greeting_light);
        // config txtLocation
        dialog.configureView(v -> {
            EditText txtNPCName = v.findViewById(R.id.txtAddNPCName);
            EditText txtNPCLocation = v.findViewById(R.id.txtAddNPCLocation);
            Button btnDialogAddNPC = v.findViewById(R.id.btnDialogAddNPC);
            txtNPCLocation.setKeyListener(null);
            txtNPCLocation.setOnFocusChangeListener((v1, hasFocus) -> {
                // when location edit text is clicked and gains focus display a choice dialog of locations
                if (hasFocus) {
                    new LovelyChoiceDialog(getContext())
                            .setTopColorRes(R.color.colorPrimary)
                            .setTitle(R.string.locations)
                            .setIcon(R.drawable.google_maps_light)
                            .setMessage(R.string.choose_a_location)
                            .setItems(locationsNames, (position, location) -> {
                                // when a location is selected, set the location txt of the npc and dismiss
                                txtNPCLocation.setText(location);
                                // clear focus so that you can click on it again once dialog closes
                                txtNPCLocation.clearFocus();
                            })
                            .show();
                }

            });
            // Set Add NPC Listener
            btnDialogAddNPC.setOnClickListener(v1 -> {
                // if no npc location provided
                if (txtNPCName.getText().toString().isEmpty()) {
                    // Show error dialog
                    new LovelyStandardDialog(getContext(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.alert_box_light)
                            .setTitle(R.string.error)
                            .setMessage(R.string.npc_name_may_not_be_empty)
                            .setPositiveButton(android.R.string.ok, v2 -> {
                            })
                            .show();

                } else if (txtNPCLocation.getText().toString().isEmpty()) {
                    // Show error dialog
                    new LovelyStandardDialog(getContext(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.alert_box_light)
                            .setTitle(R.string.error)
                            .setMessage(R.string.npc_location_may_not_be_empty)
                            .setPositiveButton(android.R.string.ok, v2 -> {
                            })
                            .show();

                } else {
                    // add location to database
                    addNPC(txtNPCName.getText().toString(), txtNPCLocation.getText().toString());
                    dialog.dismiss();
                }
            });

        });

        // show the dialog
        dialog.show();
    }

    /**
     * Add a new location to the database and update the list of npcs
     *
     * @param npcName - the new npc's name
     */
    private void addNPC(String npcName, String locationName) {

        // first add the npc to the database
        realm.executeTransaction(r -> {
            // unfortunately since realm does not support auto increment ID's yet we need to
            // get the next id before adding the new npc
            long nextID = (long) (r.where(NPC.class).max("id")) + 1;
            NPC npc = r.createObject(NPC.class, nextID);
            npc.setName(npcName);
            Location location = realm.where(Location.class).equalTo("name", locationName).findFirst();
            npc.setLocation(location);
        });

        // get the position of the item that has been inserted alphabetically into the list
        int position = 0;
        for (NPC npc : npcs) {
            if (npc.getName().equals(npcName)) {
                break;
            }
            position++;
        }
        // scroll to that position
        rvNPCS.smoothScrollToPosition(position);

    }

    /**
     * Get the npcs from the DB and sort them alphabetically
     */
    private void getNPCS() {
        npcs = realm.where(NPC.class).findAll();
        npcs = npcs.sort("name");
    }

    /**
     * Get the locations from the DB and sort them alphabetically
     */
    private void getLocations() {
        locations = realm.where(Location.class).findAll();
        locations.sort("name");
        // set locationNames list to use in choice dialog
        setLocationNames();
    }

    /**
     * Get a string list of location names to use in locations choice dialog
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
        rvNPCS.setAdapter(null);
        realm.close();
    }

    @Override
    public void onNPCClick(int position) {
        // get reference to selected npc
        NPC selectedNPC = npcsAdapter.getItem(position);
        if (selectedNPC != null) {
            // get live npc object from Realm
            NPC npc = realm.where(NPC.class).equalTo("name", selectedNPC.getName()).findFirst();
            showEditNPCDialog(npc);
        } else {
            Log.e("ERROR", "Could not find NPC");
            // Show error dialog if npc could not be found
            new LovelyStandardDialog(getContext(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                    .setTopColorRes(R.color.colorPrimary)
                    .setButtonsColorRes(R.color.colorAccent)
                    .setIcon(R.drawable.alert_box_light)
                    .setTitle(R.string.error)
                    .setMessage(R.string.could_not_find_npc)
                    .setPositiveButton(android.R.string.ok, v2 -> {
                    })
                    .show();
        }
    }

    /**
     * Setup, Configure and Show the edit location dialog
     */
    private void showEditNPCDialog(NPC npc) {
        // set up the dialog
        LovelyCustomDialog dialog = new LovelyCustomDialog(getContext())
                .setView(R.layout.dialog_edit_npc)
                .setTopColorRes(R.color.colorPrimary)
                .setTitle(R.string.edit_npc)
                .setIcon(R.drawable.skull_light);
        // config txtLocation
        dialog.configureView(v -> {
            EditText txtNPCName = v.findViewById(R.id.txtEditNPCName);
            txtNPCName.setText(npc.getName());
            EditText txtNPCLocation = v.findViewById(R.id.txtEditNPCLocation);
            txtNPCLocation.setText(npc.getLocation().getName());
            Button btnDialogSave = v.findViewById(R.id.btnDialogSave);
            Button btnDialogDelete = v.findViewById(R.id.btnDialogDelete);
            txtNPCLocation.setKeyListener(null);
            txtNPCLocation.setOnFocusChangeListener((v1, hasFocus) -> {
                // when location edit text is clicked and gains focus display a choice dialog of locations
                if (hasFocus) {
                    new LovelyChoiceDialog(getContext())
                            .setTopColorRes(R.color.colorPrimary)
                            .setTitle(R.string.locations)
                            .setIcon(R.drawable.google_maps_light)
                            .setMessage(R.string.choose_a_location)
                            .setItems(locationsNames, (position, location) -> {
                                // when a location is selected, set the location txt of the npc and dismiss
                                txtNPCLocation.setText(location);
                                // clear focus so that you can click on it again once dialog closes
                                txtNPCLocation.clearFocus();
                            })
                            .show();
                }

            });

            btnDialogDelete.setOnClickListener(v1 -> {
                deleteNPC(npc);
                dialog.dismiss();
            });

            btnDialogSave.setOnClickListener(v1 -> {
                // validate data before adding to DB
                if (txtNPCName.getText().toString().isEmpty()) {
                    // Show error dialog
                    new LovelyStandardDialog(getContext(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.alert_box_light)
                            .setTitle(R.string.error)
                            .setMessage(R.string.npc_name_may_not_be_empty)
                            .setPositiveButton(android.R.string.ok, v2 -> {
                            })
                            .show();

                } else if (txtNPCLocation.getText().toString().isEmpty()) {
                    // Show error dialog
                    new LovelyStandardDialog(getContext(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.alert_box_light)
                            .setTitle(R.string.error)
                            .setMessage(R.string.npc_location_may_not_be_empty)
                            .setPositiveButton(android.R.string.ok, v2 -> {
                            })
                            .show();

                } else {
                    // Data valid, add location to database
                    updateNPC(npc, txtNPCName.getText().toString(), txtNPCLocation.getText().toString());
                    dialog.dismiss();
                }
            });

        });

        // show the dialog
        dialog.show();
    }

    /**
     * Update an existing npc
     *
     * @param npc            - existing npc
     * @param updatedNPCName - new npc name
     */
    private void updateNPC(NPC npc, String updatedNPCName, String updatedNPCLocation) {

        realm.executeTransaction(r -> {
            NPC e = r.where(NPC.class).equalTo("name", npc.getName()).findFirst();
            if (e != null) {
                e.setName(updatedNPCName);
                Location location = realm.where(Location.class).equalTo("name", updatedNPCLocation).findFirst();
                e.setLocation(location);
                r.insertOrUpdate(e);
            }
        });

        scrollToNPC(updatedNPCName);

    }


    /**
     * Delete a npc
     *
     * @param npc - npc to delete
     */
    private void deleteNPC(NPC npc) {
        try {
            realm.executeTransaction(r -> {
                NPC npcToDelete = r.where(NPC.class).equalTo("name", npc.getName()).findFirst();
                if (npcToDelete != null) {
                    npcToDelete.deleteFromRealm();
                } else {
                    // Show error dialog
                    new LovelyStandardDialog(getContext(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.alert_box_light)
                            .setTitle(R.string.error)
                            .setMessage(R.string.could_not_find_npc)
                            .setPositiveButton(android.R.string.ok, v2 -> {
                            })
                            .show();
                }
            });
        } catch (Exception e) {
            Log.e("ERROR", "Could not delete NPC");
            // Show error dialog
            new LovelyStandardDialog(getContext(), LovelyStandardDialog.ButtonLayout.VERTICAL)
                    .setTopColorRes(R.color.colorPrimary)
                    .setButtonsColorRes(R.color.colorAccent)
                    .setIcon(R.drawable.alert_box_light)
                    .setTitle(R.string.error)
                    .setMessage(R.string.could_not_delete_npc)
                    .setPositiveButton(android.R.string.ok, v2 -> {
                    })
                    .show();

        }


    }

    private void scrollToNPC(String npcName) {
        // get the position of the npc that has been inserted alphabetically into the list
        int position = 0;
        for (NPC npc : npcs) {
            if (npc.getName().equals(npcName)) {
                break;
            }
            position++;
        }
        // scroll to that position
        rvNPCS.smoothScrollToPosition(position);
    }
}
