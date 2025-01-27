/*
 * Copyright (c) 2012 WeigleWilczek and others.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.xwt.launcher.handlers;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

import com.weiglewilczek.xwt.launcher.Activator;
import com.weiglewilczek.xwt.launcher.Messages;
import com.weiglewilczek.xwt.launcher.managers.GroupManager;
import com.weiglewilczek.xwt.launcher.managers.ImportExportManager;
import com.weiglewilczek.xwt.launcher.model.Group;

public class Import extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		File file = null;
		FileDialog dialog = new FileDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "prefs", "properties", "*" });
		String fileName = dialog.open();

		if (fileName != null) {
			file = new File(fileName);

			if (file.exists()) {
				try {
					ImportExportManager.getInstance().importProperties(file);

					// trigger refresh of other group
					Group other = GroupManager.getInstance().get(-1l);
					GroupManager.getInstance().update(other);
				} catch (IOException e) {
					Activator.logError(Messages.Import_Error, e);
					MessageDialog.openError(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(),
							Messages.Import_Import, Messages.Import_Error
									+ ": " + e.getMessage());
				}
			}
		}

		return null;
	}
}
