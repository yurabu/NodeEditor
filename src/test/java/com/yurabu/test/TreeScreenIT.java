package com.yurabu.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.TableElement;

public class TreeScreenIT extends TestBenchTestCase {
	   @Before
	   public void setUp() throws Exception {
	       setDriver(new FirefoxDriver());
	   }

	   @After
	   public void tearDown() throws Exception {
	       getDriver().quit();
	   }
	   
	   @Test
	   public void testElements() {
	       getDriver().get("http://localhost:8080/");
	       Assert.assertTrue($(TableElement.class).exists());
	   }
}
