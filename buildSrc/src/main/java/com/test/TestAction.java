package com.test;

import org.gradle.api.GradleException;
import org.gradle.workers.WorkAction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.broke.MyTest;
import org.xml.sax.InputSource;

public abstract class TestAction
        implements WorkAction<TestTask.Parameters>
{
    @Override
    public void execute() {
        try {
            // This is a class in the test.jar file, all it does is to return the name "rule.xsl" which also is
            //    embedded in the same .jar file.
            MyTest test = new MyTest();
            DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory1.newDocumentBuilder();
            builder.parse(new InputSource(test.getClass().getResourceAsStream(test.getLocation())));
        } catch (Exception ex) {
            // Reason: Stream Closed
            throw new GradleException("Could not finish task, " + ex.getMessage(), ex);
        }
    }
}
