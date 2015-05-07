package com.letv.psp.swift.common.cfg;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.letv.psp.swift.common.util.ClassLoaderUtils;
import com.letv.psp.swift.common.util.StringUtils;

public class ConfigurationFactory {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationFactory.class);

	private static final String DEFAULT_CFG_FILE = "config.xml";

	private static final XPathFactory xpathFactory = XPathFactory.newInstance();
	private static final XPath xpath = xpathFactory.newXPath();

	private static XPathExpression RESOURCE_XPATH_EXPRESSION;
	static {
		try {
			RESOURCE_XPATH_EXPRESSION = xpath.compile("//properties/@resource");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Configuration getConfiguration() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document xmlDoc = builder.parse(ClassLoaderUtils.getStream(DEFAULT_CFG_FILE));
			NodeList resourceNodes = (NodeList) RESOURCE_XPATH_EXPRESSION.evaluate(xmlDoc, XPathConstants.NODESET);

			List<String> resourceNames = new ArrayList<String>();
			for (int i = 0, size = resourceNodes.getLength(); i < size; i++) {
				resourceNames.add(StringUtils.trim(resourceNodes.item(i).getTextContent()));
			}
			return new MixedConfiguration(resourceNames.toArray(new String[] {}));
		} catch (Exception ex) {
			logger.error("", ex);
		}
		return null;
	}

	public static void main(String[] args) {
		Configuration cfg = ConfigurationFactory.getConfiguration();
		System.out.println(cfg.getString("app.search.domain"));
	}
}
