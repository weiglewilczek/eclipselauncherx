package com.weiglewilczek.xwt.launcher.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.prefs.BackingStoreException;

import com.weiglewilczek.xwt.launcher.managers.GroupManager;
import com.weiglewilczek.xwt.launcher.model.Group;
import com.weiglewilczek.xwt.launcher.model.ui.GroupDialog;

public class NewGroup extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Group group = new Group();

		GroupDialog groupDialog = new GroupDialog(
				HandlerUtil.getActiveShell(event), group);
		if (groupDialog.open() == GroupDialog.OK) {
			try {
				GroupManager.getInstance().create(group);
			} catch (BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}
}