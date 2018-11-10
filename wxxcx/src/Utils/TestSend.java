package Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.UserDao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * Application Lifecycle Listener implementation class TestSend
 *
 */

public class TestSend implements ServletContextListener {
	Timer timer = new Timer();
	private final static Logger logger = LoggerFactory.getLogger(TestSend.class);
	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    	 timer.cancel();  
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 	
    	//System.out.println();
    	logger.info("������ʼ");
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		TimerTask taskDay = new TimerTask() {
			public void run() {
				String dayNow = df.format(new Date());
				if (dayNow.matches("(19|20|21|22|23):30:00")) {
					//System.out.println("��:" + dayNow);
					logger.info("��:" + dayNow);
					// ˢ��access_token
					SendPost sp = new SendPost();
					String access_token = sp.GetAccess_token();
					//��ȡ��Ҫ���͵�
					UserDao userDao = new UserDao();
					JSONArray openidList = userDao.getNowSendUsers(dayNow.substring(0,2)+":00:00");
					int count = openidList.size();
					logger.info("��Ҫ����ģ����Ϣ��������" + count);
					// �������Ҫ���͵�
					if (count > 0) {
						//System.out.println("��Ҫ���͵��û�����"+count);
						//��ȡͬ��������formid
						JSONArray formidList = userDao.getFormID(count);
						for (int i = 0; i < count; i++) {
							JSONObject user = openidList.getJSONObject(i);
							sp.SendTemplateMes(access_token, formidList.getString(i), 
									user.getString("openid"),  user.getString("endTime"));
						}
						//ɾ���Ѿ�ʹ�ù���formid
						userDao.deleteUsedFormID(formidList);
					}
				}
			}
		};
		/*
		 * schedule �� scheduleAtFixedRate ���� �ɽ�schedule���ΪscheduleAtFixedDelay��
		 * ������Ҫ��������delay��rate 1��schedule�������һ��ִ�б���ʱ��delay����
		 * ��������ִ��ʱ�佫����һ������ʵ��ִ����ɵ�ʱ��Ϊ׼ 2��scheduleAtFixedRate�������һ��ִ�б���ʱ��delay����
		 * ��������ִ��ʱ�佫����һ������ʼִ�е�ʱ��Ϊ׼���迼��ͬ����
		 * 
		 * ������1�������� 2����ʱʱ�䣨����ָ��ִ�����ڣ�3������ִ�м��ʱ��
		 */
		// timer.schedule(task, 0, 1000 * 3);
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/DD HH:mm:ss");
		Date startDate;
		try {
			startDate = dateFormatter.parse("2018/06/08 19:30:00");
			timer.scheduleAtFixedRate(taskDay, startDate, 60 * 60 * 1000);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
    }
	
}
