package System;

import org.json.simple.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class SpecificationPanel extends CustomInterfaceObject {
    //Class attributes.
    private boolean Selected = true;
    private JSONObject SpecificationData;
    private ArrayList<TopicPanel> RelatedTopicObjects = new ArrayList<>();

    //Swing objects.
    JPanel pnl_TopicsPanel;
    JTextField tf_SpecificationTitle;
    JCheckBox cb_CheckBox;

    //Constructor.
    public SpecificationPanel(JSONObject specificationData) {
        this.SpecificationData = specificationData;

        //Creating the Swing objects.
        pnl_RootPanel = new JPanel();
        pnl_TopicsPanel = new JPanel();
        cb_CheckBox = new JCheckBox();
        tf_SpecificationTitle = new JTextField();

        //Initialising Swing objects.
        tf_SpecificationTitle.setFocusable(false);
        tf_SpecificationTitle.setOpaque(false);
        tf_SpecificationTitle.setName("SPEC_NAME");
        tf_SpecificationTitle.setText((String) specificationData.get("SpecificationName"));
        tf_SpecificationTitle.setBorder(BorderFactory.createLineBorder(new Color(175, 175, 175), 1));

        //Layout for the specification panel. This is the first occurrence of GroupLayout in this project.
        GroupLayout layout = new GroupLayout(pnl_RootPanel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(false);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGap(25)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(tf_SpecificationTitle)
                                        .addComponent(cb_CheckBox)
                                )
                                .addComponent(pnl_TopicsPanel)
                        )
                        .addGap(35)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(3)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(tf_SpecificationTitle)
                                .addComponent(cb_CheckBox)
                        )
                        .addComponent(pnl_TopicsPanel)
                        .addGap(3)
        );

        pnl_RootPanel.setLayout(layout);
        pnl_TopicsPanel.setLayout(null);

        //Initialising the checkbox and connecting an action event to it.
        //The event sets the object's internal Selected property, and those of all its related topics too.
        Toggle(this.Selected);
        cb_CheckBox.setOpaque(false);
        cb_CheckBox.setFocusPainted(false);
        cb_CheckBox.setActionCommand("Toggle");
        cb_CheckBox.addActionListener(event -> {
            if (event.getActionCommand().equals("Toggle")) {
                boolean toToggle = cb_CheckBox.isSelected();
                Toggle(toToggle);

                for (TopicPanel topicObject : RelatedTopicObjects) {
                    topicObject.cb_CheckBox.setSelected(toToggle);
                    topicObject.Toggle(toToggle);
                }
            }
        });
    }

    //Class methods.
    boolean getSelected() {
        return this.Selected;
    }

    String getSpecificationName() {return (String) this.SpecificationData.get("SpecificationName");}

    ArrayList<TopicPanel> getRelatedTopicObjects() {
        return this.RelatedTopicObjects;
    }

    boolean Toggle() {
        this.Selected = !this.Selected;
        return this.Selected;
    }

    boolean Toggle(boolean toToggle) {
        this.Selected = toToggle;

        //Changing the icon of the button based on its state. Using a custom icon for custom size control.
        if (this.Selected == true) {
            this.cb_CheckBox.setIcon(this.CheckBoxSelected);
        } else {
            this.cb_CheckBox.setIcon(this.CheckBoxUnselected);
        }

        return this.Selected;
    }

    void AddTopic(TopicPanel topic) {
        this.pnl_TopicsPanel.add(topic.pnl_RootPanel);
        RelatedTopicObjects.add(topic);
    }
}



