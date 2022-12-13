package System;

import org.json.simple.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TopicPanel extends CustomInterfaceObject {
    //Class attributes.
    private boolean Selected = true;
    private JSONObject TopicData;
    private Main MainClass;
    private boolean ReadingFrameOpen = false;

    //Swing attributes.
    JTextField tf_TopicName;
    JCheckBox cb_CheckBox;
    JButton btn_ViewExtraReading;

    //Constructor.
    public TopicPanel (Main mainClass, JSONObject topicData, String specificationName) {
        this.TopicData = topicData;
        this.TopicData.put("SpecificationName", specificationName);
        this.MainClass = mainClass;

        //Creating the Swing objects.
        pnl_RootPanel = new JPanel();
        tf_TopicName = new JTextField();
        cb_CheckBox = new JCheckBox();

        //Initialising the Swing objects.
        tf_TopicName.setFocusable(false);
        tf_TopicName.setText((String) topicData.get("TopicName"));
        tf_TopicName.setBorder(null);
        tf_TopicName.setBackground(null);
        tf_TopicName.setMaximumSize(new Dimension(200, 25));

        UpdateGraphics();
        cb_CheckBox.setSelected(this.Selected);
        cb_CheckBox.setOpaque(false);
        cb_CheckBox.setFocusPainted(false);
        cb_CheckBox.addActionListener(event -> {
           Toggle(cb_CheckBox.isSelected());
        });

        btn_ViewExtraReading = new JButton("Extra resources...");
        btn_ViewExtraReading.setFocusPainted(false);
        btn_ViewExtraReading.setBorder(null);
        btn_ViewExtraReading.setFont(new Font(Font.DIALOG_INPUT, Font.ITALIC, 11));
        btn_ViewExtraReading.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (TopicPanel.this.ReadingFrameOpen == false) {
                    ExtraReadingFrame newFrame = new ExtraReadingFrame(mainClass, topicData, TopicPanel.this);
                    TopicPanel.this.ReadingFrameOpen = true;
                }
            }

            @Override
            public void mouseEntered(MouseEvent event) {btn_ViewExtraReading.setText("Extra resources to learn from!");}

            @Override
            public void mouseExited(MouseEvent event) {
                btn_ViewExtraReading.setText("Extra resources...");
            }
        });

        //Adding the components to the root panel.
        pnl_RootPanel.setLayout(new BoxLayout(pnl_RootPanel, BoxLayout.X_AXIS));
        pnl_RootPanel.add(Box.createHorizontalGlue());
        pnl_RootPanel.add(tf_TopicName);
        pnl_RootPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        pnl_RootPanel.add(cb_CheckBox);
        pnl_RootPanel.add(Box.createHorizontalGlue());
        pnl_RootPanel.add(btn_ViewExtraReading);
        pnl_RootPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        pnl_RootPanel.setBorder(BorderFactory.createLineBorder(new Color(175,175,175), 1));
        pnl_RootPanel.updateUI();
    }

    //Class methods.
    boolean getSelected() {return this.Selected;}

    JSONObject getTopicData() {return this.TopicData;}

    boolean Toggle() {
        this.Selected = !this.Selected;
        UpdateGraphics();
        return this.Selected;
    }

    boolean Toggle(boolean toToggle) {
        this.Selected = toToggle;
        UpdateGraphics();
        return this.Selected;
    }

    void setReadingFrameOpen(boolean newState) {
        this.ReadingFrameOpen = newState;
    }

    void UpdateGraphics() {
        if (this.Selected) {
            this.cb_CheckBox.setIcon(this.CheckBoxSelected);
            this.cb_CheckBox.setText("Selected.");
        } else {
            this.cb_CheckBox.setIcon(this.CheckBoxUnselected);
            this.cb_CheckBox.setText("Not selected.");
        }
    }
}


