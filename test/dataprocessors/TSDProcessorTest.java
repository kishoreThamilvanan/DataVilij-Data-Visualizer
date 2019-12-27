/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataprocessors;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.geometry.Point2D;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Kishore Thamilvanan
 */
public class TSDProcessorTest {

    public TSDProcessorTest() {
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

    @Test()
    public void processStringTestI() throws Exception {
        String testString = "@name\ta\t1,2";
        Stream.of(testString.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    if (!list.get(0).startsWith("@")) {
                        try {
                            throw new Exception(list.get(0));
                        } catch (Exception ex) {
                            Logger.getLogger(TSDProcessorTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    String name = (list.get(0));
                    String label = list.get(1);
                    String[] pair = list.get(2).split(",");
                    Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));

                });

    }

    @Test(expected = Exception.class)
    public void processStringTestII() throws Exception {
        String testString = "Test String I";
        Stream.of(testString.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    if (!list.get(0).startsWith("@")) {
                        try {
                            throw new Exception(list.get(0));
                        } catch (Exception ex) {
                            Logger.getLogger(TSDProcessorTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    String name = (list.get(0));
                    String label = list.get(1);
                    String[] pair = list.get(2).split(",");
                    Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));

                });
    }

}
