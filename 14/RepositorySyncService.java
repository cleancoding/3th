package com.nhn.prov.activiti.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nhn.prov.common.utils.ToStringHelper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.prov.common.exception.ProvException;
import com.nhn.prov.domain.ProvRepository;
import com.nhn.prov.model.Host;


public class RepositorySyncService extends BaseService implements ActivityBehavior {
	private final Logger LOG = LoggerFactory.getLogger(RepositorySyncService.class);

	private Expression Action;

	@Override
	public void execute(ActivityExecution execution) throws Exception {
		action(execution);
	}

	@Override
	protected void run(DelegateExecution execution) throws Exception {
		if (Action == null) {
			throw new ProvException("run", "No Action defined in this task of the workflow!");
		}
		execution.setVariable("CheckResult", false);

		String action = Action.getExpressionText();

		// extract required variables
		Map<String, Object> processVariable = execution.getVariables();
		String serviceUser = (String)processVariable.get("ServiceUser");
		String timeout = String.valueOf(processVariable.get("Timeout"));
		String excludeFromSync = (String)processVariable.get("ExcludeCondition");
		
		List<Host> eachTargetHost = (List<Host>)processVariable.get("TargetHostList");

		if (eachTargetHost == null) {
            LOG.error("No host found to sync...");
			return;
		}

        LOG.debug("{}", ToStringHelper.toString(eachTargetHost));

		// get host info - os, arch, serviceName, hostName
		// then run the action with it.
		
		for (Host host : eachTargetHost) {
			Boolean checkResult = true;
			String os = host.getOsName();
			String arch = host.getKernelBit();
			String svcCode = host.getServiceCode();
			String hostName = host.getHostName();

			// validation check
			if (os == null || os.isEmpty()) {
				LOG.debug("No information for OS");
				continue;
			}

			if (arch == null || arch.isEmpty()) {
				LOG.debug("No information for arch");
				continue;
			}
			if (svcCode == null || svcCode.isEmpty()) {
				LOG.debug("No information for the service name");
				continue;
			}
			if (hostName == null || hostName.isEmpty()) {
				LOG.debug("No information for the host name");
				continue;
			}

			// create params to ues ProvRepository
			Map<String, Object> eachHost = new HashMap<String, Object>();
			eachHost.put("hostName", host.getHostName());

			// create a deploy command
			try {
				if (action.equals("CHECK_REPO_ALL")) {
					ProvRepository.newInstance()
						.fromRepo()
						.checkRepoALL(os, arch)
						.toRepo()
						.run();

					// Once setting to false, then no change it.
					if (checkResult != false) {
						checkResult = true;
					}
				} else if (action.equals("CHECK_REPO_SVC")) {
					ProvRepository.newInstance()
						.fromRepo()
						.checkRepoSVC(svcCode, os, arch)
						.toRepo()
						.run();

					if (checkResult != false) {
						checkResult = true;
					}
				} else if (action.equals("CHECK_REPO_HOST")) {
					ProvRepository.newInstance()
						.fromRepo()
						.checkRepoHost(hostName)
						.toRepo()
						.run();

					if (checkResult != false) {
						checkResult = true;
					}
				} else if (action.equals("GEN_META_REPO_ALL")) {
					ProvRepository.newInstance(serviceUser)
						.fromRepo()
						.genMetaRepoALL(os, arch, null, excludeFromSync)
						.toRepo()
						.run();
				} else if (action.equals("GEN_META_REPO_SVC")) {
					ProvRepository.newInstance(serviceUser)
						.fromRepo()
						.genMetRepoSVC(svcCode, os, arch, null, excludeFromSync)
						.toRepo()
						.run();
				} else if (action.equals("GEN_META_REPO_HOST")) {
					ProvRepository.newInstance(serviceUser)
						.fromRepo()
						.genMetaRepHost(eachHost, null, excludeFromSync)
						.toRepo()
						.run();
				} else if (action.equals("SYNC_REPO_ALL")) {
					try {
						ProvRepository.newInstance(serviceUser)
							.syncRepoALL(os, arch)
							.toTarget(eachHost, timeout)
							.run();
					} catch (ProvException e) {
						throw new Exception(e);
					}
				} else if (action.equals("SYNC_REPO_SVC")) {
					try {
						ProvRepository.newInstance(serviceUser)
							.syncRepoSVC(svcCode, os, arch)
							.toTarget(eachHost, timeout)
							.run();
					} catch (ProvException e) {
						throw new Exception(e);
					}
				} else if (action.equals("SYNC_REPO_HOST")) {
					try {
						ProvRepository.newInstance(serviceUser)
							.syncRepoHost(hostName)
							.toTarget(eachHost, timeout)
							.run();
					} catch (ProvException e) {
						throw new Exception(e);
					}
				}
			} catch (ProvException e) {
				LOG.debug("No repo for {} => {}", action, e.getMessage());
				checkResult = false;
			} catch (Exception e) {
				String errMsg = String.format("Fail to sync to host(%s)\n%s", hostName, e.getMessage());
				throw new ProvException(action, errMsg);
			} finally {
				execution.setVariable("CheckResult", checkResult);
			}

		}

	}

}
