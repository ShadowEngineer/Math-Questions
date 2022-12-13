package System;

import java.io.*;
import java.time.Instant;
import java.util.*;

import org.json.simple.*;

public class DataHandler {
    //Class attributes
    Main MainClass;

    //Constructor
    public DataHandler(Main mainClass) {
        this.MainClass = mainClass;
    }

    //Helper method to generate a string with a specified number of tabs in it.
    private String AddTab(short count) {
        String str = "";
        for (int i = 0; i < count; i++) {
            str += "\t";
        }
        return str;
    }

    //Small method to expand the JSON string created from a JSONObject (usability when editing files).
    private String ExpandJSONString(String JSONString) {
        //Converting the JSONString into a list of characters.
        ArrayList<String> StringCharacters = new ArrayList<>();
        for (String character: JSONString.split("")) { StringCharacters.add(character); }

        //Iterating the arraylist and adding new lines and indents where necessary.
        short indent = 0;
        boolean inString = false;
        for (int i = 0; i < StringCharacters.size(); i++) {
            String currentCharacter = StringCharacters.get(i);

            switch (currentCharacter) {
                case "\"":
                    inString = !inString;
                    break;
                case "{":
                case "[":
                    indent++;
                    StringCharacters.add(i+1, "\n" + AddTab(indent));
                    i++;
                    break;
                case ",":
                    if (inString == false) {
                        StringCharacters.add(i+1, "\n" + AddTab(indent));
                        i++;
                    }
                    break;
                case "}":
                case "]":
                    indent--;
                    StringCharacters.add(i, "\n" + AddTab(indent));
                    i++;
                    break;
            }
        }

        //Concatenating/stitching the string back together.
        String result = "";
        for (String token: StringCharacters) {
            result += token;
        }

        return result;
    }

    //Method to write to the file at the filepath with the JSON data. TAKE CARE: OVERWRITES ALL PREVIOUS DATA.
    private void WriteJSON(String filepath, Object data) {

        //Wrapping all the code below into a try-catch block because file handling is error-prone.
        try {
            //Creating a new file and writer to write information.
            File newFile = new File(filepath);
            FileWriter newFileWriter = new FileWriter(newFile);

            //If the file exists, do this, if not, do that. newFile.createNewFile() returns a boolean.
            if (newFile.createNewFile()) {
                if (data.getClass().equals(JSONArray.class)) {
                    newFileWriter.write(ExpandJSONString(((JSONArray) data).toJSONString()));
                } else if (data.getClass().equals(JSONObject.class)) {
                    newFileWriter.write(ExpandJSONString(((JSONObject) data).toJSONString()));
                }
            } else {
                //Currently this code is the same as above, but leaving the if-statement here in-case it changes.
                if (data.getClass().equals(JSONArray.class)) {
                    newFileWriter.write(ExpandJSONString(((JSONArray) data).toJSONString()));
                } else if (data.getClass().equals(JSONObject.class)) {
                    newFileWriter.write(ExpandJSONString(((JSONObject) data).toJSONString()));
                }
            }

            //Closing the writer so that the changes take effect.
            newFileWriter.close();
        } catch (IOException exception) {
            System.out.println("An exception occurred when writing to JSON file.");
            System.out.println("FILEPATH: " + filepath);
            exception.printStackTrace();
        }
    }

    //Method to save the user data when closing program.
    public void SaveUserSettings() {
        //Creating a new JSON object and referencing the colour arrays.
        JSONObject newJSONObject = new JSONObject();
        int[] primary = this.MainClass.PrimaryColour;
        int[] secondary = this.MainClass.SecondaryColour;

        //Putting all the necessary values into it.
        newJSONObject.put("PrimaryColour", primary[0] + "/" + primary[1] + "/" + primary[2]);
        newJSONObject.put("SecondaryColour", secondary[0] + "/" + secondary[1] + "/" + secondary[2]);
        newJSONObject.put("DisplayQuestionsAsList", this.MainClass.DisplayQuestionsAsList);
        newJSONObject.put("EnableBias", this.MainClass.EnableBias);
        newJSONObject.put("NumberOfQuestionsToGenerate", this.MainClass.NumberOfQuestionsToGenerate);
        newJSONObject.put("DisplayModelAnswers", this.MainClass.DisplayModelAnswers);

        //Writing the JSON back into the file.
        WriteJSON(this.MainClass.FP_UserData, newJSONObject);
    }

    //Method to get the current user data and assign the values to the main class.
    public void GetUserSettings(JSONObject Settings) {
        //Assigning the data.
        this.MainClass.DisplayQuestionsAsList = (boolean) Settings.get("DisplayQuestionsAsList");
        this.MainClass.DisplayModelAnswers = (boolean) Settings.get("DisplayModelAnswers");
        this.MainClass.EnableBias = (boolean) Settings.get("EnableBias");
        this.MainClass.NumberOfQuestionsToGenerate = (short) (long) Settings.get("NumberOfQuestionsToGenerate");

        String[] primary = ((String) Settings.get("PrimaryColour")).split("/");
        String[] secondary = ((String) Settings.get("SecondaryColour")).split("/");
        this.MainClass.PrimaryColour = new int[]{
                Integer.parseInt(primary[0]),
                Integer.parseInt(primary[1]),
                Integer.parseInt(primary[2])
        };
        this.MainClass.SecondaryColour = new int[]{
                Integer.parseInt(secondary[0]),
                Integer.parseInt(secondary[1]),
                Integer.parseInt(secondary[2])
        };
    }

    //Method to create a new historic performance data file, to be called when one doesn't exist.
    //If one somehow DOES exist, this will *OVERWRITE* that file. Caution is advised when calling this method.
    public void CreateNewPerformanceFile(String filepath, JSONArray data) {
        WriteJSON(filepath, data);
    }

    //Method to create a new listing (in JSON format) for performance on a certain topic at a certain time.
    public JSONObject CreateScoreObject(String topicName, int score, int maxScore) {
        JSONObject newJSONObject = new JSONObject();

        newJSONObject.put("TopicName", topicName);
        newJSONObject.put("Score", score);
        newJSONObject.put("MaxScore", maxScore);
        newJSONObject.put("Epoch", Instant.now().getEpochSecond());

        return newJSONObject;
    }

    //Primary method for saving all the scores to do with a set of questions.
    public void SaveScores() {
        //Cloning the previous data in order to not work on the live data. If errors occur, corruption could happen.
        JSONArray HistoricData = (JSONArray) this.MainClass.HistoricPerformanceData.clone();
        JSONArray SpecificationData = (JSONArray) this.MainClass.Specifications.clone();

        //Declaring the new versions of the historic and specification data arrays.
        JSONArray newHistoricData = new JSONArray();
        JSONArray newSpecificationData = new JSONArray();

        //Iterating through all historic data.
        for (Object obj : SpecificationData) {
            //Declaring variables.
            JSONObject historicFileSpecification = new JSONObject();
            JSONObject specFileSpecification = (JSONObject) obj;
            String specName_desired = (String) specFileSpecification.get("SpecificationName");
            HashMap MaxScores = new HashMap();
            HashMap CorrectScores = new HashMap();

            historicFileSpecification.put("SpecificationName", specName_desired);

            //Looping each question and counting up the score the user *could* have achieved and *did* achieve.
            for (QuestionBox question : this.MainClass.CurrentQuestions) {
                String specName_question = (String) question.QuestionData.get("SpecificationName");

                //If the question is a part of the currently desired specification, then tally scores.
                if (specName_question.equals(specName_desired)) {
                    String topicName = (String) question.QuestionData.get("TopicName");
                    Integer maxScore = (Integer) MaxScores.get(topicName);

                    //Inline conditional to increment the score in the hashmap (handles initial non-existence).
                    MaxScores.put(topicName, ((maxScore == null) ? 0 : maxScore) + 1);

                    //If it's correct, then increment the correctness counter too.
                    if (question.isCorrect()) {
                        Integer score = (Integer) CorrectScores.get(topicName);
                        CorrectScores.put(topicName, ((score == null) ? 0 : score) + 1);
                    }
                }
            }

            //Referencing correct historic specification object.
            for (Object specObj : HistoricData) {
                JSONObject specification = (JSONObject) specObj;
                if (((String) specification.get("SpecificationName")).equals(specName_desired)) {
                    historicFileSpecification = specification;
                    break;
                }
            }

            //Appending to the performance data array.
            Object performanceData = historicFileSpecification.get("PerformanceData");
            performanceData = (performanceData == null) ? new JSONArray() : performanceData;

            //Variables referenced from lambda expressions must be final or effectively final.
            JSONArray finalPerformanceData = (JSONArray) performanceData;

            //Appending new score objects.
            MaxScores.forEach((key, value) -> {
                Integer score = (((Integer) CorrectScores.get(key)) == null ? 0 : (Integer) CorrectScores.get(key));
                Integer maxScore = (Integer) value;
                if (this.MainClass.Debug == true ) {System.out.println(key + ": " + score + "/" + maxScore);}
                JSONObject newScoreObject = CreateScoreObject((String) key, score, maxScore);
                finalPerformanceData.add(newScoreObject);
            });
            historicFileSpecification.put("PerformanceData", performanceData);

            //Changing the biases IF biased generation is enabled.
            if (this.MainClass.EnableBias == true) {
                JSONArray newTopicsArray = new JSONArray();
                for (Object topicObj : (JSONArray) specFileSpecification.get("Topics")) {
                    //Getting all the data. Most of this is explicit type conversion using inline conditionals.
                    JSONObject topicData = (JSONObject) topicObj;
                    Object biasO = topicData.get("TopicBias");
                    Integer oldBias = (biasO.getClass().equals(Integer.class)) ? (Integer) biasO : (int) (long) biasO;
                    Integer maxScore = (Integer) MaxScores.get((String) topicData.get("TopicName"));
                    Integer score = (Integer) CorrectScores.get((String) topicData.get("TopicName"));
                    maxScore = (maxScore == null) ? 0 : maxScore;
                    score = (score == null) ? 0 : score;

                    //Calculating and setting new bias. Adding new data to array.
                    Integer newBias = maxScore - score + oldBias;
                    topicData.put("TopicBias", newBias);
                    newTopicsArray.add(topicData);
                }

                //Replacing the previous array with the new, amended array.
                specFileSpecification.put("Topics", newTopicsArray);
            }

            //Adding amended versions of the data to the new empty array.
            newSpecificationData.add(specFileSpecification);
            newHistoricData.add(historicFileSpecification);
        }

        //Outputting JSON data if debug is enabled.
        if (this.MainClass.Debug) {
            System.out.println("Most recently evaluated JSON data:");
            System.out.println(newHistoricData);
            System.out.println(newSpecificationData);
        }

        //Writing to file and referencing the new data inside the main class.
        WriteJSON(this.MainClass.FP_HistoricPerformanceData, newHistoricData);
        WriteJSON(this.MainClass.FP_Specifications, newSpecificationData);
        this.MainClass.HistoricPerformanceData = newHistoricData;
        this.MainClass.Specifications = newSpecificationData;
    }
}

/*
"userdata":
{
    "PrimaryColour": String rgb code
    "SecondaryColour": String rgb code
    "DisplayQuestionsAsList": boolean
    "EnableBias:" boolean
    “QuestionsPerRound”: integer
}

"historic":
[
    {
        "SpecificationName": String,
        "PerformanceData":
        [
            {
                "TopicName": String,
                "Score": integer,
                "MaxScore": integer,
                "Date_D": integer,
                "Date_M": integer,
                "Date_Y": integer
            }
        ]
    }
]
*/