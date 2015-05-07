package com.letv.psp.swift.common.util;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class UrlRewrite {
	private static final Logger logger = LoggerFactory.getLogger(UrlRewrite.class);
	private static final Pattern tokenPattern = Pattern.compile("(\\$\\d+)", Pattern.CASE_INSENSITIVE);

	private static final Map<Pattern, String> rewriteRules = new LinkedHashMap<Pattern, String>();
	private static final String configFile = "urlrewrite.xml";

	private static final XPathFactory xpathFactory = XPathFactory.newInstance();
	private static final XPath xpath = xpathFactory.newXPath();

	private static XPathExpression INCLUDE_XPATH_EXPRESSION;
	private static XPathExpression RULE_XPATH_EXPRESSION;
	private static XPathExpression FROM_XPATH_EXPRESSION;
	private static XPathExpression TO_XPATH_EXPRESSION;

	static {
		try {
			INCLUDE_XPATH_EXPRESSION = xpath.compile("//include");
			RULE_XPATH_EXPRESSION = xpath.compile("//rule");
			FROM_XPATH_EXPRESSION = xpath.compile("from/text()");
			TO_XPATH_EXPRESSION = xpath.compile("to/text()");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("", ex);
		}
	}

	public static String rewriteUrl(String url) {
		Pattern pattern = null;
		Matcher matcher = null;
		String targetUrl = null;
		for (Entry<Pattern, String> rewriteRule : rewriteRules.entrySet()) {
			pattern = rewriteRule.getKey();
			// FIXME 增加异常处理
			try {
				matcher = pattern.matcher(url);
				if (matcher.find()) {
					targetUrl = rewriteTargetUrl(rewriteRule.getValue(), matcher);
					return targetUrl;
				}
			} catch (Throwable ex) {
				String errorMsg = StringUtils.buildString("rewrite url [{0}]出现异常", ex);
				logger.error(errorMsg, ex);
			}
		}
		return url;
	}

	private static String rewriteTargetUrl(String toPattern, Matcher matcher) {
		Matcher toMatcher = tokenPattern.matcher(toPattern);
		int groupIdx = 1;

		StringBuffer sb = new StringBuffer();
		while (toMatcher.find()) {
			String token = toMatcher.group();
			groupIdx = StringUtils.toInt(token.substring(token.indexOf("$") + 1));
			toMatcher.appendReplacement(sb, matcher.group(groupIdx));
		}
		toMatcher.appendTail(sb);
		toMatcher = null;
		return sb.toString();
	}

	public static void init() {
		try {
			init(configFile);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("", ex);
		}
	}

	public static void init(String configFile) {
		try {
			rewriteRules.putAll(parse(configFile));
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("", ex);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<Pattern, String> parse(String _configfile) throws Exception {
		logger.info("初始化urlrewrite配置, {}", _configfile);
		Map<Pattern, String> _rewriteRules = new LinkedHashMap<Pattern, String>();
		InputStream is = ClassLoaderUtils.getStream(_configfile);
		if (is == null) {
			logger.warn("urlrewrite.xml not exists");
			return Collections.EMPTY_MAP;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document xmlDoc = builder.parse(is);
		// process include node
		NodeList includeNodes = (NodeList) INCLUDE_XPATH_EXPRESSION.evaluate(xmlDoc, XPathConstants.NODESET);
		for (int i = 0; i < includeNodes.getLength(); i++) {
			Element includeElement = (Element) includeNodes.item(i);
			_rewriteRules.putAll(parse(includeElement.getAttribute("resource")));
		}

		NodeList ruleNodes = (NodeList) RULE_XPATH_EXPRESSION.evaluate(xmlDoc, XPathConstants.NODESET);
		for (int i = 0; i < ruleNodes.getLength(); i++) {
			Element ruleElement = (Element) ruleNodes.item(i);
			String from = (String) FROM_XPATH_EXPRESSION.evaluate(ruleElement, XPathConstants.STRING);
			String to = (String) TO_XPATH_EXPRESSION.evaluate(ruleElement, XPathConstants.STRING);
			Pattern pattern4From = Pattern.compile(from, Pattern.CASE_INSENSITIVE);
			_rewriteRules.put(pattern4From, to);
		}
		return _rewriteRules;
	}

	public static void main(String[] args) {
		UrlRewrite.init();
	}
}
