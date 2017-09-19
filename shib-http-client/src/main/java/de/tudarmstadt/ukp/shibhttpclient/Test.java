package de.tudarmstadt.ukp.shibhttpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.security.sasl.AuthenticationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class Test {
	public static void main(String[] args) throws ConfigurationException, ClientProtocolException, IOException {
		
		String idpUrl = "https://passport.pitt.edu/idp/profile/SAML2/SOAP/ECP";
		String username = "deh95";
		String password = "1234";
		String serviceUrl1 = "https://ecp.cilogon.org/secure/getcert/";
		
		boolean test3 = authenticateSuper(username, password, idpUrl, serviceUrl1);
		System.out.println(test3);
		
	}
	
	private static boolean authenticateSuper(String username, String password, String idpUrl, String serviceUrl) throws ClientProtocolException, IOException, ConfigurationException  {
		DefaultBootstrap.bootstrap();
		HttpClient client = new ShibHttpClient(idpUrl, username, password, true);
		HttpGet req = new HttpGet(serviceUrl);
		HttpResponse res = null;
		String exceptionMessage = "";
		try {
			res = client.execute(req);
		} catch (AuthenticationException e) {
			exceptionMessage = e.toString();
		}
		return exceptionMessage.contains("302");
	}
}
