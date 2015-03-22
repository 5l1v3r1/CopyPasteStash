package com.copypastestash.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.copypastestash.persistence.StashPersistence;

public class CopyPasteStashUI implements ItemListener {
    private JFrame frame = new JFrame("Copy Paste Stash");
    private JPanel cards = new JPanel(new CardLayout());;

    private JTextField keyText = new JTextField(20);
    private JTextField valueText = new JTextField(20);

    private JTextField keySearchField = new JTextField(20);
    private JTextField matchingKeyField = new JTextField("");
    private JList<String> matchList = new JList<String>();

    private Timer superHackyExitTimer;
    private String matchingString = null;

    private StashPersistence persistence = new StashPersistence();

    public CopyPasteStashUI() {
        setupComponents();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent winEvt) {
                System.exit(0);
            }

            @Override
            public void windowOpened( WindowEvent e) {
                keySearchField.requestFocus();
            }
        });

        this.addComponentsToPane(frame.getContentPane());

        frame.pack();
        frame.setVisible(true);
    }

    public void addComponentsToPane(Container pane) {
        JPanel comboBoxPane = new JPanel();
        String comboBoxItems[] = { "Get", "Put" };
        JComboBox<String> cb = new JComboBox<String>(comboBoxItems);
        cb.setEditable(false);
        cb.addItemListener(this);
        comboBoxPane.add(cb);

        layoutGetCard(cards);
        layoutPutCard(cards);

        pane.add(comboBoxPane, BorderLayout.PAGE_START);
        pane.add(cards, BorderLayout.CENTER);
    }

    private void layoutGetCard(Container pane) {
        JPanel getCard = new JPanel();
        LayoutManager layout = new GridBagLayout();
        getCard.setLayout(layout);

        // Wrap the match list in a scroll pane
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(matchList);

        keySearchField.setMinimumSize(keySearchField.getPreferredSize());
        matchingKeyField.setPreferredSize(keySearchField.getPreferredSize());
        matchingKeyField.setMinimumSize(keySearchField.getPreferredSize());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        getCard.add(keySearchField, c);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 0;
        c2.gridy = 1;
        getCard.add(scrollPane, c2);

        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 0;
        c3.gridy = 2;
        getCard.add(matchingKeyField, c3);

        pane.add(getCard, "Get");
    }

    private void layoutPutCard(Container pane) {
        JPanel putCard = new JPanel();
        putCard.setLayout(new BoxLayout(putCard, BoxLayout.Y_AXIS));

        putCard.add(keyText);
        putCard.add(valueText);

        pane.add(putCard, "Put");
    }

    private void setupComponents() {
        // Populate list of possible keys
        Set<String> keySet = persistence.getStash().keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        matchList.setListData(keyArray);

        // Update matches when user types something into the key search box
        keySearchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateMatches() {
                List<String> matches = persistence.getMatchingStrings(keySearchField.getText());
                matchList.setListData(matches.toArray(new String[matches.size()]));

                if (matches.size() == 1) {
                    matchingString = persistence.getStash().get(matches.get(0));
                    matchingKeyField.setText( matchingString );
                } else {
                    matchingString = null;
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateMatches();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateMatches();

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateMatches();
            }
        });


        // Save new key -> value on put page if ctrl + c and on the value box
        keySearchField.getInputMap().put(KeyStroke.getKeyStroke("control C"),
                "copyAndExit");
        keySearchField.getActionMap().put("copyAndExit", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (matchingString != null) {
                    StringSelection stringSelection = new StringSelection (matchingString);
                    Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
                    clpbrd.setContents (stringSelection, null);

                    // Aweful aweful hack because if we just exit the app, Ubuntu
                    // will lose the clipboard content we just got.
                    // https://wiki.ubuntu.com/ClipboardPersistence
                    // Hide frame and exit in 5 minutes.
                    frame.setVisible(false);
                    superHackyExitTimer = new Timer(3000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.exit(0);
                        }
                    });
                    superHackyExitTimer.start();
                }
            }
        });

        // Save new key -> value on put page if ctrl + S and on the value box
        valueText.getInputMap().put(KeyStroke.getKeyStroke("control S"),
                "saveAction");
        valueText.getActionMap().put("saveAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Saving");
                persistence.putItem(keyText.getText(), valueText.getText());
                persistence.writeStash();
            }
        });
    }

    @Override
    public void itemStateChanged(ItemEvent evt) {
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, (String) evt.getItem());
    }

}
