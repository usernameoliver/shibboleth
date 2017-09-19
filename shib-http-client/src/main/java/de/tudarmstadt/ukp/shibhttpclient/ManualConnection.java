package de.tudarmstadt.ukp.shibhttpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.Consts;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ManualConnection {
	public static void main(String[] args) throws ClientProtocolException, IOException {
		String idpUrl = "https://passport.pitt.edu/idp/profile/SAML2/SOAP/ECP";
		String url = "https://passport.pitt.edu/idp/profile/SAML2/POST/SSO";
		String idpUrlRedirect = "https://passport.pitt.edu/idp/profile/SAML2/POST/SSO?execution=e1s1";
		
		String serviceUrl = "https://my.pitt.edu";
		
		String username = "deh95";
		String password = "5tgb";
		
//		boolean test11 = authenticateWithSAML(username, password, idpUrl, serviceUrl);
//		boolean test12 = authenticateWithSAML(username, password, url, serviceUrl);//followed tutorial
		boolean test13 = authenticateWithSAML(username, password, idpUrlRedirect, serviceUrl);//followed tutorial
		
//		boolean test21 = authenticate(username, password, idpUrl);
//		boolean test22 = authenticate(username, password, url);
//		boolean test23 = authenticate(username, password, idpUrlRedirect);
		
		
	}

	private static boolean authenticate(String username, String password, String url) throws ClientProtocolException, IOException {
		
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(url);

		//build the form to submit by post
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
	    params.add(new BasicNameValuePair("j_username", username));
	    params.add(new BasicNameValuePair("j_password", password));
	    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
	    httpPost.setEntity(entity);
	    
	    HttpResponse response = client.execute(httpPost);
	    System.out.println(response.toString());

	    return false;
	}

	private static boolean authenticateWithSAML(String username, String password, String idpUrl, String serviceUrl) throws ClientProtocolException, IOException {
		//Request from service provider by Http get
		HttpResponse responseFromSP = requestTargetResource(serviceUrl);
		String htmlString = getHtml(responseFromSP);
		
		//Request SSO Service from Identity Provider by posting form
		String RelayState = getFormContent(htmlString, true);		
		String SAMLRequest = getFormContent(htmlString, false);
		HttpResponse responseFromIdP = requestSSOService(RelayState, SAMLRequest, idpUrl);
		String htmlString2 = getHtml(responseFromIdP);	
		System.out.println(responseFromIdP.toString());

		
		return false;
	}

	

	private static String getFormContent(String htmlString, boolean RelayStateOrSAML) {
		Document doc = Jsoup.parse(htmlString);
		Element form = doc.body().child(1).child(0).child(0).child(0).child(0).child(0).child(3);
		String RelayState = form.child(0).attr("value").toString();
		String SAML = form.child(1).attr("value").toString();
		return RelayStateOrSAML? RelayState : SAML;
	}

	private static String getHtml(HttpResponse responseFromSP) throws IllegalStateException, IOException {
		HttpEntity entity = responseFromSP.getEntity(); 
		InputStream stream = entity.getContent();
		String htmlString = IOUtils.toString(stream, "UTF-8");
		return htmlString;
	}

	private static HttpResponse requestSSOService(String relayState, String SAMLRequest, String idpUrl) throws ClientProtocolException, IOException {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(idpUrl);
		
		//build the form
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
	    params.add(new BasicNameValuePair("RelayState", relayState));
	    params.add(new BasicNameValuePair("SAMLRequest", SAMLRequest));
	    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
	    httpPost.setEntity(entity);
	    
	    HttpResponse response = client.execute(httpPost);
		return response;
	}

	private static void printResponse(HttpResponse response) throws IllegalStateException, IOException {
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
			result.append("\n");
		}
		System.out.println(result);		
	}

	private static HttpResponse requestTargetResource(String serviceUrl) {		
		HttpResponse response = null;
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(serviceUrl);
		
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
}
