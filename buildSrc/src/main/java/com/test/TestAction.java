package com.test;

import org.gradle.api.GradleException;
import org.gradle.workers.WorkAction;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.InputStream;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.filters.FilterConfiguration;

public abstract class TestAction
        implements WorkAction<TestTask.Parameters>
{

    @Override
    public void execute() {
        TestTask.Parameters p = getParameters();
        InputStream is = null;
        try {
            /*
             * Using XSLT and Saxon-HE seems to be working. I suspect there is something timing related to the fact OKAPI
             *     does parse the parameters and that it's a lot larger .jar file to process.
             *
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(p.getName().get());
            TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",
                    Thread.currentThread().getContextClassLoader());
            Templates templates = factory.newTemplates(new StreamSource(stream));
            Transformer xform = templates.newTransformer();
            xform.transform(new StreamSource(p.getInput().getAsFile().get()), new StreamResult(p.getOutput().getAsFile().get()));
            */
            MyXmlFilter filter = new MyXmlFilter();     // This is inside the ../test.jar file.
            IParameters pr = filter.getParameters();
            FilterConfiguration fc = filter.getConfigurations().get(0);
            is = filter.getClass().getResourceAsStream(fc.parametersLocation);

            /*
             * Set a breakpoint at the line below, and you'll find after doing multiple resume operations that the InputStream
             *    returned from getResourcesAsStream() has set its internal variables 'closeRequested' and 'closed' to be true.
             *    This may happen before parsing the file, or while parsing the file.
             *
             * This seems to yield a pretty reliable repro of the issue.
             */
            pr.load(is, false);
        } catch (Exception ex) {
            // Reason: Stream Closed
            throw new GradleException("Could not finish task, " + ex.getMessage(), ex);
        }
    }
}
