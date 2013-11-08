package org.classsupplier.builders;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.classsupplier.Bundle;
import org.classsupplier.PathHelper;
import org.classsupplier.State;
import org.classsupplier.impl.OSGi;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ResourceBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = OSGi.PLUGIN_ID + '.'
			+ "resourceBuilder";

	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {
		if (kind != FULL_BUILD)
			return null;
		IFolder folder = getProject()
				.getFolder(PathHelper.getModelFolderName());
		if (!folder.exists())
			folder.create(true, true, monitor);
		ISchedulingRule rule = getRule(kind, args);
		Bundle bundle = OSGi.getClassSupplier().getWorkspace()
				.getBundle(getProject().getName());
		getProject().getWorkspace().run(new ResourceRunnable(bundle), rule, 0,
				monitor);
		return null;
	}

	private class ResourceRunnable implements IWorkspaceRunnable {

		private Bundle bundle;

		public ResourceRunnable(Bundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			IPath modelPath = PathHelper.getResourcePath(getProject());
			URI modelURI = URI.createPlatformResourceURI(modelPath.toString(),
					true);
			ResourceSet resourceSet = OSGi.getClassSupplier().getWorkspace()
					.getResourceSet();
			Resource resource = resourceSet.getResource(modelURI, false);
			if (resource == null)
				resource = resourceSet.createResource(modelURI);
			if (bundle.getState() == State.DYNAMIC
					|| bundle.getState() == State.REFRESHING) {
				resource.getContents().clear();
				resource.getContents().add(
						EcoreUtil.copy(bundle.getDynamicEPackage()));
			}
			try {
				resource.save(Collections.emptyMap());
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.WARNING,
						OSGi.PLUGIN_ID, e.getLocalizedMessage(), e));
			}
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD,
						monitor);
			} catch (OperationCanceledException e) {
				return;
			} catch (InterruptedException e) {
				return;
			}
		}
	}

}