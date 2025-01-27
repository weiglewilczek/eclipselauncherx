/*
 * Copyright (c) 2012 WeigleWilczek and others.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.xwt.launcher.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.weiglewilczek.xwt.launcher.managers.GroupFields;

public class Group extends ModelElement<GroupFields> implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private List<LaunchConfiguration> configurations = new ArrayList<LaunchConfiguration>();

	public Group(String name, List<LaunchConfiguration> configurations) {
		this.name = name;
		this.configurations = configurations;
	}

	public Group() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<LaunchConfiguration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<LaunchConfiguration> configurations) {
		this.configurations = configurations;
	}

	public void addConfiguration(LaunchConfiguration configuration) {
		this.configurations.add(configuration);
	}

	public void removeConfiguration(LaunchConfiguration configuration) {
		this.configurations.remove(configuration);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Group) {
			return super.equals(obj);
		}

		return false;
	}

	@Override
	public String toString() {
		return "Group[id=" + getId() + ", name=" + getName()
				+ ", configurations=" + getConfigurations() + "]";
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Group(getName(), getConfigurations());
	}

	@Override
	public Object getProperty(GroupFields field) {
		switch (field) {
		case NAME:
			return getName();
		case CONFIGURATIONS:
			return getConfigurations();
		default:
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setProperty(GroupFields field, Object value) {
		switch (field) {
		case NAME:
			setName((String) value);
			break;
		case CONFIGURATIONS:
			setConfigurations((List<LaunchConfiguration>) value);
			break;
		}
	}

}
