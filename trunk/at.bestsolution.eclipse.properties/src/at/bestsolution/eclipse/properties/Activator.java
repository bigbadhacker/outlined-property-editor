/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package at.bestsolution.eclipse.properties;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "at.bestsolution.eclipse.properties"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	public static String GROUP_ICON = "GROUP_ICON";
	public static String KEY_ICON = "KEY_ICON";
	public static String ALPHASORT_ICON = "ALPHASORT_ICON";
	public static String COLLAPSE_ICON = "COLLAPSE_ICON";

	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		getImageRegistry().put(GROUP_ICON, ImageDescriptor.createFromURL(new URL("platform:/plugin/"+PLUGIN_ID+"/icons/ftr_jar_obj.gif")));
		getImageRegistry().put(KEY_ICON, ImageDescriptor.createFromURL(new URL("platform:/plugin/"+PLUGIN_ID+"/icons/default_co.gif")));
		getImageRegistry().put(ALPHASORT_ICON, ImageDescriptor.createFromURL(new URL("platform:/plugin/"+PLUGIN_ID+"/icons/alphab_sort_co.gif")));
		getImageRegistry().put(COLLAPSE_ICON, ImageDescriptor.createFromURL(new URL("platform:/plugin/"+PLUGIN_ID+"/icons/collapseall.gif")));
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

}
