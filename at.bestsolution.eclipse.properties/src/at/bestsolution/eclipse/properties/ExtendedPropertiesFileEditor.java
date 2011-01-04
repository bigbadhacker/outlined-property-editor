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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class ExtendedPropertiesFileEditor extends PropertiesFileEditor {
	private PropertyContentOutlinePage contentOutline;
	@Override
	public Object getAdapter(Class adapter) {
		if( adapter == IContentOutlinePage.class ) {
			if( contentOutline == null ) {
				contentOutline = new PropertyContentOutlinePage();
			}
			
			getDocumentProvider().getDocument(getEditorInput()).addDocumentListener(new IDocumentListener() {
				
				public void documentChanged(DocumentEvent event) {
					try {
						Properties p = new Properties();
						p.load(new ByteArrayInputStream(getDocumentProvider().getDocument(getEditorInput()).get().getBytes("UTF-8")));
						contentOutline.setProperties(p.keySet().toArray(new String[0]));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				
				public void documentAboutToBeChanged(DocumentEvent event) {
					// TODO Auto-generated method stub
					
				}
			});
			
			try {
				Properties p = new Properties();
				p.load(new ByteArrayInputStream(getDocumentProvider().getDocument(getEditorInput()).get().getBytes("UTF-8")));
				contentOutline.setProperties(p.keySet().toArray(new String[0]));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return contentOutline;
		}
		//
		// TODO Auto-generated method stub
		return super.getAdapter(adapter);
	}
}
