package controller;

import java.io.IOException;
import java.io.PrintWriter;

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
 * Servlet implementation class LikeServlet
 */

public class LikeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory.getLogger(LikeServlet.class);      
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LikeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String signinID = request.getParameter("signinID");
		String token = request.getParameter("token");
		String ipInfo ="������"+ SignUtils.getIpAddress(request);
		logger.info("������ޣ�����token��"+token+"�������޼�¼id��"+signinID+ipInfo);
		UserDao userDao = new UserDao();
		JSONObject data = userDao.Like(token, signinID);
		response.setContentType("application/json;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(data);
		out.flush();
	}

}
