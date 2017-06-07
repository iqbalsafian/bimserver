/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import java.io.IOException;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.ChannelConnectionException;

/**
 *
 * @author iqbal
 */
public class Display {
    private int currentMenu = 0, attemptedLogin = 0;
    private Config config = new Config();
    private boolean isLoggedIn = false;
    
    public void clearScreen() throws IOException, InterruptedException {
        final String os = System.getProperty("os.name");
        if (os.contains("Windows"))
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        else
            Runtime.getRuntime().exec("clear");
        System.out.print("\033[H\033[2J");
    }
    
    Display() throws IOException, ChannelConnectionException, InterruptedException, ServerException, UserException, BimServerClientException, ServiceException {
        showMenu();
    }
    
    public void showMenu() throws ChannelConnectionException, IOException, InterruptedException, UserException, ServerException, BimServerClientException, ServiceException {
        clearScreen();
        System.out.println("\t\t***************************************************");
        System.out.println("\t\t*                                                 *");
        System.out.println("\t\t*             *****  ***  *****  *****            *");
        System.out.println("\t\t*            *       *   *   *  *   *             *");
        System.out.println("\t\t*           *       *   *   *  *****              *");
        System.out.println("\t\t*          *       *   *   *  *   *               *");
        System.out.println("\t\t*         *****  ***  *****  *****                *");
        System.out.println("\t\t*                                                 *");
        System.out.println("\t\t***************************************************");
        System.out.println("");
        
        if (!this.isLoggedIn && attemptedLogin < 3)
        {
            this.isLoggedIn = config.getLogin();
            showMenu();
        }
        else {
            if (this.currentMenu == 0) {
                String returnedValue = config.getProjects();
                
                if (returnedValue.equals("0")) {
                    System.exit(0);
                } else if (returnedValue.equals("00")) {
                    this.currentMenu = 1;
                    showMenu();
                } else {
                    this.currentMenu = 2;
                    showMenu();
                }
            } else if (this.currentMenu == 1) {
                config.createProject();
                this.currentMenu = 0;
                showMenu();
            } else if (this.currentMenu == 2) {
                config.checkInFile();
                this.currentMenu = 0;
                showMenu();
            }
        }
    }
}
