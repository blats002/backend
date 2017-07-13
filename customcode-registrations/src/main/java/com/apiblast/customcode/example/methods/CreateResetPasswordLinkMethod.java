package com.apiblast.customcode.example.methods;

import com.alibaba.fastjson.JSONObject;
import com.apiblast.customcode.example.Config;
import com.apiblast.customcode.example.ResetPasswordRequest;
import com.apiblast.customcode.example.helper.Sha256;
import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.nextpulse.postmarkapp.api.ApiException;
import net.nextpulse.postmarkapp.api.server.SendingAPIApi;
import net.nextpulse.postmarkapp.models.server.EmailWithTemplateRequest;
import net.nextpulse.postmarkapp.models.server.SendEmailRequest;
import net.nextpulse.postmarkapp.models.server.SendEmailResponse;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateResetPasswordLinkMethod implements CustomCodeMethod, BaseMethod {

	private static final String FROM = "***REMOVED***";

	@Override
	public String getMethodName() {
		return "reset_password_link";
	}

	@Override
	public List<String> getParams() {
		return Arrays.asList("test"); 
	}

	@Override
	public CustomCodeResponse execute(CustomCodeRequest request) {
		Map map = new LinkedHashMap();
		String requestBody = request.getBody();
		JSONObject jsonObject = JSONObject.parseObject(requestBody);
		SendingAPIApi apiInstance = new SendingAPIApi();
		String xPostmarkServerToken = Config.POSTMARK_API_KEY; // String | The token associated with the Server on which this request will operate.
		SendEmailRequest body = new SendEmailRequest(); // SendEmailRequest |
		try {
			String email = jsonObject.getString("email");
			String url = "https://www.divroll.com/#/resetPassword?email=" + email + "&token=";
			EmailWithTemplateRequest templateRequest = new EmailWithTemplateRequest();
			templateRequest.setTemplateId(***REMOVED***);
			templateRequest.setFrom(FROM);
			templateRequest.setTo(email);

			ResetPasswordRequest model = new ResetPasswordRequest();
			model.setName(email);
			model.setOperating_system(jsonObject.getString("browser_os"));
			model.setBrowser_name(jsonObject.getString("browser_name"));

			String token = createTokenFromUsername(email);
			model.setAction_url(url + token);

			templateRequest.setTemplateModel(model);
			templateRequest.setFrom(FROM);
			templateRequest.setTo(email);

			SendEmailResponse result = apiInstance.sendEmailWithTemplate(xPostmarkServerToken, templateRequest);
			map.put("to", result.getTo());
			map.put("submittedAt", result.getSubmittedAt());
			map.put("messageID", result.getMessageID());
			map.put("errorCode", result.getErrorCode());
			map.put("message", result.getMessage());

			System.out.println(result);
		} catch (ApiException e) {
			System.err.println("Exception when calling SendingAPIApi#sendEmail");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (UnirestException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		CustomCodeResponse result = new CustomCodeResponse(200, map);
		return result;
	}

	private String createTokenFromUsername(String username) throws UnirestException,
			UnsupportedEncodingException, NoSuchAlgorithmException {

		JSONObject postBody = new JSONObject();
		postBody.put("email", username);
		postBody.put("type", "RESET_PASSWORD");
		postBody.put("isExpired", false);

		JSONObject acl = new JSONObject();
		JSONObject asterisk = new JSONObject();
		asterisk.put("read", false);
		asterisk.put("write", false);
		acl.put("*", asterisk);

		postBody.put("ACL", acl);

		HttpResponse<String> post = Unirest.post(Config.TXTSTREET_PARSE_URL + "/classes/Token")
				.header(X_PARSE_APPLICATION_ID, Config.TXTSTREET_PARSE_APP_ID)
				.header(X_MASTER_KEY, Config.TXTSTREET_MASTER_KEY)
				.header("Content-Type", "application/json")
				.body(postBody.toJSONString())
				.asString();
		String body = post.getBody();
		JSONObject jsonObject = JSONObject.parseObject(body);
		String objectId = jsonObject.getString("objectId");
		return Sha256.hash(objectId.getBytes("UTF-8"));
	}

}
