package System;

import org.json.simple.*;
import javax.swing.*;
import java.awt.*;

public class ListedQuestionBox extends QuestionBox {
    //Class attributes
    static int Height = 150;

    public ListedQuestionBox(Main mainClass, JSONObject questionData) {
        this.MainClass = mainClass;
        this.QuestionData = questionData;

        //Creating necessary swing objects
        this.pnl_RootPanel = new JPanel();
        this.pnl_RootPanel.setOpaque(false);
        this.pnl_RootPanel.setBorder(MainClass._jPanelBorder);
        this.pnl_RootPanel.setPreferredSize(new Dimension(pnl_RootPanel.getPreferredSize().width, Height));

        this.lbl_QuestionNumber = new JLabel();
        this.lbl_QuestionNumber.setFont(mainClass._universalNumberFont);

        this.ta_QuestionWording = new JTextArea((String) questionData.get("QuestionWording"));
        this.ta_QuestionWording.setFont(mainClass._universalSecondaryFont);
        this.ta_QuestionWording.setOpaque(false);
        this.ta_QuestionWording.setEditable(false);

        this.lbl_CorrectAnswers = new JLabel();
        this.lbl_CorrectAnswers.setFont(mainClass._universalSecondaryFont);
        this.lbl_CorrectAnswers.setVisible(false);
        setCorrectAnswerString();

        this.tf_Answer = new JTextField();
        this.tf_Answer.setFont(mainClass._universalNumberFont);
        this.tf_Answer.setMinimumSize(new Dimension(200, 20));
        this.tf_Answer.setMaximumSize(new Dimension(200, 20));

        this.cb_Correct = new JCheckBox();
        this.cb_Correct.setVisible(false);      //Should be invisible at first, until evaluated.
        this.cb_Correct.setEnabled(false);      //Should not be toggleable by the user.
        this.cb_Correct.setForeground(new Color(255, 255, 255));
        this.cb_Correct.setName("CorrectionBox");

        this.pnl_CorrectWorkings = new JPanel();
        this.pnl_CorrectWorkings.setOpaque(false);
        this.pnl_CorrectWorkings.setVisible(false);
        this.pnl_CorrectWorkings.setPreferredSize(new Dimension(0, Height));
        this.pnl_CorrectWorkings.setLayout(new GridLayout(0, 1, 0, 0));

        //Configuring group layout.
        GroupLayout layout = new GroupLayout(this.pnl_RootPanel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(false);
        layout.setHonorsVisibility(true);

        //Because I want this component to still have space saved for it.
        layout.setHonorsVisibility(this.lbl_CorrectAnswers, false);

        //Horizontal group.
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGap(10)
                        .addGroup(layout.createParallelGroup()
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(this.lbl_QuestionNumber)
                                        .addComponent(this.ta_QuestionWording)
                                )
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 1920, 1920)
                                        .addComponent(this.cb_Correct)
                                        .addComponent(this.tf_Answer)
                                )
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 1920, 1920)
                                        .addComponent(this.lbl_CorrectAnswers)
                                )
                                .addComponent(this.pnl_CorrectWorkings)
                        )
                        .addGap(50)
        );

        //Vertical group.
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(this.lbl_QuestionNumber)
                        .addComponent(this.ta_QuestionWording)
                        .addGap(0, 100, Height)
                        .addComponent(this.pnl_CorrectWorkings)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(this.cb_Correct)
                                .addComponent(this.tf_Answer)
                        )
                        .addComponent(this.lbl_CorrectAnswers)
                        .addGap(0, 5, 5)
        );

        //Setting layout to the panel.
        this.pnl_RootPanel.setLayout(layout);

        //Adding the list of correct question workings.
        JSONArray correctWorkings = (JSONArray) this.QuestionData.get("CorrectWorkings");
        for (int i = 0; i < correctWorkings.size(); i++) {
            JSONObject data = (JSONObject) correctWorkings.get(i);

            JPanel newPanel = new JPanel();
            newPanel.setPreferredSize(new Dimension(0, 20));
            newPanel.setOpaque(false);
            newPanel.setBorder(null);
            newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.LINE_AXIS));

            JLabel wording = new JLabel((String) data.get("working"));
            wording.setFont(this.MainClass._universalSecondaryFont);

            JLabel mark = new JLabel();
            if ((long) data.get("mark") == 1) {
                mark.setText("[" + data.get("mark") + " mark]");
            } else {
                mark.setText("[" + data.get("mark") + " marks]");
            }
            mark.setFont(this.MainClass._universalNumberFont);

            newPanel.add(wording);
            newPanel.add(Box.createHorizontalGlue());
            newPanel.add(mark);

            pnl_CorrectWorkings.add(newPanel);
        }
    }
}


