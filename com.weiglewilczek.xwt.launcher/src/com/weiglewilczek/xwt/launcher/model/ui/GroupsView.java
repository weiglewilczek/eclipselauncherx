/*
 * Copyright (c) 2012 WeigleWilczek and others.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.xwt.launcher.model.ui;

import org.eclipse.e4.xwt.XWT;
import org.eclipse.e4.xwt.pde.ui.views.XWTViewPart;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.handlers.IHandlerService;

import com.weiglewilczek.xwt.launcher.Activator;
import com.weiglewilczek.xwt.launcher.Messages;
import com.weiglewilczek.xwt.launcher.listener.IListener;
import com.weiglewilczek.xwt.launcher.listener.ListenerType;
import com.weiglewilczek.xwt.launcher.managers.EclipseInstallationManager;
import com.weiglewilczek.xwt.launcher.managers.GroupManager;
import com.weiglewilczek.xwt.launcher.managers.JavaInstallationManager;
import com.weiglewilczek.xwt.launcher.managers.LaunchConfigurationManager;
import com.weiglewilczek.xwt.launcher.model.EclipseInstallation;
import com.weiglewilczek.xwt.launcher.model.Group;
import com.weiglewilczek.xwt.launcher.model.GroupsDataContext;
import com.weiglewilczek.xwt.launcher.model.GroupsObservableFactory;
import com.weiglewilczek.xwt.launcher.model.JavaInstallation;
import com.weiglewilczek.xwt.launcher.model.LaunchConfiguration;
import com.weiglewilczek.xwt.launcher.model.ModelElement;
import com.weiglewilczek.xwt.launcher.model.ObservableGroup;
import com.weiglewilczek.xwt.launcher.util.AbstractColumnViewerSorter;

public class GroupsView extends XWTViewPart implements IListener {

	private GroupsDataContext context;

	private TreeViewer groupsViewer;

	public GroupsView() {
		GroupManager.getInstance().addListener(this);
		LaunchConfigurationManager.getInstance().addListener(this);
		EclipseInstallationManager.getInstance().addListener(this);
		JavaInstallationManager.getInstance().addListener(this);

		context = new GroupsDataContext(GroupManager.getInstance()
				.enumerateAll());

		setDataContext(context);
	}

	@Override
	protected void updateContent() {
		if (container != null) {
			setContent(this.getClass().getResource("GroupsView.xwt"));

			if (container.getChildren().length > 0) {

				Control xwtContext = container.getChildren()[0];
				groupsViewer = (TreeViewer) XWT.findElementByName(xwtContext,
						"groups");
				Object nameColumn = XWT.findElementByName(xwtContext,
						"nameColumn");
				groupsViewer.getTree().setHeaderVisible(true);
				groupsViewer.getTree().setLinesVisible(true);
				ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(
						new GroupsObservableFactory(), null);
				groupsViewer.setContentProvider(contentProvider);

				groupsViewer.setInput(context);

				getSite().setSelectionProvider(groupsViewer);

				groupsViewer.addDoubleClickListener(new IDoubleClickListener() {

					@Override
					public void doubleClick(DoubleClickEvent event) {
						ISelection selection = groupsViewer.getSelection();
						if (selection != null
								&& selection instanceof IStructuredSelection
								&& ((IStructuredSelection) selection)
										.getFirstElement() instanceof LaunchConfiguration) {
							IHandlerService hs = (IHandlerService) getSite()
									.getService(IHandlerService.class);
							try {
								hs.executeCommand(
										"com.weiglewilczek.xwt.launcher.commands.Launch",
										null);
							} catch (Exception e) {
								Activator.logError(
										Messages.GroupsView_LaunchingError, e);
							}
						} else if (selection != null
								&& selection instanceof IStructuredSelection
								&& ((IStructuredSelection) selection)
										.getFirstElement() instanceof ObservableGroup) {
							IHandlerService hs = (IHandlerService) getSite()
									.getService(IHandlerService.class);
							try {
								hs.executeCommand(
										"com.weiglewilczek.xwt.launcher.commands.Edit",
										null);
							} catch (Exception e) {
								Activator.logError(
										Messages.GroupsView_EditError, e);
							}
						}
					}
				});

				if (nameColumn instanceof TreeColumn) {
					AbstractColumnViewerSorter<Object> sorter = new AbstractColumnViewerSorter<Object>(
							groupsViewer, (TreeColumn) nameColumn) {

						@Override
						public int doCompare(Object object1, Object object2) {
							if (object1 instanceof LaunchConfiguration
									&& object2 instanceof LaunchConfiguration) {
								LaunchConfiguration launchConfiguration1 = (LaunchConfiguration) object1;
								LaunchConfiguration launchConfiguration2 = (LaunchConfiguration) object2;
								return launchConfiguration1.getName()
										.compareTo(
												launchConfiguration2.getName());
							}
							if (object1 instanceof ObservableGroup
									&& object2 instanceof ObservableGroup) {
								ObservableGroup group1 = (ObservableGroup) object1;
								ObservableGroup group2 = (ObservableGroup) object2;
								return group1.getGroup().getName()
										.compareTo(group2.getGroup().getName());
							}

							return 0;
						}
					};
					sorter.setSorter(sorter, AbstractColumnViewerSorter.ASC);
				}

				int options = DND.DROP_MOVE;

				Transfer[] transfers = new Transfer[] { TextTransfer
						.getInstance() };

				groupsViewer.addDragSupport(options, transfers,
						new DragSourceAdapter(groupsViewer));

				groupsViewer.addDropSupport(options, transfers,
						new DropAdapter(groupsViewer));
			}
		}
	}

	@Override
	public void setFocus() {
		if (groupsViewer != null) {
			groupsViewer.getTree().setFocus();
		}
	}

	@Override
	public void handle(ListenerType type, ModelElement<?> object) {
		if (object instanceof Group) {
			switch (type) {
			case CREATE:
				context.addGroup((Group) object);
				groupsViewer.refresh();
				break;
			case UPDATE:
				ObservableGroup observableGroup = context
						.getGroup((Group) object);
				observableGroup.synchronizeConfigurations();
				groupsViewer.refresh(true);
				break;
			case DELETE:
				ObservableGroup observable = context.getGroup((Group) object);
				context.getGroups().remove(observable);
				groupsViewer.refresh();
				break;

			default:
				break;
			}
		} else if (object instanceof LaunchConfiguration
				&& type.equals(ListenerType.UPDATE)) {
			groupsViewer.refresh(true);
		} else if (object instanceof LaunchConfiguration
				&& type.equals(ListenerType.DELETE)) {
			groupsViewer.refresh(true);
		} else if (object instanceof EclipseInstallation
				&& type.equals(ListenerType.UPDATE)) {
			groupsViewer.refresh(true);
		} else if (object instanceof JavaInstallation
				&& type.equals(ListenerType.UPDATE)) {
			groupsViewer.refresh(true);
		}
	}

	public TreeViewer getTree() {
		return groupsViewer;
	}
}
