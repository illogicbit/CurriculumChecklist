package prog2.fingrp;
import prog2.fingrp.CourseCheckerGUI.GUImain;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

/*
 * Notes
 * run() method contains the merging of the GUI and Backend
 * updateTable() updates the table, record manipulation only happens here
 * updateComboBox() updates the curriculum list in the add/create menu
 */

public class CurriculumChecklistMain {

    //Variables for GUI and HashMaps
    private static GUImain gui;
    private static CurriculumRecord record;
    private static DefaultTableModel tableModel;
    private static ArrayList<Course> temporaryRecords; //General filtered record storage

    private static JComboBox courseList;

    private static boolean isAdditional; //Filter By Curriculum
    private static String[] yearAndTerm = {"1", "1"}; //Filter By Year And Term

    //Main method
    public static void main(String[] args) {
        try {
            gui = new GUImain();
            run();
        }catch (IOException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }

    }

    //Method that contains all the functions
    public static void run() throws IOException, ClassNotFoundException {

        //Generate template, save, and a new record manager object
        File template = new File("res/CS_template.dat");
        File save = new File("res/test.dat");
        generateTemplate(new File("res/CurriculumTemplate.txt"));
        record = new CurriculumRecord(new FileInputStream(template), new FileInputStream(save));

        //Instantiate the table model
        tableModel = gui.getTableModel();

        //Assign a key-value pair to the items in the combo box
        Hashtable<String, String> cbValues = gui.getCbValues();
        JComboBox termChoice = gui.getTermChoice();
        JComboBox sortChoice = gui.getSortChoice();
        courseList = gui.getCourseList();

        //Button declaration
        JRadioButton curriculumCheckbox = gui.getCurriculumCheckBox();
        JButton addCourse = gui.getAddCourse();
        JButton archiveCourse = gui.getArchiveCourse();
        JButton appInfo = gui.getAppInfo();

        //Initial startup
        record.setFilterRecord(record.FilterByYearAndTerm(1, 1));
        temporaryRecords = record.getFilteredCourseList();
        updateTable();

        //Filtering system for year and term
        termChoice.addItemListener(e -> {
            yearAndTerm = cbValues.get(((JComboBox) e.getSource()).getSelectedItem()).split("-");
            updateTable();
        });

        //Sorting system
        sortChoice.addItemListener(e -> {
            JComboBox cb = (JComboBox) e.getSource();
            GUImain.SORTING sortValue = GUImain.SORTING.valueOf((String) cb.getSelectedItem());
            switch (sortValue){
                case BY_TITLE_AZ:
                    temporaryRecords = record.SortByTitle(false);
                    break;
                case BY_TITLE_ZA:
                    temporaryRecords = record.SortByTitle(true);
                    break;
                case BY_GRADES_ASCENDING:
                    temporaryRecords = record.SortByGrade(false);
                    break;
                case BY_GRADES_DESCENDING:
                    temporaryRecords = record.SortByGrade(true);
                    break;
                default:
                    temporaryRecords = record.FilterByYearAndTerm(Integer.parseInt(yearAndTerm[0]), Integer.parseInt(yearAndTerm[1]));
            }
            updateTable();
        });

        //Toggle button to display
        curriculumCheckbox.addActionListener(e -> {
            JRadioButton radioButton = (JRadioButton) e.getSource();
            isAdditional = radioButton.isSelected();
            updateTable();
        });

        //Menu for editing/adding of courses
        addCourse.addActionListener(e -> {

            //Assigns the text fields to a variable
            JTextField[] editFields = gui.getEditFields();
            Object[] fields = {courseList, "Course Code", editFields[0], "Course Title",  editFields[1], "Units", editFields[2],
                    "Grade", editFields[3], "Year", editFields[4], "Term", editFields[5], "Status", gui.getCourseStatus(), gui.getIsElective()};

            //Dropdown menu for status list in add/edit menu
            gui.getCourseStatus().addItemListener(f -> {
                JComboBox cb = (JComboBox) f.getSource();
                if(cb.getSelectedItem() == Course.STATUS.COMPLETE){
                    editFields[3].setEnabled(true);
                }else {
                    editFields[3].setEnabled(false);
                    editFields[3].setText(String.valueOf(0));
                }
            });


            //Dropdown menu for course list in add/edit menu
            gui.getCourseList().addItemListener(f -> {
                JComboBox cb = (JComboBox) f.getSource();
                if(cb.getSelectedItem() != "Additional Course"){
                    editFields[0].setText((String) cb.getSelectedItem());
                    editFields[1].setText(record.getCourse((String) cb.getSelectedItem()).getTitle());
                    editFields[2].setText(String.valueOf(record.getCourse((String) cb.getSelectedItem()).getUnits()));
                    editFields[3].setText(String.valueOf(record.getCourse((String) cb.getSelectedItem()).getGrade()));
                    editFields[4].setText(String.valueOf(record.getCourse((String) cb.getSelectedItem()).getYear()));
                    editFields[5].setText(String.valueOf(record.getCourse((String) cb.getSelectedItem()).getTerm()));
                    gui.getIsElective().setSelected(record.getCourse(editFields[0].getText()).isElective());
                }else{
                    //Clears the text fields if the user wants to input an additional course
                    for(JTextField field: editFields){
                        field.setText("");
                    }
                }
            });

            //Generates a JOptionPane using the fields array and updates the table if changes were made
            int result = JOptionPane.showConfirmDialog(gui.getEditFrame(), fields, "Create a Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

            try{
                //Creates a new Course using the data
                if(result == JOptionPane.OK_OPTION) {
                    record.editCourse(editFields[0].getText(), new Course.CourseBuilder().code(editFields[0].getText()).
                            title(editFields[1].getText()).units(Integer.parseInt(editFields[2].getText()))
                            .grade(Float.parseFloat(editFields[3].getText())).year(Integer.parseInt(editFields[4].getText())).
                            term(Integer.parseInt(editFields[5].getText())).status((Course.STATUS) gui.getCourseStatus().getSelectedItem()).electiveStatus(gui.getIsElective().isSelected()));
                    updateTable();
                }
            }catch(NumberFormatException err){
                JOptionPane.showMessageDialog(null, "Invalid Input/s!", "Error!", JOptionPane.ERROR_MESSAGE);
                err.printStackTrace();
            }catch (RuntimeException err){
                JOptionPane.showMessageDialog(null, "Invalid Year/Term", "Error!", JOptionPane.ERROR_MESSAGE);
                err.printStackTrace();
            }
        });

        //Menu for deleting a course
        archiveCourse.addActionListener(e -> {
            Object[] delCourse = {"Input course code:", gui.getEditFields()[0]};
            int result = JOptionPane.showConfirmDialog(gui.getEditFrame(), delCourse, "Delete a course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if(result == JOptionPane.OK_OPTION){
                int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete course " + gui.getEditFields()[0].getText() + "?","Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if(res == JOptionPane.YES_OPTION)
                    record.deleteCourse(gui.getEditFields()[0].getText());
                updateTable();
            }
            gui.getEditFields()[0].setText("");
        });

        appInfo.addActionListener(e ->{
            Object msg [] = {"Welcome to the curriculum checklist management system!", "The first two menus at the navigation bar is a filter that helps you choose the year term of the courses you want to view\nThe second one gives you an option to sort your courses by title or by grade.\n",
                    "", new ImageIcon("res/Icons/AddIcon.png") , "This button allows you to edit a course by selecting the course from a list. \nIt also allows you to add a course by filling up information about your additional course.\n",
                    "", new ImageIcon("res/Icons/ArchiveIcon.png"), "This button allows you to delete an existing course by inputting the corresponding course code.",};
            JOptionPane.showMessageDialog(new JPanel(new GridLayout(2, 1)), msg, "Info", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    //Update the table. Any changes in the data will only occur here
    public static void updateTable(){
        //Clears the table
        tableModel.setRowCount(0);

        //Clears the record to display all courses in that year/term.
        record.setFilterRecord(record.FilterByYearAndTerm(Integer.parseInt(yearAndTerm[0]), Integer.parseInt(yearAndTerm[1])));

        //Filters the array to check which courses to display.
        record.setFilterRecord(temporaryRecords);
        temporaryRecords = record.FilterByCurriculum(isAdditional);

        //Generate a map to determine the range of grades per GPA
        //NOTE: This just converts the initial grade to an equivalent GPA.
        TreeMap<Float,Float> map = new TreeMap<>();
        map.put(0f, 0f);
        for (float gpaMap = 1.0f, gradeMap = 75.0f; gpaMap < 5; gpaMap+=1.0f, gradeMap+=4.99f){
            map.put(gradeMap, gpaMap);
        }

        Object grade;
        Object gpa;
        for (Course course: temporaryRecords){
            //Display the grade and gpa if the course is complete
            grade = (course.getStatus() == Course.STATUS.COMPLETE) ? course.getGrade() : course.getStatus();
            gpa = (course.getStatus() == Course.STATUS.COMPLETE) ? map.get(map.floorKey((Float) grade)) : "";

            tableModel.addRow(new Object[]{course.getUnits(), course.getCode(), course.getTitle(), grade, gpa,
                    (course.getStatus() == Course.STATUS.COMPLETE) ? ((course.getGrade() != null && course.getGrade() >= 75 && course.getGrade() > 0.00) ? "Passed" : "Failed") : ""});
        }

        //Reverts to the original list of courses for reuse
        record.setFilterRecord(record.FilterByYearAndTerm(Integer.parseInt(yearAndTerm[0]), Integer.parseInt(yearAndTerm[1])));
        temporaryRecords = record.getFilteredCourseList();

        //Auto-save; remove if planning to use manual sav.
        try {record.saveChanges(new FileOutputStream("res/test.dat"));
        }catch (IOException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        updateCourseComboBox();
    }

    public static void updateCourseComboBox(){
        //NOTE: Using removeAllItems() causes an error hence why I had to use a for loop
        for (int i = courseList.getItemCount() - 1; i > 0; i--){
            courseList.removeItemAt(i);
        }

        for(Course course: record.getFilteredCourseList()){
            courseList.addItem(course.getCode());
        }
    }

    //Generate a new template in case of changes in curriculum (Included in the case of DevTest being removed)
    static void generateTemplate(File templateLoc) {
        ArrayList<Course> templateList = new ArrayList<Course>();
        try (Scanner file = new Scanner(new FileInputStream(templateLoc))) {
            while (file.hasNextLine()) {
                String[] in = file.nextLine().split("/");
                templateList.add(new Course(new Course.CourseBuilder()
                        .code(in[0])
                        .title(in[1])
                        .units(Integer.parseInt(in[2]))
                        .year(Integer.parseInt(in[3]))
                        .term(Integer.parseInt(in[4]))
                        .electiveStatus(Boolean.parseBoolean(in[5]))
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("res/CS_template.dat"))) {
            out.writeObject(templateList);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
