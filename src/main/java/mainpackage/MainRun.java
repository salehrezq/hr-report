/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpackage;

import gui.LookAndFeelLisener;
import gui.Menu;
import gui.MenuItemsSalaryUpToDateMode;
import gui.Stage;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Saleh
 */
public class MainRun implements LookAndFeelLisener {

    private static Stage stage;
    private static Menu menu;
    private static JFrame frame;
    private Preferences prefs;

    public MainRun() {

    }

    public static JFrame getFrame() {
        return MainRun.frame;
    }

    private void createAndShowGUI() {

        prefs = Preferences.userNodeForPackage(Menu.class);

        stage = new Stage();
        stage.createStage();

        menu = new Menu();
        menu.createMenuBar();
        menu.setLookAndFeelListener(this);
        MenuItemsSalaryUpToDateMode itemSalaryUpToDateMode = menu.getMenuItemsSalaryUpToDateMode();
        itemSalaryUpToDateMode.addMenuItemSalaryUpToDateModeListener(stage.getSalaryTab().getSalaryUpToDayInSubjectMonth());
        stage.addEmployeeSelectedListener(itemSalaryUpToDateMode);

//        menu.
        //Create and set up the window.
        frame = new JFrame("UniGlobe HR");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.getContentPane().add(stage);
        frame.setJMenuBar(menu.getMenuBar());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private void setSavedLookAndFeel() {
        try {
            String savedTheme = prefs.get(Menu.THEME, UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(savedTheme);
            lookAndFeelChanged();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainRun mainRun = new MainRun();
            mainRun.createAndShowGUI();
            mainRun.setSavedLookAndFeel();
        });
    }

    @Override
    public void lookAndFeelChanged() {
        SwingUtilities.updateComponentTreeUI(frame);
    }

}
