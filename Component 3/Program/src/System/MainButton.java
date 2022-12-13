package System;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainButton extends JButton {
    public MainButton (String t, Border b, Font f) {
        setMinimumSize(new Dimension(75,25));
        setPreferredSize(new Dimension(150,50));
        setMaximumSize(new Dimension(250,50));
        setText(t);
        setBorder(b);
        setFont(f);
    }
}

