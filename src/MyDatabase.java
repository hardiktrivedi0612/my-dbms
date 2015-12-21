
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hardik
 */
public class MyDatabase {

    private static String fileName = "";
    private static String currentDirectory = System.getProperty("user.dir");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Hello! Welcome to MyDatabase.");
        System.out.println("Please select any one from the following options:");
        System.out.println("1. Import");
        System.out.println("2. Query");
        System.out.println("3. Insert");
        System.out.println("4. Delete");
        System.out.println("5. Exit");
        System.out.println("Please enter either 1/2/3/4/5");
        try {
            String userInput = userInputReader.readLine();
            while (!userInput.equals("5")) {
                switch (userInput) {
                    case "1":
                        System.out.println("You have selected Import. Please enter the import command");
                        System.out.println("Format is:");
                        System.out.println("IMPORT<space>file_name.csv;");
                        handleImportCommand(userInputReader.readLine());
                        break;
                    case "2":
                        System.out.println("You have selected Query. Please enter the query to be executed");
                        System.out.println("Format is:");
                        System.out.println("SELECT<space>[comma_separated_field_list|*]<space>FROM<space>TABLE<space>WHERE<space>field_name[<space>NOT]<space>[=|>|≥|<|≤]<space>value;");
                        handleQueryCommand(userInputReader.readLine());
                        break;
                    case "3":
                        System.out.println("You have selected Insert. Please enter the insert command");
                        System.out.println("Format is:");
                        System.out.println("INSERT<space>INTO<space>TABLE<space>VALUES<space>(values_list);");
                        handleInsertCommand(userInputReader.readLine());
                        break;
                    case "4":
                        System.out.println("You have selected Delete. Please enter the delete command");
                        System.out.println("Format is:");
                        System.out.println("DELETE<space>FROM<space>TABLE<space>WHERE<space>field_name[<space>NOT]<space>[=|>|≥|<|≤]<space>value;");
                        handleDeleteCommand(userInputReader.readLine());
                        break;
                    default:
                        System.out.println("Incorrect input. Please try again.");
                        break;
                }
//                Thread.sleep(500);
                System.out.println("");
                System.out.println("Please select any one from the following options:");
                System.out.println("1. Import");
                System.out.println("2. Query");
                System.out.println("3. Insert");
                System.out.println("4. Delete");
                System.out.println("5. Exit");
                System.out.println("Please enter either 1/2/3/4/5");
                userInput = userInputReader.readLine();
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void handleImportCommand(String importCommand) {
        if (importCommand.charAt(importCommand.length() - 1) == ';') {
            importCommand = importCommand.substring(0, importCommand.length() - 1);
        }
        if (!importCommand.matches(Constants.importRegexString)) {
            System.out.println("Error parsing the import command. Please check the syntax and try again");
            return;
        }
        String keywords[] = importCommand.split(" ");
        if (keywords.length == 0) {
            System.out.println("Please enter something");
            return;
        }

        if (keywords.length == 1) {
            System.out.println("Incomplete command. Please try again.");
            return;
        }

        if (!keywords[0].equalsIgnoreCase(Constants.importKeyword)) {
            System.out.println("Import keyword missing. Please try again");
            return;
        }

        if (keywords[0].equalsIgnoreCase(Constants.importKeyword)) {
            if (!keywords[1].contains("csv")) {
                System.out.println("Please give a cvs file with .csv mentioned in the name");
                return;
            }

            File file = new File(currentDirectory + "/" + keywords[1]);
            try {
                if (!file.exists()) {
                    System.out.println("The file mentioned does not exist. Please check the file name and try again.");
                    return;
                }
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                ArrayList<TableRecord> list = new ArrayList<TableRecord>();
                String s = bufferedReader.readLine();
                int offset = 0;

                //Creating index lists
                IndexList idIndexList = new IndexList(Data_Types.INT);
                IndexList companyIndexList = new IndexList(Data_Types.STRING);
                IndexList drugIdIndexList = new IndexList(Data_Types.STRING);
                IndexList trialsIndexList = new IndexList(Data_Types.INT);
                IndexList patientsIndexList = new IndexList(Data_Types.INT);
                IndexList dosageMgIndexList = new IndexList(Data_Types.INT);
                IndexList readingIndexList = new IndexList(Data_Types.FLOAT);
                IndexList doubleBlindIndexList = new IndexList(Data_Types.BOOLEAN);
                IndexList controlledStudyIndexList = new IndexList(Data_Types.BOOLEAN);
                IndexList govtFundedIndexList = new IndexList(Data_Types.BOOLEAN);
                IndexList fdaApprovedIndexList = new IndexList(Data_Types.BOOLEAN);

                if (s == null) {
                    System.out.println("The file seems to be empty. Please try again.");
                } else {
                    //Reading first record
                    s = bufferedReader.readLine();
                    fileName = keywords[1].replace(".csv", "");
                    File databaseFile = new File(currentDirectory + "/" + fileName + ".db");
                    RandomAccessFile dbRandomAccessFile = new RandomAccessFile(databaseFile, "rw");
                    while (s != null) {
                        String data[] = s.split(Constants.regexString);
                        //Writing the data onto the file
                        TableRecord record = new TableRecord(Integer.parseInt(data[0]), data[1].replaceAll("\"", "").length(), data[1].replaceAll("\"", ""), data[2],
                                Integer.parseInt(data[3]), Integer.parseInt(data[4]), Integer.parseInt(data[5]), Float.parseFloat(data[6]),
                                false, Boolean.parseBoolean(data[7]), Boolean.parseBoolean(data[8]), Boolean.parseBoolean(data[9]), Boolean.parseBoolean(data[10]));
                        record.writeToFile(dbRandomAccessFile);
                        list.add(record);
                        idIndexList.insert(data[0], offset);
                        companyIndexList.insert(data[1].replaceAll("\"", ""), offset);
                        drugIdIndexList.insert(data[2], offset);
                        trialsIndexList.insert(data[3], offset);
                        patientsIndexList.insert(data[4], offset);
                        dosageMgIndexList.insert(data[5], offset);
                        readingIndexList.insert(data[6], offset);
                        doubleBlindIndexList.insert(data[7], offset);
                        controlledStudyIndexList.insert(data[8], offset);
                        govtFundedIndexList.insert(data[9], offset);
                        fdaApprovedIndexList.insert(data[10], offset);

                        offset += record.getLength();
                        s = bufferedReader.readLine();
                    }

                    //Creating index files for all columns
                    RandomAccessFile idIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.id + ".ndx"), "rw");
                    idIndexList.writeToFile(idIndexFile);
//                    idIndexList.printFile();
//                    IndexList idIndexes = new IndexList();
//                    idIndexes.readFromFile(idIndexFile);
//                    System.out.println(idIndexes.list.size());
                    idIndexFile.close();

                    RandomAccessFile companyIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.company + ".ndx"), "rw");
                    companyIndexList.writeToFile(companyIndexFile);
//                    companyIndexList.printFile();
                    companyIndexFile.close();

                    RandomAccessFile drugIdIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.drugId + ".ndx"), "rw");
                    drugIdIndexList.writeToFile(drugIdIndexFile);
//                    drugIdIndexList.printFile();
                    drugIdIndexFile.close();

                    RandomAccessFile trialsIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.trials + ".ndx"), "rw");
                    trialsIndexList.writeToFile(trialsIndexFile);
//                    trialsIndexList.printFile();
                    trialsIndexFile.close();

                    RandomAccessFile patientsIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.patients + ".ndx"), "rw");
                    patientsIndexList.writeToFile(patientsIndexFile);
//                    patientsIndexList.printFile();
                    patientsIndexFile.close();

                    RandomAccessFile dosageMgIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.dosageMg + ".ndx"), "rw");
                    dosageMgIndexList.writeToFile(dosageMgIndexFile);
//                    dosageMgIndexList.printFile();
                    dosageMgIndexFile.close();

                    RandomAccessFile readingIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.reading + ".ndx"), "rw");
                    readingIndexList.writeToFile(readingIndexFile);
//                    readingIndexList.printFile();
                    readingIndexFile.close();

                    RandomAccessFile doubleBlindIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.doubleBlind + ".ndx"), "rw");
                    doubleBlindIndexList.writeToFile(doubleBlindIndexFile);
//                    doubleBlindIndexList.printFile();
                    doubleBlindIndexFile.close();

                    RandomAccessFile controlledStudyIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.controlledStudy + ".ndx"), "rw");
                    controlledStudyIndexList.writeToFile(controlledStudyIndexFile);
//                    controlledStudyIndexList.printFile();
                    controlledStudyIndexFile.close();

                    RandomAccessFile govtFundedIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.govtFunded + ".ndx"), "rw");
                    govtFundedIndexList.writeToFile(govtFundedIndexFile);
//                    govtFundedIndexList.printFile();
                    govtFundedIndexFile.close();

                    RandomAccessFile fdaApprovedIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.fdaApproved + ".ndx"), "rw");
                    fdaApprovedIndexList.writeToFile(fdaApprovedIndexFile);
//                    fdaApprovedIndexList.printFile();
                    fdaApprovedIndexFile.close();

                    dbRandomAccessFile.close();
                }
                fileReader.close();
                bufferedReader.close();
                System.out.println("Import Success!!!!");

            } catch (FileNotFoundException ex) {
//                Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex);
            } catch (IOException ex) {
//                Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex);
            }
        }
    }

    private static void handleQueryCommand(String queryCommand) {

        if (queryCommand.length() < 4) {
            System.out.println("Incomplete select statement. Please try again.");
            return;
        }

        if (queryCommand.charAt(queryCommand.length() - 1) == ';') {
            queryCommand = queryCommand.substring(0, queryCommand.length() - 1);
        }

        if (!queryCommand.matches(Constants.selectRegexString)) {
            System.out.println("Error parsing the select query. Please check the syntax and try again.");
            return;
        }

        ArrayList<String> keywords = new ArrayList<>(Arrays.asList(queryCommand.split(" ")));
        if (keywords.isEmpty()) {
            System.out.println("Please enter something");
            return;
        }

        if (keywords.size() == 1) {
            System.out.println("Incomplete command. Please try again.");
            return;
        }

        if (!keywords.get(0).equalsIgnoreCase(Constants.selectKeyword)) {
            System.out.println("'Select' keyword missing. Please try again");
            return;
        }

        if (fileName == null || fileName.equalsIgnoreCase("")) {
            try {
                File file = new File(currentDirectory + "/" + keywords.get(3) + ".db");
                if (file.exists()) {
                    fileName = keywords.get(3);
                } else {
                    System.out.println("Database table might not have been initialized. Please use import command first and then try again.");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Database table might not have been initialized. Please use import command first and then try again.");
                return;
            }
            fileName = keywords.get(3);
        }

        if (!keywords.get(2).equalsIgnoreCase(Constants.fromKeyword)) {
            System.out.println("'From' keyword is missing. Please try again");
            return;
        }

        if (!keywords.get(3).equalsIgnoreCase(fileName)) {
            System.out.println("Table name not identified. Please try again.");
            return;
        }

        try {
            if (keywords.size() > 4) {
                //Where condition is present

                if (!Constants.columnNames.contains(keywords.get(5))) {
                    System.out.println("Column '" + keywords.get(5) + "' does not exist. Please try again.");
                    return;
                }

                if (keywords.get(1).equalsIgnoreCase("*")) {
                    //show all columns
                    String whereColumnName = keywords.get(5);
                    ArrayList<Integer> rowsToShow = new ArrayList<>();
                    String operator = null;
                    IndexList columnList = getColumnIndexList(whereColumnName);
                    Data_Types dataType = getDataType(keywords.get(5));
                    boolean hasNot = false;
                    if (keywords.contains("NOT") || keywords.contains("not") || keywords.contains("Not")) {
                        //Contains NOT keyword
                        hasNot = true;
                        operator = keywords.get(7);
                        if (operator.equalsIgnoreCase("=")) {
                            //Not equal to
                            operator = "!=";
                        } else if (operator.equalsIgnoreCase(">")) {
                            //lesser than equal to
                            operator = "<=";
                        } else if (operator.equalsIgnoreCase("<")) {
                            //greater than equal to
                            operator = ">=";
                        } else if (operator.equalsIgnoreCase(">=")) {
                            //lesser than
                            operator = "<";
                        } else if (operator.equalsIgnoreCase("<=")) {
                            //greater than
                            operator = ">";
                        } else {
                            System.out.println("Operator not recognised. Please try again");
                            return;
                        }
                    } else {
                        operator = keywords.get(6);
                    }

                    if (dataType == Data_Types.INT) {
                        int data;
                        try {
                            if (hasNot) {
                                data = Integer.parseInt(keywords.get(8));
                            } else {
                                data = Integer.parseInt(keywords.get(7));
                            }
                        } catch (Exception e) {
                            System.out.println("Please enter integer data only. Please try again.");
                            return;
                        }

                        for (Entry entry : columnList.list) {
                            if (operator.equalsIgnoreCase("=")) {
                                if (Integer.parseInt(entry.getKey()) == data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("!=")) {
                                if (Integer.parseInt(entry.getKey()) != data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">")) {
                                if (Integer.parseInt(entry.getKey()) > data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<")) {
                                if (Integer.parseInt(entry.getKey()) < data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">=")) {
                                if (Integer.parseInt(entry.getKey()) >= data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<=")) {
                                if (Integer.parseInt(entry.getKey()) <= data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else {
                                System.out.println("Operator not found. Please try again.");
                                return;
                            }
                        }
                    } else if (dataType == Data_Types.FLOAT) {
                        float data;
                        try {
                            if (hasNot) {
                                data = Float.parseFloat(keywords.get(8));
                            } else {
                                data = Float.parseFloat(keywords.get(7));
                            }
                        } catch (Exception e) {
                            System.out.println("Please enter float data only. Please try again.");
                            return;
                        }

                        for (Entry entry : columnList.list) {
                            if (operator.equalsIgnoreCase("=")) {
                                if (Float.parseFloat(entry.getKey()) == data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("!=")) {
                                if (Float.parseFloat(entry.getKey()) != data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">")) {
                                if (Float.parseFloat(entry.getKey()) > data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<")) {
                                if (Float.parseFloat(entry.getKey()) < data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">=")) {
                                if (Float.parseFloat(entry.getKey()) >= data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<=")) {
                                if (Float.parseFloat(entry.getKey()) <= data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else {
                                System.out.println("Operator not found. Please try again.");
                                return;
                            }
                        }
                    } else if (dataType == Data_Types.STRING) {
                        String data;
                        try {
                            if (hasNot) {
                                data = keywords.get(8);
                            } else {
                                data = keywords.get(7);
                            }
                        } catch (Exception e) {
                            System.out.println("Please enter string data only. Please try again.");
                            return;
                        }
                        int index = queryCommand.indexOf(data);
                        data = queryCommand.substring(index, queryCommand.length());

                        if (data.startsWith("'") || data.startsWith("\"")) {
                            if (data.endsWith("'") || data.endsWith("\"")) {
                                data = data.substring(1, data.length() - 1);
                            } else {
                                System.out.println("Value starts with \' or \" but does not end with it. Please try again.");
                                return;
                            }
                        }

//                        System.out.println("Data = " + data);
                        for (Entry entry : columnList.list) {
                            if (operator.equalsIgnoreCase("=")) {
                                if ((entry.getKey()).equalsIgnoreCase(data)) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("!=")) {
                                if (!entry.getKey().equalsIgnoreCase(data)) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">")) {
                                if (entry.getKey().compareToIgnoreCase(data) > 0) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<")) {
                                if (entry.getKey().compareToIgnoreCase(data) < 0) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">=")) {
                                if (entry.getKey().compareToIgnoreCase(data) > 0 || entry.getKey().compareToIgnoreCase(data) == 0) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<=")) {
                                if (entry.getKey().compareToIgnoreCase(data) < 0 || entry.getKey().compareToIgnoreCase(data) == 0) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else {
                                System.out.println("Operator not found. Please try again.");
                                return;
                            }
                        }
                    } else if (dataType == Data_Types.BOOLEAN) {
                        if (!operator.equals("=") && !operator.equals("!=")) {
                            System.out.println("greater than or lesser than not allowed for boolean data types. Please try again.");
                            return;
                        }

                        boolean data;
                        try {
                            if (hasNot) {
                                data = Boolean.parseBoolean(keywords.get(8));
                            } else {
                                data = Boolean.parseBoolean(keywords.get(7));
                            }
                        } catch (Exception e) {
                            System.out.println("Error parsing data. Please enter boolean data only. Please try again.");
                            return;
                        }

                        for (Entry entry : columnList.list) {
                            if (operator.equalsIgnoreCase("=")) {
                                if (Boolean.parseBoolean(entry.getKey()) == data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("!=")) {
                                if (Boolean.parseBoolean(entry.getKey()) != data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else {
                                System.out.println("Operator not found. Please try again.");
                                return;
                            }
                        }
                    }

//                    System.out.println("Count = " + rowsToShow.size());
                    RandomAccessFile raf = new RandomAccessFile(new File(currentDirectory + "/" + fileName + ".db"), "r");
                    ArrayList<TableRecord> records = new ArrayList<>();
                    for (Integer seekPosition : rowsToShow) {
                        records.add(new TableRecord(raf, seekPosition));
                    }
                    int noOfRecs = 0;
                    for (TableRecord record : records) {
                        if (!record.isDeleted()) {
                            noOfRecs++;
                        }
                    }

                    if (noOfRecs == 0) {
                        System.out.println("(No records found)");
                        return;
                    }
                    for (String columnName : Constants.columnNames) {
                        System.out.print(columnName + "\t");
                    }
                    System.out.println("");
                    for (TableRecord record : records) {
                        if (!record.isDeleted()) {
                            System.out.print(record.getId() + "\t");
                            System.out.print(record.getCompany() + "\t");
                            System.out.print(record.getDrugId() + "\t");
                            System.out.print(record.getTrials() + "\t");
                            System.out.print(record.getPatients() + "\t");
                            System.out.print(record.getDosageMg() + "\t");
                            System.out.print(record.getReading() + "\t");
                            System.out.print(record.isDoubleBlind() + "\t");
                            System.out.print(record.isControlledStudy() + "\t");
                            System.out.print(record.isGovtFunded() + "\t");
                            System.out.print(record.isFdaApproved() + "\t");
                            System.out.println("");
                        }

                    }
                    System.out.println("Fetching success!!");
                    raf.close();
                } else {
                    //which columns to show is given
                    String whereColumnName = keywords.get(5);
                    ArrayList<Integer> rowsToShow = new ArrayList<>();
                    String operator = null;
                    IndexList columnList = getColumnIndexList(whereColumnName);
                    Data_Types dataType = getDataType(keywords.get(5));
                    boolean hasNot = false;
                    if (keywords.contains("NOT") || keywords.contains("not") || keywords.contains("Not")) {
                        //Contains NOT keyword
                        hasNot = true;
                        operator = keywords.get(7);
                        if (operator.equalsIgnoreCase("=")) {
                            //Not equal to
                            operator = "!=";
                        } else if (operator.equalsIgnoreCase(">")) {
                            //lesser than equal to
                            operator = "<=";
                        } else if (operator.equalsIgnoreCase("<")) {
                            //greater than equal to
                            operator = ">=";
                        } else if (operator.equalsIgnoreCase(">=")) {
                            //lesser than
                            operator = "<";
                        } else if (operator.equalsIgnoreCase("<=")) {
                            //greater than
                            operator = ">";
                        } else {
                            System.out.println("Operator not recognised. Please try again");
                            return;
                        }
                    } else {
                        operator = keywords.get(6);
                    }

                    if (dataType == Data_Types.INT) {
                        int data;
                        try {
                            if (hasNot) {
                                data = Integer.parseInt(keywords.get(8));
                            } else {
                                data = Integer.parseInt(keywords.get(7));
                            }
                        } catch (Exception e) {
                            System.out.println("Please enter integer data only. Please try again.");
                            return;
                        }

                        for (Entry entry : columnList.list) {
                            if (operator.equalsIgnoreCase("=")) {
                                if (Integer.parseInt(entry.getKey()) == data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("!=")) {
                                if (Integer.parseInt(entry.getKey()) != data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">")) {
                                if (Integer.parseInt(entry.getKey()) > data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<")) {
                                if (Integer.parseInt(entry.getKey()) < data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">=")) {
                                if (Integer.parseInt(entry.getKey()) >= data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<=")) {
                                if (Integer.parseInt(entry.getKey()) <= data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else {
                                System.out.println("Operator not found. Please try again.");
                                return;
                            }
                        }
                    } else if (dataType == Data_Types.FLOAT) {
                        float data;
                        try {
                            if (hasNot) {
                                data = Float.parseFloat(keywords.get(8));
                            } else {
                                data = Float.parseFloat(keywords.get(7));
                            }
                        } catch (Exception e) {
                            System.out.println("Please enter float data only. Please try again.");
                            return;
                        }

                        for (Entry entry : columnList.list) {
                            if (operator.equalsIgnoreCase("=")) {
                                if (Float.parseFloat(entry.getKey()) == data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("!=")) {
                                if (Float.parseFloat(entry.getKey()) != data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">")) {
                                if (Float.parseFloat(entry.getKey()) > data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<")) {
                                if (Float.parseFloat(entry.getKey()) < data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">=")) {
                                if (Float.parseFloat(entry.getKey()) >= data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<=")) {
                                if (Float.parseFloat(entry.getKey()) <= data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else {
                                System.out.println("Operator not found. Please try again.");
                                return;
                            }
                        }
                    } else if (dataType == Data_Types.STRING) {
                        String data;
                        try {
                            if (hasNot) {
                                data = keywords.get(8);
                            } else {
                                data = keywords.get(7);
                            }
                        } catch (Exception e) {
                            System.out.println("Please enter string data only. Please try again.");
                            return;
                        }
                        int index = queryCommand.indexOf(data);
                        data = queryCommand.substring(index, queryCommand.length());

                        if (data.startsWith("'") || data.startsWith("\"")) {
                            if (data.endsWith("'") || data.endsWith("\"")) {
                                data = data.substring(1, data.length() - 1);
                            } else {
                                System.out.println("Value starts with \' or \" but does not end with it. Please try again.");
                                return;
                            }
                        }

//                        System.out.println("Data = " + data);
                        for (Entry entry : columnList.list) {
                            if (operator.equalsIgnoreCase("=")) {
                                if ((entry.getKey()).equalsIgnoreCase(data)) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("!=")) {
                                if (!entry.getKey().equalsIgnoreCase(data)) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">")) {
                                if (entry.getKey().compareToIgnoreCase(data) > 0) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<")) {
                                if (entry.getKey().compareToIgnoreCase(data) < 0) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals(">=")) {
                                if (entry.getKey().compareToIgnoreCase(data) > 0 || entry.getKey().compareToIgnoreCase(data) == 0) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("<=")) {
                                if (entry.getKey().compareToIgnoreCase(data) < 0 || entry.getKey().compareToIgnoreCase(data) == 0) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else {
                                System.out.println("Operator not found. Please try again.");
                                return;
                            }
                        }
                    } else if (dataType == Data_Types.BOOLEAN) {
                        if (!operator.equals("=") && !operator.equals("!=")) {
                            System.out.println("greater than or lesser than not allowed for boolean data types. Please try again.");
                            return;
                        }

                        boolean data;
                        try {
                            if (hasNot) {
                                data = Boolean.parseBoolean(keywords.get(8));
                            } else {
                                data = Boolean.parseBoolean(keywords.get(7));
                            }
                        } catch (Exception e) {
                            System.out.println("Error parsing data. Please enter boolean data only. Please try again.");
                            return;
                        }

                        for (Entry entry : columnList.list) {
                            if (operator.equalsIgnoreCase("=")) {
                                if (Boolean.parseBoolean(entry.getKey()) == data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else if (operator.equals("!=")) {
                                if (Boolean.parseBoolean(entry.getKey()) != data) {
                                    ArrayList<Integer> list = columnList.get(entry.getKey());
                                    for (Integer position : list) {
                                        rowsToShow.add(position);
                                    }
                                }
                            } else {
                                System.out.println("Operator not found. Please try again.");
                                return;
                            }
                        }
                    }

//                    System.out.println("Count = " + rowsToShow.size());
                    RandomAccessFile raf = new RandomAccessFile(new File(currentDirectory + "/" + fileName + ".db"), "r");
                    ArrayList<TableRecord> records = new ArrayList<>();
                    for (Integer seekPosition : rowsToShow) {
                        records.add(new TableRecord(raf, seekPosition));
                    }

                    ArrayList<String> columnNames = new ArrayList<>(Arrays.asList(keywords.get(1).split(",")));

                    for (String columnName : columnNames) {
                        if (!Constants.columnNames.contains(columnName)) {
                            System.out.println("Column name '" + columnName + "' not found. Please try again");
                            return;
                        }
                    }

                    int noOfRecs = 0;
                    for (TableRecord record : records) {
                        if (!record.isDeleted()) {
                            noOfRecs++;
                        }
                    }

                    if (noOfRecs == 0) {
                        System.out.println("(No records found)");
                        return;
                    }

                    for (String columnName : Constants.columnNames) {
                        if (columnNames.contains(columnName)) {
                            System.out.print(columnName + "\t");
                        }
                    }

                    System.out.println("");

                    for (TableRecord record : records) {
                        if (!record.isDeleted()) {
                            if (columnNames.contains(Constants.id)) {
                                System.out.print(record.getId() + "\t");
                            }
                            if (columnNames.contains(Constants.company)) {
                                System.out.print(record.getCompany() + "\t");
                            }
                            if (columnNames.contains(Constants.drugId)) {
                                System.out.print(record.getDrugId() + "\t");
                            }
                            if (columnNames.contains(Constants.trials)) {
                                System.out.print(record.getTrials() + "\t");
                            }
                            if (columnNames.contains(Constants.patients)) {
                                System.out.print(record.getPatients() + "\t");
                            }
                            if (columnNames.contains(Constants.dosageMg)) {
                                System.out.print(record.getDosageMg() + "\t");
                            }
                            if (columnNames.contains(Constants.reading)) {
                                System.out.print(record.getReading() + "\t");
                            }
                            if (columnNames.contains(Constants.doubleBlind)) {
                                System.out.print(record.isDoubleBlind() + "\t");
                            }
                            if (columnNames.contains(Constants.controlledStudy)) {
                                System.out.print(record.isControlledStudy() + "\t");
                            }
                            if (columnNames.contains(Constants.govtFunded)) {
                                System.out.print(record.isGovtFunded() + "\t");
                            }
                            if (columnNames.contains(Constants.fdaApproved)) {
                                System.out.print(record.isFdaApproved() + "\t");
                            }
                            System.out.println("");
                        }
                    }
                    System.out.println("Fetching success!!");
                    raf.close();
                }
            } else {
                //Where condition is not present
                if (keywords.get(1).equalsIgnoreCase("*")) {
                    //display all columns
                    RandomAccessFile raf = new RandomAccessFile(new File(currentDirectory + "/" + fileName + ".db"), "r");
                    ArrayList<TableRecord> records = new ArrayList<>();
                    int seekPosition = 0;
                    try {
                        while (true) {
                            TableRecord record = new TableRecord(raf, seekPosition);
                            records.add(record);
                            seekPosition += record.getLength();
                        }
                    } catch (EOFException e) {
                    }

                    int noOfRecs = 0;
                    for (TableRecord record : records) {
                        if (!record.isDeleted()) {
                            noOfRecs++;
                        }
                    }

                    if (noOfRecs == 0) {
                        System.out.println("(No records found)");
                        return;
                    }

                    for (String columnName : Constants.columnNames) {
                        System.out.print(columnName + "\t");
                    }
                    System.out.println("");
                    for (TableRecord record : records) {
                        if (!record.isDeleted()) {
                            System.out.print(record.getId() + "\t");
                            System.out.print(record.getCompany() + "\t");
                            System.out.print(record.getDrugId() + "\t");
                            System.out.print(record.getTrials() + "\t");
                            System.out.print(record.getPatients() + "\t");
                            System.out.print(record.getDosageMg() + "\t");
                            System.out.print(record.getReading() + "\t");
                            System.out.print(record.isDoubleBlind() + "\t");
                            System.out.print(record.isControlledStudy() + "\t");
                            System.out.print(record.isGovtFunded() + "\t");
                            System.out.print(record.isFdaApproved() + "\t");
                            System.out.println("");
                        }

                    }
                    System.out.println("Fetching success!!");
                    raf.close();
                } else {
                    //display specific columns
                    ArrayList<String> columnNames = new ArrayList<>(Arrays.asList(keywords.get(1).split(",")));
                    RandomAccessFile raf = new RandomAccessFile(new File(currentDirectory + "/" + fileName + ".db"), "r");
                    ArrayList<TableRecord> records = new ArrayList<>();
                    int seekPosition = 0;
                    try {
                        while (true) {
                            TableRecord record = new TableRecord(raf, seekPosition);
                            records.add(record);
                            seekPosition += record.getLength();
                        }
                    } catch (EOFException e) {
                    }

                    for (String columnName : columnNames) {
                        if (!Constants.columnNames.contains(columnName)) {
                            System.out.println("Column name '" + columnName + "' not found. Please try again");
                            return;
                        }
                    }

                    int noOfRecs = 0;
                    for (TableRecord record : records) {
                        if (!record.isDeleted()) {
                            noOfRecs++;
                        }
                    }

                    if (noOfRecs == 0) {
                        System.out.println("(No records found)");
                        return;
                    }

                    for (String columnName : Constants.columnNames) {
                        if (columnNames.contains(columnName)) {
                            System.out.print(columnName + "\t");
                        }
                    }

                    System.out.println("");

                    for (TableRecord record : records) {
                        if (!record.isDeleted()) {
                            if (columnNames.contains(Constants.id)) {
                                System.out.print(record.getId() + "\t");
                            }
                            if (columnNames.contains(Constants.company)) {
                                System.out.print(record.getCompany() + "\t");
                            }
                            if (columnNames.contains(Constants.drugId)) {
                                System.out.print(record.getDrugId() + "\t");
                            }
                            if (columnNames.contains(Constants.trials)) {
                                System.out.print(record.getTrials() + "\t");
                            }
                            if (columnNames.contains(Constants.patients)) {
                                System.out.print(record.getPatients() + "\t");
                            }
                            if (columnNames.contains(Constants.dosageMg)) {
                                System.out.print(record.getDosageMg() + "\t");
                            }
                            if (columnNames.contains(Constants.reading)) {
                                System.out.print(record.getReading() + "\t");
                            }
                            if (columnNames.contains(Constants.doubleBlind)) {
                                System.out.print(record.isDoubleBlind() + "\t");
                            }
                            if (columnNames.contains(Constants.controlledStudy)) {
                                System.out.print(record.isControlledStudy() + "\t");
                            }
                            if (columnNames.contains(Constants.govtFunded)) {
                                System.out.print(record.isGovtFunded() + "\t");
                            }
                            if (columnNames.contains(Constants.fdaApproved)) {
                                System.out.print(record.isFdaApproved() + "\t");
                            }
                            System.out.println("");
                        }
                    }
                    System.out.println("Fetching success!!");
                    raf.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Error in fetching data. Please try again");
            System.out.println(e);
            return;
        }
    }

    private static void handleInsertCommand(String insertCommand) {
        if (insertCommand.charAt(insertCommand.length() - 1) == ';') {
            insertCommand = insertCommand.substring(0, insertCommand.length() - 1);
        }

        if (!insertCommand.matches(Constants.insertRegexString)) {
            System.out.println("Error parsing the insert command. Please check the syntax and try again.");
            return;
        }

        String keywords[] = insertCommand.split(" ");

        if (keywords.length == 0) {
            System.out.println("Please enter something");
            return;
        }

        if (keywords.length == 1) {
            System.out.println("Incomplete command. Please try again.");
            return;
        }

        if (!keywords[0].equalsIgnoreCase(Constants.insertKeyWord)) {
            System.out.println("Insert keyword missing. Please try again");
            return;
        }

        if (!keywords[1].equalsIgnoreCase(Constants.intoKeyword)) {
            System.out.println("Into keyword is missing. Please try again.");
            return;
        }

        if (fileName == null || fileName.equalsIgnoreCase("")) {
            try {
                File file = new File(currentDirectory + "/" + keywords[2] + ".db");
                if (file.exists()) {
                    fileName = keywords[2];
                } else {
                    System.out.println("Database table might not have been initialized. Please use import command first and then try again.");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Database table might not have been initialized. Please use import command first and then try again.");
                return;
            }
            fileName = keywords[2];
        }

        if (!keywords[3].equalsIgnoreCase(Constants.valuesKeyWord)) {
            System.out.println("'values' keyword is missing. Please try again");
            return;
        }

        if (!keywords[2].equalsIgnoreCase(fileName)) {
            System.out.println("Table name not identified. Please try again.");
            return;
        }

        if (keywords.length > 5) {
            for (int i = 5; i < keywords.length; i++) {
                keywords[4] += " " + keywords[i];
            }
        }

        String stringData[] = keywords[4].substring(1, keywords[4].length() - 1).split(",");

        if (stringData.length < 11) {
            System.out.println("Seems that some of the values are missing. Please check the data and try again");
            return;
        }

        for (int i = 0; i < stringData.length; i++) {
            String data1 = stringData[i];
            if (data1.startsWith("'") || data1.startsWith("\"")) {
                if (data1.endsWith("'") || data1.endsWith("\"")) {
                    stringData[i] = data1.substring(1, data1.length() - 1);
                } else {
                    System.out.println(data1 + " => Inconsistent data in 'values'. Please check the values and try again");
                    return;
                }
            }
        }

//        System.out.println("Data to be inserted is:");
//        for (int i = 0; i < stringData.length; i++) {
//            System.out.println(stringData[i]);
//        }
        ArrayList<String> data = new ArrayList<>(Arrays.asList(stringData));

        int id, trials, patients, dosageMg;
        String company = null;
        String drugId = null;
        float reading;
        boolean deleteFlag = false, doubleBlind, controlledStudy, govtFunded, fdaApproved;
        try {
            id = Integer.parseInt(data.get(0));
        } catch (Exception e) {
            System.out.println("Error parsing value of id to integer. Please try again");
            return;
        }

        //Check if id is already present
        try {
            RandomAccessFile indexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.id + ".ndx"), "rw");
            IndexList indexList = new IndexList(Data_Types.INT);
            indexList.readFromFile(indexFile);
            if (indexList.contains(Integer.toString(id))) {
                ArrayList<Integer> positions = indexList.get(Integer.toString(id));
                RandomAccessFile dbFile = new RandomAccessFile(currentDirectory + "/" + fileName + ".db", "rw");
                for (Integer position : positions) {
                    TableRecord record = new TableRecord(dbFile, position);
                    if (!record.isDeleted()) {
                        System.out.println("An entry with same id = " + id + " is already present in the table. Please try again.");
                        return;
                    }
                }
            }
            indexFile.close();
        } catch (IOException e) {
            System.out.println("Exception reading the index file. Please try again.");
            return;
        }

        company = data.get(1);
        drugId = data.get(2);
        if (drugId.length() > 6) {
            System.out.println("Max characters allowed in drug_id is 6. So cannot insert");
            return;
        }
        try {
            trials = Integer.parseInt(data.get(3));
        } catch (Exception e) {
            System.out.println("Error parsing value of trials to integer. Please try again");
            return;
        }
        try {
            patients = Integer.parseInt(data.get(4));
        } catch (Exception e) {
            System.out.println("Error parsing value of patients to integer. Please try again");
            return;
        }
        try {
            dosageMg = Integer.parseInt(data.get(5));
        } catch (Exception e) {
            System.out.println("Error parsing value of dosage_mg to integer. Please try again");
            return;
        }
        try {
            reading = Float.parseFloat(data.get(6));
        } catch (Exception e) {
            System.out.println("Error parsing value of reading to float. Please try again");
            return;
        }
        try {
            doubleBlind = Boolean.parseBoolean(data.get(7));
        } catch (Exception e) {
            System.out.println("Error parsing value of double_blind to boolean. Please try again");
            return;
        }
        try {
            controlledStudy = Boolean.parseBoolean(data.get(8));
        } catch (Exception e) {
            System.out.println("Error parsing value of controlled_study to boolean. Please try again");
            return;
        }
        try {
            govtFunded = Boolean.parseBoolean(data.get(9));
        } catch (Exception e) {
            System.out.println("Error parsing value of govt_funded to boolean. Please try again");
            return;
        }
        try {
            fdaApproved = Boolean.parseBoolean(data.get(10));
        } catch (Exception e) {
            System.out.println("Error parsing value of fda_approved to boolean. Please try again");
            return;
        }

        TableRecord record = new TableRecord(id, company.length(), company, drugId, trials, patients, dosageMg, reading, deleteFlag, doubleBlind, controlledStudy, govtFunded, fdaApproved);
//        System.out.println(record);
        RandomAccessFile raf = null;
        long length;
        try {
            File file = new File(currentDirectory + "/" + fileName + ".db");
            length = file.length();
            raf = new RandomAccessFile(file, "rw");
            record.writeToFileAtLocation(raf, length);
            raf.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Error writing data to the database file. Please try again.");
            return;
        } catch (IOException ex) {
            System.out.println("Error writing data to the database file. Please try again.");
            return;
        }

        try {
            RandomAccessFile idIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.id + ".ndx"), "rw");
            IndexList idIndexList = new IndexList(Data_Types.INT);
            idIndexList.readFromFile(idIndexFile);
            idIndexList.insert(Integer.toString(id), (int) length);
            idIndexFile.seek(0);
            idIndexList.writeToFile(idIndexFile);
//            idIndexList.printFile();
            idIndexFile.close();

            RandomAccessFile companyIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.company + ".ndx"), "rw");
            IndexList companyIndexList = new IndexList(Data_Types.STRING);
            companyIndexList.readFromFile(companyIndexFile);
            companyIndexList.insert(company, (int) length);
            companyIndexFile.seek(0);
            companyIndexList.writeToFile(companyIndexFile);
//            companyIndexList.printFile();
            companyIndexFile.close();

            RandomAccessFile drugIdIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.drugId + ".ndx"), "rw");
            IndexList drugIdIndexList = new IndexList(Data_Types.STRING);
            drugIdIndexList.readFromFile(drugIdIndexFile);
            drugIdIndexList.insert(drugId, (int) length);
            drugIdIndexFile.seek(0);
            drugIdIndexList.writeToFile(drugIdIndexFile);
//                    drugIdIndexList.printFile();
            drugIdIndexFile.close();

            RandomAccessFile trialsIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.trials + ".ndx"), "rw");
            IndexList trialsIndexList = new IndexList(Data_Types.INT);
            trialsIndexList.readFromFile(trialsIndexFile);
            trialsIndexList.insert(Integer.toString(trials), (int) length);
            trialsIndexFile.seek(0);
            trialsIndexList.writeToFile(trialsIndexFile);
//                    trialsIndexList.printFile();
            trialsIndexFile.close();

            RandomAccessFile patientsIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.patients + ".ndx"), "rw");
            IndexList patientsIndexList = new IndexList(Data_Types.INT);
            patientsIndexList.readFromFile(patientsIndexFile);
            patientsIndexList.insert(Integer.toString(patients), (int) length);
            patientsIndexFile.seek(0);
            patientsIndexList.writeToFile(patientsIndexFile);
//                    patientsIndexList.printFile();
            patientsIndexFile.close();

            RandomAccessFile dosageMgIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.dosageMg + ".ndx"), "rw");
            IndexList dosageMgIndexList = new IndexList(Data_Types.INT);
            dosageMgIndexList.readFromFile(dosageMgIndexFile);
            dosageMgIndexList.insert(Integer.toString(dosageMg), (int) length);
            dosageMgIndexFile.seek(0);
            dosageMgIndexList.writeToFile(dosageMgIndexFile);
//                    dosageMgIndexList.printFile();
            dosageMgIndexFile.close();

            RandomAccessFile readingIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.reading + ".ndx"), "rw");
            IndexList readingIndexList = new IndexList(Data_Types.FLOAT);
            readingIndexList.readFromFile(readingIndexFile);
            readingIndexList.insert(Float.toString(reading), (int) length);
            readingIndexFile.seek(0);
            readingIndexList.writeToFile(readingIndexFile);
//                    readingIndexList.printFile();
            readingIndexFile.close();

            RandomAccessFile doubleBlindIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.doubleBlind + ".ndx"), "rw");
            IndexList doubleBlindIndexList = new IndexList(Data_Types.BOOLEAN);
            doubleBlindIndexList.readFromFile(doubleBlindIndexFile);
            doubleBlindIndexList.insert(Boolean.toString(doubleBlind), (int) length);
            doubleBlindIndexFile.seek(0);
            doubleBlindIndexList.writeToFile(doubleBlindIndexFile);
//                    doubleBlindIndexList.printFile();
            doubleBlindIndexFile.close();

            RandomAccessFile controlledStudyIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.controlledStudy + ".ndx"), "rw");
            IndexList controlledStudyIndexList = new IndexList(Data_Types.BOOLEAN);
            controlledStudyIndexList.readFromFile(controlledStudyIndexFile);
            controlledStudyIndexList.insert(Boolean.toString(controlledStudy), (int) length);
            controlledStudyIndexFile.seek(0);
            controlledStudyIndexList.writeToFile(controlledStudyIndexFile);
//                    controlledStudyIndexList.printFile();
            controlledStudyIndexFile.close();

            RandomAccessFile govtFundedIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.govtFunded + ".ndx"), "rw");
            IndexList govtFundedIndexList = new IndexList(Data_Types.BOOLEAN);
            govtFundedIndexList.readFromFile(govtFundedIndexFile);
            govtFundedIndexList.insert(Boolean.toString(govtFunded), (int) length);
            govtFundedIndexFile.seek(0);
            govtFundedIndexList.writeToFile(govtFundedIndexFile);
//                    govtFundedIndexList.printFile();
            govtFundedIndexFile.close();

            RandomAccessFile fdaApprovedIndexFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.govtFunded + ".ndx"), "rw");
            IndexList fdaApprovedIndexList = new IndexList(Data_Types.BOOLEAN);
            fdaApprovedIndexList.readFromFile(fdaApprovedIndexFile);
            fdaApprovedIndexList.insert(Boolean.toString(fdaApproved), (int) length);
            fdaApprovedIndexFile.seek(0);
            fdaApprovedIndexList.writeToFile(fdaApprovedIndexFile);
//                    fdaApprovedIndexList.printFile();
            fdaApprovedIndexFile.close();

            System.out.println("Insertion successful!!!");

        } catch (Exception e) {
            System.out.println("Error occured while inserting into index files.");
            return;
        }
    }

    private static void handleDeleteCommand(String deleteCommand) {
        if (deleteCommand.charAt(deleteCommand.length() - 1) == ';') {
            deleteCommand = deleteCommand.substring(0, deleteCommand.length() - 1);
        }

        if (!deleteCommand.matches(Constants.deleteRegexString)) {
            System.out.println("Error parsing the delete command. Please check the syntax and try again.");
            return;
        }

        ArrayList<String> keywords = new ArrayList<>(Arrays.asList(deleteCommand.split(" ")));

        if (keywords.size() == 0) {
            System.out.println("Please enter something");
            return;
        }

        if (keywords.size() < 6) {
            System.out.println("Incomplete command. Please try again.");
            return;
        }

        if (!keywords.get(0).equalsIgnoreCase(Constants.deleteKeyword)) {
            System.out.println("'delete' keyword missing. Please try again");
            return;
        }

        if (!keywords.get(1).equalsIgnoreCase(Constants.fromKeyword)) {
            System.out.println("'from' keyword is missing. Please try again.");
            return;
        }

        if (fileName == null || fileName.equalsIgnoreCase("")) {
            try {
                File file = new File(currentDirectory + "/" + keywords.get(2) + ".db");
                if (file.exists()) {
                    fileName = keywords.get(2);
                } else {
                    System.out.println("Database table might not have been initialized. Please use import command first and then try again.");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Database table might not have been initialized. Please use import command first and then try again.");
                return;
            }
            fileName = keywords.get(2);
        }

        if (!keywords.get(3).equalsIgnoreCase(Constants.whereKeyword)) {
            System.out.println("'where' keyword is missing. Please try again");
            return;
        }

        if (!keywords.get(2).equalsIgnoreCase(fileName)) {
            System.out.println("Table name not identified. Please try again.");
            return;
        }

        if (!Constants.columnNames.contains(keywords.get(4))) {
            System.out.println("Column '" + keywords.get(4) + "' does not exist. Please try again.");
            return;
        }

        try {
            String whereColumnName = keywords.get(4);
            ArrayList<Integer> rowsToDelete = new ArrayList<>();
            String operator = null;
            IndexList columnList = getColumnIndexList(whereColumnName);
            Data_Types dataType = getDataType(whereColumnName);
            boolean hasNot = false;
            if (keywords.contains("NOT") || keywords.contains("not") || keywords.contains("Not")) {
                //Contains NOT keyword
                hasNot = true;
                operator = keywords.get(6);
                if (operator.equalsIgnoreCase("=")) {
                    //Not equal to
                    operator = "!=";
                } else if (operator.equalsIgnoreCase(">")) {
                    //lesser than equal to
                    operator = "<=";
                } else if (operator.equalsIgnoreCase("<")) {
                    //greater than equal to
                    operator = ">=";
                } else if (operator.equalsIgnoreCase(">=")) {
                    //lesser than
                    operator = "<";
                } else if (operator.equalsIgnoreCase("<=")) {
                    //greater than
                    operator = ">";
                } else {
                    System.out.println("Operator not recognised. Please try again");
                    return;
                }
            } else {
                operator = keywords.get(5);
            }

            if (dataType == Data_Types.INT) {
                int data;
                try {
                    if (hasNot) {
                        data = Integer.parseInt(keywords.get(7));
                    } else {
                        data = Integer.parseInt(keywords.get(6));
                    }
                } catch (Exception e) {
                    System.out.println("Please enter integer data only. Please try again.");
                    return;
                }

                for (Entry entry : columnList.list) {
                    if (operator.equalsIgnoreCase("=")) {
                        if (Integer.parseInt(entry.getKey()) == data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("!=")) {
                        if (Integer.parseInt(entry.getKey()) != data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals(">")) {
                        if (Integer.parseInt(entry.getKey()) > data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("<")) {
                        if (Integer.parseInt(entry.getKey()) < data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals(">=")) {
                        if (Integer.parseInt(entry.getKey()) >= data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("<=")) {
                        if (Integer.parseInt(entry.getKey()) <= data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else {
                        System.out.println("Operator not found. Please try again.");
                        return;
                    }
                }
            } else if (dataType == Data_Types.FLOAT) {
                float data;
                try {
                    if (hasNot) {
                        data = Float.parseFloat(keywords.get(7));
                    } else {
                        data = Float.parseFloat(keywords.get(6));
                    }
                } catch (Exception e) {
                    System.out.println("Please enter float data only. Please try again.");
                    return;
                }

                for (Entry entry : columnList.list) {
                    if (operator.equalsIgnoreCase("=")) {
                        if (Float.parseFloat(entry.getKey()) == data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("!=")) {
                        if (Float.parseFloat(entry.getKey()) != data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals(">")) {
                        if (Float.parseFloat(entry.getKey()) > data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("<")) {
                        if (Float.parseFloat(entry.getKey()) < data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals(">=")) {
                        if (Float.parseFloat(entry.getKey()) >= data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("<=")) {
                        if (Float.parseFloat(entry.getKey()) <= data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else {
                        System.out.println("Operator not found. Please try again.");
                        return;
                    }
                }
            } else if (dataType == Data_Types.STRING) {
                String data;
                try {
                    if (hasNot) {
                        data = keywords.get(7);
                    } else {
                        data = keywords.get(6);
                    }
                } catch (Exception e) {
                    System.out.println("Please enter string data only. Please try again.");
                    return;
                }
                int index = deleteCommand.indexOf(data);
                data = deleteCommand.substring(index, deleteCommand.length());

                if (data.startsWith("'") || data.startsWith("\"")) {
                    if (data.endsWith("'") || data.endsWith("\"")) {
                        data = data.substring(1, data.length() - 1);
                    } else {
                        System.out.println("Value starts with \' or \" but does not end with it. Please try again.");
                        return;
                    }
                }

//                        System.out.println("Data = " + data);
                for (Entry entry : columnList.list) {
                    if (operator.equalsIgnoreCase("=")) {
                        if ((entry.getKey()).equalsIgnoreCase(data)) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("!=")) {
                        if (!entry.getKey().equalsIgnoreCase(data)) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals(">")) {
                        if (entry.getKey().compareToIgnoreCase(data) > 0) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("<")) {
                        if (entry.getKey().compareToIgnoreCase(data) < 0) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals(">=")) {
                        if (entry.getKey().compareToIgnoreCase(data) > 0 || entry.getKey().compareToIgnoreCase(data) == 0) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("<=")) {
                        if (entry.getKey().compareToIgnoreCase(data) < 0 || entry.getKey().compareToIgnoreCase(data) == 0) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else {
                        System.out.println("Operator not found. Please try again.");
                        return;
                    }
                }
            } else if (dataType == Data_Types.BOOLEAN) {
                if (!operator.equals("=") && !operator.equals("!=")) {
                    System.out.println("greater than or lesser than not allowed for boolean data types. Please try again.");
                    return;
                }

                boolean data;
                try {
                    if (hasNot) {
                        data = Boolean.parseBoolean(keywords.get(7));
                    } else {
                        data = Boolean.parseBoolean(keywords.get(6));
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing data. Please enter boolean data only. Please try again.");
                    return;
                }

                for (Entry entry : columnList.list) {
                    if (operator.equalsIgnoreCase("=")) {
                        if (Boolean.parseBoolean(entry.getKey()) == data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else if (operator.equals("!=")) {
                        if (Boolean.parseBoolean(entry.getKey()) != data) {
                            ArrayList<Integer> list = columnList.get(entry.getKey());
                            for (Integer position : list) {
                                rowsToDelete.add(position);
                            }
                        }
                    } else {
                        System.out.println("Operator not found. Please try again.");
                        return;
                    }
                }
            }

            if (rowsToDelete.size() == 0) {
                System.out.println("No rows found. 0 rows deleted.");
                return;
            }

            int count = 0;
            RandomAccessFile raf = new RandomAccessFile(new File(currentDirectory + "/" + fileName + ".db"), "rw");
            ArrayList<TableRecord> records = new ArrayList<>();
            for (Integer seekPosition : rowsToDelete) {
                TableRecord record = new TableRecord(raf, seekPosition);
                if (!record.isDeleted()) {
                    count++;
                }
                record.setDeleted(true);
                record.writeToFileAtLocation(raf, seekPosition);
            }
            System.out.println("Delete success!! (" + count + ") rows deleted!");
            raf.close();
        } catch (Exception e) {
            System.out.println("Error in deleting data. Please try again");
            return;
        }
    }

    private static IndexList getColumnIndexList(String columnName) throws FileNotFoundException, IOException {
        RandomAccessFile randomAccessFile = null;
        IndexList indexList = null;
        if (columnName.equalsIgnoreCase(Constants.id)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.id + ".ndx"), "r");
            indexList = new IndexList(Data_Types.INT);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.company)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.company + ".ndx"), "r");
            indexList = new IndexList(Data_Types.STRING);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.drugId)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.drugId + ".ndx"), "r");
            indexList = new IndexList(Data_Types.STRING);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.trials)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.trials + ".ndx"), "r");
            indexList = new IndexList(Data_Types.INT);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.patients)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.patients + ".ndx"), "r");
            indexList = new IndexList(Data_Types.INT);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.dosageMg)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.dosageMg + ".ndx"), "r");
            indexList = new IndexList(Data_Types.INT);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.reading)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.reading + ".ndx"), "r");
            indexList = new IndexList(Data_Types.FLOAT);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.doubleBlind)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.doubleBlind + ".ndx"), "r");
            indexList = new IndexList(Data_Types.BOOLEAN);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.controlledStudy)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.controlledStudy + ".ndx"), "r");
            indexList = new IndexList(Data_Types.BOOLEAN);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.govtFunded)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.govtFunded + ".ndx"), "r");
            indexList = new IndexList(Data_Types.BOOLEAN);
            indexList.readFromFile(randomAccessFile);
        } else if (columnName.equalsIgnoreCase(Constants.fdaApproved)) {
            randomAccessFile = new RandomAccessFile(new File(currentDirectory + "/" + fileName + "." + Constants.fdaApproved + ".ndx"), "r");
            indexList = new IndexList(Data_Types.BOOLEAN);
            indexList.readFromFile(randomAccessFile);
        }
        randomAccessFile.close();
        return indexList;
    }

    private static Data_Types getDataType(String columnName) {
        Data_Types dataType = null;
        if (columnName.equalsIgnoreCase(Constants.id)) {
            dataType = Data_Types.INT;
        } else if (columnName.equalsIgnoreCase(Constants.company)) {
            dataType = Data_Types.STRING;
        } else if (columnName.equalsIgnoreCase(Constants.drugId)) {
            dataType = Data_Types.STRING;
        } else if (columnName.equalsIgnoreCase(Constants.trials)) {
            dataType = Data_Types.INT;
        } else if (columnName.equalsIgnoreCase(Constants.patients)) {
            dataType = Data_Types.INT;
        } else if (columnName.equalsIgnoreCase(Constants.dosageMg)) {
            dataType = Data_Types.INT;
        } else if (columnName.equalsIgnoreCase(Constants.reading)) {
            dataType = Data_Types.FLOAT;
        } else if (columnName.equalsIgnoreCase(Constants.doubleBlind)) {
            dataType = Data_Types.BOOLEAN;
        } else if (columnName.equalsIgnoreCase(Constants.controlledStudy)) {
            dataType = Data_Types.BOOLEAN;
        } else if (columnName.equalsIgnoreCase(Constants.govtFunded)) {
            dataType = Data_Types.BOOLEAN;
        } else if (columnName.equalsIgnoreCase(Constants.fdaApproved)) {
            dataType = Data_Types.BOOLEAN;
        }
        return dataType;

    }
}

class Constants {

    public static final String id = "id";
    public static final String company = "company";
    public static final String drugId = "drug_id";
    public static final String trials = "trials";
    public static final String patients = "patients";
    public static final String dosageMg = "dosage_mg";
    public static final String reading = "reading";
    public static final String doubleBlind = "double_blind";
    public static final String controlledStudy = "controlled_study";
    public static final String govtFunded = "govt_funded";
    public static final String fdaApproved = "fda_approved";

    public static final ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList(Constants.id, Constants.company, Constants.drugId, Constants.trials, Constants.patients,
            Constants.dosageMg, Constants.reading, Constants.doubleBlind, Constants.controlledStudy, Constants.govtFunded, Constants.fdaApproved));

    public static final String importKeyword = "import";
    public static final String selectKeyword = "select";
    public static final String fromKeyword = "from";
    public static final String whereKeyword = "where";
    public static final String insertKeyWord = "insert";
    public static final String intoKeyword = "into";
    public static final String valuesKeyWord = "values";
    public static final String deleteKeyword = "delete";

    public static final String regexString = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    public static final String selectRegexString = "^((S|s)(E|e)(L|l)(E|e)(C|c)(T|t)\\s(\\*|((\\S+,)*(\\S+)))\\s(F|f)(R|r)(O|o)(M|m)\\s\\S+(\\s(W|w)(H|h)(E|e)(R|r)(E|e)\\s(\\S+)(\\s(N|n)(O|o)(T|t)){0,1}\\s(=|>=|<=|>|<)\\s.+){0,1})$";
    public static final String importRegexString = "^((I|i)(M|m)(P|p)(O|o)(R|r)(T|t)\\s\\S+\\.\\S+)$";
    public static final String insertRegexString = "^((I|i)(N|n)(S|s)(E|e)(R|r)(T|t)\\s(I|i)(N|n)(T|t)(O|o)\\s\\S+\\s(V|v)(A|a)(L|l)(U|u)(E|e)(S|s)\\s\\(.+\\))$";
    public static final String deleteRegexString = "^((D|d)(E|e)(L|l)(E|e)(T|t)(E|e)\\s(F|f)(R|r)(O|o)(M|m)\\s\\S+\\s(W|w)(H|h)(E|e)(R|r)(E|e)\\s\\S+(\\s(N|n)(O|o)(T|t)){0,1}\\s(=|>=|<=|>|<)\\s\\S+)$";
}

class TableRecord {

    @Override
    public String toString() {
        return "TableRecord{" + "id=" + id + ", length=" + length + ", company=" + company + ", drugId=" + drugId + ", trials=" + trials + ", patients=" + patients + ", dosageMg=" + dosageMg + ", reading=" + reading + ", deleted=" + deleted + ", doubleBlind=" + doubleBlind + ", controlledStudy=" + controlledStudy + ", govtFunded=" + govtFunded + ", fdaApproved=" + fdaApproved + '}';
    }

    private int id;
    private int length;
    private String company;
    private String drugId;
    private int trials;
    private int patients;
    private int dosageMg;
    private float reading;
    private boolean deleted;
    private boolean doubleBlind;
    private boolean controlledStudy;
    private boolean govtFunded;
    private boolean fdaApproved;

    public TableRecord(int id, int length, String company, String drugId, int trials, int patients, int dosageMg, float reading, boolean deleted, boolean doubleBlind, boolean controlledStudy, boolean govtFunded, boolean fdaApproved) {
        this.id = id;
        this.length = length;
        this.company = company;
        this.drugId = drugId;
        this.trials = trials;
        this.patients = patients;
        this.dosageMg = dosageMg;
        this.reading = reading;
        this.deleted = deleted;
        this.doubleBlind = doubleBlind;
        this.controlledStudy = controlledStudy;
        this.govtFunded = govtFunded;
        this.fdaApproved = fdaApproved;
    }

    public TableRecord(RandomAccessFile raf, int seekPosition) throws IOException {

        raf.seek(seekPosition);
        this.id = raf.readInt();

        int length = raf.read();
        this.length = length;
        this.company = new String();
        for (int i = 0; i < length; i++) {
            this.company += Character.toString((char) raf.readByte());
        }
        this.drugId = new String();
        for (int i = 0; i < 6; i++) {
            this.drugId += Character.toString((char) raf.readByte());
        }
        this.trials = raf.readShort();
        this.patients = raf.readShort();
        this.dosageMg = raf.readShort();
        this.reading = raf.readFloat();

        int deleteMask = 128;
        byte doubleBlindMask = 8;
        byte controlledStudyMask = 4;
        byte govtFundedMask = 2;
        byte fdaApprovedMask = 1;
        int data = raf.read();
        if ((deleteMask & data) == deleteMask) {
            this.deleted = true;
        } else {
            this.deleted = false;
        }
        if ((doubleBlindMask & data) == doubleBlindMask) {
            this.doubleBlind = true;
        } else {
            this.doubleBlind = false;
        }
        if ((controlledStudyMask & data) == controlledStudyMask) {
            this.controlledStudy = true;
        } else {
            this.controlledStudy = false;
        }
        if ((govtFundedMask & data) == govtFundedMask) {
            this.govtFunded = true;
        } else {
            this.govtFunded = false;
        }
        if ((fdaApprovedMask & data) == fdaApprovedMask) {
            this.fdaApproved = true;
        } else {
            this.fdaApproved = false;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDrugId() {
        return drugId;
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    public int getTrials() {
        return trials;
    }

    public void setTrials(int trials) {
        this.trials = trials;
    }

    public int getPatients() {
        return patients;
    }

    public void setPatients(int patients) {
        this.patients = patients;
    }

    public int getDosageMg() {
        return dosageMg;
    }

    public void setDosageMg(int dosageMg) {
        this.dosageMg = dosageMg;
    }

    public float getReading() {
        return reading;
    }

    public void setReading(float reading) {
        this.reading = reading;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDoubleBlind() {
        return doubleBlind;
    }

    public void setDoubleBlind(boolean doubleBlind) {
        this.doubleBlind = doubleBlind;
    }

    public boolean isControlledStudy() {
        return controlledStudy;
    }

    public void setControlledStudy(boolean controlledStudy) {
        this.controlledStudy = controlledStudy;
    }

    public boolean isGovtFunded() {
        return govtFunded;
    }

    public void setGovtFunded(boolean govtFunded) {
        this.govtFunded = govtFunded;
    }

    public boolean isFdaApproved() {
        return fdaApproved;
    }

    public void setFdaApproved(boolean fdaApproved) {
        this.fdaApproved = fdaApproved;
    }

    public void writeToFile(RandomAccessFile randomAccessFile) throws IOException {
        randomAccessFile.writeInt(id);
        randomAccessFile.writeByte(length);
        randomAccessFile.writeBytes(company);
        randomAccessFile.writeBytes(drugId);
        randomAccessFile.writeShort(trials);
        randomAccessFile.writeShort(patients);
        randomAccessFile.writeShort(dosageMg);
        randomAccessFile.writeFloat(reading);
        String binaryString = new String();
        if (deleted) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }

        binaryString += "000";

        if (doubleBlind) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }

        if (controlledStudy) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }

        if (govtFunded) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }

        if (fdaApproved) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }
        randomAccessFile.write(Integer.parseInt(binaryString, 2));
    }

    public void writeToFileAtLocation(RandomAccessFile randomAccessFile, long position) throws IOException {
        randomAccessFile.seek(position);
        randomAccessFile.writeInt(id);
        randomAccessFile.writeByte(length);
        randomAccessFile.writeBytes(company);
        randomAccessFile.writeBytes(drugId);
        randomAccessFile.writeShort(trials);
        randomAccessFile.writeShort(patients);
        randomAccessFile.writeShort(dosageMg);
        randomAccessFile.writeFloat(reading);
        String binaryString = new String();
        if (deleted) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }

        binaryString += "000";

        if (doubleBlind) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }

        if (controlledStudy) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }

        if (govtFunded) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }

        if (fdaApproved) {
            binaryString += "1";
        } else {
            binaryString += "0";
        }
        randomAccessFile.write(Integer.parseInt(binaryString, 2));
    }

    public int getLength() {
        return (4 + 1 + length + 6 + 2 + 2 + 2 + 4 + 1);
    }
}

class Entry {

    protected String key;
    protected int value;
    Entry next;

    public Entry(String key, int value) {
        this.key = key;
        this.value = value;
        this.next = null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Entry getNext() {
        return next;
    }

    public void setNext(Entry next) {
        this.next = next;
    }
}

class IndexList {

    ArrayList<Entry> list;

    private Data_Types dataType;

    public IndexList(Data_Types dataType) {
        this.dataType = dataType;
        list = new ArrayList<Entry>() {
            public boolean add(Entry mt) {
                super.add(mt);
                Collections.sort(list, new Comparator<Entry>() {
                    @Override
                    public int compare(Entry lhs, Entry rhs) {
                        if (dataType == Data_Types.BOOLEAN) {
                            return new Boolean(lhs.getKey()).compareTo(new Boolean(rhs.getKey()));
                        }
                        if (dataType == Data_Types.FLOAT) {
                            return new Float(lhs.getKey()).compareTo(new Float(rhs.getKey()));
                        }
                        if (dataType == Data_Types.INT) {
                            return new Integer(lhs.getKey()).compareTo(new Integer(rhs.getKey()));
                        }
                        return lhs.getKey().compareTo(rhs.getKey());
                    }
                });
                return true;
            }
        };
    }

    public void insert(String key, int value) {
        for (Entry entry : list) {
            if (entry.getKey().equals(key)) {
                while (entry.next != null) {
                    entry = entry.next;
                }
                Entry newEntry = new Entry(key, value);
                entry.next = newEntry;
                return;
            }
        }
        Entry newEntry = new Entry(key, value);
        list.add(newEntry);
    }

    public boolean contains(String key) {
        for (Entry entry : list) {
            if (entry.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Integer> get(String key) {
        for (Entry entry : list) {
            if (entry.getKey().equals(key)) {
                ArrayList<Integer> positionList = new ArrayList<>();
                positionList.add(entry.getValue());
                while (entry.next != null) {
                    entry = entry.next;
                    positionList.add(entry.getValue());
                }
                return positionList;
            }
        }
        return null;
    }

    public void writeToFile(RandomAccessFile raf) throws IOException {
        for (Entry entry : list) {
            int length = entry.getKey().length();
            raf.writeShort(length);
            raf.writeBytes(entry.getKey());
            int count = 1;
            Entry tempEmtry = entry;
            while (tempEmtry.next != null) {
                tempEmtry = tempEmtry.next;
                count++;
            }
            raf.writeShort(count);
            raf.writeInt(entry.getValue());
            while (entry.next != null) {
                entry = entry.next;
                raf.writeInt(entry.getValue());
            }
        }
    }

    public void readFromFile(RandomAccessFile raf) throws IOException {
        try {
            raf.seek(0);
            while (true) {
                int length = raf.readShort();
                String key = new String();
                for (int i = 0; i < length; i++) {
                    key += Character.toString((char) raf.readByte());
                }
                int count = raf.readShort();
                int value = raf.readInt();
                Entry entry = new Entry(key, value);
                list.add(entry);
                for (int i = 2; i <= count; i++) {
                    Entry newEntry = new Entry(key, raf.readInt());
                    entry.next = newEntry;
                    entry = newEntry;
                }
            }
        } catch (EOFException ex) {
//            System.out.println("End of file reached. Reading from index file completed");
        }
    }

    public void printFile() {
        for (Entry entry : list) {
            System.out.print("Key = " + entry.getKey() + " ");
            System.out.print("Pos = " + entry.getValue());
            while (entry.next != null) {
                entry = entry.next;
                System.out.print(" " + entry.getValue());
            }
            System.out.println(" ");
        }

    }
}

enum Data_Types {

    INT, FLOAT, BOOLEAN, STRING;
}
