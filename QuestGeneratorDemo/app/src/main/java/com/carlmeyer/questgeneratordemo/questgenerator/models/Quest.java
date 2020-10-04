package com.carlmeyer.questgeneratordemo.questgenerator.models;

public class Quest {

    public String name;
    public String description;
    public String motivation;
    public StoryFragment storyFragment;
    public String dialog;
    public String completedDialog;
    public String questText;

    public Action root;

    public Quest(Action root, StoryFragment storyFragment) {
        this.root = root;

        this.name = storyFragment.getDescription();
        this.description = root.actionDescription;
        this.motivation = storyFragment.getMotivation();
        this.storyFragment = storyFragment;
    }

    public int getDepth() {
        int depth = 0;
        depth = chilDepth(this.root, depth);
        return depth;
    }

    public int chilDepth(Action root, int depth) {
        depth++;
        for (Action child : root.subActions) {
            int subDepth = chilDepth(child, depth);
            if (subDepth > depth) {
                depth = subDepth;
            }
        }
        return depth;
    }

}
