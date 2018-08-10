/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Factory for creating {@link Launcher} instances by invoking {@link #create()}.
 *
 * <p>Test engines are discovered at runtime using the
 * {@link java.util.ServiceLoader ServiceLoader} facility. For that purpose, a
 * text file named {@code META-INF/services/org.junit.platform.engine.TestEngine}
 * has to be added to the engine's JAR file in which the fully qualified name
 * of the implementation class of the {@link org.junit.platform.engine.TestEngine}
 * interface is declared.
 *
 * <p>{@link org.junit.platform.launcher.TestExecutionListener}s discovered at
 * runtime via the {@link java.util.ServiceLoader ServiceLoader} facility are
 * automatically registered with the {@link Launcher} created by the factory.
 * Users may register additional listeners using the
 * {@link Launcher#registerTestExecutionListeners(TestExecutionListener...)}
 * method on the created launcher instance.
 *
 * @since 1.0
 * @see Launcher
 */
@API(status = STABLE, since = "1.0")
public class LauncherFactory {

	private LauncherFactory() {
		/* no-op */
	}

	/**
	 * Factory method for creating a new {@link Launcher} using dynamically
	 * detected test engines.
	 *
	 * <p>All dynamically detected {@link org.junit.platform.launcher.TestExecutionListener}s
	 * are automatically registered in the created {@link Launcher} instance.
	 *
	 * @throws PreconditionViolationException if no test engines are detected
	 */
	public static Launcher create() throws PreconditionViolationException {
		return create(LauncherConfig.builder().build());
	}

	/**
	 * Factory method for creating a new {@link Launcher} using provided configuration
	 *
	 * @param config the configuration for launcher; never {@code null}
	 * @throws PreconditionViolationException if no test engines are detected or
	 * provided configuration is null
	 */
	@API(status = EXPERIMENTAL, since = "1.3")
	public static Launcher create(LauncherConfig config) throws PreconditionViolationException {
		Preconditions.notNull(config, "configuration must not be null");
		List<TestEngine> engines = new ArrayList<>();
		if (config.isTestEngineAutoRegistrationEnabled()) {
			new ServiceLoaderTestEngineRegistry().loadTestEngines().forEach(engines::add);
		}
		engines.addAll(config.getAdditionalTestEngines());
		Launcher launcher = new DefaultLauncher(engines);

		if (config.isTestExecutionListenerAutoRegistrationEnabled()) {
			new ServiceLoaderTestExecutionListenerRegistry().loadListeners().forEach(
				launcher::registerTestExecutionListeners);
		}
		config.getAdditionalTestExecutionListeners().forEach(launcher::registerTestExecutionListeners);
		return launcher;
	}

}
