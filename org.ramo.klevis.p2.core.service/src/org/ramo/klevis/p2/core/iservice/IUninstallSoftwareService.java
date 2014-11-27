package org.ramo.klevis.p2.core.iservice;

import java.util.List;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;

public interface IUninstallSoftwareService {
	int GROUP = 0;
	int CATEGORY = 1;
	int ANY = 2;

	List<IInstallableUnit> listInstalledSoftware(IProvisioningAgent agen, int i);

	String uninstallSelected(List<IInstallableUnit> listToUninstall);

}
