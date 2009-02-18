package org.guvnor.tools;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.drools.guvnor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static GuvnorLocationManager locManager;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}
	
	public static GuvnorLocationManager getLocationManager() {
		if (locManager == null) {
			locManager = new GuvnorLocationManager();
		}
		return locManager;
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		getWorkspace().addResourceChangeListener(new ResourceChangeListener());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	private IStatus createStatus(int severity, String msg, Throwable t) {
		return new Status(severity, PLUGIN_ID, msg, t);
	}
	
	public void writeLog(int severity, String msg, Throwable t) {
		super.getLog().log(createStatus(severity, msg, t));
	}
	
	public void writeLog(IStatus status) {
		super.getLog().log(status);
	}
	
	public void displayMessage(final int severity, final String msg) {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				switch (severity) {
					case IStatus.ERROR:
						MessageDialog.openError(display.getActiveShell(), 
											Messages.getString("error"), msg); //$NON-NLS-1$
					break;
					case IStatus.WARNING:
						MessageDialog.openWarning(display.getActiveShell(), 
											Messages.getString("warning"), msg); //$NON-NLS-1$
					break;
				}
			}
		});
	}
	
	public void displayError(int severity, final String msg, 
			                 Throwable t, boolean shouldLog) {
		final IStatus status = createStatus(severity, msg, t);
		if (shouldLog) {
			writeLog(status);
		}
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				ErrorDialog.openError(display.getActiveShell(),
						           null,
						           Messages.getString("error"), status); //$NON-NLS-1$
			}
		});
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String id) {
		ImageDescriptor img = getCachedImageDescriptor(id);
		if (img == null) {
			img = loadImageDescriptor(id);
		}
		return img;
	}
	
	private static ImageDescriptor loadImageDescriptor(String id) {
		String iconPath = "icons/"; //$NON-NLS-1$

		try {
			URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
			URL url = new URL(installURL, iconPath + id);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static ImageDescriptor getCachedImageDescriptor(String id) {
		ImageDescriptor img = null;
		if (id.equals(IMG_GUVCONTROLLED)) {
			if (GUVCONTROLLED_IMG == null) {
				GUVCONTROLLED_IMG = loadImageDescriptor(IMG_GUVCONTROLLED);
			}
			img = GUVCONTROLLED_IMG; 
		}
		if (id.equals(IMG_GUVLOCADD)) {
			if (GUVLOCADD_IMG == null) {
				GUVLOCADD_IMG = loadImageDescriptor(IMG_GUVLOCADD);
			}
			img = GUVLOCADD_IMG; 
		}
		if (id.equals(IMG_GUVREPWIZBAN)) {
			if (GUVREPWIZBAN_IMG == null) {
				GUVREPWIZBAN_IMG = loadImageDescriptor(IMG_GUVREPWIZBAN);
			}
			img = GUVREPWIZBAN_IMG; 
		}
		return img;
	}
	
	private static ImageDescriptor GUVCONTROLLED_IMG;
	private static ImageDescriptor GUVLOCADD_IMG;
	private static ImageDescriptor GUVREPWIZBAN_IMG;
	
	public static final String IMG_GUVCONTROLLED 		= "guvnor_controlled.gif"; //$NON-NLS-1$
	public static final String IMG_GUVLOCADD 			= "guvnor_rep_add.gif"; //$NON-NLS-1$
	public static final String IMG_GUVREPWIZBAN 		= "guvnor_rep_wizban.gif"; //$NON-NLS-1$
	
	public boolean useDebugHttpClient() {
		return Boolean.parseBoolean(Platform.getDebugOption("org.guvnor.tools/debugHttpClient")); //$NON-NLS-1$
	}
}
