package dataprocessors;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.templates.ApplicationTemplate;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the concrete application-specific implementation of the data
 * component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

   

    private TSDProcessor processor;

    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

     public TSDProcessor getProcessor() {
        return processor;
    }
     
    @Override
    public void loadData(Path dataFilePath) {
        try (PrintWriter w = new PrintWriter(Files.newOutputStream(dataFilePath))){
            w.write(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
        } catch(IOException ex){
                        Logger.getLogger(AppData.class.getName()).log(Level.SEVERE, null, ex);

        }
       
    }

    public void loadData(String dataString){
            
            ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
        try {
            processor.processString(dataString);
        } catch (Exception ex) {
            Logger.getLogger(AppData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
    }

    @Override
    public void saveData(Path dataFilePath) {
        try (PrintWriter w = new PrintWriter(Files.newOutputStream(dataFilePath))){
            w.write(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
        } catch(IOException ex){
                        Logger.getLogger(AppData.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
}
