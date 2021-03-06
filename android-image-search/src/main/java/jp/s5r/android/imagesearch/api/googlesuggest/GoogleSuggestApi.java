package jp.s5r.android.imagesearch.api.googlesuggest;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.splunk.mint.Mint;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.net.Uri;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleSuggestApi {

  private static final String SCHEME = "http";
  private static final String HOST = "www.google.com";
  private static final String PATH = "/complete/search";
  private static final String PARAM_OUTPUT = "output";
  private static final String PARAM_QUERY = "q";

  private final AsyncHttpClient mAsyncHttpClient;

  private OnGoogleSuggestResponseListener mOnGoogleSuggestResponseListener;

  public interface OnGoogleSuggestResponseListener {
    void onGoogleSuggestResponse(List<String> response);

    void onGoogleSuggestFailure();
  }

  public GoogleSuggestApi() {
    mAsyncHttpClient = new AsyncHttpClient();
  }

  public void setOnGoogleSuggestResponseListener(OnGoogleSuggestResponseListener onGoogleSuggestResponseListener) {
    mOnGoogleSuggestResponseListener = onGoogleSuggestResponseListener;
  }

  public void suggest(String query) {
    String uri = buildUri(query);
    mAsyncHttpClient.get(uri, new AsyncHttpResponseHandler() {

      @Override
      public void onSuccess(String content) {
        super.onSuccess(content);

        try {
          List<String> results = parseXml(content);
          if (results != null) {
            if (mOnGoogleSuggestResponseListener != null) {
              mOnGoogleSuggestResponseListener.onGoogleSuggestResponse(results);
            }
          } else {
            Mint.logException(new Exception("GoogleSuggestResponse is null."));
          }
        } catch (Exception e) {
          if (mOnGoogleSuggestResponseListener != null) {
            mOnGoogleSuggestResponseListener.onGoogleSuggestFailure();
            Mint.logException(e);
          }
        }
      }

      @Override
      public void onFailure(Throwable error) {
        super.onFailure(error);
        if (mOnGoogleSuggestResponseListener != null) {
          mOnGoogleSuggestResponseListener.onGoogleSuggestFailure();
          if (error instanceof Exception) {
            Mint.logException((Exception) error);
          } else {
            Mint.logException(new Exception(error));
          }
        }
      }
    });
  }

  private List<String> parseXml(String xmlString) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));

    ArrayList<String> results = new ArrayList<String>();
    NodeList list = document.getElementsByTagName("suggestion");
    for (int i = 0; i < list.getLength(); i++) {
      Node node = list.item(i);
      Node data = node.getAttributes().getNamedItem("data");
      results.add(data.getNodeValue());
    }

    return results;
  }

  private String buildUri(String query) {
    Uri.Builder builder = new Uri.Builder()
      .scheme(SCHEME)
      .authority(HOST)
      .path(PATH);

    builder.appendQueryParameter(PARAM_QUERY, query.replaceAll("[ 　]+", "+"));
    builder.appendQueryParameter(PARAM_OUTPUT, "toolbar");

    return builder.build().toString();
  }
}
