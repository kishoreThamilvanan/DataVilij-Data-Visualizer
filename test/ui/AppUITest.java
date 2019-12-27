/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import com.sun.media.sound.InvalidDataException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Kishore Thamilvanan
 */
public class AppUITest {

    public AppUITest() {
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
    public void AlgorithmConfigurationParametersTestI() throws InvalidDataException {
        // checking zero or null values 
        
         int maxIteration = 0,updateIntervals = 0, clusters = 0,testValueI = 0,
                testValueII = 0, testValueIII = 0;
       

        if (testValueI == 0) {
            maxIteration = testValueI;
        } else if (testValueII == 0) {
            updateIntervals = testValueII;
        } else if (testValueIII == 0) {
            clusters = testValueIII;
        } else {
            throw new InvalidDataException();
        }
        assertEquals(maxIteration, testValueI);
        assertEquals(updateIntervals, testValueII);
        assertEquals(clusters, testValueIII);

    }

    @Test
    public void AlgorithmConfigurationParametersTestII() throws InvalidDataException {
        // checking for the positive values 
        
        int maxIteration = 0,updateIntervals = 0, clusters = 0,testValueI = 1,
                testValueII = 2, testValueIII = 3;
       

        if (testValueI > 0) {
            maxIteration = testValueI;
        } else if (testValueII > 0) {
            updateIntervals = testValueII;
        } else if (testValueIII > 0) {
            clusters = testValueIII;
        } else {
            throw new InvalidDataException();
        }
    }

    @Test(expected = Exception.class)
    public void AlgorithmConfigurationParametersTestIII() throws Exception {
        // checking for the negative values
        int maxIteration = 0,updateIntervals = 0, clusters = 0,testValueI = -1,
                testValueII = -2, testValueIII = -3;
       

        if (testValueI > 0) {
            maxIteration = testValueI;
        } else if (testValueII > 0) {
            updateIntervals = testValueII;
        } else if (testValueIII > 0) {
            clusters = testValueIII;
        } else {
            throw new InvalidDataException();
        }

    }

    @Test(expected = Exception.class)
    public void AlgorithmConfigurationParametersTestIV() throws Exception {
        // checking for the invalid string values.
        
        int maxIteration = 0, intervals = 0, clusters = 0;
        String test1 = "test String I", test2 = "test String II", test3 = "test String III";

        maxIteration = Integer.parseInt(test1);
        intervals = Integer.parseInt(test2);
        clusters = Integer.parseInt(test3);
    }

}
