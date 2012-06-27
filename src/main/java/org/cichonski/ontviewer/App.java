/**
 * 
 */
package org.cichonski.ontviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cichonski.ontviewer.servlet.PathBuilder;
import org.cichonski.ontviewer.servlet.StaticPathBuilder;
import org.cichonski.ontviewer.servlet.View;
import org.cichonski.ontviewer.servlet.ViewBuilder;
import org.cichonski.ontviewer.servlet.ViewBuilder.ViewContainer;

/**
 * <p>
 * The alternative way of using this application. The main method of this class
 * will allow the use to generate the static pages with the same directory
 * structure and page naming as the dynamic web application running behind the
 * servlet code.
 * </p>
 * 
 * @author Paul Cichonski
 */
public class App {
    private static final Logger log = Logger.getLogger(App.class.getName());
    private static final String ONT_DIR_LOC = "ontologies/";
    private static final String PROPERTY_FILE_LOC ="ont-viewer.properties";
    private static final String PATH_SEPERATOR = System.getProperty("file.separator");

    // below arguments need to either come from properties file, or command line
    private static final String CONTEXT_PATH = "/vocabs";
    
    /* command line argument keys */
    private static final String STORAGE_DIRECTORY_KEY = "storageDir";
    
    /**
     * @param args - 
     */
    public static void main(String[] args) {
        try {
            final CommandLine cmd = parseArgs(args);
            final Properties props = new Properties();
            String storageDirectory = readProperty(cmd, STORAGE_DIRECTORY_KEY, true);
            props.load(new FileInputStream(getFile(PROPERTY_FILE_LOC)));
            String fileExtension = props.getProperty("file-extension");
            final PathBuilder pathBuilder = new StaticPathBuilder(CONTEXT_PATH, fileExtension);
            final ViewContainer viewContainer = ViewBuilder.buildViews(getFile(ONT_DIR_LOC), pathBuilder, props); 
            writeFile(new File(storageDirectory + CONTEXT_PATH.substring(1) + "/index.html"), viewContainer.getIndex());
            for (View view : viewContainer.getViews().values()){
                writeFile(new File(storageDirectory
                    + makePathOsAgnostic(view.getPath())), view.getView());
            }
        } catch (FileNotFoundException e){
            log.info("exception writing html to file, cannot proceed");
            throw new RuntimeException(e);
        } catch (IOException e){
            log.info("exception writing html to file, cannot proceed");
            throw new RuntimeException(e);
        } 
    }
    
    /**
     * 
     * @param cmd
     * @param name
     * @param required - the required value specified in the Options object for the specific property.
     * @return - the property value, this will return null if the property was not present and it was not required
     * @throws RuntimeException - if the property was required, but the cmd returned null
     */
    private static String readProperty(CommandLine cmd, String name, boolean required){
        if (required){
            String value = cmd.getOptionValue(name);
            if (value != null){
                return value;
            } else {
                throw new RuntimeException(name + "was required, but not present");
            }
        } else {
            if (cmd.hasOption(name)){
                return cmd.getOptionValue(name);
            } else {
                return null;
            }
        }
    }
    
    private static CommandLine parseArgs(String[] args){
        try {
            final CommandLineParser parser = new GnuParser();
            return parser.parse(buildOptions(), args);
        }catch (ParseException e){
            log.info("exception parsing command line arguments, cannot proceed");
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("static-access")
    private static Options buildOptions(){
        final Options options = new Options();
        final Option storageDir =
            OptionBuilder.isRequired().withArgName(STORAGE_DIRECTORY_KEY).hasArg().withDescription(
                "storage diractory for putting webpages").create("storageDir");
        options.addOption(storageDir);
        return options;
    }
    
    private static void writeFile(File file, String html){
        FileWriter writer = null;
        try {
            file.getParentFile().mkdirs();
            writer = new FileWriter(file);
            writer.write(html);
        } catch (IOException e){
            throw new RuntimeException(file.getName(), e);
        } finally {
            try {
                if (writer != null){writer.close();}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static File getFile(String fileLoc){
        File file = null;
        try {
            file = new File(App.class.getClassLoader().getResource(fileLoc).toURI());
        } catch (URISyntaxException e){
            log.log(Level.SEVERE, "could not find ontologies for view generation", e);
        }
        return file;
    }
    
    private static String makePathOsAgnostic(String path){
        final StringBuilder pathBuilder = new StringBuilder();
        final StringTokenizer tokenizer = new StringTokenizer(path, "/");
        boolean first = true;
        while (tokenizer.hasMoreElements()){
            if (!first){
                pathBuilder.append(PATH_SEPERATOR);
            } else {
                first = false;
            }
            pathBuilder.append(tokenizer.nextElement());
        }
        return pathBuilder.toString();
    }

}
