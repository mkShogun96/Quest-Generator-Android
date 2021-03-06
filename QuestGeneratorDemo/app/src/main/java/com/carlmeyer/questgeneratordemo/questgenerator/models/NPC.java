package com.carlmeyer.questgeneratordemo.questgenerator.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class NPC extends RealmObject {

    @PrimaryKey
    private int id;
    // Name of the NPC. Primary Key may not be null
    private String name;
    // Location the NPC is in. One to one relationship in realm db
    private Location location;

    public NPC() {

    }

    public NPC(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
