

package com.nhn.prov.domain;

import java.io.Serializable;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.prov.common.exception.ProvException;


public class DeployReqDoc implements Serializable {
	private static final long serialVersionUID = -8190617139632207650L;
	private static final Logger LOG = LoggerFactory.getLogger(DeployReqDoc.class);

	private static final String TEMPLATE =
		"<ns2:commandAgentRequest bizid=\"AGENT\" txid=\"\" xmlns:ns2=\"http://prov.nhncorp.com/PROVAgentRequest\">" +
			"<header>" +
			"    <deployNo></deployNo>" +
			"    <serviceId>prov</serviceId>" +
			"    <componentId>prov</componentId>" +
			"    <deployProviderIp></deployProviderIp>" +
			"    <deployTargetIp></deployTargetIp>" +
			"</header>" +
			"<deployBody>" +
			"    <jobList>" +
			"    </jobList>" +
			"</deployBody>" +
			"</ns2:commandAgentRequest>";

	private String reqId;
	private Document reqDoc;
	private Integer jobId;

	public DeployReqDoc(String reqId) throws ProvException {
		this.reqId = reqId;
		this.jobId = 0;

		// Load XML and Make Deploy-Request-Document
		SAXBuilder builder = new SAXBuilder();
		try {
			this.reqDoc = builder.build(new StringReader(DeployReqDoc.TEMPLATE));
		} catch (Exception e) {
			throw new ProvException("DeployReqDoc", "Error on loading the xml template.", e);
		}
	}

	public DeployReqDoc addHeader(String sourceIp) {
		Element root = this.reqDoc.getRootElement();
		root.getChild("header").getChild("deployNo").setText(this.reqId);
		root.getChild("header").getChild("deployProviderIp").setText(sourceIp);
		root.getChild("header").getChild("deployTargetIp").setText("127.0.0.1");
		return this;
	}

	private String createProvURI(String ip, String path) {
		StringBuilder uri = new StringBuilder("prov://");
		uri.append(ip);

		if (path.charAt(0) != '/') {
			uri.append("/");
		}

		uri.append(path);

		return uri.toString();
	}

	private Element createJob(String type) {
		Element job = new Element("job");
		job.setAttribute("id", String.valueOf(++this.jobId));
		job.setAttribute("type", type);
		return job;
	}

	public DeployReqDoc addDeploy(String sourceIp, String sourcePath, String targetPath, Boolean isDelete,
		String exPattern, String inPattern) {

		Element job = createJob("deploy-1.0");

		job.addContent(new Element("jobName").addContent("RepoDeploy"));
		job.addContent(new Element("sourceURI").addContent(createProvURI(sourceIp, sourcePath)));
		job.addContent(new Element("targetURI").addContent(createProvURI("127.0.0.1", targetPath)));

		Element fileParameter = new Element("fileParameter");

		if (isDelete != null) {
			Element delete = new Element("delete");
			Element enable = new Element("enable");
			enable.addContent(isDelete.toString());
			delete.addContent(enable);
			fileParameter.addContent(delete);
		}

		if (exPattern != null) {
			Element exclude = new Element("exclude");
			Element patternForExclude = new Element("pattern");
			patternForExclude.addContent(exPattern);
			exclude.addContent(patternForExclude);
			fileParameter.addContent(exclude);
		}

		if (inPattern != null) {
			Element include = new Element("include");
			Element patternForInclude = new Element("pattern");
			patternForInclude.addContent(inPattern);
			include.addContent(patternForInclude);
			fileParameter.addContent(include);
		}

		job.addContent(fileParameter);

		this.reqDoc.getRootElement().getChild("deployBody").getChild("jobList").addContent(job);
		return this;
	}

	public DeployReqDoc addScript(String scriptCmd, String parameter) {
		Element job = createJob("script-1.0");

		job.addContent(new Element("jobName").addContent("RepoScriptJob"));
		job.addContent(new Element("scriptLocation").addContent(scriptCmd));
		if (parameter != null && parameter.isEmpty() == false) {
            parameter = String.format("%s", parameter);
			job.addContent(new Element("scriptParameter").addContent(parameter));
		}

		this.reqDoc.getRootElement().getChild("deployBody").getChild("jobList").addContent(job);
		return this;
	}

	public DeployReqDoc addCreateDir(String path, String user, String permission) {
		Element job = createJob("filesystem-1.0");

		Element createDir = new Element("createDirectory");
		createDir.addContent(new Element("path").addContent(path));

		String owner = user;
		if (owner == null) {
			owner = "irteam";
		}

		String per = permission;
		if (per == null) {
			per = "775";
		}

		createDir.addContent(new Element("owner").addContent(owner));
		createDir.addContent(new Element("group").addContent(owner));
		createDir.addContent(new Element("permission").addContent(per));

		job.addContent(new Element("jobName").addContent("RepoCreateDir"));
		job.addContent(createDir);

		this.reqDoc.getRootElement().getChild("deployBody").getChild("jobList").addContent(job);
		return this;
	}

	public DeployReqDoc addRemoveFile(String path) {
		Element job = createJob("filesystem-1.0");

		Element removeFile = new Element("removeFile");
		removeFile.addContent(new Element("path").addContent(path));

		job.addContent(new Element("jobName").addContent("RepoRemoveFile"));
		job.addContent(removeFile);

		this.reqDoc.getRootElement().getChild("deployBody").getChild("jobList").addContent(job);
		return this;
	}

	public DeployReqDoc addMetaFile(String path, String include, String exclude) {
		Element job = createJob("metainfo-1.0");

		job.addContent(new Element("jobName").addContent("RepoMetaInfoJob"));
		job.addContent(new Element("fileSource").addContent(path));

		Element fileParameter = new Element("fileParameter");

		if (exclude != null) {
			Element patternForExclude = new Element("pattern");
			patternForExclude.addContent(exclude);

			Element excludeTag = new Element("exclude");
			excludeTag.addContent(patternForExclude);

			fileParameter.addContent(excludeTag);
		}

		if (include != null) {
			Element patternForInclude = new Element("pattern");
			patternForInclude.addContent(include);

			Element includeTag = new Element("include");
			includeTag.addContent(patternForInclude);

			fileParameter.addContent(includeTag);
		}

		job.addContent(fileParameter);

		this.reqDoc.getRootElement().getChild("deployBody").getChild("jobList").addContent(job);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
        return sb.append(outp.outputString(this.reqDoc.getRootElement())).toString();
	}

	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

}
