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

import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import at.bestsolution.eclipse.properties.PropertyContentOutlinePage.Property;
import at.bestsolution.eclipse.properties.PropertyContentOutlinePage.PropertyGroup;

@SuppressWarnings("restriction")
public class ExtendedPropertiesFileEditor extends PropertiesFileEditor {
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IContentOutlinePage.class) {
			final PropertyContentOutlinePage contentOutline = new PropertyContentOutlinePage(this);

			contentOutline
					.addSelectionChangedListener(new ISelectionChangedListener() {

						public void selectionChanged(SelectionChangedEvent event) {
							Object o = ((IStructuredSelection) event
									.getSelection()).getFirstElement();
							String searchKey = null;
							int selectionLength = 0;
							
							if (o instanceof PropertyGroup) {
								searchKey = ((PropertyGroup) o).name;
							} else if (o instanceof Property) {
								searchKey = ((Property) o).pair.key;
								selectionLength = ((Property) o).pair.value.length();
							}

							if (searchKey == null) {
								return;
							}

							IDocument doc = getDocumentProvider().getDocument(
									getEditorInput());
							int lines = doc.getNumberOfLines();
							try {
								for (int i = 0; i < lines; i++) {
									IRegion r = doc.getLineInformation(i);
									String line = doc.get(r.getOffset(),
											r.getLength());
									if (line.startsWith(searchKey)) {
										selectAndReveal(
												r.getOffset()
														+ r.getLength() - selectionLength,
												selectionLength);
										break;
									}
								}
							} catch (BadLocationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});

			return contentOutline;
		}
		//
		// TODO Auto-generated method stub
		return super.getAdapter(adapter);
	}
}
