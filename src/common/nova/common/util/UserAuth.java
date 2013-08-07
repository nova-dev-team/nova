package nova.common.util;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Map;

import javax.crypto.Cipher;

import nova.common.service.SimpleAddress;
import nova.master.api.messages.AddUserMessage;
import nova.master.handler.AddUserHandler;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class UserAuth {

    public SimpleAddress Auth_addr = new SimpleAddress("192.168.253.56", 8443);

    public static String redirectToWayf() {

        StringBuffer newurlbuf = new StringBuffer();

        String url = null;

        newurlbuf.append("https://192.168.1.230:8443/LoisIDP2/SSO");

        String response_url = "http://192.168.253.180:3000/authback";

        newurlbuf.append("?response_url=");
        try {
            newurlbuf.append(URLEncoder.encode(response_url, "utf-8"));

            String sessionId = "1";
            newurlbuf.append("&challenge=");
            newurlbuf.append(URLEncoder.encode(sessionId, "utf-8"));

            String appid = "urn:mace:tca:sp:nova";
            newurlbuf.append("&app_id=");
            newurlbuf.append(URLEncoder.encode(appid, "utf-8"));

            String timestamp = Long
                    .toString(System.currentTimeMillis() + 300000);
            newurlbuf.append("&timestamp="
                    + URLEncoder.encode(timestamp, "utf-8"));

            StringBuffer info = new StringBuffer();
            info = info.append(appid + response_url + sessionId + timestamp);
            String sign = signInfo(info);
            newurlbuf.append("&sign=" + URLEncoder.encode(sign, "utf-8"));

            url = newurlbuf.toString();

        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        System.out.println(url);
        return url;

    }

    public static String signInfo(StringBuffer info) {

        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(
                    "/home/hestream/tencent863/nova.p12");
            ks.load(fis, "123456".toCharArray());
            fis.close();
            Enumeration enuml = ks.aliases();
            String keyAlias = null;
            if (enuml.hasMoreElements()) {
                keyAlias = (String) enuml.nextElement();
                // System.out.println("alias = [" + keyAlias + "]");
            }
            PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias,
                    "123456".toCharArray());
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initSign(prikey);
            byte[] b = (info.toString().getBytes());
            sig.update(b);
            byte[] signature = sig.sign();
            // BASE64Encoder encoder = new BASE64Encoder();
            // String signtobase64 = encoder.encode(signature);
            String signto16 = StringUtil.encode(signature);
            return signto16;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static String handleAssertions(Map<String, String> queryMap) {

        try {
            String attr;
            JSONObject jsonObj;
            String username;
            String nickname;
            String email;
            String result_code = queryMap.get("result_code");
            String authenticator_provider = queryMap
                    .get("authenticator_provider");
            String app_id = queryMap.get("app_id");
            String challenge = queryMap.get("challenge");
            String timestamp = queryMap.get("timestamp");
            String authtype = queryMap.get("authtype");
            String user_profile = queryMap.get("user_profile");
            String sign = queryMap.get("sign");
            StringBuffer info = new StringBuffer(result_code
                    + authenticator_provider + app_id + challenge + timestamp
                    + authtype + user_profile);
            // if (System.currentTimeMillis() > Long.parseLong(timestamp)
            if (veriSgin(info.toString(), sign)) {
                attr = decInfo(user_profile);
                @SuppressWarnings("rawtypes")
                Map<String, String> jsonMsg = (Map) JSONValue.parse(attr);
                /*
                 * String[] temp = attr.split(";"); Map<String, String> reqarr =
                 * null; for (int i = 0; i != temp.length; i++) { String[] pair
                 * = temp[i].split(":"); reqarr.put(pair[0], pair[1]); }
                 */
                username = jsonMsg.get("username");
                nickname = jsonMsg.get("nickname");
                email = jsonMsg.get("email");

                new AddUserHandler().handleMessage(new AddUserMessage(username,
                        email, "", "normal", "true"), null, null, null);

                return username;

            } else {

                return "UserAutheError";

            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return "UserAutheError";
    }

    public static boolean veriSgin(String info, String sign) {
        try {
            CertificateFactory certificatefactory = CertificateFactory
                    .getInstance("X.509");
            FileInputStream fin = new FileInputStream(
                    "/home/hestream/tencent863/idp.crt");

            X509Certificate certificate = (X509Certificate) certificatefactory
                    .generateCertificate(fin);
            PublicKey pub = certificate.getPublicKey();

            // BASE64Decoder decoder = new BASE64Decoder();
            // byte[] a = decoder.decodeBuffer(sign);
            byte[] a = StringUtil.decode(sign);
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initVerify(pub);
            sig.update(info.getBytes());
            if (sig.verify(a)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

    public static String decInfo(String ciphertext) {

        try {
            byte[] encdata = StringUtil.decode(ciphertext);

            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(
                    "/home/hestream/tencent863/nova.p12");
            ks.load(fis, "123456".toCharArray());
            fis.close();
            Enumeration enuml = ks.aliases();
            String keyAlias = null;
            if (enuml.hasMoreElements()) {
                keyAlias = (String) enuml.nextElement();
                // System.out.println("alias = [" + keyAlias + "]");
            }
            PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias,
                    "123456".toCharArray());
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, prikey);
            byte[] dedata = cipher.doFinal(encdata);
            return new String(dedata);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

}
