
import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.lang.StringBuilder;

public class Main {
  public static void main(String args[]){
    ArrayList<DataEntry> allData;
    ArrayList<DataEntry> acceptedData = new ArrayList<DataEntry>();
    ArrayList<String> dependent = new ArrayList<String>();
    String dependentType;
    ArrayList<String> independent = new ArrayList<String>();
    String independentType = "";
    String userInput = "";
    
    Scanner input = new Scanner(System.in);
    
    allData = readNewData();
    for(DataEntry entry : allData){
      if(entry.getStatus()){
        acceptedData.add(entry);
      }
    }
    
    userInput = "";
    
    if(dependentType.equals("Tags")){
      independentType = "School";
    }else if(dependentType.equals("AcceptedStatus")){
      independentType = "School";
    }
    
    System.out.println("Please enter the Schools you would like to filter by. Type \"exit\" when done.");
    while(!userInput.equals("exit")){
      userInput = input.nextLine();
      if(!userInput.equals("exit")){
        independent.add(userInput);
      }
    }
    
    System.out.println(analysis(independent, independentType, dependent, dependentType, allData, true));
  }
  
  private static ArrayList<DataEntry> readNewData(){
    Scanner input1 = null;
    Scanner input2 = null;
    File uniFile;
    File collegeFile;
    String row;
    boolean status = false;
    String school;
    String programName;
    String programCode;
    ArrayList<String> tags = new ArrayList<String>();    
    ArrayList<DataEntry> allData = new ArrayList<DataEntry>();
    ArrayList<DataEntry> acceptedData = new ArrayList<DataEntry>();
    FileChooser fileChoose = new FileChooser();
    Tagger tagger = new Tagger();
    
    //testing
    tags.add("Engineering");
    tags.add("Math");
    
    try{ //load files and create scanners, return error message if there is an error
      uniFile = fileChoose.getFile("University");
      collegeFile = fileChoose.getFile("College");
      input1 = new Scanner(collegeFile);
      input2 = new Scanner(uniFile);
    }catch(FileNotFoundException e){
      System.out.println("File not found");
    }
    
    //reading college file
    input1.nextLine(); //skip header row;
    while(input1.hasNext()){
      row = input1.nextLine();
      ArrayList<String> rowList = new ArrayList<>();

      //Iterates through the row and makes sure we're only splitting on commas not part of quotes
      boolean inElement = false;
      int startIndex = 0;
      for (int i = 0; i < row.length(); i++) {
          char c = row.charAt(i);
          //Split on this comma
          if (!inElement && c == ',') {
              rowList.add(row.substring(startIndex, i));
              startIndex = i + 1;
          //Quotes start here, ignore everything until the next quote
          } else if (!inElement && c == '"') {
              inElement = true;
          //Quotes end here, we can start reading commas again
          } else if (inElement && c == '"') {
              inElement = false;
          }
      }

      status = rowList.get(0).equals("A");
      school = rowList.get(1);
      programCode = rowList.get(2);
      programName = rowList.get(3);
      //tags = tagger.getTags(programCode, programName);
      
      allData.add(new DataEntry(status, school, programName, programCode, new ArrayList<String>())); 
    }
    
    //Reading universities file
    input2.nextLine(); //skip header row
    while(input2.hasNext()){
      row = input2.nextLine();
        ArrayList<String> rowList = new ArrayList<>();

        //Iterates through the row and makes sure we're only splitting on commas not part of quotes
        boolean inElement = false;
        int startIndex = 0;
        for (int i = 0; i < row.length(); i++) {
            char c = row.charAt(i);
            //Split on this comma
            if (!inElement && c == ',') {
                rowList.add(row.substring(startIndex, i));
                startIndex = i + 1;
                //Quotes start here, ignore everything until the next quote
            } else if (!inElement && c == '"') {
                inElement = true;
                //Quotes end here, we can start reading commas again
            } else if (inElement && c == '"') {
                inElement = false;
            }
        }

        school = rowList.get(0);
        programName = rowList.get(1);
        status = rowList.get(3).length() > 0;;
        programCode = "";

      //tags = tagger.getTags(programCode, programName); //add tags
      
      allData.add(new DataEntry(status, school, programName, programCode, tags)); 
    }  
    input1.close();
    input2.close();
    writeToFile(acceptedData); //write data to CSV file
    return allData;
  }
  
  public static ArrayList<Integer> analysis(ArrayList<String> independent, ArrayList<String> dependent, ArrayList<DataEntry> data){
    ArrayList<Integer> count = new ArrayList<Integer>(); 
    ArrayList<DataEntry> matchData = new ArrayList<DataEntry>();

    for(int p = 0; p < independent.size(); p ++){
      count.add(0);
    }
    
    for(int i = 0; i < data.size(); i ++){
      for(int j = 0; j < independent.size(); j ++){ //add data that fits the independent variable to new arraylist
        if(data.get(i).getCombinedTraits().indexOf(independent.get(j)) != -1){
          matchData.add(data.get(i));
        }
      }
    }
    
    for(int a = 0; a < matchData.size(); a ++){
      for(int b = 0; b < dependent.size(); b ++){ //compare data that matches independent variable to dependent variable
        if(matchData.get(a).getCombinedTraits().indexOf(dependent.get(b)) != -1){
          for(int c = 0; c < independent.size(); c ++){
            if(matchData.get(a).getCombinedTraits().indexOf(independent.get(c)) != -1){ //add to count in corresponding arraylist index
              count.set(c, count.get(c) + 1);
            }
          }
        }
      }
    }
    return count;
  }
  
  
  public static void writeToFile(ArrayList<DataEntry> data){
    File storage = null; 
    PrintWriter output = null;
    try{ //load files and create PrintWriter
      storage = new File("res/storage.csv");
      output = new PrintWriter(storage);
    }catch(FileNotFoundException e){
      System.out.println("File not found");
    }
    StringBuilder outputString = new StringBuilder();
    output.println("Accepted Status,School,Program Name,Program Code,Tags"); //header
    
    for(int i = 0; i < data.size(); i ++){
      outputString.setLength(0);
      outputString.append(data.get(i).getStatus() + ","); //adding status to StringBuilder, add quotes if there are commas
      if(data.get(i).getSchool().contains(",")){
        outputString.append("\"" + data.get(i).getSchool() + "\"" + ",");
      }else{
        outputString.append(data.get(i).getSchool() + ",");
      }
      
      if(data.get(i).getProgramName().contains(",")){ //adding program name to StringBuilder, add quotes if there are commas
        outputString.append("\"" + data.get(i).getProgramName() + "\"" + ",");
      }else{
        outputString.append(data.get(i).getProgramName() + ",");
      }
      
      if(data.get(i).getProgramCode().contains(",")){ //adding program code, add quotes if there are commas
        outputString.append("\"" + data.get(i).getProgramCode() + "\"" + ",");
      }else{
        outputString.append(data.get(i).getProgramCode() + ",");
      }
      
      if(data.get(i).getTags().size() > 1){ //adding tags to StringBuilder if there are more than one
        outputString.append("\"");
        for(int j = 0; j < data.get(i).getTags().size(); j ++){
          if(j != data.get(i).getTags().size() - 1){ //adds commas after every tag except the last
            outputString.append(data.get(i).getTags().get(j) + ",");
          }else{
            outputString.append(data.get(i).getTags().get(j));
          }
        }
        outputString.append("\"");
      }else if(data.get(i).getTags().size() == 1){ //add tag to StringBuilder if there is a single one
        outputString.append(data.get(i).getTags().get(0));
      }
      output.println(outputString);
    }
    output.close(); //close PrintWriter
  }
  
  public ArrayList<Integer> compareSchools (ArrayList<DataEntry> data, ArrayList<String> schools){
    ArrayList<Integer> schoolCount = new ArrayList<Integer>();
    
    for(int i = 0; i < schools.size(); i ++){
      schoolCount.add(0);
    }
    
    for(int j = 0; j < schools.size(); j ++){
      for(int k = 0; k < data.size(); k ++){
        if(data.get(k).getSchool().equals(schools.get(j))){
          schoolCount.set(j, schoolCount.get(j) + 1);
        }
      }
    }
    return schoolCount;
  }
  
  public ArrayList<String> getAllSchools(ArrayList<DataEntry> data){ //returns ArrayList of unique schools
    ArrayList<String> schools = new ArrayList<String>();
    for(int i = 0; i < data.size(); i ++){
      if(schools.indexOf(data.get(i).getSchool()) == -1){
        schools.add(data.get(i).getSchool()); //add if school isn't in the schools ArrayList
      }
    }
    return schools;
  }
  
  public ArrayList<Integer> compareTags (ArrayList<DataEntry> data, ArrayList<String> tags){ 
    ArrayList<Integer> tagCount = new ArrayList<Integer>();
    
    for(int i = 0; i < tags.size(); i ++){ //initialize an ArrayList with the same length as tags
      tagCount.add(0);
    }
    
    for(int j = 0; j < tags.size(); j ++){
      for(int k = 0; k < data.size(); k ++){
        for(int m = 0; m < data.get(k).getTags().size(); k ++){
          if(data.get(k).getTags().get(k).equals(tags.get(j))){
            tagCount.set(j, tagCount.get(j) + 1); //add to the corresponding ArrayList entry if there is an instance
          }
        }
      }
    }
    return tagCount;
  }
  
  public ArrayList<String> getAllTags(ArrayList<DataEntry> data){ //returns ArrayList of unique tags
    ArrayList<String> tags = new ArrayList<String>();
    for(int i = 0; i < data.size(); i ++){ //loop through data and each DataEntry's tags
      for(int j = 0; j < data.get(i).getTags().size(); j ++){
        if(tags.indexOf(data.get(i).getTags().get(j)) == -1){
          tags.add(data.get(i).getTags().get(j)); //add if specific tag isn't in the tags ArrayList
        }
      }
    }
    return tags;
  }
}

