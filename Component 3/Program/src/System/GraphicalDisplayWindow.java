package System;

import javax.swing.*;
import java.awt.*;

public class GraphicalDisplayWindow extends JFrame {
    //Class attributes.
    Main MainClass;
    String Title;
    String XAxisLabel;
    String YAxisLabel;
    int WINDOW_WIDTH = 600;
    int WINDOW_HEIGHT = 600;
    int AXIS_LABEL_HEIGHT = 20;
    int AXIS_LABEL_WIDTH = 500;
    Font LABEL_FONT = new Font(Font.MONOSPACED, Font.TRUETYPE_FONT, 8);
    private final int[] GLOBAL_GRAPH_OFFSET = {-25, 10};

    //Swing components.
    JLabel lbl_XAxisLine;
    JLabel lbl_YAxisLine;
    JLabel lbl_XAxis;
    JTextArea ta_YAxis;

    //Constructor
    public GraphicalDisplayWindow(Main mainClass, String title, String xLabel, String yLabel) {
        //Class attribute assignment.
        this.MainClass = mainClass;
        this.Title = title;
        this.XAxisLabel = xLabel;
        this.YAxisLabel = yLabel;

        //Window parameters.
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        //Window size parameters.
        Dimension WindowSize = getToolkit().getScreenSize();    //from the AWT package
        double WindowHeight = WindowSize.getHeight();
        double WindowWidth = WindowSize.getWidth();
        setBounds(
                (int) WindowWidth / 2 - WINDOW_WIDTH / 2,
                (int) WindowHeight / 2 - WINDOW_HEIGHT / 2,
                WINDOW_WIDTH,
                WINDOW_HEIGHT + 25
        );
        setTitle(title);
        setVisible(true);

        //Configuring JFrame
        setLayout(null);
        Container contentPane = getContentPane();
        contentPane.setBackground(new Color(255, 255, 255));

        //Creating axes.
        contentPane.add(lbl_XAxisLine = new JLabel());
        contentPane.add(lbl_YAxisLine = new JLabel());

        lbl_XAxisLine.setBackground(new Color(0,0,0));
        lbl_XAxisLine.setName("ReversibleColour");
        lbl_XAxisLine.setBounds(100, WINDOW_HEIGHT - 100, WINDOW_WIDTH - 100, 1);
        lbl_XAxisLine.setOpaque(true);

        lbl_YAxisLine.setBackground(new Color(0,0,0));
        lbl_YAxisLine.setName("ReversibleColour");
        lbl_YAxisLine.setBounds(100, 0, 1, WINDOW_HEIGHT - 100);
        lbl_YAxisLine.setOpaque(true);

        //Creating axis labels.
        contentPane.add(lbl_XAxis = new JLabel());
        contentPane.add(ta_YAxis = new JTextArea());

        lbl_XAxis.setBounds(
                (WINDOW_WIDTH - AXIS_LABEL_WIDTH)/2,
                WINDOW_HEIGHT - 50 - AXIS_LABEL_HEIGHT /2,
                AXIS_LABEL_WIDTH,
                AXIS_LABEL_HEIGHT
        );
        ta_YAxis.setBounds(
                (100 - AXIS_LABEL_HEIGHT)/2,
                (WINDOW_HEIGHT - yLabel.length() * 10 - 100)/2,
                AXIS_LABEL_HEIGHT,
                yLabel.length() * 20
        );

        lbl_XAxis.setText(xLabel);
        lbl_XAxis.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_XAxis.setFont(mainClass._universalSecondaryFont);

        ta_YAxis.setText(verticaliseString(yLabel));
        ta_YAxis.setColumns(1);
        ta_YAxis.setRows(yLabel.length());
        ta_YAxis.setFocusable(false);
        ta_YAxis.setEditable(false);
        ta_YAxis.setOpaque(false);
        ta_YAxis.setFont(mainClass._universalSecondaryFont);

    }

    //Small method to add newline characters between the characters of a standard string.
    public String verticaliseString(String str) {
        String newString = "";
        char[] splitString = str.toCharArray();

        for (int i = 0; i < splitString.length; i++) {
            newString += splitString[i] + ((i + 1 == splitString.length) ? "" : "\n");
        }

        return newString;
    }

    //Method to globally offset the entire graph window's components.
    //*SHOULD ONLY BE CALLED AFTER AXES AND POINTS HAVE BEEN PLOTTED*.
    public void OffsetGraph() {
        for (Component comp : this.getContentPane().getComponents()) {
            Rectangle componentBounds = comp.getBounds();
            componentBounds.x += GLOBAL_GRAPH_OFFSET[0];
            componentBounds.y += GLOBAL_GRAPH_OFFSET[1];
            comp.setBounds(componentBounds);
        }
    }
}

