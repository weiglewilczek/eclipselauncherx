/*
 * Copyright (c) 2012 WeigleWilczek and others.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.xwt.launcher.managers;

public class ConstraintViolationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ConstraintViolationException(String message) {
		super(message);
	}
}
