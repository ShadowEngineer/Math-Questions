package Testing;
import javax.swing.*;
import java.awt.*;

public class FrmBlank extends JFrame {
    private JPanel mainPanel = new JPanel();
    private JLabel lbl_Result = new JLabel();

    public FrmBlank() {
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 600));
        setTitle("Blank Frame");

        Container contentPane = getContentPane();
        contentPane.add(mainPanel);
        mainPanel.add(lbl_Result);
        lbl_Result.setText(LoginTestForm.username);
    }
}
