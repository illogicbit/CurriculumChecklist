package prog2.fingrp;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

/**
 * Development notes:
 * We split the record into 2 array lists so changes made by the user can be stored separately
 * from the template course files.
 * 1. Compiled record
 *  - Initially the data from the template file. This is what gets passed to the GUI when
 *      they ask for data from the CurriculumRecord.
 *  - Sorting and filtering functions should return a modified version of this array list.
 * 2. Personal record
 *  - These are the changes made by the user. Refer to the mergeData function of the Course class
 *      to see what values constitute as a change.
 *  - This is what gets saved to the personal record .dat file using the saveChanges() function.
 * 3. Filtered Record
 *  - Stores changes from the FilterByYearAndTerm function which allows us to manipulate a record without tampering with the original compiled record.
 *  - Just for visual changes in the GUI.
 * 4. General process for data manipulation and storage.
 *  1. The first filter would be by year and term, which display courses for that year and term. The data from would be filtered out from the compiled record.
 *  2. The next filter would be the filter by curriculum which also takes data from the template record and stores it in an array
 *  3. The final filter would be the sorting functions, which uses data from the filter by curriculum and stores them in an array in the main method.
 *  4. Any added courses will be stored and deleted permanently when the saveChanges function is invoked, but any deleted courses from the original template will be returned (Bug or Feature probably).
 */

public class CurriculumRecord {
    private ArrayList<Course> filterRecord; //Little bro finally has a use
    private ArrayList<Course> personalRecord;
    private ArrayList<Course> compiledRecord;

    public CurriculumRecord(InputStream templateRecord, InputStream personalRecord) throws IOException, ClassNotFoundException{
        this.compiledRecord = (ArrayList<Course>) new ObjectInputStream(templateRecord).readObject();
        this.personalRecord = (ArrayList<Course>) new ObjectInputStream(personalRecord).readObject();
        this.filterRecord = this.compiledRecord;


        //Early close the files. We won't be needing them anymore.
        templateRecord.close();
        personalRecord.close();


        //Find the first match of the courses with changes and merge their data with the existing data.
        for (Course personalData: this.personalRecord){
            boolean matchFound = false;
            for(Course outputCourse: this.compiledRecord){
                if (outputCourse.equals(personalData)){
                    outputCourse.mergeData(personalData);
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) compiledRecord.add(personalData);
        }

    }

    public CurriculumRecord(InputStream templateRecord) throws IOException, ClassNotFoundException{
        this.compiledRecord = (ArrayList<Course>) new ObjectInputStream(templateRecord).readObject();
        this.filterRecord = new ArrayList<Course>();
        this.personalRecord = new ArrayList<Course>();

        templateRecord.close();
    }

    //Sets the filter record
    public void setFilterRecord(ArrayList<Course> courses) {
        filterRecord = courses;
    }

    //Gets the compiled record
    public ArrayList<Course> getCourseList() {
        return compiledRecord;
    }

    //Get the filtered list
    public ArrayList<Course> getFilteredCourseList() {
        return filterRecord;
    }

    //Returns a course from the record
    public Course getCourse(String courseCode){
        for (Course outputCourse:compiledRecord){
            if(outputCourse.getCode().equals(courseCode)){
                return outputCourse;
            }
        }
        return null;
    }

    //Saving file only outputs files.
    public void saveChanges(OutputStream out) throws IOException {
        ObjectOutputStream outputFile = new ObjectOutputStream(out);
        outputFile.writeObject(personalRecord);
        out.close();
    }

    public void editCourse(String courseCode, Course.CourseBuilder courseData) {
        //Edit or add to compiled list of courses
        boolean matchFound = false;
        for (Course outputCourse:compiledRecord){
            if (outputCourse.getCode().equals(courseCode)){
                outputCourse.mergeData(courseData);
                matchFound = true;
                break;
            }
        }

        //Course is not part of existing records
        if (!matchFound)
        {
            //Assume it is additional if first time being added after adding template record files.
            courseData.additionalStatus(true);
            compiledRecord.add(new Course(courseData));
            personalRecord.add(new Course(courseData));
            return;
        }

        //Check if course exists in already edited courses.
        matchFound = false;
        for(Course personalData: personalRecord){
            if (personalData.getCode().equals(courseCode)) {
                personalData.mergeData(courseData);
                matchFound = true;
                break;
            }
        }
        if (!matchFound) personalRecord.add(new Course(courseData));
    }

    public void deleteCourse(String courseCode) {
        for (Course outputCourse : compiledRecord) {
            if (outputCourse.getCode().equals(courseCode)) {
                filterRecord.remove(outputCourse);
                personalRecord.remove(outputCourse);
                compiledRecord.remove(outputCourse);
                break;
            }
        }

    }
    public ArrayList<Course> FilterByYearAndTerm(int year, int term) {
        List<Course> filteredCourses = compiledRecord.stream()
                .filter(course -> course.getYear() == year && course.getTerm() == term)
                .toList();

        // Return the filtered list
        return new ArrayList<Course>(filteredCourses);
    }

    public ArrayList<Course> FilterByCurriculum(boolean isAdditional){
        List<Course> filteredCourses = getFilteredCourseList().stream()
                .filter(e -> e.isAdditional() == isAdditional)
                .toList();
        return new ArrayList<Course>(filteredCourses);
    }

    public ArrayList<Course> SortByGrade(boolean descending) {
        List<Course> filteredCourses;
        filteredCourses = getFilteredCourseList().stream().filter(e -> e.getGrade() > 0).sorted((o1, o2) -> {
            //If result is negative, returns o1.
            //If result is positive returns o2.
            if(!descending){
                return (int) (o1.getGrade() - o2.getGrade());
            }
                return (int) (o2.getGrade() - o1.getGrade());
        }).collect(Collectors.toList());

        // Return the filtered list
        return (ArrayList<Course>) filteredCourses;
    }

    public ArrayList<Course> SortByGPA(boolean descending){

        return compiledRecord;
    }

    public ArrayList<Course> SortByTitle(boolean descending) {
        List<Course> filteredCourses;
        filteredCourses = getFilteredCourseList().stream().filter(e -> e.getTitle() != null)
                .sorted(Comparator.comparing(Course::getTitle))
                .collect(Collectors.toList());

        if (descending) {
            filteredCourses = filteredCourses.reversed();
        }

        // Return the filtered list
        return new ArrayList<Course>(filteredCourses);
    }
}
