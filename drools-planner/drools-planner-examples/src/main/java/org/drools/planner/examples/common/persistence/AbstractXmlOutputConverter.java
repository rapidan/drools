package org.drools.planner.examples.common.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.drools.planner.core.solution.Solution;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractXmlOutputConverter extends AbstractOutputConverter {

    protected static final String DEFAULT_OUTPUT_FILE_SUFFIX = ".xml";

    protected AbstractXmlOutputConverter(SolutionDao solutionDao) {
        super(solutionDao);
    }

    protected String getOutputFileSuffix() {
        return DEFAULT_OUTPUT_FILE_SUFFIX;
    }

    public abstract XmlOutputBuilder createXmlOutputBuilder();

    public void writeSolution(Solution solution, File outputFile) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            Document document = new Document();
            XmlOutputBuilder xmlOutputBuilder = createXmlOutputBuilder();
            xmlOutputBuilder.setDocument(document);
            xmlOutputBuilder.setSolution(solution);
            xmlOutputBuilder.writeSolution();
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(document, out);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write the file (" + outputFile.getName() + ").", e);
        } catch (JDOMException e) {
            throw new IllegalArgumentException("Could not format the XML file (" + outputFile.getName() + ").", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public abstract class XmlOutputBuilder {

        protected Document document;

        public void setDocument(Document document) {
            this.document = document;
        }

        public abstract void setSolution(Solution solution);

        public abstract void writeSolution() throws IOException, JDOMException;

        // ************************************************************************
        // Helper methods
        // ************************************************************************

    }

}
