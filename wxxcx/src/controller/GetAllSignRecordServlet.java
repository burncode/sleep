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
 * Servlet implementation class GetAllSignRecordServlet
 */

public class GetAllSignRecordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory.getLogger(GetAllSignRecordServlet.class);        
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetAllSignRecordServlet() {
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
		String pageIndex = request.getParameter("pageIndex");
		String pageCount = request.getParameter("pageCount");
		String token = request.getParameter("token");
		String ipInfo ="，来自"+ SignUtils.getIpAddress(request);
		logger.info("请求find页，页数："+pageIndex+"，请求数量："+pageCount+"，来自token："+token+ipInfo);
		UserDao userDao = new UserDao();
		JSONObject data = userDao.getAllSignRecordAndIsLike(pageIndex, pageCount, token);
		response.setContentType("application/json;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(data);
		out.flush();	
	}

}
