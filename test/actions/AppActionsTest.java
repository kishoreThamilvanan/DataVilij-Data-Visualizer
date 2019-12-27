/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kishore Thamilvanan
 */
public class AppActionsTest {

    public AppActionsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void saveFileTestI() throws Exception {

        String testString = "@testInstance  testlabel   1,1";
        FileOutputStream fileout = new FileOutputStream("testFile.tsd");
        ObjectOutputStream out = new ObjectOutputStream(fileout);
        out.writeObject(testString);
        fileout.close();
        FileInputStream openFile = new FileInputStream("testFile.tsd");
        ObjectInputStream in = new ObjectInputStream(openFile);
        String s = (String) in.readObject();
        assertEquals(testString, s);

    }

    @Test(expected = FileNotFoundException.class)
    public void SaveFileTestII() throws Exception {

        String savedFilePath = "";
        FileWriter fileWriter;
        fileWriter = new FileWriter(new File(savedFilePath));
        fileWriter.write("Test Data");
        fileWriter.close();

    }

}
