package com.example.questgenerator.actions;

import com.example.questgenerator.activities.MainActivity;
import com.example.questgenerator.generator.QuestGenerator;
import com.example.questgenerator.models.Action;
import com.example.questgenerator.models.Enemy;
import com.example.questgenerator.models.Item;
import com.example.questgenerator.models.Quest;
import com.example.questgenerator.utils.Actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Steal extends Action {

    private MainActivity activity;

    /**
     * Steal and item from an enemy
     * @param item - item to steal
     * @param enemy - enemy to steal the item from
     * @param activity - reference to main activity
     */
    public Steal(Item item, Enemy enemy, MainActivity activity){
        this.activity = activity;
        this.actionText = "Steal " + item.name + " from " + enemy.name;
        this.subActions = new ArrayList<>();
        initialize(item, enemy);
    }

    /**
     * Add any sub quests before stealing the item
     * @param item - item to steal
     * @param enemy - enemy to steal from
     */
    public void initialize(Item item, Enemy enemy){
        QuestGenerator questGenerator = QuestGenerator.getInstance(activity);
        // Add possible ways quest can go
        List<String[]> questPatterns = new ArrayList<>();
        // Goto enemy location before stealing item
        questPatterns.add(new String[]{Actions.GOTO});
        // Just steal the item
        questPatterns.add(new String[]{});

        Random random = new Random();

        for(String action : questPatterns.get(random.nextInt(questPatterns.size()))){
            if(action.equals(Actions.GOTO)){
                subActions.add(new Goto(enemy.location, activity));
            }
        }


    }

}
