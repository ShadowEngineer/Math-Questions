package System;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ScatterGraph extends GraphicalDisplayWindow {
    //Class attributes.
    private final int LABEL_HEIGHT = 10;
    private final int LABEL_WIDTH = 40;
    private final int NOTCH_HEIGHT = 5;
    private final int NOTCH_WIDTH = 1;
    private final int POINT_SIZE = 3;

    //Internal enumerations.
    public enum Axis {
        X,
        Y
    }

    //Constructor.
    public ScatterGraph(Main mainClass, String title, String xLabel, String yLabel) {
        super(mainClass, title, xLabel, yLabel);

        //Creating a listener for the window to detect when it closes.
        WindowListener listener = new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                ScatterGraph.this.MainClass.NumberOfOpenWindows -= 1;
            }
        };
        addWindowListener(listener);
    }

    //Method to add a point to the scatter graph.
    public void AddPoint(float xValue, float yValue) {
        JLabel point = new JLabel();
        point.setName("GraphPoint");
        point.setBackground(new Color(255,0,0));
        point.setBounds(
                (int) (100 + Visualiser.lerp(xValue, 0, WINDOW_WIDTH - 100)) - POINT_SIZE/2,
                (int) (WINDOW_HEIGHT - 100 - Visualiser.lerp(yValue, 0, WINDOW_HEIGHT - 100)) - POINT_SIZE/2,
                POINT_SIZE,
                POINT_SIZE
        );
        point.setOpaque(true);
        this.getContentPane().add(point);
    }

    //Method to add increment lines to the axes.
    private void AddAxisLabel(Axis axis, double t, float value) {
        int position;
        JLabel lbl_Notch = new JLabel();
        lbl_Notch.setName("ReversibleColour");
        lbl_Notch.setOpaque(true);
        lbl_Notch.setBackground(new Color(0,0,0));

        JLabel lbl_Label = new JLabel();
        lbl_Label.setText("" + value);
        lbl_Label.setFont(LABEL_FONT);
        lbl_Label.setOpaque(false);
        if (axis == Axis.X) {
            position = (int) Visualiser.lerp(t, 100, 600);
            lbl_Label.setHorizontalAlignment(SwingConstants.CENTER);
            lbl_Notch.setBounds(position, 500, NOTCH_WIDTH, NOTCH_HEIGHT);
            lbl_Label.setBounds(position - LABEL_WIDTH/2, 500 + NOTCH_HEIGHT, LABEL_WIDTH, LABEL_HEIGHT);
        } else if (axis == Axis.Y) {
            position = (int) Visualiser.lerp(t, 500, 0);
            lbl_Label.setHorizontalAlignment(SwingConstants.RIGHT);
            lbl_Notch.setBounds(100 - NOTCH_HEIGHT, position, NOTCH_HEIGHT, NOTCH_WIDTH);
            lbl_Label.setBounds(100 - LABEL_WIDTH - NOTCH_HEIGHT, position - LABEL_HEIGHT/2, LABEL_WIDTH, LABEL_HEIGHT);
        }

        this.getContentPane().add(lbl_Notch);
        this.getContentPane().add(lbl_Label);
    }

    //Method to add an axis.
    public void AddAxis(Axis axis, float minValue, float maxValue, int divisions) {
        //With n divisions, there will always be n + 1 labels, hence i <= divisions.
        for (int i = 0; i <= divisions; i++) {
            float value = (float) Visualiser.lerp(((double) i)/divisions, minValue, maxValue);
            AddAxisLabel(axis, (double) i / divisions, value);
        }
    }
}
