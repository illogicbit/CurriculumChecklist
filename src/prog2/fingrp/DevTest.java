package prog2.fingrp;

import java.util.*;
import java.io.*;

//THIS IS A DEVELOPMENT CLASS. REMOVE AFTER USE.
public class DevTest {
    public static void main(String[] args) {
        CurriculumRecord record = null;
        File template = new File("res/CS_template.dat");
        File save = new File("res/record.dat");

        generateTemplate(new File("res/CurriculumTemplate.txt"));

        try {
            if (save.exists()) {
                record = new CurriculumRecord(new FileInputStream(template), new FileInputStream(save));
            } else {
                System.out.println("Cannot access record file.");
                record = new CurriculumRecord(new FileInputStream(template));
            }
        } catch (IOException e) {
            System.out.println("Error accessing file.");
        } catch (ClassNotFoundException e) {
            System.out.println("Error processing file.");
        }

        //Print out record test
        for (Course course : record.getCourseList()) {
            System.out.println(course);
        }

        //Filter test.

        System.out.println("-----");
        System.out.println("Filtering to Year 1");
        System.out.println("-----");
        Collection<Course> filteredList = record.getCourseList().stream()
                .filter(e -> e.getYear() == 1).toList();

        for (Course course : filteredList) {
            System.out.println(course);
        }

        //Edit test
        System.out.println("-----");
        System.out.println("Editing CS 111");
        System.out.println("-----");
        record.editCourse("CS 111", new Course.CourseBuilder()
                .code("CS 111")
                .title("Edit Test 1")
                .grade(75)
                .status(Course.STATUS.COMPLETE)
        );

        record.editCourse("ADD 1", new Course.CourseBuilder()
                .code("ADD 1")
                .title("Additional Test 1")
                .grade(80)
                .status(Course.STATUS.COMPLETE)
        );

//        try (FileOutputStream file = new FileOutputStream("res/record.dat")) {
//            record.saveChanges(file);
//        } catch (IOException e){
//            System.out.println("Error accessing file.");
//        }

        record.editCourse("CSE 1", new Course.CourseBuilder()
                .code("CSE 11")
                .title("Elective edit test")
                .grade(91)
                .status(Course.STATUS.COMPLETE)
        );

        for (Course course : record.getCourseList()) {
            System.out.println(course);
        }

        //Sort test.
        //For ease of filtering and sorting use Aggregate Operations on stream. Refer to Java docs.
        //Move filtering and sorting functionality to CurriculumRecord class.
        System.out.println("------\nSorting by Grade (Descending)\n-----");
        for (Course course : record.getCourseList().stream()
                .filter(e -> e.getGrade() > 0)
                .sorted((o1, o2) -> {
                    //If result is negative, returns o1.
                    //If result is positive returns o2.
                    return (int) (o1.getGrade() - o2.getGrade());
                }).toList()
        )
            System.out.println(course);

record.editCourse("CS 111", new Course.CourseBuilder().grade(900).title("DISCRETE STRUCTURES"));

        System.out.println("------\nSorting by Year and Term\n-----");
        for(int year = 1; year < 5; year++){
            for (int term = 1; term < 4; term++){
                for(Course course: record.FilterByYearAndTerm(year, term)) {
                    System.out.println(course);
                }
            }
            System.out.println("--------------");
        }
        //Test to filter and sort simutansimultaneously
        System.out.println("------\nSorting by Curriculum\n-----");
        record.editCourse("ADD 111", new Course.CourseBuilder().title("Test 1").year(1).term(1));
        record.setFilter(record.FilterByYearAndTerm(1, 1));
        for (Course course: record.SortByTitle(false)){
            System.out.println(course);
        }
        
        System.out.println("------\nSorting by Title\n-----");
        for (Course course: record.SortByTitle(false)){
            System.out.println(course);
        }
    }

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
