package Testing;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LoginTestForm extends JFrame {

    private JPanel mainPanel = new JPanel();
    private JLabel label = new JLabel();
    private JTextField textEntry = new JTextField();
    private JButton submitButton = new JButton();

    static String username = "";

    private boolean activated = false;

    public LoginTestForm() {
        setTitle("Testing");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        setMinimumSize(new Dimension(500, 500));

        Container contentPane = getContentPane();

        contentPane.add(mainPanel);
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        mainPanel.setMinimumSize(new Dimension(mainPanel.getParent().getWidth(), mainPanel.getParent().getHeight()));
        mainPanel.setPreferredSize(new Dimension(mainPanel.getParent().getWidth(), mainPanel.getParent().getHeight()));
        mainPanel.setMaximumSize(new Dimension(mainPanel.getParent().getWidth(), mainPanel.getParent().getHeight()));

        mainPanel.add(label);
        label.setText("Enter your name.");
        label.setVisible(true);

        mainPanel.add(textEntry);
        textEntry.setPreferredSize(new Dimension(200, 25));

        mainPanel.add(submitButton);
        submitButton.setText("SUBMIT");
        submitButton.setActionCommand("ree");
        submitButton.setPreferredSize(new Dimension(100, 25));
        submitButton.addActionListener(new ActionListener() {
            @Override       //these are technically not needed here
            public void actionPerformed(ActionEvent actionEvent) {
                if (activated == false) {
                    username = textEntry.getText();
                    if (username.length() >= 6) {
                        activated = true;
                        FrmBlank newForm = new FrmBlank();
                        newForm.setVisible(true);
                        newForm.addWindowListener(new WindowAdapter() {
                            @Override           //and here. It works fine without them
                            public void windowClosing(WindowEvent windowEvent) {
                                System.out.println(windowEvent.paramString());
                                activated = false;
                            }
                        });
                    } else {
                        System.out.println("Incorrect username length.");
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        LoginTestForm testForm = new LoginTestForm();
    }
}
