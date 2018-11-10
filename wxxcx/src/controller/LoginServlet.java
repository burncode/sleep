package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.SignUtils;
import dao.UserDao;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class LoginServlet
 */
//@.asfWebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    /**
     * Default constructor. 
     */
    public LoginServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//������get����
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String code = request.getParameter("code");
		String nickName = request.getParameter("nickName");
		String avatarUrl = request.getParameter("avatarUrl");
		String appId = "*";
	    String secret = "*";
	    String wxUrl = "https://api.weixin.qq.com/sns/jscode2session?appid="
	    				+ appId + "&secret=" + secret + "&js_code=" + code + 
	    				"&grant_type=authorization_code";
        String urlString = "";
		try
	      {
	         URL url = new URL(wxUrl);
	         URLConnection urlConnection = url.openConnection();
	         HttpURLConnection connection = null;
	         if(urlConnection instanceof HttpURLConnection)
	         {
	            connection = (HttpURLConnection) urlConnection;
	         }
	         else
	         {
	            System.out.println("������ URL ��ַ");
	            return;
	         }
	         BufferedReader in = new BufferedReader(
	         new InputStreamReader(connection.getInputStream()));
	         String current;
	         while((current = in.readLine()) != null)
	         {
	            urlString += current;
	         }
	      }catch(IOException e)
	      {
	         e.printStackTrace();
	      }
		JSONObject json = JSONObject.fromObject(urlString);  
		String openid = json.getString("openid");
		String session_key = json.getString("session_key");
		String ipInfo ="������"+ SignUtils.getIpAddress(request);
		logger.info("openid:"+openid+"�����¼"+ipInfo);
		UserDao userDao = new UserDao();
		//����ǵ�һ����Ȩ
		if(userDao.isFirstRegister(openid)){
			JSONObject result = userDao.addUser(openid, session_key,nickName,avatarUrl);
			//�����Ȩ�ɹ�
			if(!result.get("token").equals("failure")){
				//�������userdao����
				response.setContentType("application/json;charset=utf-8");
				PrintWriter out = response.getWriter();
				out.print(result);
				out.flush();
			}
		}else{
		//�Ѿ���Ȩ����
			JSONObject result = userDao.updateUser(openid, session_key,nickName,avatarUrl);
			//���������Ȩ�ɹ�
			if(!result.get("token").equals("failure")){
				response.setContentType("application/json;charset=utf-8");
				PrintWriter out = response.getWriter();
				out.print(result);
				out.flush();
			}
		}
	}

}
