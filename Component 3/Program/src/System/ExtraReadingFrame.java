package System;

import javax.swing.*;
import javax.swing.text.*;

import org.json.simple.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ExtraReadingFrame extends JFrame {
    static int HEIGHT = 600;
    static int WIDTH = 1000;

    //Class attributes
    private Main MainClass;
    private JSONObject TopicData;
    private JSONArray ExtraReadingData;
    private TopicPanel OriginPanel;

    //Swing components.
    private JScrollPane scpn_ScrollPane;
    private JPanel pnl_ReadingList;
    private JLabel lbl_TopicName;
    private JTextPane tp_Description;

    //Constructor.
    public ExtraReadingFrame(Main mainClass, JSONObject topicData, TopicPanel originPanel) {
        this.MainClass = mainClass;
        this.TopicData = topicData;
        this.OriginPanel = originPanel;

        //Window parameters.
        Container contentPane = getContentPane();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(true);
        setTitle("Extra Reading Suggestions");

        //Creating a listener for the window to detect when it closes.
        WindowListener listener = new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                originPanel.setReadingFrameOpen(false);
            }
        };
        addWindowListener(listener);

        Dimension WindowSize = getToolkit().getScreenSize();
        double WindowHeight = WindowSize.getHeight();
        double WindowWidth = WindowSize.getWidth();
        setBounds((int) (WindowWidth - WIDTH) / 2, (int) (WindowHeight - HEIGHT - 25) / 2, WIDTH, HEIGHT + 25);

        //Creating swing components.
        pnl_ReadingList = new JPanel();
        pnl_ReadingList.setLayout(new GridLayout(0, 1, 0, 0));
        pnl_ReadingList.setOpaque(false);
        pnl_ReadingList.setBorder(null);

        scpn_ScrollPane = new JScrollPane(pnl_ReadingList);
        scpn_ScrollPane.setOpaque(false);
        scpn_ScrollPane.setBorder(this.MainClass._jPanelBorder);

        JScrollBar verticalScrollBar = new JScrollBar();
        verticalScrollBar.setEnabled(true);
        verticalScrollBar.setVisible(true);
        verticalScrollBar.setOpaque(false);
        verticalScrollBar.setUnitIncrement(25);
        scpn_ScrollPane.setVerticalScrollBar(verticalScrollBar);

        lbl_TopicName = new JLabel((String) topicData.get("TopicName"));
        lbl_TopicName.setFont(new Font(Font.SERIF, Font.PLAIN, 25));

        //Using a JTextPane instead of a JTextArea because it allows for centered text.
        String description = "Below is a list of links to use to learn more about this topic!";
        tp_Description = new JTextPane();
        tp_Description.setText(description);
        tp_Description.setFont(new Font(Font.SERIF, Font.PLAIN, 15));
        tp_Description.setMaximumSize(new Dimension(WIDTH, Math.min(75, HEIGHT / 5)));
        tp_Description.setEditable(false);
        tp_Description.setFocusable(false);
        tp_Description.setVisible(true);

        //This is a rather "hacky" way of centering text in a JTextPane.
        StyledDocument documentStyle = tp_Description.getStyledDocument();
        SimpleAttributeSet centerAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER);
        documentStyle.setParagraphAttributes(0, documentStyle.getLength(), centerAttribute, false);

        //Adding components and configuring layout.
        GroupLayout layout = new GroupLayout(contentPane);
        layout.setHonorsVisibility(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(false);

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, WIDTH, WIDTH)
                                .addComponent(lbl_TopicName)
                                .addGap(0, WIDTH, WIDTH)
                        )
                        .addComponent(tp_Description)
                        .addComponent(scpn_ScrollPane)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(lbl_TopicName)
                        .addComponent(tp_Description)
                        .addComponent(scpn_ScrollPane)

        );

        contentPane.setLayout(layout);

        //Adding listings.
        JSONArray extraReading = (JSONArray) this.TopicData.get("UsefulReading");
        for (Object obj : extraReading) {
            JSONObject readingObject = (JSONObject) obj;
            String link = (String) readingObject.get("link");
            String message = (String) readingObject.get("message");

            //Creating Swing components.
            JPanel pnl_NewPanel = new JPanel();
            JLabel lbl_Link = new JLabel();
            JLabel lbl_Message = new JLabel();

            pnl_NewPanel.setMaximumSize(new Dimension(0, 50));

            //Using a mouse listener, and accessing the user's Desktop and default browser to use the hyperlink.
            lbl_Link.setText("<html><a href=" + link + ">" + link + "</a></html>");
            lbl_Link.setFont(this.MainClass._universalSecondaryFont);
            lbl_Link.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    try {
                        Desktop.getDesktop().browse(new URI(link));
                    } catch(IOException | URISyntaxException error) {
                        error.printStackTrace();
                    }
                }
            });

            lbl_Message.setText(message);
            lbl_Message.setFont(new Font(Font.SERIF, Font.ITALIC, 13));

            //Laying out and adding components.
            GroupLayout newGroupLayout = new GroupLayout(pnl_NewPanel);
            newGroupLayout.setAutoCreateGaps(false);
            newGroupLayout.setAutoCreateContainerGaps(false);
            newGroupLayout.setHorizontalGroup(newGroupLayout.createParallelGroup()
                    .addGroup(newGroupLayout.createSequentialGroup()
                            .addGap(0, WIDTH, WIDTH)
                            .addComponent(lbl_Message)
                            .addGap(0, WIDTH, WIDTH)
                    )
                    .addGroup(newGroupLayout.createSequentialGroup()
                            .addGap(0, WIDTH, WIDTH)
                            .addComponent(lbl_Link)
                            .addGap(0, WIDTH, WIDTH)
                    )
            );
            newGroupLayout.setVerticalGroup(newGroupLayout.createSequentialGroup()
                    .addComponent(lbl_Message)
                    .addComponent(lbl_Link)
            );
            pnl_NewPanel.setLayout(newGroupLayout);

            pnl_ReadingList.add(pnl_NewPanel);
        }

        //Updating UI for scrollpane viewport.
        pnl_ReadingList.setPreferredSize(new Dimension(0, pnl_ReadingList.getComponents().length * 50));
        pnl_ReadingList.revalidate();

        //Colouring.
        this.MainClass.CurrentVisualiserInstance.ChangeColours(
                this.getRootPane(),
                Main.ArrayToColour(this.MainClass.PrimaryColour),
                Main.ArrayToColour(this.MainClass.SecondaryColour)
        );
    }
}
