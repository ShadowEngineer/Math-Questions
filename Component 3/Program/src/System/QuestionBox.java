package System;

import org.json.simple.*;
import javax.swing.*;
import java.awt.*;

public class QuestionBox extends CustomInterfaceObject {
    //Variables.
    protected Main MainClass;
    protected boolean Correct = false;
    public JSONObject QuestionData;

    //Swing objects.
    protected JTextField tf_Answer;
    protected JCheckBox cb_Correct;
    protected JLabel lbl_QuestionNumber;
    protected JTextArea ta_QuestionWording;
    protected JLabel lbl_CorrectAnswers;
    protected JPanel pnl_CorrectWorkings;

    public QuestionBox() {

    }

    //Method to evaluate this question.
    public boolean Evaluate() {
        //Referencing variables.
        boolean correct = false;
        String providedAnswer = Main.RemoveWhitespace(this.tf_Answer.getText().toLowerCase());
        JSONArray correctAnswers = (JSONArray) this.QuestionData.get("CorrectAnswers");

        //Checking each answer iteratively.
        for (int i = 0; i < correctAnswers.size(); i++) {
            String correctAnswer = ((String) correctAnswers.get(i)).toLowerCase();
            if (providedAnswer.toLowerCase().equals(correctAnswer)) {
                correct = true;
                break;
            }
        }

        //Setting visibility of the list of correct workings.
        this.pnl_CorrectWorkings.setVisible(true);
        this.pnl_RootPanel.setPreferredSize(new Dimension(0, ListedQuestionBox.Height * 2));

        //Changing the UI based on the correct state.
        this.cb_Correct.setSelected(correct);
        this.cb_Correct.setVisible(true);
        this.lbl_CorrectAnswers.setVisible(true);
        if (correct) {
            this.cb_Correct.setText("Correct!");
            this.cb_Correct.setBackground(new Color(0, 225, 0));
        } else {
            this.cb_Correct.setText("Incorrect.");
            this.cb_Correct.setBackground(new Color(225, 0, 0));
        }

        this.Correct = correct;
        return correct;
    }

    //Getter for correct value.
    public boolean isCorrect() { return this.Correct; }

    //Method to set the question number.
    public void setQuestionNumber(int number) {
        this.lbl_QuestionNumber.setText("Question " + (number + 1) + ".");
    }

    //Method to set the correct answers in a nice format to the label.
    protected void setCorrectAnswerString() {
        //Referencing variables.
        String correctString = "The correct answers are: ";
        JSONArray answers = (JSONArray) this.QuestionData.get("CorrectAnswers");

        //Iteratively adding the answers to the list.
        for (int i = 0; i < answers.size(); i++) {
            if (i < answers.size() - 1) {
                correctString += answers.get(i) + ", ";
            } else {
                correctString += (String) answers.get(i);
            }
        }

        //Setting the text.
        this.lbl_CorrectAnswers.setText(correctString);
    }
}


