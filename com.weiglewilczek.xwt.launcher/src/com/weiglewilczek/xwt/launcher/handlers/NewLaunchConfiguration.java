/*
 * Copyright (c) 2012 WeigleWilczek and others.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.xwt.launcher.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.weiglewilczek.xwt.launcher.Activator;
import com.weiglewilczek.xwt.launcher.Messages;
import com.weiglewilczek.xwt.launcher.managers.EclipseInstallationManager;
import com.weiglewilczek.xwt.launcher.managers.GroupManager;
import com.weiglewilczek.xwt.launcher.managers.JavaInstallationManager;
import com.weiglewilczek.xwt.launcher.managers.LaunchConfigurationManager;
import com.weiglewilczek.xwt.launcher.model.EclipseInstallation;
import com.weiglewilczek.xwt.launcher.model.Group;
import com.weiglewilczek.xwt.launcher.model.JavaInstallation;
import com.weiglewilczek.xwt.launcher.model.LaunchConfiguration;
import com.weiglewilczek.xwt.launcher.model.LaunchConfigurationDataContext;
import com.weiglewilczek.xwt.launcher.model.ObservableGroup;
import com.weiglewilczek.xwt.launcher.model.ui.GroupsView;
import com.weiglewilczek.xwt.launcher.model.ui.LaunchConfigurationDialog;

public class NewLaunchConfiguration extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Group parentGroup = null;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection viewerSelection = (IStructuredSelection) selection;
			if (viewerSelection.getFirstElement() instanceof ObservableGroup) {
				parentGroup = ((ObservableGroup) viewerSelection
						.getFirstElement()).getGroup();
			} else if (viewerSelection.getFirstElement() instanceof LaunchConfiguration) {
				LaunchConfiguration sisterNode = (LaunchConfiguration) viewerSelection
						.getFirstElement();
				parentGroup = GroupManager.getInstance()
						.getGroupForConfiguration(sisterNode);
			}
		}

		if (parentGroup == null) {
			parentGroup = GroupManager.getInstance().get(-1l);
		}

		LaunchConfiguration configuration = new LaunchConfiguration();

		List<EclipseInstallation> eclipses = EclipseInstallationManager
				.getInstance().enumerateAll();
		WritableList writableEclipses = new WritableList();
		writableEclipses.addAll(eclipses);

		List<JavaInstallation> javas = JavaInstallationManager.getInstance()
				.enumerateAll();
		WritableList writableJavas = new WritableList();
		writableJavas.addAll(javas);

		LaunchConfigurationDataContext dataContext = new LaunchConfigurationDataContext(
				configuration, writableEclipses, writableJavas);

		LaunchConfigurationDialog launchConfiguration = new LaunchConfigurationDialog(
				HandlerUtil.getActiveShell(event), dataContext);
		if (launchConfiguration.open() == LaunchConfigurationDialog.OK) {
			try {
				LaunchConfigurationManager.getInstance().create(configuration);

				parentGroup.addConfiguration(configuration);
				GroupManager.getInstance().update(parentGroup);

				if (!(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().getActivePart() instanceof GroupsView)) {
					try {
						PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.showView(
										"com.weiglewilczek.xwt.launcher.views.Groups");
					} catch (PartInitException e) {
						throw new ExecutionException(
								Messages.NewLaunchConfiguration_ErrorOpeningGroupsView,
								e);
					}

				}
				GroupsView view = (GroupsView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getActivePart();
				view.getTree().setSelection(
						new StructuredSelection(configuration));
			} catch (Exception e) {
				Activator.logError(Messages.NewLaunchConfiguration_Error, e);
				MessageDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(),
						Messages.NewLaunchConfiguration_NewLaunchConfiguration,
						Messages.NewLaunchConfiguration_Error + ": "
								+ e.getMessage());
			}
		}

		return null;
	}
}
