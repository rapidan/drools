package org.drools.integration.console.forms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.jboss.bpm.console.server.plugin.FormAuthorityRef;
import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * @author Kris Verlaenen
 */
public abstract class AbstractFormDispatcher implements FormDispatcherPlugin {

	public URL getDispatchUrl(FormAuthorityRef ref) {
		StringBuffer sb = new StringBuffer();
		// TODO: fix this for non localhost:8080
		sb.append("http://");
		sb.append("localhost");
		sb.append(":").append(8080);
		sb.append("/gwt-console-server/rs/form/" + getType(ref) + "/");
		sb.append(ref.getReferenceId());
		sb.append("/render");

		try {
			return new URL(sb.toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException("Failed to resolve form dispatch url", e);
		}
	}
	
	protected String getType(FormAuthorityRef ref) {
		FormAuthorityRef.Type type = ref.getType();
		if (type.equals(FormAuthorityRef.Type.TASK)) {
			return "task";
		}
		if (type.equals(FormAuthorityRef.Type.PROCESS)) {
			return "process";
		}
		throw new IllegalArgumentException(
			"Unknown form authority type: " + ref.getType());
	}
	
	public InputStream getTemplate(String name) {
		return AbstractFormDispatcher.class.getResourceAsStream("/" + name + ".ftl");
	}
	
	protected DataHandler processTemplate(final String name, InputStream src, Map<String, Object> renderContext) {
		DataHandler merged = null;
		try {
			freemarker.template.Configuration cfg = new freemarker.template.Configuration();
			cfg.setObjectWrapper(new DefaultObjectWrapper());
			cfg.setTemplateUpdateDelay(0);
			Template temp = new Template(name, new InputStreamReader(src), cfg);
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			Writer out = new OutputStreamWriter(bout);
			temp.process(renderContext, out);
			out.flush();
			merged = new DataHandler(new DataSource() {
				public InputStream getInputStream() throws IOException {
					return new ByteArrayInputStream(bout.toByteArray());
				}
				public OutputStream getOutputStream() throws IOException {
					return bout;
				}
				public String getContentType() {
					return "*/*";
				}
				public String getName() {
					return name + "_DataSource";
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("Failed to process form template", e);
		}
		return merged;
	}

}
