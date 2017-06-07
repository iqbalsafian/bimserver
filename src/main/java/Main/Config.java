/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.bimserver.client.BimServerClient;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.plugins.services.Flow;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.shared.interfaces.ServiceInterface;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServiceException;

/**
 *
 * @author iqbal
 */
public class Config {
    private JSONParser jsonParser = new JSONParser();
    private String[] configData = new String[2];
    private BimServerClient client;
    private boolean isLoggedIn = false;
    private long selectedProjectId = 0;
    
    Config() {
        try {
            Object obj = this.jsonParser.parse(new FileReader("config/config"));
 
            JSONObject jsonObject = (JSONObject) obj;
        
            this.configData[0] = (String) jsonObject.get("hostServer");
            this.configData[1] = (String) jsonObject.get("userEmail");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean getLoginStatus() {
        return this.isLoggedIn;
    }
    
    public boolean getLogin() throws InterruptedException, BimServerClientException, ServiceException, ChannelConnectionException {
        JsonBimServerClientFactory factory = new JsonBimServerClientFactory(this.configData[0]);
        
            Console console = System.console();
            String userPassword = new String(console.readPassword("Please provide the password for " + this.configData[1] + ": "));
            System.out.println("\t\tConnecting to the host server @ " + this.configData[0]);
            BimServerClient client = factory.create(new UsernamePasswordAuthenticationInfo(this.configData[1], userPassword));
        
                this.client = client;
                System.out.println("\t\tConnected");
                Thread.sleep(500);
                return true;
        
    }
    
    public String getProjects() throws UserException, ServerException {
        ServiceInterface serviceInterface = client.getServiceInterface();
        List <SProject> allProjects = serviceInterface.getAllProjects(true, false);
        long[] projectIDs = new long[allProjects.size()];
        
        if (allProjects != null) {
            int i = 0;
            System.out.println("\t\tProject List: ");
            
            for (SProject sp: allProjects) {
                projectIDs[i] = sp.getOid();
                System.out.println("\t\t  " + (i+1) + " - " + sp.getName() + " (" + sp.getCreatedDate() + ")");
                i++;
            }
            System.out.println("\n\t\tCheckin IFC file by selecting project's index number\n\t\t\t\tOR");
        } else {
            System.out.println("\n\t\tNo project found");
        }
        System.out.println("\t\t00 - Create New Project");
        System.out.println("\t\t0 - Exit Application");
        
        Console console = System.console();
        String projectChoice = console.readLine("\t\tYour choice: ");
        
        if (!projectChoice.equals("0") && !projectChoice.equals("00"))
        {
            this.selectedProjectId = projectIDs[Integer.parseInt(projectChoice)-1];
        }
        
        return projectChoice;
    }
    
    public void createProject() throws InterruptedException, ServerException, UserException {
        ServiceInterface serviceInterface = this.client.getServiceInterface();
        String ifcType = "ifc2x3tc1";
        
        System.out.println("\t\tPlease fill in the particulars below:");
        
        Console console = System.console();
        String projectName = console.readLine("\t\t  Project Name: ");
        String projectType = console.readLine("\t\t  Schema:- (1)=ifc2x3tc1 (2)=ifc4: ");
        
        if (Integer.parseInt(projectType) == 2)
            ifcType = "ifc4";
        
	SProject project = serviceInterface.addProject(projectName, ifcType);
        
        System.out.println("\t\tNew project created - ID: " + project.getOid());
        Thread.sleep(1000);
    }
    
    public void checkInFile() throws ServerException, UserException, InterruptedException, IOException {
        ServiceInterface serviceInterface = this.client.getServiceInterface();
        SProject sp = serviceInterface.getProjectByPoid(this.selectedProjectId);
        
        System.out.println("\t\tProject: " + sp.getName());
        System.out.print("\t\tCopy the IFC into ifc folder then press enter");
        
        Console console = System.console();
        String nothing = console.readLine();
        
        Path currentRelativePath = Paths.get("ifc");
        String s = currentRelativePath.toAbsolutePath().toString();
        
        File dir = new File(s);
        File files[] = dir.listFiles();
        String[] filesArray = new String[files.length];
        int i = 0;
        
        if (files.length == 0) {
            System.out.println("\t\tDirectory in " + s + " is empty.");
            System.out.print("\t\tCopy the intended file in the dir and try again later.");
            
        } else {
            System.out.println("\t\tSelect the file you wish to upload by typing its index number.");
            System.out.println("\t\tOr 0 to cancel.");
            for (File aFile: files) {
                files[i] = aFile;
                filesArray[i] = aFile.toString();
                
                System.out.println("\t\t " + (i+1) + " - " + aFile.getName()/* + " - " + aFile.length()*/);
                i++;
            }
            
            String fileChoice = console.readLine("\t\tYour choice: ");
            
            if (fileChoice.equals("0")) {
                System.out.println("\t\tYou have cancelled the operation.");
            } else {
                System.out.println("\t\tSelected file directory: " + files[Integer.parseInt(fileChoice)-1].toPath());
                String comment = console.readLine("\t\tInsert the file description: ");
                Path IfcFile = files[Integer.parseInt(fileChoice)-1].toPath();
                System.out.println("Uploading the file... Please wait until it's finished.");
                SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", this.selectedProjectId);
                this.client.checkin(this.selectedProjectId, comment, deserializer.getOid(), false, Flow.SYNC, IfcFile);
                System.out.println("File uploaded.");
            }
        }
        Thread.sleep(500);
    }
}
