package net.sf.py4j.defaultserver.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import py4j.GatewayServer;

import net.sf.py4j.defaultserver.DefaultServerActivator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = DefaultServerActivator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PREF_DEFAULT_PORT, GatewayServer.DEFAULT_PORT);
		store.setDefault(PreferenceConstants.PREF_DEFAULT_CALLBACK_PORT, GatewayServer.DEFAULT_PYTHON_PORT);
	}

}
