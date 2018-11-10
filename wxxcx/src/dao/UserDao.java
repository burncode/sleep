package dao;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.SignUtils;
import controller.FeedbackServlet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UserDao extends DBConn {
	private final static Logger logger = LoggerFactory.getLogger(UserDao.class); 
	// ��ѯ�Ƿ�Ϊ���û�
	public boolean isFirstRegister(String openid) {
		boolean result = true;// Ĭ���ǵ�һ��ע��
		String sql = "select * from users where openid=?";
		Object[] params = { openid };
		ResultSet rs = super.executeQuery(sql, params);
		try {
			if (rs.next()) {
				// �м�¼��˵�����ǵ�һ��
				result = false;
				//System.out.println("���ݿ�");
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
		return result;
	}

	// ������û�
	public JSONObject addUser(String openid, String session_key, String nickName, String avatarUrl)
			throws UnsupportedEncodingException {
		JSONObject data = new JSONObject();
		boolean result = false;
		// ����userid
		int userid = 40000 + this.countUser()+1;
		// �����״�ע��ʱ��
		Date now = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		String first_register = ft.format(now);
		// ����token
		String token = Encrypt.getMD5("wxdk" + String.valueOf(userid) + first_register);
		// ��nickName����,��ֹemoji
		final Base64.Encoder encoder = Base64.getEncoder();
		final byte[] textByte = nickName.getBytes("UTF-8");
		final String encodedText = encoder.encodeToString(textByte);
		// ����¼�¼
		String sql = "insert into users(userid,openid,nickName,avatarUrl,session_key,token,"
				+ "first_register,beginTime,endTime,sign_days,running_days,"
				+ "long_running_days,is_robot) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		// ע�������values����ȫ���ʺţ�������executeSQL(sql, params)�����滻
		Object[] params = { userid, openid, encodedText, avatarUrl, session_key, token, first_register, "21:00",
				"23:00", 0, 0, 0, 1 };
		try {
			int rows = super.executeSQL(sql, params);
			if (rows > 0) {
				result = true;
				data.put("token", token);
				data.put("userid", userid);
				logger.info("openid:"+openid+"��һ����Ȩ��¼��ע��ɹ���userid��"+userid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!result) {
			data.put("token", "failure");
			logger.info("openid:"+openid+"��һ����Ȩ��¼��ע��ʧ��");
		}
		return data;
	}

	// �޸�token��session_key
	public JSONObject updateUser(String openid, String session_key, String nickName, String avatarUrl)
			throws UnsupportedEncodingException {
		JSONObject userInfo = this.getUseridAndFirstReg(openid);
		JSONObject data = new JSONObject();
		boolean result = false;
		// ����token
		String token = Encrypt.getMD5("wxdk" + userInfo.getString("userid") + userInfo.getString("first_register"));
		// ��nickName����,��ֹemoji
		final Base64.Encoder encoder = Base64.getEncoder();
		final byte[] textByte = nickName.getBytes("UTF-8");
		final String encodedText = encoder.encodeToString(textByte);
		// ����¼�¼
		String sql = "update users set nickName=?,avatarUrl=?,session_key=?,token=? where openid=?";
		Object[] params = { encodedText, avatarUrl, session_key, token, openid };
		try {
			int rows = super.executeSQL(sql, params);
			if (rows > 0) {
				result = true;
				data.put("token", token);
				data.put("userid", userInfo.getString("userid"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!result) {
			data.put("token", "failure");
		}
		return data;
	}

	// ��ȡuserid��first_register����������token
	public JSONObject getUseridAndFirstReg(String openid) {
		JSONObject data = new JSONObject();
		String sql = "select * from users where openid=?";
		Object[] params = { openid };
		ResultSet rs = super.executeQuery(sql, params);
		try {
			if (rs.next()) {
				// �м�¼��˵�����ǵ�һ��
				data.put("userid", rs.getString("userid"));
				data.put("first_register", rs.getString("first_register"));
				// System.out.println("���ݿ�");
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
		return data;
	}

	// ��ѯ���õ�ʱ��
	public JSONObject getTime(String token) {
		JSONObject data = new JSONObject();
		String sql = "select * from users where token=?";
		Object[] params = { token };
		ResultSet rs = super.executeQuery(sql, params);
		try {
			if (rs.next()) {
				// ��ֹ�쳣
				String tempBegin = rs.getString("beginTime") == null ? "" : rs.getString("beginTime").substring(0, 5);
				String tempEnd = rs.getString("endTime") == null ? "" : rs.getString("endTime").substring(0, 5);
				if (tempEnd.equals("00:00"))
					tempEnd = "24:00";
				data.put("beginTime", tempBegin);
				data.put("endTime", tempEnd);
				data.put("last_update", rs.getString("last_update") == null ? "" : rs.getString("last_update"));
				logger.info("��ѯ����ʱ�����ã�����token��"+token+"���鵽��Ϣ");
			}else{
				logger.info("��ѯ����ʱ�����ã�����token��"+token+"��δ�鵽��Ϣ");
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
		return data;
	}

	// ����ʱ��
	public JSONObject setTime(String token, String last_update, String beginTime, String endTime) {
		JSONObject data = new JSONObject();
		String sql = "update users set last_update=?,beginTime=?,endTime=? where token=?";
		Object[] params = { last_update, beginTime, endTime, token };
		try {
			int rows = super.executeSQL(sql, params);
			if (rows > 0) {
				logger.info("��������ʱ�� ���ɹ�������token��"+token+"��ʱ��Σ�"+beginTime+"~"+endTime+"���ϴ��޸����ڣ�"+last_update);
				data.put("code", "1");
			} else {
				logger.info("��������ʱ�� ��ʧ�ܣ�����token��"+token+"��ʱ��Σ�"+beginTime+"~"+endTime+"���ϴ��޸����ڣ�"+last_update);
				data.put("code", "0");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	// ǩ���ж�
	public JSONObject SigninJudge(String token) {
		JSONObject data = new JSONObject();
		String sql = "select beginTime,endTime,last_signDate from users where token=?";
		Object[] params = { token };
		ResultSet rs = super.executeQuery(sql, params);
		try {
			if (rs.next()) {
				String tempBegin = rs.getString("beginTime") == null ? "" : rs.getString("beginTime");
				String tempEnd = rs.getString("endTime") == null ? "" : rs.getString("endTime");
				String last_signDate = rs.getString("last_signDate") == null ? "" : rs.getString("last_signDate");
				Date now = new Date();
				SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
				String today = ft.format(now);
				// ���жϽ��컹ûǩ��
				if (!today.equals(last_signDate) || last_signDate.isEmpty()) {
					// �����ʱ�����
					if (SignUtils.isInTime(tempBegin, tempEnd)) {
						data.put("code", "1");
						logger.info("����ǩ�� ������ǩ����ʱ��Σ�"+tempBegin+"~"+tempEnd+"������token��"+token);
					} else {
						// �������ʱ�����
						data.put("code", "3");
						logger.info("����ǩ�� ������ǩ��ʱ��Σ�"+tempBegin+"~"+tempEnd+"������token��"+token);
					}
				} else {
					// ����Ѿ�ǩ����
					data.put("code", "2");
					logger.info("����ǩ�� �������Ѿ�ǩ���ˣ�����token��"+token+"���ϴ�ǩ�����ڣ�"+last_signDate);
				}
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
		return data;
	}

	// ǩ��
	public JSONObject Signin(String token, String word, String isPublic) {
		JSONObject data = new JSONObject();
		String sql = "select sign_days,last_signDate,running_days,long_running_days,userid from users where token=?";
		Object[] params = { token };
		//��ȡ���������
		Date day = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String today = df.format(day);
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
					this.Signin_signinRecord(userid, today, word, isPublic, sign_days);
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
					this.Signin_signinRecord(userid, today, word, isPublic, sign_days);
				}
				data.put("sign_days", sign_days);
				data.put("running_days", running_days);
				data.put("long_running_days", long_running_days);
			}else{
				logger.info("����ǩ�� ������token��"+token+"��δ�鵽userid");
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
				logger.info("����ǩ�� ������token��"+token+"���޸�users��ɹ�");
			}else{
				logger.info("����ǩ�� ������token��"+token+"���޸�users��ʧ��");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// ǩ�������ǩ����
	public boolean Signin_signinRecord(String userid, String today, String word, String isPublic, int sign_days)
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
		Object[] params = { signinID, userid, today, nowTime, encodedText, 0, isPublic };
		String sql = "insert into signin_record(signinID,userid,date,time,"
				+ "word,likes_num,isPublic) values(?,?,?,?,?,?,?)";
		// ע�������values����ȫ���ʺţ�������executeSQL(sql, params)�����滻
		try {
			int rows = super.executeSQL(sql, params);
			if (rows > 0) {
				result = true;
				logger.info("����ǩ�� ������userid��"+userid+"���޸�signin_record��ɹ�");
			}else{
				logger.info("����ǩ�� ������userid��"+userid+"���޸�signin_record��ʧ��");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// ��ѯ����ǩ����¼
	public JSONObject getMySignRecord(String token) throws UnsupportedEncodingException {
		JSONObject data = new JSONObject();
		String sql = "select * from signin_record where userid in(" + "select userid from users where token=?)";
		Object[] params = { token };
		ResultSet rs = super.executeQuery(sql, params);
		try {
			int i = 0;
			JSONArray row = new JSONArray();
			while (rs.next()) {
				final Base64.Decoder decoder = Base64.getDecoder();
				JSONObject temp = new JSONObject();
				temp.put("signinID", rs.getString("signinID"));
				temp.put("date", rs.getString("date"));
				temp.put("time", rs.getString("time"));
				temp.put("likes_num", rs.getString("likes_num"));
				// ����
				temp.put("word", new String(decoder.decode(rs.getString("word")), "UTF-8"));
				row.add(i, temp);
				++i;
			}
			data.put("count", i);
			data.put("sign_record", row);
			logger.info("�������ǩ����¼��������"+i+"������token��"+token);
		} catch (SQLException e) {
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

	// ��ѯȫ��ǩ����¼�����ܲ��Ƿ����ޣ�
	public JSONObject getAllSignRecord(String pageIndex, String pageCount) throws UnsupportedEncodingException {
		JSONObject data = new JSONObject();
		int index = Integer.parseInt(pageIndex);
		int count = Integer.parseInt(pageCount);
		if (index != 0)
			index = index * count - 1;
		String sql = "select signin_record.*,nickName,avatarUrl from signin_record,users "
				+ "where isPublic='y' and signin_record.userid=users.userid " + "ORDER BY date DESC,time DESC "
				+ "limit " + index + "," + count;
		// System.out.println(sql);
		ResultSet rs = super.executeQuery(sql);
		try {
			int i = 0;
			JSONArray row = new JSONArray();
			while (rs.next()) {
				// System.out.println("�鵽��");
				final Base64.Decoder decoder = Base64.getDecoder();
				JSONObject temp = new JSONObject();
				temp.put("signinID", rs.getString("signinID"));
				temp.put("nickName", new String(decoder.decode(rs.getString("nickName")), "UTF-8"));
				// System.out.println("�鵽���ǳ�");
				temp.put("avatarUrl", rs.getString("avatarUrl"));
				temp.put("date", rs.getString("date"));
				String tempTime = rs.getString("time");
				if (tempTime.equals("00:00:00"))
					tempTime = "24:00:00";
				temp.put("time", tempTime);
				temp.put("likes_num", rs.getString("likes_num"));
				// ����
				temp.put("word", new String(decoder.decode(rs.getString("word")), "UTF-8"));
				row.add(i, temp);
				++i;
			}
			data.put("count", i);
			data.put("sign_record", row);
		} catch (SQLException e) {
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

	// ��ѯȫ��ǩ����¼���ܲ��Ƿ����ޣ�
	public JSONObject getAllSignRecordAndIsLike(String pageIndex, String pageCount, String token)
			throws UnsupportedEncodingException {
		JSONObject data = new JSONObject();
		int index = Integer.parseInt(pageIndex);
		int count = Integer.parseInt(pageCount);
		if (index != 0)
			index = index * count ;
		String sql = "select signin_record.*,nickName,avatarUrl from signin_record,users "
				+ "where isPublic='y' and signin_record.userid=users.userid " + "ORDER BY date DESC,time DESC "
				+ "limit " + index + "," + count;
		// �Ȳ��¼
		ResultSet rs = super.executeQuery(sql);
		try {
			int i = 0;
			JSONArray row = new JSONArray();
			while (rs.next()) {
				// System.out.println("�鵽��");
				final Base64.Decoder decoder = Base64.getDecoder();
				JSONObject temp = new JSONObject();
				temp.put("signinID", rs.getString("signinID"));
				temp.put("nickName", new String(decoder.decode(rs.getString("nickName")), "UTF-8"));
				// System.out.println("�鵽���ǳ�");
				temp.put("avatarUrl", rs.getString("avatarUrl"));
				temp.put("date", rs.getString("date"));
				String tempTime = rs.getString("time");
				if (tempTime.equals("00:00:00"))
					tempTime = "24:00:00";
				temp.put("time", tempTime);
				temp.put("likes_num", rs.getString("likes_num"));
				// ����
				temp.put("word", new String(decoder.decode(rs.getString("word")), "UTF-8"));
				temp.put("isLike", "");
				row.add(i, temp);
				++i;
			}
			data.put("count", i);
			data.put("sign_record", row);
			logger.info("����findҳ���鵽��������"+count+"������token��"+token);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// �ٲ�userid
		String userid = "";
		if (!(userid = this.getUserIDByToken(token)).matches("\\s{0,}")) {
			//�д��û�
			logger.info("����findҳ��token��"+token+"�����userid��"+userid);
			// ���ж��Ƿ�����
			JSONArray row = data.getJSONArray("sign_record");
			String sql_isLike = "select signinID from likes where userid=? and signinID=?";
			JSONArray rowNew = new JSONArray();
			for (int i = 0; i < row.size(); i++) {
				JSONObject temp = row.getJSONObject(i);
				String signinID = temp.getString("signinID");
				Object[] params = { userid, signinID };
				ResultSet rs_isLike = super.executeQuery(sql_isLike, params);
				try {
					if (rs_isLike.next()) {
						// �м�¼��˵�������
						temp.put("isLike", "d");
						rowNew.add(i, temp);
					} else {
						rowNew.add(i, row.getJSONObject(i));
					}
					rs_isLike.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					try {
						rs_isLike.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			data.put("sign_record", rowNew);
		} else {
			// ���û�д��û�
			logger.info("����findҳ��token��"+token+"��δ���userid");
			data.put("code", "2");
		}
		return data;
	}

	// ����
	public JSONObject Like(String token, String signinID) {
		JSONObject data = new JSONObject();
		// ���ж��Ƿ���������û�
		String userid = "";
		if (!(userid = this.getUserIDByToken(token)).matches("\\s{0,}")) {
			//�д��û������ж��Ƿ�����
			if (!this.isAllreadyLike(userid, signinID)) {
				// �޸ĵ��ޱ��ǩ����¼��
				this.Like_signinRec(userid,signinID);
				this.Like_like(userid, signinID);
				// ���޳ɹ�
				data.put("code", "1");
				logger.info("������ޣ�����token��"+token+"�������޼�¼id��"+signinID+"���鵽userid��"+userid+"�����޳ɹ�");
			} else {
				// �Ѿ������
				data.put("code", "3");
				logger.info("������ޣ�����token��"+token+"�������޼�¼id��"+signinID+"���鵽userid��"+userid+"���Ѿ��������");
			}
		} else {
			// ���û�д��û�
			logger.info("������ޣ�����token��"+token+"�������޼�¼id��"+signinID+"��δ�鵽userid");
			data.put("code", "2");
		}
		return data;
	}

	// �����Ƿ��Ѿ��������
	public boolean isAllreadyLike(String userid, String signinID) {
		boolean result = false;
		String sql = "select * from likes where userid=? and signinID=?";
		Object[] params = { userid, signinID };
		ResultSet rs = super.executeQuery(sql, params);
		try {
			if (rs.next()) {
				// �м�¼˵�����������
				result = true;
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
		return result;
	}

	// ����token��userid
	public String getUserIDByToken(String token) {
		String userid = "";
		String sql = "select userid from users where token=?";
		Object[] params = { token };
		ResultSet rs = super.executeQuery(sql, params);
		try {
			if (rs.next()) {
				userid = rs.getString("userid");
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
		return userid;
	}

	// ���ޣ��޸�ǩ����¼��
	public boolean Like_signinRec(String userid,String signinID) {
		boolean result = false;
		String sql = "update signin_record set likes_num=likes_num+1  where signinID=?";
		Object[] params = { signinID };
		try {
			int rows = super.executeSQL(sql, params);
			if (rows > 0) {
				result = true;
				logger.info("������ޣ��޸�signin_record��ɹ��������޼�¼id��"+signinID+"��userid��"+userid);
			}else{
				logger.info("������ޣ��޸�signin_record��ʧ�ܣ������޼�¼id��"+signinID+"��userid��"+userid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// ���ޣ��޸ĵ��ޱ�
	public boolean Like_like(String userid, String signinID) {
		boolean result = false;
		Object[] params = { signinID, userid };
		String sql = "insert into likes(signinID,userid) values(?,?)";
		// ע�������values����ȫ���ʺţ�������executeSQL(sql, params)�����滻
		try {
			int rows = super.executeSQL(sql, params);
			if (rows > 0) {
				result = true;
				logger.info("������ޣ��޸�likes��ɹ��������޼�¼id��"+signinID+"��userid��"+userid);
			}else{
				logger.info("������ޣ��޸�likes��ʧ�ܣ������޼�¼id��"+signinID+"��userid��"+userid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// ��ȡuserid��first_register����������token
	public JSONObject getSignCalendar(String token) {
		JSONObject data = new JSONObject();
		// ���ж��Ƿ���������û�
		String userid = "";
		if (!(userid = this.getUserIDByToken(token)).matches("\\s{0,}")) {
			//�д��û�
			logger.info("�������ǩ������������token��"+token+"���鵽userid��"+userid);
			String sql = "select date from signin_record where userid=?";
			Object[] params = { userid };
			ResultSet rs = super.executeQuery(sql, params);
			try {
				int count = 0;
				JSONArray cal_record = new JSONArray();
				while (rs.next()) {
					// �м�¼��˵��ǩ����
					cal_record.add(count, rs.getString("date"));
					++count;
				}
				// û�м�¼
				if (count == 0)
					data.put("code", "3");
				else {
					data.put("code", "1");
					data.put("cal_record", cal_record);
				}
				logger.info("�������ǩ���������鵽������"+count+"������userid��"+userid);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			// ���û�д��û�
			logger.info("�������ǩ������������token��"+token+"��δ�鵽userid");
			data.put("code", "2");
		}
		return data;
	}

	// �洢�»�ȡ��formid
	public boolean SetFormID(String formid) {
		boolean result = false;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM:dd HH:mm:ss");
		Date now = new Date();
		String datetime = df.format(now);
		Object[] params = { formid, datetime };
		String sql = "insert into formid(formid,datetime) values(?,?)";
		// ע�������values����ȫ���ʺţ�������executeSQL(sql, params)�����滻
		try {
			int rows = super.executeSQL(sql, params);
			if (rows > 0) {
				result = true;
				logger.info("�洢�ɹ� ��formid��"+formid);
			}else{
				logger.info("�洢ʧ�� ��formid��"+formid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// ��ȡ��ʱ��Ҫ���͵��û�����ûǩ�����һ��ʹ�õģ����Ҳ��ǻ����ˣ�
	public JSONArray getNowSendUsers(String beginTime) {
		System.out.println(beginTime);
		JSONArray data = new JSONArray();
		// ��ȡ��������
		SimpleDateFormat df = new SimpleDateFormat("yyyy:MM:dd");
		String todayDate = df.format(new Date());
		String sql = "select openid,endTime from users where beginTime=? and is_robot=1 " + 
							"and (last_signDate!=? or last_signDate is NULL)";//is null ��ʾһ��ûǩ����
		Object[] params = { beginTime, todayDate };
		ResultSet rs = super.executeQuery(sql, params);
		try {
			int i = 0;
			while (rs.next()) {
				// �м�¼��˵�����ǵ�һ��
				JSONObject user = new JSONObject();
				user.put("openid", rs.getString("openid"));
				String endTime = rs.getString("endTime");
				if (endTime.equals("00:00:00"))
					endTime = "24:00:00";
				user.put("endTime", endTime);
				data.add(i, user);
				++i;
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
		return data;
	}

	// ��ȡͬ��������fomrid
	public JSONArray getFormID(int count) {
		JSONArray data = new JSONArray();
		String sql = "select formid from formid order by datetime DESC limit 0," + count;
		ResultSet rs = super.executeQuery(sql);
		try {
			int i = 0;
			while (rs.next()) {
				// �м�¼��˵�����ǵ�һ��
				data.add(i, rs.getString("formid"));
				++i;
			}
			logger.info("�����ݿ��ȡ��formid������" + i);
		} catch (SQLException e) {
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

	// ɾ���Ѿ�ʹ�ù���fomrid
	public JSONArray deleteUsedFormID(JSONArray formidList) {
		JSONArray data = new JSONArray();
		int j = 0;
		for (int i = 0; i < formidList.size(); i++) {
			String sql = "delete from formid where formid=?";
			Object[] params = { formidList.getString(i) };
			try {
				int result = super.executeSQL(sql, params);
				if(result>0)
					++j;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info("ɾ����ʹ�õ�formid������" + j);
		return data;
	}

	// �洢������Ϣ
	public boolean FeedBack(String token, String word) throws UnsupportedEncodingException {
		boolean result = false;
		// ��word����,��ֹemoji
		final Base64.Encoder encoder = Base64.getEncoder();
		final byte[] textByte = word.getBytes("UTF-8");
		final String encodedText = encoder.encodeToString(textByte);
		// ����¼�¼
		String sql = "insert into feedback(token,word) values(?,?)";
		// ע�������values����ȫ���ʺţ�������executeSQL(sql, params)�����滻
		Object[] params = { token, encodedText };
		try {
			int rows = super.executeSQL(sql, params);
			if (rows > 0) {
				result = true;
				logger.info("��������ɹ�������token:"+token);
			}else{
				logger.info("�������ʧ�ܣ�����token:"+token);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// ��ѯ���û�������������userid
	public int countUser() {
		int count = 0;
		String sql = "select COUNT(*) as rowcount from users";
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
