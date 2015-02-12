import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.security.sasl.Sasl;

import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smackx.*;

@SuppressWarnings("unused")
public class SASLXFacebookPlatformMechanism extends SASLMechanism {
	
    public SASLXFacebookPlatformMechanism(SASLAuthentication saslAuthentication) {
    	// call the constructor of SASLMechanism class
    	super(saslAuthentication);
    }
    
    @Override
    protected String getName() { return "X-FACEBOOK-PLATFORM"; }

    @Override
    protected void authenticate() throws IOException, XMPPException {
        // Send the authentication to the server
        getSASLAuthentication().send(new AuthMechanism(getName(), ""));
    }

    public void authenticate(String apiKey, String host, String accessToken) throws IOException, XMPPException {
        this.authenticationId = apiKey;
        this.password = accessToken;
        this.hostname = host;

        String[] mechanisms = { "DIGEST-MD5" };
        Map<String, String> props = new HashMap<String, String>();
        this.sc = Sasl.createSaslClient(mechanisms, null, "xmpp", host, props, this);
        authenticate();
    }

    @Override
    public void challengeReceived(String rawChallenge) throws IOException {
    	byte[] response = null;

    	if (rawChallenge != null) {
    		String challenge = new String(Base64.decode(rawChallenge)); 
    		Map<String, String> parameters = getQueryMap(challenge);

    		String nonce = parameters.get("nonce");
    		String method = parameters.get("method");

    		String composedResponse = "&method=" + URLEncoder.encode(method, "utf-8")
    				+ "&nonce=" + URLEncoder.encode(nonce, "utf-8")
    				+ "&access_token=" + URLEncoder.encode(this.password, "utf-8")
    				+ "&api_key=" + URLEncoder.encode(this.authenticationId, "utf-8")
    				+ "&call_id=0&v=1.0";

    		response = composedResponse.getBytes("utf-8");
    	}

    	String authenticationText = "";
    	if (response != null)
    		authenticationText = Base64.encodeBytes(response, Base64.DONT_BREAK_LINES);

    	// Send the authentication to the server
    	getSASLAuthentication().send(new Response(authenticationText));
    }

    // parsing HTML query string
    private Map<String, String> getQueryMap(String query) {
    	Map<String, String> map = new HashMap<String, String>();
    	String[] params = query.split("\\&");

    	for (String param : params) {
    		String[] fields = param.split("=", 2);
    		map.put(fields[0], (fields.length > 1 ? fields[1] : null));
    	}
    	return map;
    }
}