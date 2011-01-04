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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class PropertyContentOutlinePage extends ContentOutlinePage {
	private List<Object> list = new ArrayList<Object>();
	private List<Property> currentList = new ArrayList<Property>();
		
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if( element instanceof PropertyGroup ) {
					return ((PropertyGroup) element).name;
				} else if( element instanceof Property ) {
					return ((Property) element).name;
				}
				return super.getText(element);
			}
			
			@Override
			public Image getImage(Object element) {
				if( element instanceof PropertyGroup ) {
					return Activator.getDefault().getImageRegistry().get(Activator.GROUP_ICON);
				} else {
					return Activator.getDefault().getImageRegistry().get(Activator.KEY_ICON);
				}
			}
		});
		viewer.setContentProvider(new ContentProvider());
		viewer.setInput(list);
	}

	public void setProperties(String[] properties) {
		List<Property> found = new ArrayList<Property>();
		List<Property> added = new ArrayList<Property>();
		for (String property : properties) {
			boolean isFound = false;
			Iterator<Property> it = currentList.iterator();
			while (it.hasNext()) {
				Property p = it.next();
				if (p.name.equals(property)) {
					isFound = true;
					found.add(p);
					it.remove();
				}
			}
			if( ! isFound ) {
				added.add(new Property(property));
			}
		}
		
		// These are removed
		for( Property p : currentList ) {
			if( p.parent != null ) {
				p.parent.items.remove(p);
				p.parent = null;
			} else {
				list.remove(p);
			}
		}
		
		currentList.clear();
		currentList.addAll(found);
		
		for( Property p : added ) {
			List<String> parts = new ArrayList<String>(Arrays.asList(p.name.split("_")));
			if( parts.size() > 1 ) {
				PropertyGroup group = getGroup(list, parts);
				if( parts.size() > 1 ) {
					PropertyGroup tmp = group;
					PropertyGroup root = null;
					for( int i = 0; i < parts.size() - 1; i++ ) {
						tmp = new PropertyGroup(tmp, parts.get(i));
						if( i == 0 ) {
							root = tmp;	
						}
					}
					
					p.parent = tmp;
					p.parent.items.add(p);
					
					if( group == null ) {
						list.add(root);
					}
				} else {
					p.parent = group;
					p.parent.items.add(p);
				}
			} else {
				list.add(p);
			}
			currentList.add(p);
		}
		
		removeEmptyGroups(list);
		
		if( getTreeViewer() != null ) {
			getTreeViewer().refresh();	
		}
		
	}
	
	private void removeEmptyGroups(List<?> list) {
		Iterator<?> it = list.iterator();
		while( it.hasNext() ) {
			Object o = it.next();
			if( o instanceof PropertyGroup ) {
				removeEmptyGroups(((PropertyGroup) o).groups);
				if( ((PropertyGroup) o).groups.size() == 0 && ((PropertyGroup) o).items.size() == 0 ) {
					it.remove();
				}
			}
		}
	}
	
	private PropertyGroup getGroup(List<?> list , List<String> parts) {
		PropertyGroup group = null;
		for( Object o : list ) {
			if( o instanceof PropertyGroup ) {
				if( ((PropertyGroup) o).name.equals(parts.get(0)) ) {
					parts.remove(0);
					group = (PropertyGroup) o;
					if( parts.size() > 1 ) {
						PropertyGroup tmp = getGroup(group.groups,parts);
						if( tmp != null ) {
							return tmp;
						}
					}
					return group;
				}
			}
		}
		return group;
	}

	static class LabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			return super.getText(element);
		}
	}

	static class ContentProvider implements ITreeContentProvider {

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		public Object[] getElements(Object inputElement) {
			return ((List<?>) inputElement).toArray();
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof PropertyGroup) {
				Object[] groups = ((PropertyGroup) parentElement).groups
						.toArray();
				Object[] items = ((PropertyGroup) parentElement).items
						.toArray();
				Object[] rv = new Object[groups.length + items.length];
				System.arraycopy(groups, 0, rv, 0, groups.length);
				System.arraycopy(items, 0, rv, groups.length, items.length);
				
				return rv;
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if (element instanceof PropertyGroup) {
				return ((PropertyGroup) element).parent;
			} else if (element instanceof Property) {
				return ((Property) element).parent;
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof PropertyGroup) {
				return ((PropertyGroup) element).groups.size() > 0
						|| ((PropertyGroup) element).items.size() > 0;
			}
			return false;
		}

	}

	static class PropertyGroup {
		private PropertyGroup parent;
		public String name;
		private List<PropertyGroup> groups = new ArrayList<PropertyContentOutlinePage.PropertyGroup>();
		private List<Property> items = new ArrayList<Property>();

		public PropertyGroup(String name) {
			this(null, name);
		}

		public PropertyGroup(PropertyGroup parent, String name) {
			this.parent = parent;
			if( parent != null ) {
				parent.groups.add(this);
			}
			this.name = name;
		}

		@Override
		public String toString() {
			return super.toString() + "#" +name;
		}
	}

	static class Property {
		private PropertyGroup parent;
		private String name;

		public Property(String name) {
			this(null, name);
		}

		public Property(PropertyGroup parent, String name) {
			this.parent = parent;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return super.toString() + "#" + name;
		}
	}
	
//	public static void main(String[] args) {
//		Display d = new Display();
//		final Shell s = new Shell(d);
//		s.setLayout(new FillLayout());
//		final PropertyContentOutlinePage page = new PropertyContentOutlinePage();
//		page.createControl(s);
//		
//		final String[][] vals = new String[][] {
//				{
//					"ClassA_PropertyA",
//					"ClassB_PropertyB",
//					"ClassC_PropertyC",
//					"ClassD_InnerA_PropertyA"
//				},
//				{
//					"ClassA_PropertyA",
//					"ClassA_PropertyB",
//					"ClassB_PropertyB",
//					"ClassC_PropertyC",
//					"ClassC_InnerC_PropertyC",
//					"ClassD_InnerA_PropertyA",
//					"ClassD_InnerA_PropertyB"
//				},
//				{
//					"ClassA_PropertyA",
//					"ClassB_PropertyB",
//					"ClassC_PropertyC",
//					"ClassD_InnerA_PropertyA"
//				}
//		};
//		
//		Thread t = new Thread() {
//			@Override
//			public void run() {
//				for( String[] props : vals ) {
//					final String[] in = props;
//					s.getDisplay().syncExec(new Runnable() {
//						public void run() {
//							page.setProperties(in);
//							page.getTreeViewer().expandAll();
//						}
//					});
//					try {
//						Thread.sleep(3000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//				}
//			}
//		};
//		t.start();
//		
//		
//		
//		
//		s.open();
//		
//		while( ! s.isDisposed() ) {
//			if( ! d.readAndDispatch() ) {
//				d.sleep();
//			}
//		}
//	}
}
