package dao;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.AddRobotServlet;
import net.sf.json.JSONObject;

public class RobotDao extends DBConn{
	private final static Logger logger = LoggerFactory.getLogger(RobotDao.class);   
	// ����»�����
	public JSONObject addRobot(String nickName)
				throws UnsupportedEncodingException {
			JSONObject data = new JSONObject();
			boolean result = false;
			int openid = this.countUser()+1;
			// ����userid
			int userid = 40000 + openid;
			//ͷ���ַ
			String avatarUrl = "*/imagesWX/"+openid+".jpg";
			// �����״�ע��ʱ��
			Date now = new Date();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
			String first_register = ft.format(now);
			// ����token
			String token = "token"+openid;
			// ��nickName����,��ֹemoji
			final Base64.Encoder encoder = Base64.getEncoder();
			final byte[] textByte = nickName.getBytes("UTF-8");
			final String encodedText = encoder.encodeToString(textByte);
			// ����¼�¼
			String sql = "insert into users(userid,openid,nickName,avatarUrl,session_key,token,"
					+ "first_register,beginTime,endTime,sign_days,running_days,"
					+ "long_running_days,is_robot) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			// ע�������values����ȫ���ʺţ�������executeSQL(sql, params)�����滻
			Object[] params = { userid, openid, encodedText, avatarUrl, 1, token, first_register, "19:00",
					"24:00", 0, 0, 0, 0 };
			try {
				int rows = super.executeSQL(sql, params);
				if (rows > 0) {
					result = true;
					data.put("token", token);
					data.put("userid", userid);
					logger.info("��ӳɹ���userid��"+userid);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!result) {
				data.put("token", "failure");
				logger.info("���ʧ��");
			}
			return data;
		}

	//ǩ���������ˣ�
	public JSONObject Signin(String token, String today, String word, String isPublic,String likes_num) {
		JSONObject data = new JSONObject();
		String sql = "select sign_days,last_signDate,running_days,long_running_days,userid from users where token=?";
		Object[] params = { token };
		ResultSet rs = super.executeQuery(sql, params);
		try {
			if (rs.next()) {
				int sign_days = rs.getInt("sign_days");
				int running_days = rs.getInt("running_days");
				int long_running_days = rs.getInt("long_running_days");
				String userid = rs.getString("userid");
				String last_signDate = rs.getString("last_signDate") == null ? "" : rs.getString("last_signDate");
				// ����ǵ�һ��ǩ������last_signDateΪ��
				if (last_signDate.isEmpty()) {
					// ���޸��û���
					sign_days = 1;
					running_days = 1;
					long_running_days = 1;
					this.Signin_users(token, today, sign_days, running_days, long_running_days);
					// �����ǩ����,���ж�word�Ƿ�Ϊ��
					if (word.isEmpty() || word.matches("\\s{0,}")) {
						word = "��˯��" + String.valueOf(sign_days) + "��";
					}
					// ��ȡ��ǰʱ��
					this.Signin_signinRecord(userid, today, word, isPublic, sign_days,likes_num);

				}
				// ���ǵ�һ��ǩ��
				else {
					sign_days = sign_days + 1;
					// �ж��Ƿ�Ϊ����ǩ��
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date date1 = format.parse(today);
					Date date2 = format.parse(last_signDate);
					int a = (int) ((date1.getTime() - date2.getTime()) / (1000 * 3600 * 24));
					// ���ϴ������������1����ǩ��
					if (a > 1) {
						long_running_days = long_running_days > running_days ? long_running_days : running_days;
						running_days = 1;
					} else {
						running_days = running_days + 1;
						long_running_days = long_running_days > running_days ? long_running_days : running_days;
					}
					// �����ǩ����,���ж�word�Ƿ�Ϊ��
					if (word.isEmpty() || word.matches("\\s{0,}")) {
						word = "��˯��" + String.valueOf(sign_days) + "��";
					}
					this.Signin_users(token, today, sign_days, running_days, long_running_days);
					this.Signin_signinRecord(userid, today, word, isPublic, sign_days,likes_num);
				}
				data.put("sign_days", sign_days);
				data.put("running_days", running_days);
				data.put("long_running_days", long_running_days);
			}else{
				logger.info("����������ǩ�� ������token��"+token+"��δ�鵽userid");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	// ǩ�����޸��û���
		public boolean Signin_users(String token, String today, int sign_days, int running_days, int long_running_days) {
			boolean result = false;
			String sql = "update users set last_signDate=?,sign_days=?,running_days=?,long_running_days=? where token=?";
			Object[] params = { today, sign_days, running_days, long_running_days, token };
			try {
				int rows = super.executeSQL(sql, params);
				if (rows > 0) {
					result = true;
					logger.info("����������ǩ�� ������token��"+token+"���޸�users��ɹ�");
				}else{
					logger.info("����������ǩ�� ������token��"+token+"���޸�users��ʧ��");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		// ǩ�������ǩ����
		public boolean Signin_signinRecord(String userid, String today, String word, String isPublic, int sign_days,String likes_num)
				throws UnsupportedEncodingException {
			boolean result = false;
			// ����signinID
			String signinID = userid + "-" + String.valueOf(sign_days);
			// ��ȡ��ǰʱ��
			Date day = new Date();
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
			String nowTime = df.format(day);
			// ����word����ֹemoji
			final Base64.Encoder encoder = Base64.getEncoder();
			final byte[] textByte = word.getBytes("UTF-8");
			// ����
			final String encodedText = encoder.encodeToString(textByte);
			Object[] params = { signinID, userid, today, nowTime, encodedText, likes_num, isPublic };
			String sql = "insert into signin_record(signinID,userid,date,time,"
					+ "word,likes_num,isPublic) values(?,?,?,?,?,?,?)";
			// ע�������values����ȫ���ʺţ�������executeSQL(sql, params)�����滻
			try {
				int rows = super.executeSQL(sql, params);
				if (rows > 0) {
					result = true;
					logger.info("����������ǩ�� ������token��"+userid+"���޸�signin_record��ɹ�");
				}else{
					logger.info("����������ǩ�� ������token��"+userid+"���޸�signin_record��ʧ��");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
		// ��ѯ�����˵ĸ�������������userid
		public int countUser() {
			int count = 0;
			String sql = "select COUNT(*) as rowcount from users where is_robot=0";
			ResultSet rs = super.executeQuery(sql);
			try {
				if (rs.next()) {
					// �м�¼��˵�����ǵ�һ��
					count = rs.getInt("rowcount");
					//System.out.println("���ݿ����û�����" + count);
				} else {
					count = 0;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return count;
		}
}
