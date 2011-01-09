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

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.osgi.service.prefs.BackingStoreException;

public class PropertyContentOutlinePage extends ContentOutlinePage {
	private List<Object> list = new ArrayList<Object>();
	private List<Property> currentList = new ArrayList<Property>();
	private IEclipsePreferences preferences = new InstanceScope().getNode("propertiesoutline");

	private static final String PREF_SORTED = "sorted";
	
	private Action sortAction;
	
	public PropertyContentOutlinePage() {
		sortAction = new Action("",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				preferences.putBoolean(PREF_SORTED, isChecked());
				try {
					preferences.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
		};
		sortAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.ALPHASORT_ICON));
		sortAction.setChecked(preferences.getBoolean(PREF_SORTED, true));
		
		preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
			
			public void preferenceChange(PreferenceChangeEvent event) {
				if( event.getKey().equals(PREF_SORTED) ) {
					boolean val = preferences.getBoolean(PREF_SORTED, true);
					setSorted(val);
					sortAction.setChecked(val);
				}
			}
		});
	}
	
	@Override
	public void makeContributions(IMenuManager menuManager,
			IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {
		super.makeContributions(menuManager, toolBarManager, statusLineManager);
		toolBarManager.add(sortAction);
		
		Action a = new Action("",IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				getTreeViewer().collapseAll();
			}
		};
		a.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.COLLAPSE_ICON));
		toolBarManager.add(a);
	}
	
	private void setSorted(boolean sorted) {
		//FIXME We should delay until visible!
		if( sorted ) {
			getTreeViewer().setComparator(new ViewerComparator() {
				@Override
				public int compare(Viewer viewer, Object e1, Object e2) {
					if( e1 instanceof PropertyGroup && e2 instanceof PropertyGroup ) {
						return ((PropertyGroup)e1).name.compareTo(((PropertyGroup)e2).name);
					} else if( e1 instanceof Property && e2 instanceof Property ) {
						return ((Property)e1).pair.key.compareTo(((Property)e2).pair.key);
					} else if( e1 instanceof Property ) {
						return -1;
					} else if( e2 instanceof Property ) {
						return -1;
					}
					return super.compare(viewer, e1, e2);
				}
			});
		} else {
			getTreeViewer().setComparator(null);
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				if (element instanceof PropertyGroup) {
					cell.setText(((PropertyGroup) element).name);
					cell.setImage(Activator.getDefault().getImageRegistry().get(Activator.GROUP_ICON));
					cell.setStyleRanges(null);
				} else if (element instanceof Property) {
					cell.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_ICON));
					StyledString s = new StyledString(((Property) element).pair.key);
					String text = ((Property) element).pair.value;
					if( text.length() > 20 ) {
						text = text.substring(0,20) + "...";
					}
					s.append(" : " + text, StyledString.DECORATIONS_STYLER);
					cell.setStyleRanges(s.getStyleRanges());
					cell.setText(s.getString());
				}
				super.update(cell);
			}
		});
		viewer.setContentProvider(new ContentProvider());
		if( preferences.getBoolean(PREF_SORTED, true) ) {
			setSorted(true);
		}
		viewer.setInput(list);
	}

	public void setProperties(Pair[] properties) {
		List<Property> found = new ArrayList<Property>();
		List<Property> added = new ArrayList<Property>();
		for (Pair property : properties) {
			boolean isFound = false;
			Iterator<Property> it = currentList.iterator();
			while (it.hasNext()) {
				Property p = it.next();
				if (p.pair.key.equals(property)) {
					isFound = true;
					found.add(p);
					it.remove();
				}
			}
			if (!isFound) {
				added.add(new Property(property));
			}
		}

		// These are removed
		for (Property p : currentList) {
			if (p.parent != null) {
				p.parent.items.remove(p);
				p.parent = null;
			} else {
				list.remove(p);
			}
		}

		currentList.clear();
		currentList.addAll(found);

		for (Property p : added) {
			List<String> parts = new ArrayList<String>(Arrays.asList(p.pair.key
					.split("_")));
			if (parts.size() > 1) {
				PropertyGroup group = getGroup(list, parts);
				if (parts.size() > 1) {
					PropertyGroup tmp = group;
					PropertyGroup root = null;
					for (int i = 0; i < parts.size() - 1; i++) {
						tmp = new PropertyGroup(tmp, parts.get(i));
						if (i == 0) {
							root = tmp;
						}
					}

					p.parent = tmp;
					p.parent.items.add(p);

					if (group == null) {
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

		if (getTreeViewer() != null) {
			getTreeViewer().refresh();
		}

	}

	private void removeEmptyGroups(List<?> list) {
		Iterator<?> it = list.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof PropertyGroup) {
				removeEmptyGroups(((PropertyGroup) o).groups);
				if (((PropertyGroup) o).groups.size() == 0
						&& ((PropertyGroup) o).items.size() == 0) {
					it.remove();
				}
			}
		}
	}

	private PropertyGroup getGroup(List<?> list, List<String> parts) {
		PropertyGroup group = null;
		for (Object o : list) {
			if (o instanceof PropertyGroup) {
				if (((PropertyGroup) o).name.equals(parts.get(0))) {
					parts.remove(0);
					group = (PropertyGroup) o;
					if (parts.size() > 1) {
						PropertyGroup tmp = getGroup(group.groups, parts);
						if (tmp != null) {
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

	public static class PropertyGroup {
		private PropertyGroup parent;
		public String name;
		private List<PropertyGroup> groups = new ArrayList<PropertyContentOutlinePage.PropertyGroup>();
		private List<Property> items = new ArrayList<Property>();

		public PropertyGroup(String name) {
			this(null, name);
		}

		public PropertyGroup(PropertyGroup parent, String name) {
			this.parent = parent;
			if (parent != null) {
				parent.groups.add(this);
			}
			this.name = name;
		}

		@Override
		public String toString() {
			return super.toString() + "#" + name;
		}
	}

	public static class Property {
		private PropertyGroup parent;
		public Pair pair;

		public Property(Pair pair) {
			this(null, pair);
		}

		public Property(PropertyGroup parent, Pair pair) {
			this.parent = parent;
			this.pair = pair;
		}

		@Override
		public String toString() {
			return super.toString() + "#" + pair.key + " / " + pair.value;
		}
	}

	public static class Pair {
		public final String key;
		public final String value;

		public Pair(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}

	// public static void main(String[] args) {
	// Display d = new Display();
	// final Shell s = new Shell(d);
	// s.setLayout(new FillLayout());
	// final PropertyContentOutlinePage page = new PropertyContentOutlinePage();
	// page.createControl(s);
	//
	// final String[][] vals = new String[][] {
	// {
	// "ClassA_PropertyA",
	// "ClassB_PropertyB",
	// "ClassC_PropertyC",
	// "ClassD_InnerA_PropertyA"
	// },
	// {
	// "ClassA_PropertyA",
	// "ClassA_PropertyB",
	// "ClassB_PropertyB",
	// "ClassC_PropertyC",
	// "ClassC_InnerC_PropertyC",
	// "ClassD_InnerA_PropertyA",
	// "ClassD_InnerA_PropertyB"
	// },
	// {
	// "ClassA_PropertyA",
	// "ClassB_PropertyB",
	// "ClassC_PropertyC",
	// "ClassD_InnerA_PropertyA"
	// }
	// };
	//
	// Thread t = new Thread() {
	// @Override
	// public void run() {
	// for( String[] props : vals ) {
	// final String[] in = props;
	// s.getDisplay().syncExec(new Runnable() {
	// public void run() {
	// page.setProperties(in);
	// page.getTreeViewer().expandAll();
	// }
	// });
	// try {
	// Thread.sleep(3000);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	// }
	// };
	// t.start();
	//
	//
	//
	//
	// s.open();
	//
	// while( ! s.isDisposed() ) {
	// if( ! d.readAndDispatch() ) {
	// d.sleep();
	// }
	// }
	// }
}
