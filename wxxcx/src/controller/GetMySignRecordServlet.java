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
 * Servlet implementation class GetSignRecordServlet
 */

public class GetMySignRecordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory.getLogger(GetMySignRecordServlet.class);        
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetMySignRecordServlet() {
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
		String token = request.getParameter("token");
		String ipInfo ="������"+ SignUtils.getIpAddress(request);
		logger.info("�������ǩ����¼������token��"+token+ipInfo);
		UserDao userDao = new UserDao();
		JSONObject data = userDao.getMySignRecord(token);
		response.setContentType("application/json;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(data);
		out.flush();	
	}

}
