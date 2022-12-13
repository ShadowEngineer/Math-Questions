package System;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import org.json.simple.*;
import org.json.simple.parser.*;

public class Main extends JFrame {
    //Swing component declarations
    private JPanel pnl_Topics;
    private JScrollPane scpn_TopicsList;
    private JPanel pnl_Questions;
    private JScrollPane scpn_QuestionsList;
    private JPanel pnl_InnerQuestionList;
    private JPanel pnl_Buttons;
    private JPanel pnl_Settings;
    private JPanel pnl_Statistics;
    private JPanel pnl_SuggestedTopics;

    private JButton btn_Generate;
    private JButton btn_Evaluate;
    private JButton btn_Finish;
    private JButton btn_NextQuestion;
    private JButton btn_PreviousQuestion;
    private JButton btn_CreateGraphs;
    private JCheckBox cb_QuestionsAsList;
    private JCheckBox cb_EnableBias;
    private JCheckBox cb_EnableModelAnswer;
    private JComboBox cmb_ChooseGraph;
    private JTextField tf_NumberOfQuestionsBox;
    private JTextField tf_PrimaryColour;
    private JTextField tf_SecondaryColour;
    private JTextArea ta_AnsweringGuidance;
    private JLabel lbl_NumberOfQuestionsLabel;
    private JLabel lbl_SuggestedTopics;
    private JLabel lbl_PrimaryColourLabel;
    private JLabel lbl_SecondaryColourLabel;

    String FP_Specifications = "src/System/local_files/Specifications.json";
    String FP_UserData = "src/System/local_files/Userdata.json";
    String FP_HistoricPerformanceData = "src/System/local_files/HistoricPerformanceData.json";

    JSONObject UserData;
    JSONArray Specifications;
    JSONArray HistoricPerformanceData;

    boolean DisplayQuestionsAsList = true;      //default
    boolean DisplayModelAnswers = true;         //default
    boolean EnableBias = true;                  //default
    short NumberOfQuestionsToGenerate = 10;     //default
    short maxNumberOfQuestions = 50;
    int[] PrimaryColour = {250, 250, 250};
    int[] SecondaryColour = {250, 250, 250};
    int NumberOfOpenWindows = 0;

    //Global variable declarations
    boolean Debug = false; //If this is true, outputs will be logged and things coloured weirdly for debugging purposes.
    boolean DoingQuestions = false; //flag to track if the user is doing questions or not.
    ArrayList<SpecificationPanel> SpecificationObjects = new ArrayList<>();
    QuestionBox[] CurrentQuestions;

    QuestionHandler CurrentQuestionHandlerInstance = null;
    Visualiser CurrentVisualiserInstance = null;
    DataHandler CurrentDataHandler = null;

    final short MAX_ALLOWED_GRAPHS = 3;

    //Configuration variables.
    final Border _jPanelBorder = BorderFactory.createLineBorder(new Color(50,50,50), 1);
    final Border _mainButtonBorder = BorderFactory.createLineBorder(new Color(50,50,50), 1);

    final Font _universalFont = new Font(Font.MONOSPACED, Font.PLAIN, 17);
    final Font _universalSecondaryFont = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    final Font _universalNumberFont = new Font(Font.MONOSPACED, Font.TRUETYPE_FONT, 14);

    final short _mainComponentPadding = 30;
    final short _secondaryComponentPadding = 10;
    final short _specificationPanelPadding = 5;
    final short _questionPadding = 5;

    final short _ScrollerUnitIncrement = 75;

    //Constructor of the main window.
    public Main () {
        //Getting initial user data and storing it in the class as public instance fields.
        this.Specifications = (JSONArray) ParseJSON(FP_Specifications);
        this.UserData = (JSONObject) ParseJSON(FP_UserData);
        this.HistoricPerformanceData = (JSONArray) ParseJSON(FP_HistoricPerformanceData);

        //Creating instances of the 3 major classes.
        CurrentQuestionHandlerInstance = new QuestionHandler(this);
        CurrentDataHandler = new DataHandler(this);
        CurrentVisualiserInstance = new Visualiser(this);

        //Reporting files not existing. Stack trace also gets printed. These will be removed at the end of development.
        if (this.Specifications == null) {
            System.out.println("ERROR OCCURRED WHEN TRYING TO FIND SPECIFICATION DATA.");
        }
        if (this.UserData == null) {
            System.out.println("ERROR OCCURRED WHEN TRYING TO FIND USER DATA.");
        } else {
            //Getting initial settings if they exist.
            CurrentDataHandler.GetUserSettings(UserData);
        }
        if (this.HistoricPerformanceData == null) {
            System.out.println("ERROR OCCURRED WHEN TRYING TO FIND HISTORIC PERFORMANCE DATA. CREATING NEW FILE.");

            //Creating a new file for all the historic data (since one doesn't exist).
            JSONArray newArray = new JSONArray();

            for (Object obj : this.Specifications) {
                JSONObject specification = (JSONObject) obj;

                JSONObject newSpecificationObject = new JSONObject();
                newSpecificationObject.put("SpecificationName", specification.get("SpecificationName"));
                newSpecificationObject.put("PerformanceData", new ArrayList<JSONObject>());

                newArray.add(newSpecificationObject);
            }

            //Creating the new file and saving the already-parsed array to the variable.
            this.CurrentDataHandler.CreateNewPerformanceFile(this.FP_HistoricPerformanceData, newArray);
            this.HistoricPerformanceData = newArray;
        }

        //Creating a listener for the window to detect when it closes.
        WindowListener listener = new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                CurrentDataHandler.SaveUserSettings();
            }
        };
        addWindowListener(listener);

        //Initialising the UI.
        InitialiseUI();
        Recolour();
    }

    //Helper methods
    //Converts a scale between 0 and 1 to the number of pixels as confined by maxSize.
    int Scale(double scale, double maxSize) {
        return (int) (maxSize * scale);
    }

    //Sets up the main buttons at the top of the screen. Reasoning is in the documentation.
    private void SetupMainButton (JButton b, String T, Border B, Font F) {
        pnl_Buttons.add(Box.createRigidArea(new Dimension(_mainComponentPadding, 0)));
        b.setMinimumSize(new Dimension(75,50));
        b.setPreferredSize(new Dimension(150,50));
        b.setMaximumSize(new Dimension(250,50));
        b.setAlignmentY(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setName("ReversibleColour");
        b.setText(T);
        b.setBorder(B);
        b.setFont(F);
    }

    //JSON parser helper method to parse every file. Useful since I have 3 different files.
    public Object ParseJSON(String filepath) {
        JSONParser JSONFileParser = new JSONParser();
        try (FileReader fileReader = new FileReader(filepath)) {
            return JSONFileParser.parse(fileReader);
        } catch (FileNotFoundException error) {
            error.printStackTrace();
        } catch (IOException error) {
            error.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;            //Returning null if an exception occurred.
    }

    //Method to create each topic, to be listed under each specification.
    private void CreateTopicListing(JSONObject topic, SpecificationPanel newSpecPanel) {
        //Creating the object, and setting a few parameters that can not be set in the constructor.
        TopicPanel newTopicPanel = new TopicPanel(this, topic, newSpecPanel.getSpecificationName());
        newSpecPanel.AddTopic(newTopicPanel);
        newTopicPanel.pnl_RootPanel.setPreferredSize(new Dimension(newSpecPanel.pnl_RootPanel.getWidth(), 50));
        newTopicPanel.tf_TopicName.setFont(_universalSecondaryFont);

        if (Debug) {
            System.out.println(topic.get("TopicName"));
            newTopicPanel.pnl_RootPanel.setBackground(new Color(0,200,255));
        }
    }

    //Method to create each specification panel, to contain all the topic labels.
    private void CreateSpecificationItem(JSONObject spec) {
        //Creating the object, and setting a few parameters that can not be set in the constructor.
        JSONArray topics = (JSONArray) spec.get("Topics");
        SpecificationPanel newPanel = new SpecificationPanel(spec);
        SpecificationObjects.add(newPanel);
        newPanel.pnl_RootPanel.setMinimumSize(new Dimension(pnl_Topics.getWidth(), 0));
        newPanel.pnl_RootPanel.setPreferredSize(new Dimension(pnl_Topics.getWidth(), topics.size() * 50 + 25));
        newPanel.pnl_RootPanel.setBorder(_jPanelBorder);
        newPanel.tf_SpecificationTitle.setFont(_universalFont);
        pnl_Topics.add(newPanel.pnl_RootPanel);

        //Creating each topic necessary for this specification, iteratively.
        topics.forEach(top -> CreateTopicListing((JSONObject) top, newPanel));
        newPanel.pnl_TopicsPanel.setLayout(new GridLayout(topics.size(), 1, 0, 2));

        if (Debug) {
            System.out.println(spec.get("SpecificationName"));
            newPanel.pnl_RootPanel.setBackground(new Color(0, 0, 200));
        }
    }

    //I think this method should be relatively self-explanatory. Recursively removes whitespace.
    public static String RemoveWhitespace(String str) {
        if (str.equals("")) { return str;}

        if (str.substring(0, 1).equals(" ")) {
            str = str.substring(1);
            str = RemoveWhitespace(str);
        } else if (str.substring(str.length()-1).equals(" ")) {
            str = str.substring(0, str.length() - 1);
            str = RemoveWhitespace(str);
        }

        return str;
    }

    //This method verifies a string as a number, leaving only the digits and "-" signs. Returns number validity.
    public boolean VerifyStringAsNumbersBoolean(String str) {
        if (str.equals("")) {return false;}

        //Converting to a list of characters to process
        char[] charList = str.toCharArray();
        str = "";

        //Deciding limit
        short limit = 0;
        if (charList.length > 8) {
            return false;
        } else {
            limit = (short) charList.length;
        }

        //Exceptional case.
        boolean zero_flag = true;
        for (int i = 0; i < limit; i++) {
            if ((int) charList[i] != 48) {
                zero_flag = false;
                break;
            }
        }
        if (limit > 0 && zero_flag == true) {return true;}

        //Flag for leading zeros.
        boolean leadingZero = true;

        //48 to 57 inclusive are 0-9. 45 is the "-" symbol.
        //Range is 8 because any more would cause a buffer overflow when parsing.
        for (int i = 0; i < limit; i++) {
            if ((int) charList[i] == 48 && leadingZero == false) {
                str += "" + charList[i];
            } else if (i == 0 && (int) charList[i] == 45) {
                str += "" + charList[i];
                leadingZero = false;
            } else if ((int) charList[i] >= 49 && (int) charList[i] <= 57) {
                str += "" + charList[i];
                leadingZero = false;
            } else {
                return false;
            }
        }

        return true;
    }

    //This method gets rid of any alphabetical symbols from a string, leaving only the digits and "-" signs.
    public String VerifyStringAsNumbers(String str) {
        if (str.equals("")) {return "0";}

        //Converting to a list of characters to process
        char[] charList = str.toCharArray();
        str = "";

        //Deciding limit
        short limit = 0;
        if (charList.length > 8) {
            limit = 8;
        } else {
            limit = (short) charList.length;
        }

        //Exceptional case.
        boolean zero_flag = true;
        for (int i = 0; i < limit; i++) {
            if ((int) charList[i] != 48) {
                zero_flag = false;
                break;
            }
        }
        if (limit > 0 && zero_flag == true) {return "0";}

        //Flag for leading zeros.
        boolean leadingZero = true;

        //48 to 57 inclusive are 0-9. 45 is the "-" symbol.
        //Range is 8 because any more would cause a buffer overflow when parsing.
        for (int i = 0; i < limit; i++) {
            if ((int) charList[i] == 48 && leadingZero == false) {
                str += "" + charList[i];
            } else if (i == 0 && (int) charList[i] == 45) {
                str += "" + charList[i];
                leadingZero = false;
            } else if ((int) charList[i] >= 49 && (int) charList[i] <= 57) {
                str += "" + charList[i];
                leadingZero = false;
            }
        }

        return str;
    }

    //Method for collecting all selected topics
    public ArrayList<TopicPanel> GetSelectedTopics() {
        ArrayList<TopicPanel> selectedTopics = new ArrayList<>();

        //Iterating through all specifications and their topics.
        for (int i = 0; i < SpecificationObjects.size(); i++) {
            SpecificationPanel specification = SpecificationObjects.get(i);

            //Looping through each topic in the given specification, and checking its selected state.
            for (int j = 0; j < specification.getRelatedTopicObjects().size(); j++) {
                TopicPanel topic = specification.getRelatedTopicObjects().get(j);
                if (topic.getSelected() == true) {
                    selectedTopics.add(topic);
                }
            }
        }

        return selectedTopics;
    }

    //Method for updating the list of questions
    public void UpdateQuestionListUI(boolean evaluated) {
        int contentSizeY = (ListedQuestionBox.Height + _questionPadding + ((evaluated) ? ListedQuestionBox.Height : 0));
        contentSizeY *= CurrentQuestions.length;
        if (DisplayModelAnswers == true) {contentSizeY += GetSelectedTopics().size() * ModelAnswer.Height;}
        pnl_InnerQuestionList.setPreferredSize(new Dimension(pnl_InnerQuestionList.getWidth(), contentSizeY));
        pnl_InnerQuestionList.revalidate();
    }

    //Handler for the generation button.
    public void HandleGenerateButtonToggled (ActionEvent event) {

        //Collecting all the selected topics.
        ArrayList<TopicPanel> selectedTopics = GetSelectedTopics();
        short numberOfTopicsSelected = (short) selectedTopics.size();

        //Making sure at least one of the topics is selected before proceeding.
        if (numberOfTopicsSelected > 0) {

            //Generating model answers.
            if (DisplayModelAnswers == true) {
                for (int i = 0; i < selectedTopics.size(); i++) {
                    JSONObject answerData = new JSONObject();
                    JSONArray examples = (JSONArray) selectedTopics.get(i).getTopicData().get("ExampleQuestions");
                    answerData.put("ExampleQuestions", examples);
                    answerData.put("TopicName", selectedTopics.get(i).getTopicData().get("TopicName"));

                    ModelAnswer newModelAnswerPanel = new ModelAnswer(this, answerData);
                    pnl_InnerQuestionList.add(newModelAnswerPanel.pnl_RootPanel);
                }
            }

            //Generating questions.
            ArrayList<JSONObject> generatedDataObjects = CurrentQuestionHandlerInstance.GenerateQuestions(selectedTopics);

            //Visualising the questions.
            QuestionBox[] questionBoxes;
            questionBoxes = CurrentVisualiserInstance.CreateListQuestionFrames(generatedDataObjects);

            //Saving a reference to this array for use later (in the evaluation).
            CurrentQuestions = questionBoxes;

            //Placing the questions in the frame.
            for (int i = 0; i < questionBoxes.length; i++) {
                pnl_InnerQuestionList.add(questionBoxes[i].pnl_RootPanel);
            }

            //I have to do this to overwrite Swing's BorderLayout's intentions, because it isn't smart.
            Container pane = getContentPane();
            pane.add(pnl_Questions, BorderLayout.LINE_START);

            //Configuring panels.
            pnl_Topics.setVisible(false);
            pnl_Questions.setVisible(true);
            pnl_InnerQuestionList.setLayout(new GridLayout(0, 1, 0, _questionPadding));

            UpdateQuestionListUI(false);

            scpn_TopicsList.setVisible(false);

            //Flags
            DoingQuestions = true;
            btn_Generate.setEnabled(false);
            btn_Evaluate.setEnabled(true);

            //Recolouring.
            Recolour();
        }
    }

    //The method to evaluate the questions.
    public void HandleEvaluateButtonToggled(ActionEvent event) {
        CurrentQuestionHandlerInstance.EvaluateQuestions(CurrentQuestions);

        //Setting buttons.
        btn_Evaluate.setEnabled(false);
        btn_Finish.setEnabled(true);

        //Updating the list UI.
        UpdateQuestionListUI(true);

        //Recolouring to ensure the UI aesthetic is unchanged.
        Recolour();
    }

    //Method for handling the finish button toggling.
    public void HandleFinishButtonToggled(ActionEvent event) {
        //Variables
        Container pane = getContentPane();

        //Destroying all question panels.
        pnl_InnerQuestionList.removeAll();
        CurrentQuestions = null;        //To ensure memory leaks don't happen.

        //Setting visibility of panels.
        pnl_Questions.setVisible(false);
        scpn_TopicsList.setVisible(true);
        pnl_Topics.setVisible(true);
        pane.add(scpn_TopicsList, BorderLayout.LINE_START);
        UpdateSuggestionsList();

        //Setting buttons and flags.
        btn_Finish.setEnabled(false);
        btn_Generate.setEnabled(true);
        DoingQuestions = false;

        //Recolouring everything to make sure the UI's aesthetic is not changed.
        Recolour();
    }

    //Small method to update the list of suggestions.
    private void UpdateSuggestionsList() {
        JLabel[] listOfTopics = CurrentVisualiserInstance.DisplaySuggestedTopics();
        pnl_SuggestedTopics.removeAll();
        for (int i = 0; i < listOfTopics.length; i++) {
            if (listOfTopics[i] == null) {continue;}
            pnl_SuggestedTopics.add(listOfTopics[i]);
        }
    }

    //Method to handle the user input logic for the colour choosing boxes.
    private boolean ProcessColourInput(JTextField textField, int arrayNumber) {
        //Validating and sanitising user input.
        String stringValue = RemoveWhitespace(textField.getText());
        if (stringValue.equals("")) {stringValue = "0/0/0";}

        //Splitting by the forward-slash symbol.
        String[] strings = stringValue.split("/");
        if (strings.length != 3) {return false;}

        //Validating input.
        for (int i = 0; i < strings.length; i++) {
            if (!VerifyStringAsNumbersBoolean(strings[i])) {
                return false;
            } else {
                if (!strings[i].equals("") && !strings[i].equals("-")) {
                    int result = Integer.parseInt(VerifyStringAsNumbers(strings[i]));
                    if (result < 0 || result > 255) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        //If the array has 3 items in it, then process the data. Can't have 3 colour channels but only 2 inputs.
        if (strings.length >= 3) {
            //Removing any non-alphabetic characters/
            String red = VerifyStringAsNumbers(strings[0]);
            String green = VerifyStringAsNumbers(strings[1]);
            String blue = VerifyStringAsNumbers(strings[2]);

            //If they're empty strings, make them 0.
            red = (red.equals("")) ? "0" : red;
            green = (green.equals("")) ? "0" : green;
            blue = (blue.equals("")) ? "0" : blue;

            //Updating the array values based on the input parameter.
            if (arrayNumber == 0) {
                PrimaryColour[0] = Math.abs(Integer.parseInt(red)) % 256;
                PrimaryColour[1] = Math.abs(Integer.parseInt(green)) % 256;
                PrimaryColour[2] = Math.abs(Integer.parseInt(blue)) % 256;
            } else if (arrayNumber == 1) {
                SecondaryColour[0] = Math.abs(Integer.parseInt(red)) % 256;
                SecondaryColour[1] = Math.abs(Integer.parseInt(green)) % 256;
                SecondaryColour[2] = Math.abs(Integer.parseInt(blue)) % 256;
            }

            //Outputting debug values.
            if (Debug) {
                System.out.println("R: "+PrimaryColour[0]+"\nG: "+PrimaryColour[1]+"\nB: "+PrimaryColour[2]);
            }

            return true;
        }

        return false;
    }

    //Method to recolour the entire user interface (uses the Visualiser class to do that).
    private void Recolour() {
        Color primaryColor = new Color(PrimaryColour[0], PrimaryColour[1], PrimaryColour[2]);
        Color secondaryColor = new Color(SecondaryColour[0], SecondaryColour[1], SecondaryColour[2]);
        this.CurrentVisualiserInstance.ChangeColours(this, primaryColor, secondaryColor);
        tf_PrimaryColour.setText(PrimaryColour[0] + "/" + PrimaryColour[1] + "/" + PrimaryColour[2]);
        tf_SecondaryColour.setText(SecondaryColour[0] + "/" + SecondaryColour[1] + "/" + SecondaryColour[2]);
    }

    static Color ArrayToColour(int[] colourArray) {
        return new Color(colourArray[0], colourArray[1], colourArray[2]);
    }

    //*The* method for initialising the entire user interface.
    private void InitialiseUI() {
        //Configuring the window.
        setTitle("Mathematics Revision Question Generator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);    //Stops the program on close.
        setExtendedState(JFrame.MAXIMIZED_BOTH);                    //Fullscreen.
        setVisible(true);                                           //Actually visible.
        Container pane = getContentPane();

        //Window size parameters.
        Dimension WindowSize = getToolkit().getScreenSize();    //from the AWT package
        double WindowHeight = WindowSize.getHeight();
        double WindowWidth = WindowSize.getWidth();
        setMinimumSize(new Dimension(Scale(0.5, WindowWidth), Scale(0.5, WindowHeight)));

        //-------------------- Creating the main panels and configuring them. --------------------
        pnl_Topics = new JPanel();
        scpn_TopicsList = new JScrollPane(pnl_Topics);
        pnl_Questions = new JPanel();
        pnl_InnerQuestionList = new JPanel();
        scpn_QuestionsList = new JScrollPane(pnl_InnerQuestionList);
        pnl_Buttons = new JPanel();
        pnl_Settings = new JPanel();
        pnl_Statistics = new JPanel();

        if (Debug) {setBackground(new Color(50, 50, 150));}

        //Button panel.
        pane.add(pnl_Buttons, BorderLayout.PAGE_START);
        pnl_Buttons.setMinimumSize(new Dimension(0, 50));
        pnl_Buttons.setPreferredSize(new Dimension((int)WindowWidth, Scale(0.1, WindowHeight)));
        pnl_Buttons.setMaximumSize(new Dimension((int)WindowWidth, Scale(0.1, WindowHeight)));
        pnl_Buttons.setBorder(_jPanelBorder);
        pnl_Buttons.setLayout(new BoxLayout(pnl_Buttons, BoxLayout.LINE_AXIS));
        pnl_Buttons.setName("ButtonPanel");
        if (Debug) {pnl_Buttons.setBackground(new Color(0, 0, 0));}

        //Question panels
        pane.add(pnl_Questions, BorderLayout.LINE_START);
        pnl_Questions.setVisible(false);    //I don't want the question panel to be visible, since it has nothing on it.
        pnl_Questions.setMinimumSize(new Dimension(0, Scale(0.7, WindowHeight)));
        pnl_Questions.setPreferredSize(new Dimension(Scale(0.8, WindowWidth), Scale(0.9, WindowHeight)));
        pnl_Questions.setMaximumSize(new Dimension(Scale(0.8, WindowWidth), Scale(0.9, WindowHeight)));
        pnl_Questions.setBorder(_jPanelBorder);
        pnl_Questions.setLayout(new BorderLayout());
        if (Debug) {pnl_Questions.setBackground(new Color(255,255,0));}

        pnl_InnerQuestionList.setPreferredSize(new Dimension(
                Scale(0.8, WindowWidth),
                Scale(0.9, WindowHeight)
        ));
        if (Debug) {pnl_InnerQuestionList.setBackground(new Color(125, 50, 0));}

        pnl_Questions.add(scpn_QuestionsList, BorderLayout.CENTER);
        scpn_QuestionsList.setWheelScrollingEnabled(true);
        scpn_QuestionsList.setHorizontalScrollBar(null);
        scpn_QuestionsList.setBorder(_jPanelBorder);

        JScrollBar QuestionPanelScroller = new JScrollBar();
        QuestionPanelScroller.setEnabled(true);
        QuestionPanelScroller.setVisible(true);
        QuestionPanelScroller.setUnitIncrement(_ScrollerUnitIncrement);
        scpn_QuestionsList.setVerticalScrollBar(QuestionPanelScroller);

        //Topic panels.
        pnl_Topics.setPreferredSize(new Dimension(Scale(0.8, WindowWidth), Scale(0.9, WindowHeight)));
        if (Debug) {pnl_Topics.setBackground(new Color(255, 0, 0));}

        pane.add(scpn_TopicsList, BorderLayout.LINE_START);
        scpn_TopicsList.setWheelScrollingEnabled(true);
        scpn_TopicsList.setHorizontalScrollBar(null);
        scpn_TopicsList.setBorder(_jPanelBorder);

        JScrollBar TopicsListScroller = new JScrollBar();
        TopicsListScroller.setEnabled(true);
        TopicsListScroller.setVisible(true);
        TopicsListScroller.setUnitIncrement(_ScrollerUnitIncrement);
        scpn_TopicsList.setVerticalScrollBar(TopicsListScroller);

        //Statistics panel.
        pane.add(pnl_Statistics, BorderLayout.LINE_END);
        pnl_Statistics.setMinimumSize(new Dimension(100, 200));
        pnl_Statistics.setPreferredSize(new Dimension(Scale(0.2, WindowWidth), Scale(0.8, WindowHeight)));
        pnl_Statistics.setMaximumSize(new Dimension(Scale(0.2, WindowWidth), Scale(0.8, WindowHeight)));
        pnl_Statistics.setBorder(_jPanelBorder);
        pnl_Statistics.setLayout(new BoxLayout(pnl_Statistics, BoxLayout.PAGE_AXIS));//I want this to look like a list.
        if (Debug) {pnl_Statistics.setBackground(new Color(0, 0, 255));}

        //Settings panel.
        pane.add(pnl_Settings, BorderLayout.PAGE_END);
        pnl_Settings.setMinimumSize(new Dimension(400, 100));
        pnl_Settings.setPreferredSize(new Dimension(Scale(1, WindowWidth), Scale(0.1, WindowHeight)));
        pnl_Settings.setMaximumSize(new Dimension(Scale(1, WindowWidth), Scale(0.1, WindowHeight)));
        pnl_Settings.setBorder(_jPanelBorder);
        pnl_Settings.setLayout(new BoxLayout(pnl_Settings, BoxLayout.LINE_AXIS));
        if (Debug) {pnl_Settings.setBackground(new Color(0,255,0));}

        //-------------------- Creating and adding the buttons for the top panel. --------------------
        pnl_Buttons.add(Box.createHorizontalGlue());
        pnl_Buttons.add(Box.createRigidArea(new Dimension(_mainComponentPadding, 0)));//Spacing between buttons.

        pnl_Buttons.add(btn_Generate = new JButton());
        SetupMainButton(btn_Generate, "GENERATE", _mainButtonBorder, _universalFont);

        pnl_Buttons.add(btn_Evaluate = new JButton());
        SetupMainButton(btn_Evaluate, "EVALUATE", _mainButtonBorder, _universalFont);
        btn_Evaluate.setEnabled(false);

        pnl_Buttons.add(btn_Finish = new JButton());
        SetupMainButton(btn_Finish, "FINISH", _mainButtonBorder, _universalFont);
        btn_Finish.setEnabled(false);

        pnl_Buttons.add(btn_NextQuestion = new JButton());
        SetupMainButton(btn_NextQuestion, "NEXT QUESTION", _mainButtonBorder, _universalFont);
        btn_NextQuestion.setVisible(false);

        pnl_Buttons.add(btn_PreviousQuestion = new JButton());
        SetupMainButton(btn_PreviousQuestion, "PREVIOUS QUESTION", _mainButtonBorder, _universalFont);
        btn_PreviousQuestion.setVisible(false);

        pnl_Buttons.add(Box.createHorizontalGlue());

        //Adding functionality to the generate button.
        btn_Generate.setActionCommand("Toggled");
        btn_Generate.addActionListener(event -> {
            //This event handler here serves as a proxy at the moment, but can be added to later when needed.
            HandleGenerateButtonToggled(event);
        });

        //Adding functionality to the evaluate button.
        btn_Evaluate.setActionCommand("Toggled");
        btn_Evaluate.addActionListener(event -> {
            //This event handler also serves as just a proxy.
            HandleEvaluateButtonToggled(event);
        });

        //Adding functionality to the finish button.
        btn_Finish.setActionCommand("Toggled");
        btn_Finish.addActionListener(event -> {
            //Again, this event handler here serves just as a proxy for now.
            HandleFinishButtonToggled(event);
        });

        pnl_Buttons.updateUI();     //Necessary to make sure Swing renders the objects. Sometimes it didn't.

        //Adding the specifications panels and topics panels.
        Specifications.forEach(specification -> CreateSpecificationItem((JSONObject) specification));

        //Updating the scroll pane's UI.
        scpn_TopicsList.updateUI();

        //Updating the list scroller.
        int absoluteContentHeight = 0;
        for (Component specificationPanel : pnl_Topics.getComponents()) {
            absoluteContentHeight += specificationPanel.getPreferredSize().height + _specificationPanelPadding;
        }
        if (Debug) {System.out.println(absoluteContentHeight);}

        pnl_Topics.setPreferredSize(new Dimension(pnl_Topics.getWidth(), absoluteContentHeight));
        pnl_Topics.revalidate();

        //-------------------- Creating statistics components. --------------------
        //Creating components for creating graphs.
        pnl_Statistics.add(Box.createRigidArea(new Dimension(0, _mainComponentPadding)));
        pnl_Statistics.add(btn_CreateGraphs = new JButton("CREATE GRAPH(S)"));
        btn_CreateGraphs.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn_CreateGraphs.setMaximumSize(new Dimension(150, 25));
        btn_CreateGraphs.setFocusPainted(false);
        btn_CreateGraphs.setOpaque(true);
        btn_CreateGraphs.setName("ReversibleColour");
        btn_CreateGraphs.setActionCommand("Change");
        btn_CreateGraphs.addActionListener(event -> {
            if (event.getActionCommand().equals(btn_CreateGraphs.getActionCommand())) {
                switch (cmb_ChooseGraph.getSelectedIndex()) {
                    case 0:
                        CurrentVisualiserInstance.generateScatterGraph();
                        break;
                }
            }
        });

        pnl_Statistics.add(Box.createRigidArea(new Dimension(0, 2)));
        String[] graphs = {"Scatter graph", "Bar chart", "Line chart"};
        pnl_Statistics.add(cmb_ChooseGraph = new JComboBox(graphs));
        cmb_ChooseGraph.setSelectedIndex(0);
        cmb_ChooseGraph.setAlignmentX(Component.CENTER_ALIGNMENT);
        cmb_ChooseGraph.setMaximumSize(new Dimension(250, 30));
        cmb_ChooseGraph.setOpaque(false);

        //Creating components for the suggested topics list.
        pnl_Statistics.add(Box.createRigidArea(new Dimension(0, _mainComponentPadding)));
        pnl_Statistics.add(lbl_SuggestedTopics = new JLabel());
        lbl_SuggestedTopics.setText("Suggested topics for improvement:");
        lbl_SuggestedTopics.setFont(_universalFont);
        lbl_SuggestedTopics.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnl_Statistics.add(pnl_SuggestedTopics = new JPanel());
        pnl_SuggestedTopics.setMinimumSize(new Dimension(100, 200));
        pnl_SuggestedTopics.setPreferredSize(new Dimension(Scale(0.2, WindowWidth), 250));
        pnl_SuggestedTopics.setMaximumSize(new Dimension(Scale(0.2, WindowWidth), 250));
        pnl_SuggestedTopics.setBorder(_jPanelBorder);
        pnl_SuggestedTopics.setLayout(new GridLayout(5, 1, 0, 1));

        //Creating a small text box to explain how answers should be given.
        pnl_Statistics.add(Box.createVerticalGlue());
        pnl_Statistics.add(ta_AnsweringGuidance = new JTextArea());
        ta_AnsweringGuidance.setText("Guidance for answering:\n" +
                "Note that it is necessary to round any numerical answers to 1 decimal place.\n" +
                "In the case that it is a whole number, include a \".0\" at the end of the number.");
        ta_AnsweringGuidance.setFont(_universalSecondaryFont);
        ta_AnsweringGuidance.setOpaque(false);
        ta_AnsweringGuidance.setLineWrap(true);
        ta_AnsweringGuidance.setWrapStyleWord(true);
        ta_AnsweringGuidance.setEditable(false);
        ta_AnsweringGuidance.setFocusable(false);
        ta_AnsweringGuidance.setBorder(null);
        ta_AnsweringGuidance.setMinimumSize(new Dimension(100, 50));
        ta_AnsweringGuidance.setPreferredSize(new Dimension(Scale(0.18, WindowWidth), 100));
        ta_AnsweringGuidance.setMaximumSize(new Dimension(Scale(0.18, WindowWidth), 100));
        ta_AnsweringGuidance.setAlignmentX(0.5F);

        //Displaying the top topics for improvement.
        UpdateSuggestionsList();

        pnl_Statistics.updateUI();

        //-------------------- Creating settings components. --------------------
        //Checkbox for displaying as a list.
        cb_QuestionsAsList = new JCheckBox();
        cb_QuestionsAsList.setText("Display questions as a list?");
        cb_QuestionsAsList.setFont(_universalSecondaryFont);
        cb_QuestionsAsList.setSelected(DisplayQuestionsAsList);
        cb_QuestionsAsList.setOpaque(false);
        cb_QuestionsAsList.setFocusPainted(false);
        cb_QuestionsAsList.setActionCommand("Toggle");
        cb_QuestionsAsList.addActionListener(event -> {
            if (event.getActionCommand().equals("Toggle")) {
                DisplayQuestionsAsList = !DisplayQuestionsAsList;
                cb_QuestionsAsList.setSelected(DisplayQuestionsAsList);
            }
        });

        //Checkbox for displaying model answers.
        cb_EnableModelAnswer = new JCheckBox();
        cb_EnableModelAnswer.setText("Model answers?");
        cb_EnableModelAnswer.setFont(_universalSecondaryFont);
        cb_EnableModelAnswer.setSelected(DisplayModelAnswers);
        cb_EnableModelAnswer.setOpaque(false);
        cb_EnableModelAnswer.setFocusPainted(false);
        cb_EnableModelAnswer.setActionCommand("Toggle");
        cb_EnableModelAnswer.addActionListener(event -> {
            if (event.getActionCommand().equals("Toggle")) {
                DisplayModelAnswers = !DisplayModelAnswers;
                cb_EnableModelAnswer.setSelected(DisplayModelAnswers);
            }
        });

        //Checkbox for enabling bias.
        cb_EnableBias = new JCheckBox();
        cb_EnableBias.setText("Smart question generation?");
        cb_EnableBias.setFont(_universalSecondaryFont);
        cb_EnableBias.setSelected(EnableBias);
        cb_EnableBias.setOpaque(false);
        cb_EnableBias.setFocusPainted(false);
        cb_EnableBias.setActionCommand("Toggle");
        cb_EnableBias.addActionListener(event -> {
           if (event.getActionCommand().equals("Toggle")) {
               this.EnableBias = !this.EnableBias;
               cb_EnableBias.setSelected(this.EnableBias);
           }
        });

        //Text box for inputting number of questions to generate.
        tf_NumberOfQuestionsBox = new JTextField();
        tf_NumberOfQuestionsBox.setText("" + NumberOfQuestionsToGenerate);
        tf_NumberOfQuestionsBox.setFont(_universalNumberFont);
        tf_NumberOfQuestionsBox.setOpaque(false);
        tf_NumberOfQuestionsBox.setHorizontalAlignment(SwingConstants.CENTER);
        tf_NumberOfQuestionsBox.setMinimumSize(new Dimension(50, 20));
        tf_NumberOfQuestionsBox.setPreferredSize(new Dimension(75, 20));
        tf_NumberOfQuestionsBox.setMaximumSize(new Dimension(100, 20));
        tf_NumberOfQuestionsBox.setActionCommand("Change");
        tf_NumberOfQuestionsBox.addActionListener(event -> {
            if (event.getActionCommand().equals("Change")) {
                //Removing all whitespace and validating as a pure number (removing characters, etc).
                String stringValue = VerifyStringAsNumbers(RemoveWhitespace(tf_NumberOfQuestionsBox.getText()));
                if (stringValue.equals("")) {stringValue = "0";}

                //Converting to an integer.
                int numValue = Integer.parseInt(stringValue);

                //Extra sanity checks, and setting as text in box and internal value if valid value.
                if (stringValue.equals("") || numValue < 0) {
                    NumberOfQuestionsToGenerate = 0;
                    tf_NumberOfQuestionsBox.setText("0");
                } else if (numValue > maxNumberOfQuestions) {
                    NumberOfQuestionsToGenerate = maxNumberOfQuestions;
                    tf_NumberOfQuestionsBox.setText("" + NumberOfQuestionsToGenerate);
                } else {
                    NumberOfQuestionsToGenerate = (short) numValue;
                    tf_NumberOfQuestionsBox.setText(stringValue);
                }
            }
        });

        //Label for the above text box.
        lbl_NumberOfQuestionsLabel = new JLabel();
        lbl_NumberOfQuestionsLabel.setText("Number of questions?");
        lbl_NumberOfQuestionsLabel.setFont(_universalSecondaryFont);
        lbl_NumberOfQuestionsLabel.setOpaque(false);
        lbl_NumberOfQuestionsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        //Colour input components
        tf_PrimaryColour = new JTextField();
        tf_PrimaryColour.setText("");
        tf_PrimaryColour.setFont(_universalNumberFont);
        tf_PrimaryColour.setOpaque(false);
        tf_PrimaryColour.setHorizontalAlignment(SwingConstants.CENTER);
        tf_PrimaryColour.setMinimumSize(new Dimension(50, 20));
        tf_PrimaryColour.setPreferredSize(new Dimension(150, 20));
        tf_PrimaryColour.setMaximumSize(new Dimension(200, 20));
        tf_PrimaryColour.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (ProcessColourInput(tf_PrimaryColour, 2) == true) {
                    tf_PrimaryColour.setForeground(new Color(0, 255, 0));
                } else {
                    tf_PrimaryColour.setForeground(new Color(255, 0, 0));
                }
                //10 corresponds to ENTER
                if (keyEvent.getKeyCode() == 10) {
                    //Updating array values
                    ProcessColourInput(tf_PrimaryColour, 0);

                    //Colouring.
                    Recolour();
                }
            }
        });
        tf_PrimaryColour.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                if (ProcessColourInput(tf_PrimaryColour, 2) == true) {
                    tf_PrimaryColour.setForeground(new Color(0, 255, 0));
                } else {
                    tf_PrimaryColour.setForeground(new Color(255, 0, 0));
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                //Updating array values
                ProcessColourInput(tf_PrimaryColour, 0);

                //Colouring.
                Recolour();
            }
        });

        lbl_PrimaryColourLabel = new JLabel();
        lbl_PrimaryColourLabel.setText("Primary colour");
        lbl_PrimaryColourLabel.setFont(_universalSecondaryFont);

        tf_SecondaryColour = new JTextField();
        tf_SecondaryColour.setText("");
        tf_SecondaryColour.setFont(_universalNumberFont);
        tf_SecondaryColour.setOpaque(false);
        tf_SecondaryColour.setHorizontalAlignment(SwingConstants.CENTER);
        tf_SecondaryColour.setMinimumSize(new Dimension(50, 20));
        tf_SecondaryColour.setPreferredSize(new Dimension(150, 20));
        tf_SecondaryColour.setMaximumSize(new Dimension(200, 20));
        tf_SecondaryColour.setActionCommand("Change");
        tf_SecondaryColour.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (ProcessColourInput(tf_SecondaryColour, 2) == true) {
                    tf_SecondaryColour.setForeground(new Color(0, 255, 0));
                } else {
                    tf_SecondaryColour.setForeground(new Color(255, 0, 0));
                }
                //10 corresponds to ENTER
                if (keyEvent.getKeyCode() == 10) {
                    //Updating array values
                    ProcessColourInput(tf_SecondaryColour, 1);

                    //Colouring.
                    Recolour();
                }
            }
        });
        tf_SecondaryColour.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                if (ProcessColourInput(tf_SecondaryColour, 2) == true) {
                    tf_SecondaryColour.setForeground(new Color(0, 255, 0));
                } else {
                    tf_SecondaryColour.setForeground(new Color(255, 0, 0));
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                //Updating array values
                ProcessColourInput(tf_SecondaryColour, 1);

                //Colouring.
                Recolour();
            }
        });

        lbl_SecondaryColourLabel = new JLabel();
        lbl_SecondaryColourLabel.setText("Secondary colour");
        lbl_SecondaryColourLabel.setFont(_universalSecondaryFont);

        //Adding all settings components to the panel.
        pnl_Settings.add(Box.createHorizontalGlue());
        pnl_Settings.add(cb_QuestionsAsList);
        pnl_Settings.add(Box.createRigidArea(new Dimension(_secondaryComponentPadding,0)));
        pnl_Settings.add(cb_EnableModelAnswer);
        pnl_Settings.add(Box.createRigidArea(new Dimension(_secondaryComponentPadding,0)));
        pnl_Settings.add(cb_EnableBias);
        pnl_Settings.add(Box.createRigidArea(new Dimension(_secondaryComponentPadding, 0)));
        pnl_Settings.add(tf_NumberOfQuestionsBox);
        pnl_Settings.add(Box.createRigidArea(new Dimension(_secondaryComponentPadding, 0)));
        pnl_Settings.add(lbl_NumberOfQuestionsLabel);
        pnl_Settings.add(Box.createRigidArea(new Dimension(_secondaryComponentPadding, 0)));
        pnl_Settings.add(tf_PrimaryColour);
        pnl_Settings.add(lbl_PrimaryColourLabel);
        pnl_Settings.add(Box.createRigidArea(new Dimension(_secondaryComponentPadding * 2, 0)));
        pnl_Settings.add(tf_SecondaryColour);
        pnl_Settings.add(lbl_SecondaryColourLabel);
        pnl_Settings.add(Box.createHorizontalGlue());

        pnl_Settings.updateUI();
    }

    //Main method for controlling workflow.
    public static void main(String[] args) {
        //Initialising the main window.
        Main MainWindow = new Main();
    }
}

/*
 */