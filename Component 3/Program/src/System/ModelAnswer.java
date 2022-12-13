package System;

import org.json.simple.*;
import javax.swing.*;
import java.awt.*;

public class ModelAnswer extends CustomInterfaceObject {
    //Class attributes.
    Main MainClass;
    JSONObject ModelAnswerData;

    static int Height = 150;

    //Swing attributes.
    JLabel lbl_TopicName;
    JTextArea ta_AnswerWording;

    public ModelAnswer(Main mainClass, JSONObject modelAnswerData) {
        this.MainClass = mainClass;
        this.ModelAnswerData = modelAnswerData;

        //Choosing example question.
        JSONArray examples = (JSONArray) ModelAnswerData.get("ExampleQuestions");
        String chosenExampleQuestion = (String) examples.get((int) Math.floor(Math.random() * examples.size()));

        //Configuring swing components.
        this.pnl_RootPanel = new JPanel();
        this.pnl_RootPanel.setOpaque(false);
        this.pnl_RootPanel.setBorder(this.MainClass._jPanelBorder);
        this.pnl_RootPanel.setPreferredSize(new Dimension(pnl_RootPanel.getPreferredSize().width, Height));

        this.lbl_TopicName = new JLabel();
        this.lbl_TopicName.setText("An example question with a model answer for " +  ModelAnswerData.get("TopicName"));
        this.lbl_TopicName.setFont(this.MainClass._universalFont);
        this.lbl_TopicName.setHorizontalAlignment(SwingConstants.LEFT);

        this.ta_AnswerWording = new JTextArea();
        this.ta_AnswerWording.setText(chosenExampleQuestion);
        this.ta_AnswerWording.setFont(this.MainClass._universalSecondaryFont);
        this.ta_AnswerWording.setEditable(false);
        this.ta_AnswerWording.setLineWrap(true);

        //Placing objects with GroupLayout.
        GroupLayout layout = new GroupLayout(this.pnl_RootPanel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(false);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGap(10)
                        .addGroup(
                        layout.createParallelGroup()
                                .addComponent(this.lbl_TopicName)
                                .addComponent(this.ta_AnswerWording))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(this.lbl_TopicName)
                .addComponent(this.ta_AnswerWording)
        );

        //Assigning layout.
        this.pnl_RootPanel.setLayout(layout);
    }
}


