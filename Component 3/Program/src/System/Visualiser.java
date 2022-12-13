package System;

import java.awt.*;
import java.time.*;
import java.util.*;
import org.json.simple.*;

import javax.swing.*;

public class Visualiser {
    boolean Debug;
    Main MainClass;

    //Constructor
    public Visualiser (Main mainClass) {
        this.Debug = mainClass.Debug;
        this.MainClass = mainClass;
    }

    //Method for creating a listed question frame.
    public QuestionBox[] CreateListQuestionFrames(ArrayList<JSONObject> questionDataArray) {
        QuestionBox[] questionPanels = new QuestionBox[questionDataArray.size()];

        //Not creating new panels if the toggle is turned off.
        if (this.MainClass.DisplayQuestionsAsList) {
            for (int i = 0; i < questionDataArray.size(); i++) {
                ListedQuestionBox newQuestionBox = new ListedQuestionBox(this.MainClass, questionDataArray.get(i));
                newQuestionBox.setQuestionNumber(i);
                questionPanels[i] = newQuestionBox;
            }
        }

        return questionPanels;
    }

    public JLabel[] DisplaySuggestedTopics() {
        //Declaring a new hashmap to store all the topics in.
        HashMap topics = new HashMap();

        //Getting all the topics in every specification.
        for (Object obj1 : this.MainClass.Specifications) {
            JSONObject specification = (JSONObject) obj1;

            for (Object obj2 : (JSONArray) specification.get("Topics")) {
                JSONObject topic = (JSONObject) obj2;

                //Making sure the bias object is an Integer. Sometimes they're Integers, other times they're Longs.
                Object biasObj = topic.get("TopicBias");
                Integer bias = (biasObj.getClass().equals(Long.class)) ? (int) (long) biasObj : (Integer) biasObj;

                //Placing both objects into the HashMap for use later.
                topics.put((String) topic.get("TopicName"), bias);
            }
        }

        //Getting the top 5 topics with the highest biases.
        ArrayList<String> sortedTopics = PartialSort(topics, 5);

        //Creating the necessary Swing components.
        JLabel[] arrayOfLabels = new JLabel[5];
        for (int i = 0; i < sortedTopics.size(); i++) {
            JLabel newLabel = new JLabel();
            newLabel.setText("    " + (i + 1) + ". " + sortedTopics.get(i));
            newLabel.setFont(this.MainClass._universalSecondaryFont);
            newLabel.setBorder(BorderFactory.createLineBorder(new Color(0,0,0), 1));
            arrayOfLabels[i] = newLabel;
        }

        return arrayOfLabels;
    }

    private ArrayList<String> PartialSort(HashMap map, int numberOfHighest) {
        ArrayList<String> emptyList = new ArrayList<>();

        //Small error check to make sure the map has the desired number of elements in it in the first place.
        if (map.size() < numberOfHighest) {
            System.out.println("Unable to find all " + numberOfHighest + " items since not that many exist.");
            return emptyList;
        }

        //Iterating until the desired number of elements has been reached.
        for (int i = 0; i < numberOfHighest; i++) {
            int max = 0;
            String selected = "";

            //Iterating each element in the map.
            for (Object keyObj : map.keySet()) {
                String topicName = (String) keyObj;
                Integer bias = (Integer) map.get(topicName);

                if (bias >= max) {
                    max = bias;
                    selected = topicName;
                }
            }

            //Removing the selected value from the map and adding it to the empty list.
            map.remove(selected);
            emptyList.add(selected);
        }

        return emptyList;
    }

    //Recursive method to change all the colours of every object.
    int layers = -1;
    public void ChangeColours(Container rootComp, Color primaryColour, Color secondaryColor) {
        layers++;

        //Iterating through each child component of the root component.
        for (Component childComponent : rootComp.getComponents()) {

            //Debug printout.
            if (this.Debug) {
                for (int i = 0; i < layers; i++) { System.out.print("  "); }
                System.out.println(childComponent.getClass() + "\t\t" + childComponent.getName());
            }

            //Colouring
            if (this.isColourable(childComponent)) {
                if (childComponent.getName() != null && childComponent.getName().equals("ReversibleColour")) {
                    childComponent.setBackground(secondaryColor);
                    childComponent.setForeground(primaryColour);
                } else {
                    childComponent.setBackground(primaryColour);
                    childComponent.setForeground(secondaryColor);
                }
            }

            //Casting the child component to a Container object.
            Container newRootComp = null;
            if (Container.class.isAssignableFrom(childComponent.getClass())) {
                newRootComp = (Container) childComponent;
            } else if (childComponent.getClass().equals(JRootPane.class)) {
                newRootComp = ((JRootPane) childComponent).getContentPane();
            }

            //If the new root component exists, call this method on that. If not, simply continue.
            //Continuing means the component is not an object with the BackgroundColor and ForegroundColor properties.
            if (newRootComp != null) {
                ChangeColours(newRootComp, primaryColour, secondaryColor);
            } else {
                continue;
            }
        }

        //This is here for formatting the output from earlier.
        layers--;
    }

    //Returns whether an object can be coloured or not.
    private boolean isColourable(Component compToCompare) {
        Object[] colourableClasses = {
                JPanel.class,
                JButton.class,
                JTextField.class,
                JLabel.class,
                JTextArea.class,
                JCheckBox.class,
                JComboBox.class,
                JScrollBar.class,
                JScrollPane.class,
                JTextPane.class
        };
        String[] nonColourableNames = {
                "CorrectionBox",
                "GraphPoint"
        };

        boolean canColour = false;

        //Checking for legal classes.
        for (Object allowedClass : colourableClasses) {
            if (compToCompare.getClass().equals(allowedClass)) {
                canColour = true;
                break;
            }
        }

        //Checking for illegal names.
        for (String disallowedName : nonColourableNames) {
            if (compToCompare.getName() != null && compToCompare.getName().equals(disallowedName)) {
                canColour = false;
                break;
            }
        }

        return canColour;
    }

    //Method to generate a scatter graph.
    public void generateScatterGraph() {
        ArrayList<TopicPanel> selectedTopics = this.MainClass.GetSelectedTopics();

        //Ensuring the max number of topics for which graphs can be generated for is obeyed.
        if ((selectedTopics.size() + this.MainClass.NumberOfOpenWindows) > this.MainClass.MAX_ALLOWED_GRAPHS) {return;}

        for (int i = 0; i < selectedTopics.size(); i++) {
            TopicPanel topic = selectedTopics.get(i);
            JSONObject topicData = topic.getTopicData();
            ScatterGraph newGraph = new ScatterGraph(
                    this.MainClass,
                    (String) topicData.get("TopicName"),
                    "Time (Days)",
                    "Score Percentage"
            );

            ArrayList<Float> xValues = new ArrayList<>();
            ArrayList<Float> yValues = new ArrayList<>();

            //Getting all the topic's saved data from the historic data array.
            for (Object obj1 : this.MainClass.HistoricPerformanceData) {
                JSONObject historicSpecData = (JSONObject) obj1;
                if (historicSpecData.get("SpecificationName").equals(topicData.get("SpecificationName"))) {
                    for (Object obj2 : (JSONArray) historicSpecData.get("PerformanceData")) {
                        JSONObject dataObject = (JSONObject) obj2;
                        if (dataObject.get("TopicName").equals(topicData.get("TopicName"))) {
                            int diff = (int) (Instant.now().getEpochSecond() - (Long) dataObject.get("Epoch"));
                            float numberOfDays = EpochToDay(diff);

                            //Not taking into account data that is more than 30 days old.
                            if (numberOfDays <= 30) {
                                xValues.add(numberOfDays);
                                Object scoreObj = dataObject.get("Score");
                                Object maxScoreObj = dataObject.get("MaxScore");
                                float score = (scoreObj.getClass().equals(Integer.class))?
                                        (int) (Integer) scoreObj:
                                        (long) (Long) scoreObj;
                                float maxScore = (maxScoreObj.getClass().equals(Integer.class))?
                                        (int) (Integer) maxScoreObj:
                                        (long) (Long) maxScoreObj;
                                float yValue = score / maxScore;
                                yValues.add(yValue * 100F);
                            }
                        }
                    }
                }
            }

            //Using the enums defined in the ScatterGraph class to create axes for the newly generated scatter graph.
            newGraph.AddAxis(ScatterGraph.Axis.X, 0, 30, 6);
            newGraph.AddAxis(ScatterGraph.Axis.Y, 0, 100, 10);

            //Adding points. Choosing to iterate with respect to X values is an arbitrary choice.
            //The arrays will always be the same length because they get elements added at the same time.
            //Inverse lerping the values because the scatter graph itself should care about plotting them correctly.
            for (int j = 0; j < xValues.size(); j++) {
                newGraph.AddPoint(
                        (float) invLerp(xValues.get(j), 0, 30),
                        (float) invLerp(yValues.get(j), 0, 100)
                );
            }

            //Colouring the graph.
            int[] primary = this.MainClass.PrimaryColour;
            int[] secondary = this.MainClass.SecondaryColour;
            Color primaryColour = new Color(primary[0], primary[1], primary[2]);
            Color secondaryColour = new Color(secondary[0], secondary[1], secondary[2]);
            ChangeColours(newGraph.getRootPane(), primaryColour, secondaryColour);

            //Offsetting to fix certain rendering and placement issues. It's not the best solution, but it works.
            newGraph.OffsetGraph();

            //Incrementing the number of open windows in the main class.
            this.MainClass.NumberOfOpenWindows += 1;
        }
    }

    //Method to convert an epoch value to a day value.
    public float EpochToDay(int epochDifference) {
        return epochDifference / (60F * 60F * 24F);
    }

    //Method to remap a value to another. Remaps 1 value between a min and max into another min and max range.
    public static double remap(double val, double mini, double maxi, double mino, double maxo) {
        double alpha = invLerp(mini, maxi, val);
        return lerp(mino, maxo, alpha);
    }

    //Linear interpolation. Also very well known mathematics.
    public static double lerp(double alpha, double min, double max) {
        return min + alpha * (max - min);
    }

    //The opposite of above.
    public static double invLerp(double val, double min, double max) {
        return (val - min) / (max - min);
    }
}

