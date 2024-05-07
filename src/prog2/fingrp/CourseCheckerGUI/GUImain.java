package prog2.fingrp.CourseCheckerGUI;
import prog2.fingrp.Course;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Hashtable;

/*
 * 28/04/2024
 *
 * V1 : implemented
 * Working Window
 * Icons
 * ComboBox
 * Table
 *
 *    !! TO WORK ON:
 * Action Listeners
 *       - for action listener, change the 3 buttons' color from panelColor to outBtn on hover
 * Table Data (array list)
 * Second Table for Incomplete
 * Sorting Icons and Editable Icons
 * Font import
 * Combo Box Design
 * Finalization of Colors and Design
 * Preferrably incorporate use of Graphics Environment for dual monitor (fucks up the panel sizing though idk what to do abt that too bad lol)
 * Loops and Exception Handling
 *
 * V2
 * Added ComboBoxes for Filtering and Sorting functions
 * Added JRadioButton for Filtering by curriculum function
 * Converted JTable to DefaultTableModel for easier row manipulation
 * Added a key-value pair system for ComboBoxes
 * Added Accessors and Mutators
 * Added a column to indicate remarks (passed or failed)
 * Found Victoria's Secret
 *
 *
 * */

public class GUImain implements ActionListener, TableModelListener, ItemListener {

    public enum SORTING{
        DEFAULT,
        BY_TITLE_AZ,
        BY_TITLE_ZA,
        BY_GRADES_ASCENDING,
        BY_GRADES_DESCENDING
    }
    //Resources
    ImageIcon icon = new ImageIcon("res/Icons/MainIcon.png");
    ImageIcon add = new ImageIcon("res/Icons/AddIcon.png");
    ImageIcon archive = new ImageIcon("res/Icons/ArchiveIcon.png");
    ImageIcon info = new ImageIcon("res/Icons/InfoIcon.png");

    //J COMPS
    JFrame frame = new JFrame("Saint Louis University Checklist Manager");

    JTextField title = new JTextField("     My Checklist Manager");

    //Key and pair hashtables for combo boxes
    Hashtable<String, String> cbValues = new Hashtable<>(); //for Term and Year
    Hashtable<String, Integer> sortValues = new Hashtable<String, Integer>();

    JPanel btnPanel = new JPanel();
    JPanel basePanel = new JPanel();
    JPanel topPanel = new JPanel();
    JPanel navPanel = new JPanel();
    JPanel mainPanel = new JPanel(new BorderLayout());
    JPanel baseWrapperPanel = new JPanel(new GridBagLayout());

    JButton addCourse = new JButton(add);
    JButton archiveCourse = new JButton(archive);
    JButton appInfo = new JButton(info);

    JTable tbl;
    DefaultTableModel tableModel;

    //Sorting and Filters
    JComboBox termChoice = new JComboBox<>();
    JComboBox sortChoice = new JComboBox<>();

    //Edit and Add courses
    JFrame editFrame = new JFrame();
    JPanel panel = new JPanel( new GridLayout(2, 2) );
    //In order of Title, Course, Units, Grade, Year, Term, ...
    JTextField[] courseFields = {new JTextField(10), new JTextField(10), new JTextField(10),
            new JTextField(10), new JTextField(10), new JTextField(10)};

    JComboBox courseStatus = new JComboBox<>();
    JComboBox courseList = new JComboBox<>();

    JCheckBox isElective = new JCheckBox("Elective");

    //In curriculum
    JRadioButton curriculumCheckBox = new JRadioButton("Show additional courses only");

    // MEASUREMENTS
    Dimension btnSml = new Dimension(30,30);
    Dimension drpDm = new Dimension(330,30);
    Dimension srtDrpDm = new Dimension(250,30);
    Dimension minSize = new Dimension(1200,590);

    // MAIN COLORS
    Color bgColor = new Color(239, 243, 246);
    Color topColor = new Color(20, 48, 102);
    Color panelColor = new Color(248, 250, 251);
    Color lineColor = new Color(225, 227, 237);
    Color outButton = new Color(0xEEEFEF);
    Color fontColor = new Color(102, 102, 102);
    Font mainFont = new Font("Arial",1,16);

    //Layout Managers
    FlowLayout btnL = new FlowLayout();
    GridBagLayout navL = new GridBagLayout();
    public GUImain() {

        // top panel (blue strip at top)
        topPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        topPanel.setPreferredSize(new Dimension(1280, 30));
        topPanel.setBackground(topColor);

        // top add text
        topPanel.add(title);
        title.setBorder(null);
        title.setFont(mainFont);
        title.setForeground(bgColor);
        title.setBackground(topColor);

        //set buttons (inside button panel)
        btnPanel.setLayout(btnL);
        btnPanel.setSize(120,60);
        btnPanel.add(curriculumCheckBox);
        btnPanel.add(addCourse);
        btnPanel.add(archiveCourse);
        btnPanel.add(appInfo);
        btnPanel.setBackground(panelColor);

        //Plus sign
        addCourse.setPreferredSize(btnSml);
        addCourse.setBorder(new LineBorder(outButton, 2));
        addCourse.setBackground(bgColor);
        addCourse.setFocusPainted(false);
        //Box SIgn
        archiveCourse.setPreferredSize(btnSml);
        archiveCourse.setBorder(new LineBorder(outButton, 2));
        archiveCourse.setBackground(bgColor);
        archiveCourse.setFocusPainted(false);
        // i with circle sign
        appInfo.setPreferredSize(btnSml);
        appInfo.setBorder(new LineBorder(outButton, 2));
        appInfo.setBackground(bgColor);
        appInfo.setFocusPainted(false);

        //Year and term comboBox
        termChoice.setPreferredSize(drpDm);
        termChoice.setBackground(bgColor);
        termChoice.setEditable(false);
        termChoice.setFocusable(false);

        //Sorting comboBox
        sortChoice.setPreferredSize(srtDrpDm);
        sortChoice.setBackground(bgColor);
        sortChoice.setEditable(false);
        sortChoice.setFocusable(false);

        //Create drop-down menu and stores a key and value pair in a hash table
        //Potato code
        for (int year = 1; year < 5; year++) {
            for (int term = 1; term < 4; term++) {
                String yrSuffix;
                String tmSuffix;
                String choice;
                if(year == 4 && term == 3){
                    break;
                }
                yrSuffix = switch (year) {
                    case 1 -> "ST";
                    case 2 -> "ND";
                    case 3 -> "RD";
                    default -> "TH";
                };
                tmSuffix = switch (term) {
                    case 1 -> "ST";
                    case 2 -> "ND";
                    case 3 -> "RD";
                    default -> "TH";
                };
                if(term < 3){
                    choice = String.format("%d%s YEAR, %d%s SEMESTER", year, yrSuffix, term, tmSuffix);
                }else{
                    choice = String.format("%d%s YEAR, SHORT TERM", year, yrSuffix);
                }
                cbValues.put(choice, String.format("%d-%d", year, term));
                termChoice.addItem(choice);
            }
        }

        //Generate drop-down menu for status;
        for (Course.STATUS status : Course.STATUS.values()) {
            courseStatus.addItem(status);
        }

        //For the course list in the add/edit method
        courseList.addItem("Additional Course");
        //Creates the drop-down menu for sorting filter.
        for(GUImain.SORTING sorting: GUImain.SORTING.values()){
            sortChoice.addItem(sorting.name());
        }

        //Edit/Add course panel
        editFrame = new JFrame();
        editFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        editFrame.setLocationRelativeTo(null);



        // set nav panel (where buttons are)
        navPanel.setLayout(navL);
        navPanel.setPreferredSize(new Dimension(1280, 60));
        navPanel.setBackground(panelColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0.2;
        gbc.weighty = 0.333;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0,30,0,0);
        gbc.gridheight = 60;
        gbc.gridwidth = 120;
        gbc.anchor = GridBagConstraints.WEST;
        navPanel.add(termChoice, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        navPanel.add(sortChoice, gbc);

        //This just overwrites the constraints might bbe okay for now????...
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0,0,0,30);
        gbc.gridheight = 60;
        gbc.gridwidth = 120;
        navPanel.add(btnPanel, gbc);


        // base panel
        basePanel.setPreferredSize(new Dimension(1170, 440));
        basePanel.setBackground(panelColor);
        basePanel.setLayout(new GridBagLayout());


        // making table
        tableModel = new DefaultTableModel(new String[] {"Units", "Course Number", "Course Description", "Grade", "Grade Point Average", "Remarks"}, 0);
        tbl = new JTable(tableModel){
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(1140,400);
            }
        };


        //table specifications
        JScrollPane jp = new JScrollPane(tbl); // viewport
        jp.setViewportView(tbl);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(120);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(300);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(540);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(119);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(119);
        tbl.getColumnModel().getColumn(5).setPreferredWidth(200);
        tbl.setRowHeight(20);
        tbl.setEnabled(false);
        tbl.setGridColor(lineColor);
        tbl.setBackground(panelColor);
        tbl.setForeground(fontColor);
        //table font tbc when data is imoportwed...

        // basepanel
        GridBagConstraints gbbc = new GridBagConstraints();
        gbbc.gridx = 0;
        gbbc.gridy = 0;
        gbbc.weightx = 1.0;
        gbbc.fill = GridBagConstraints.HORIZONTAL;

        //main
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(navPanel, BorderLayout.CENTER);

        //wrapper
        baseWrapperPanel.setBackground(new Color(0xEFF3F6));
        baseWrapperPanel.add(basePanel);
        basePanel.add(jp);

        editFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        editFrame.setIconImage(icon.getImage());
        editFrame.setSize(1280, 720);
        editFrame.setMinimumSize(minSize);
        editFrame.getContentPane().setBackground(bgColor);
        editFrame.add(mainPanel, BorderLayout.NORTH);
        editFrame.add(baseWrapperPanel, BorderLayout.CENTER);
        editFrame.setLocationRelativeTo(null);
        editFrame.setResizable(true);
        editFrame.setVisible(true);

    }
    //Accessors and mutators
    public JTable getTable(){return tbl;}

    public JButton getAddCourse(){return addCourse;}

    public JButton getArchiveCourse(){return archiveCourse;}

    public JButton getAppInfo(){return appInfo;}

    public JRadioButton getCurriculumCheckBox(){return curriculumCheckBox;}

    public JTextField[] getEditFields(){return courseFields;}

    public JComboBox getCourseStatus(){return courseStatus;}

    public JComboBox getCourseList(){return courseList;}

    public JFrame getEditFrame(){return editFrame;}

    public JComboBox getTermChoice(){return termChoice;}

    public JComboBox getSortChoice(){return sortChoice;}

    public Hashtable getCbValues(){return cbValues;}

    public JCheckBox getIsElective(){return isElective;}
    public DefaultTableModel getTableModel(){return tableModel;}

    @Override
    public void actionPerformed(ActionEvent e) {}

    @Override
    public void tableChanged(TableModelEvent e) {}

    @Override
    public void itemStateChanged(ItemEvent e) {}

}
