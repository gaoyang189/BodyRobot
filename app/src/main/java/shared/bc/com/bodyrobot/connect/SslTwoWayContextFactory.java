package shared.bc.com.bodyrobot.connect;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public final class SslTwoWayContextFactory {
	
	 private static final String PROTOCOL = "TLS";
	
    private static SSLContext CLIENT_CONTEXT;//客户端安全套接字协议

	private static final String KEY_PASS = "xt2018";

	
	 public static SSLContext getClientContext(InputStream inputStream,InputStream inputStream2){
		 if(CLIENT_CONTEXT!=null) return CLIENT_CONTEXT;

		 InputStream in = null;
		 InputStream tIN = null;
		 try{
			 KeyManagerFactory kmf = null;
			 if (inputStream != null) {
				 KeyStore ks = KeyStore.getInstance("BKS");
				 in = inputStream;
				 ks.load(in, KEY_PASS.toCharArray());
				 kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				 kmf.init(ks, KEY_PASS.toCharArray());

			 }

			 TrustManagerFactory tf = null;
			 if (inputStream != null) {
				 KeyStore tks = KeyStore.getInstance("BKS");
				 tIN = inputStream2;
                 tks.load(tIN, KEY_PASS.toCharArray());
				 tf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				 tf.init(tks);
			 }

			 CLIENT_CONTEXT = SSLContext.getInstance(PROTOCOL);
			 //初始化此上下文
			 //参数一：认证的密钥      参数二：对等信任认证  参数三：伪随机数生成器 。 由于单向认证，服务端不用验证客户端，所以第二个参数为null
			 CLIENT_CONTEXT.init(kmf.getKeyManagers(),tf.getTrustManagers(), null);

		 }catch(Exception e){
			 throw new Error("Failed to initialize the client-side SSLContext");
		 }finally{
			 if(in !=null){
				 try {
					 in.close();
				 } catch (IOException e) {
					 e.printStackTrace();
				 }
				 in = null;
			 }

			 if (tIN != null){
				 try {
					 tIN.close();
				 } catch (IOException e) {
					 e.printStackTrace();
				 }
				 tIN = null;
			 }
		 }

		 return CLIENT_CONTEXT;
	 }

}
