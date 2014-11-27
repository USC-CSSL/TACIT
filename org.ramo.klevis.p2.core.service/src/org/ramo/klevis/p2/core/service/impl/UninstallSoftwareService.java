package org.ramo.klevis.p2.core.service.impl;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UninstallOperation;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.ramo.klevis.p2.core.iservice.IUninstallSoftwareService;

public class UninstallSoftwareService implements IUninstallSoftwareService {
	IProvisioningAgent agent;
	public static final int GROUP = 0;
	public static final int CATEGORY = 1;
	public static final int ANY = 2;

	@Override
	public List<IInstallableUnit> listInstalledSoftware(
			IProvisioningAgent agen, int i) {

		this.agent = agen;
		IProfileRegistry service = (IProfileRegistry) agen
				.getService(IProfileRegistry.SERVICE_NAME);

		IQueryable<IInstallableUnit> queryable = service.getProfile("_SELF_");

		if(queryable==null){
			return null;
		}
		NullProgressMonitor monitor = new NullProgressMonitor();
		IQuery<IInstallableUnit> createIU = null;
		if (i == GROUP) {
			createIU = QueryUtil.createIUGroupQuery();
		} else if (i == CATEGORY) {
			createIU = QueryUtil.createIUCategoryQuery();
		} else if (i == ANY) {

			createIU = QueryUtil.createIUAnyQuery();
		}
		IQueryResult<IInstallableUnit> query = queryable.query(createIU,
				monitor);

		List<IInstallableUnit> list = org.ramo.klevis.p2.core.util.Utils
				.toList(query);

		return list;

	}

	@Override
	public String uninstallSelected(List<IInstallableUnit> listToUninstall) {
		try {
			UninstallOperation uninstallOperation = new UninstallOperation(
					new ProvisioningSession(agent), listToUninstall);
			uninstallOperation.setProvisioningContext(new ProvisioningContext(
					agent));
			NullProgressMonitor monitor = new NullProgressMonitor();
			IStatus resolveModal = uninstallOperation.resolveModal(monitor);
			String resolutionDetails = uninstallOperation
					.getResolutionDetails();
			if (!resolveModal.isOK()) {
				return resolutionDetails;
			}
			if (resolveModal.getSeverity() == IStatus.ERROR) {
				return resolutionDetails;
			}

			if (resolveModal.getCode() == IStatus.ERROR) {

				return resolutionDetails;
			} else if (resolveModal.getCode() == IStatus.WARNING) {
				return resolutionDetails;
			} else if (resolveModal.getCode() == IStatus.CANCEL) {
				return resolutionDetails;
			} else if (resolveModal.getCode() == IStatus.INFO) {
				return resolutionDetails;
			}

			ProvisioningJob provisioningJob = uninstallOperation
					.getProvisioningJob(null);

			provisioningJob.addJobChangeListener(new JobChangeAdapter() {

				@Override
				public void scheduled(IJobChangeEvent event) {
					// TODO Auto-generated method stub

					super.scheduled(event);
				}

				@Override
				public void sleeping(IJobChangeEvent event) {
					// TODO Auto-generated method stub

					super.sleeping(event);
				}

				@Override
				public void aboutToRun(IJobChangeEvent event) {
					// TODO Auto-generated method stub

					super.aboutToRun(event);

				}

				@Override
				public void running(IJobChangeEvent event) {
					// TODO Auto-generated method stub

					super.running(event);
				}

				@Override
				public void done(IJobChangeEvent event) {
					// TODO Auto-generated method stub

					super.done(event);
				}

			});

			IStatus run = provisioningJob.runModal(monitor);

		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage());
		}

		return null;
	}

}
