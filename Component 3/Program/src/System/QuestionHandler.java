package System;

import java.util.*;
import org.json.simple.*;

public class QuestionHandler {
    //Class attributes.
    boolean Debug = false;
    Main MainClass;

    //Constructor
    public QuestionHandler(Main mainClass) {
        this.Debug = mainClass.Debug;
        this.MainClass = mainClass;
    }

    //Method to round floats to 1 dp.
    private float roundFloat(float input) {
        return Math.round(input * 10F) /10F;
    }

    //Defining the standard order operations for the method (below this) to follow.
    String[] orderOfOperations = {
            "^",
            "*",
            "/",
            "+",
            "-"
    };

    //The method which processes the algebra given in the JSON files to generate the right answer.
    private float ProcessString(String inputAlgebra, JSONArray givenValues) {
        float answer = 0.0F;

        //Converting the given string into an arraylist of strings separated by spaces.
        ArrayList<String> symbols = new ArrayList<>(Arrays.asList(inputAlgebra.split(" ")));

        //Converting the given values from a JSONArray to an ArrayList of strings.
        ArrayList<String> givenValues_String = new ArrayList<>();
        for (Object listItem: givenValues) {
            givenValues_String.add(listItem + "");
        }

        if (this.Debug) {
            System.out.println("Symbols: " + symbols.toString());
            System.out.println("Given values: " + givenValues_String.toString());
        }

        //Iterating through each order of operations and amalgamating the array of symbols down in size.
        for (int i = 0; i < orderOfOperations.length; i++) {
            for (int j = 0; j < symbols.size(); j++) {
                if (symbols.get(j).equals(orderOfOperations[i])) {
                    int PreviousIndex, NextIndex;
                    float PreviousValue, NextValue;

                    //Getting the next and previous values.
                    if (symbols.get(j - 1).substring(0, 1).equals("#")) {
                        PreviousIndex = Integer.parseInt(symbols.get(j - 1).substring(1)) - 1;
                        PreviousValue = Float.parseFloat(givenValues_String.get(PreviousIndex));
                    } else {
                        PreviousValue = Float.parseFloat(symbols.get(j - 1));
                    }

                    if (symbols.get(j + 1).substring(0, 1).equals("#")) {
                        NextIndex = Integer.parseInt(symbols.get(j + 1).substring(1)) - 1;
                        NextValue = Float.parseFloat(givenValues_String.get(NextIndex));
                    } else {
                        NextValue = Float.parseFloat(symbols.get(j + 1));
                    }

                    //Outputting if needed.
                    if (this.Debug) {
                        System.out.print("Previous: " + PreviousValue + " | ");
                        System.out.println("Next: " + NextValue);

                        System.out.println(symbols.toString());
                    }

                    //Removing the 3 values.
                    symbols.remove(j);
                    symbols.remove(j);
                    symbols.remove(j-1);

                    //Carrying out the operation on the two values, and replacing the previous 3 with this result.
                    switch (orderOfOperations[i]) {
                        case "^":
                            symbols.add(j - 1, Math.pow(PreviousValue, NextValue) + "");
                            break;
                        case "*":
                            symbols.add(j - 1, PreviousValue * NextValue + "");
                            break;
                        case "/":
                            symbols.add(j - 1, PreviousValue / NextValue + "");
                            break;
                        case "+":
                            symbols.add(j - 1, PreviousValue + NextValue + "");
                            break;
                        case "-":
                            symbols.add(j - 1, PreviousValue - NextValue + "");
                            break;
                        default:
                            //no default for now.
                    }

                    if (this.Debug) {System.out.println(symbols.toString());}
                    j -= 2; //Decrementing the iterator since I've removed 2 values.
                }
            }
        }

        answer = roundFloat(Float.parseFloat(symbols.get(0)));

        if (this.Debug) {
            System.out.println("Answer: " + answer);
        }

        return answer;
    }

    private String ReplacePlaceholdersInString(String wording, JSONArray valuesArray) {
        String generatedQuestionWording = "";
        String[] characters = wording.split("");

        //Iterating characters and replacing #SOME_NUMBER with the corresponding value.
        for (int i = 0; i < characters.length; i++) {
            if (characters[i].equals("#")) {
                int correctValueIndex = Integer.parseInt(characters[i+1]) - 1;
                String value = (String) valuesArray.get(correctValueIndex);
                characters[i] = value;
                characters[i+1] = "";   //Can't remove from a static array, but this is effectively equivalent.
            }
        }

        //Stitching the character-strings back together.
        for (int i = 0; i < characters.length; i++) {
            generatedQuestionWording += characters[i];
        }

        return generatedQuestionWording;
    }

    //Private method to be used in the question generation process, and arguably, the most important one.
    private JSONObject RandomGenerate(JSONObject topicData) {
        JSONObject newObject = new JSONObject();
        Random RNG = new Random();

        //Choosing question format.
        JSONArray questionFormats = (JSONArray) topicData.get("QuestionFormats");
        short randomIndex = (short) RNG.nextInt(questionFormats.size());
        JSONObject chosenQuestionFormat = (JSONObject) questionFormats.get(randomIndex);

        //Generating values.
        JSONArray valuesArray = new JSONArray();
        JSONArray correctAnswersArray = new JSONArray();

        //Iterating through the values array and randomly choosing the value for each based on its type.
        for (Object valueFormat: (JSONArray) chosenQuestionFormat.get("Values")) {
            JSONObject convertedValueFormat = (JSONObject) valueFormat;
            String type = (String) convertedValueFormat.get("type");
            String chosenValue = "NULL";

            if (type.equals("integer")) {
                int min = (int) (long) convertedValueFormat.get("min");
                int max = (int) (long) convertedValueFormat.get("max");
                chosenValue = String.valueOf(RNG.nextInt(max - min) + min);
                valuesArray.add(chosenValue);
            } else if (type.equals("float")) {
                Object minimum = convertedValueFormat.get("min");
                Object maximum = convertedValueFormat.get("max");
                float min = (float) ((minimum.getClass().equals(Double.class)) ? (double) minimum : (long) minimum);
                float max = (float) ((maximum.getClass().equals(Double.class)) ? (double) maximum : (long) maximum);

                //.nextFloat() returns a number between 0 and 1, multiplying it by the range and adding the min
                //linearly interpolates the value between the range I want. Rounding the x10 version of that number
                //and then dividing that number by 1 produces the 1 decimal place version of this number.
                chosenValue = String.valueOf(roundFloat((Float) RNG.nextFloat() * (max - min) + min));
                valuesArray.add(chosenValue);
            } else if (type.equals("String")) {
                String valuesList = (String) convertedValueFormat.get("value");
                String[] arrayOfStrings = valuesList.split(",");

                int randomStringIndex = RNG.nextInt(arrayOfStrings.length);
                valuesArray.add(arrayOfStrings[randomStringIndex]);
            }
        }

        //Generating correct answers.
        String correctAnswer = "";
        for (Object correctAnswerData: (JSONArray) chosenQuestionFormat.get("CorrectAnswers")) {
            String convertedAnswerData = (String) correctAnswerData;

            //Deciding what to do with the data.;
            if (convertedAnswerData.length() >= 5 && convertedAnswerData.substring(0, 5).equals("ALG: ")) {
                convertedAnswerData = convertedAnswerData.substring(5);
                correctAnswer = String.valueOf(ProcessString(convertedAnswerData, valuesArray));
            } else if (convertedAnswerData.length() >= 10 && convertedAnswerData.substring(0, 10).equals("FILEPATH: ")) {
                convertedAnswerData = convertedAnswerData.substring(10);
                continue;
                //No code for this.
            } else {
                correctAnswer = convertedAnswerData;
            }

            //Adding the correct answer to the list.
            correctAnswersArray.add(correctAnswer);
        }

        //Generating question wording.
        String questionWording = (String) chosenQuestionFormat.get("QuestionWording");
        String generatedQuestionWording = ReplacePlaceholdersInString(questionWording, valuesArray);

        //Referencing other important data before constructing the new data object.
        JSONArray exampleQuestions = (JSONArray) topicData.get("ExampleQuestions");
        JSONArray correctWorkings = (JSONArray) chosenQuestionFormat.get("CorrectWorkings");
        JSONArray usefulReading = (JSONArray) topicData.get("UsefulReading");

        //Splicing the correct workings strings and adding the generated values.
        //Creating a new array with essentially the same values because this data should be forgotten when the
        //program closes because otherwise, the generated values will get saved to file and it will invalidate the
        //specifications.json file.
        JSONArray newCorrectWorkings = new JSONArray();
        for (Object obj: correctWorkings) {
            JSONObject dataObject = (JSONObject) obj;
            JSONObject newDataObject = new JSONObject();
            String newWording = ReplacePlaceholdersInString((String) dataObject.get("working"), valuesArray);
            newDataObject.put("working", newWording);
            newDataObject.put("mark", dataObject.get("mark"));
            newCorrectWorkings.add(newDataObject);
        }

        //Constructing the new data object
        newObject.put("SpecificationName", topicData.get("SpecificationName"));
        newObject.put("TopicName", topicData.get("TopicName"));
        newObject.put("ExampleQuestions", exampleQuestions);
        newObject.put("QuestionWording", generatedQuestionWording);
        newObject.put("CorrectAnswers", correctAnswersArray);
        newObject.put("CorrectWorkings", newCorrectWorkings);
        newObject.put("UsefulReading", usefulReading);

        return newObject;
    }

    //Main method to generate and process all the topics and their data.
    ArrayList<JSONObject> GenerateQuestions(ArrayList<TopicPanel> selectedTopics) {
        //Declaring the array to store all the topics which are chosen.
        ArrayList<TopicPanel> chosenTopics = new ArrayList<>();

        //Bias handling.
        short totalBias = 0;

        for (TopicPanel topic: selectedTopics) {
            //I have to convert to primitive long first before primitive short. Errors otherwise.
            Object bias = topic.getTopicData().get("TopicBias");
            totalBias += (short) ((bias.getClass().equals(Integer.class)) ? (Integer) bias : (Long) bias);
        }

        //Repeatedly choosing questions from topic. The algorithm below is explained more deeply in the documentation.
        int counter = 0;
        Random RNG = new Random();

        while (counter < this.MainClass.NumberOfQuestionsToGenerate) {
            TopicPanel chosenTopic;

            //If bias is enabled, do the following.
            if (this.MainClass.EnableBias == true) {
                float randomFloat = RNG.nextFloat();
                float offset = 0.0F;

                for (TopicPanel topic: selectedTopics) {
                    Object bias = topic.getTopicData().get("TopicBias");
                    short topicBias = (short) ((bias.getClass().equals(Integer.class)) ? (Integer) bias : (Long) bias);
                    float weightedProbability = (float) topicBias / (float) totalBias;

                    //If the randomly generated float is between a certain range,
                    //(the range being the probability of selecting that topic out of the other selected topics)
                    //Then select that topic, otherwise, increment the range.
                    if (randomFloat > offset && randomFloat < offset + weightedProbability) {
                        chosenTopics.add(topic);
                        counter++;
                        break;
                    } else {
                        offset += weightedProbability % 1;
                    }
                }
            } else {

                //If bias is not true, randomly select them with equal probability.
                int randomIndex = RNG.nextInt(selectedTopics.size());
                chosenTopic = selectedTopics.get(randomIndex);
                chosenTopics.add(chosenTopic);
                counter++;
            }
        }

        //Printing out the number of instances of those topics that were chosen, to prove the bias works.
        if (Debug) {
            HashMap counterMap = new HashMap();
            for (TopicPanel selectedTopic : selectedTopics) {
                counterMap.put(selectedTopic.getTopicData().get("TopicName"), 0);
            }

            for (TopicPanel chosenTopic : chosenTopics) {
                String topicName = (String) chosenTopic.getTopicData().get("TopicName");
                counterMap.put(topicName, (Integer) counterMap.get(topicName) + 1);
            }

            for (Object entry : counterMap.keySet()) {
                System.out.println(entry);
                System.out.println(counterMap.get(entry));
            }
        }

        //Generating each of the chosen topics.
        ArrayList<JSONObject> questionDataObjects = new ArrayList<>();
        for (TopicPanel chosenTopic: chosenTopics) {
            questionDataObjects.add(RandomGenerate(chosenTopic.getTopicData()));
        }

        //Returning the array of data objects.
        return questionDataObjects;
    }

    public void EvaluateQuestions(QuestionBox[] questions) {

        //Iterating through each question and evaluating them.
        for (int i = 0; i < questions.length; i++) {
            questions[i].Evaluate();
        }

        //Saving scores.
        this.MainClass.CurrentDataHandler.SaveScores();
    }
}


