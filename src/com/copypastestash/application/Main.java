package com.copypastestash.application;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.copypastestash.ui.CopyPasteStashUI;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CopyPasteStashUI app = new CopyPasteStashUI();
            }
        });
    }
}
