package dao; 

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class DBConn {
	
	protected PreparedStatement pstm;// Ԥ����sql
	public Connection conn; // ����Connection�����ʵ��
	public Statement stmt; // ����Statement�����ʵ��
	public ResultSet rs; // ����ResultSet�����ʵ��
	private static String dbClassName;// ���屣�����ݿ������ı���
	private static String dbUrl;
	private static String dbUser;
	private static String dbPwd;

	public DBConn() { // ���幹�췽��
//			try { // ��׽�쳣
				dbClassName = "com.mysql.jdbc.Driver"; // ��ȡ���ݿ�����
				dbUrl = "jdbc:mysql://localhost:3306/wxxcx?useUnicode=true&characterEncoding=utf8"; // ��ȡURL
				dbUser = "root"; // ��ȡ��¼�û�
				dbPwd = "*"; // ��ȡ����
		        
//			} catch (Exception e) {
//				e.printStackTrace(); // ����쳣��Ϣ
//			}
	}

	//��ȡ���ݿ�����
	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName(dbClassName).newInstance();
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		if (conn == null) {
			System.err
					.println("����: DBConnectionManager.getConnection() ������ݿ�����ʧ��.\r\n\r\n��������:"
							+ dbClassName
							+ "\r\n����λ��:"
							+ dbUrl
							+ "\r\n�û�/����"
							+ dbUser + "/" + dbPwd);
		}
		return conn;
	}
	
	/*
	 * ����:�ر����ݿ������
	 */
	protected void closeAll() {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (pstm != null) {
			try {
				pstm.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			if (conn != null && conn.isClosed() == false) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * ִ��sql���
	 * */
	protected int executeSQL(String sql, Object[] param) throws Exception {
		int rows = 0;  
		try {
    			conn=getConnection();
			if (param != null && param.length > 0) {
				pstm =  conn.prepareStatement(sql);
				for (int i = 0; i < param.length; i++) {
					pstm.setString(i + 1, param[i].toString());
				}
				rows = pstm.executeUpdate();
			} else {
				stmt = conn.createStatement();
				rows = stmt.executeUpdate(sql);
			}
		} finally {
			this.closeAll(); 
		}
		//System.out.println("����ɹ�����");
		return rows; 
	}

	/*
	 * ���ܣ�ִ�в�ѯ���
	 */
	public ResultSet executeQuery(String sql,Object[] params) {
		try { // ��׽�쳣
			conn = getConnection(); // ����getConnection()��������Connection�����һ��ʵ��conn
		
			if (params != null && params.length > 0) {
				pstm =  conn.prepareStatement(sql);
				for (int i = 0; i < params.length; i++) {
					pstm.setString(i + 1, params[i].toString());
				}
				rs=pstm.executeQuery();
				
			} else {
				stmt = conn.createStatement();
				rs=stmt.executeQuery(sql);
			}			
			
		} catch (SQLException ex) {
			System.err.println(ex.getMessage()); // ����쳣��Ϣ
		}
		//System.out.println("����ɹ�����");
		return rs; // ���ؽ��������
	}

	/*
	 * ���ܣ�ִ�в�ѯ���
	 */
	public ResultSet executeQuery(String sql) {
		try { // ��׽�쳣
			conn = getConnection(); // ����getConnection()��������Connection�����һ��ʵ��conn
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(sql);
		} catch (SQLException ex) {
			System.err.println(ex.getMessage()); // ����쳣��Ϣ
		}
		return rs; // ���ؽ��������
	}
	
	
	/*
	 * ����:ִ�и��²���
	 */
	public int executeUpdate(String sql) {
		int result = 0; // ���屣�淵��ֵ�ı���
		try { // ��׽�쳣
			conn = getConnection(); // ����getConnection()��������Connection�����һ��ʵ��conn
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			result = stmt.executeUpdate(sql); // ִ�и��²���
		} catch (SQLException ex) {
			ex.printStackTrace();
			result = 0; // �����淵��ֵ�ı�����ֵΪ0
		}
		return result; // ���ر��淵��ֵ�ı���
		//�ɹ�����1��ʧ�ܷ���0
	}

	public int executeUpdate_id(String sql) {
		int result = 0;
		try {
			conn = getConnection();
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			result = stmt.executeUpdate(sql);
			String ID = "select @@IDENTITY as id";
			rs = stmt.executeQuery(ID);
			if (rs.next()) {
				int autoID = rs.getInt("id");
				result = autoID;
			}
		} catch (SQLException ex) {
			result = 0;
		}
		return result;
	}
	

    public ResultSet execProcedureForQuery(String procSQL,Object[] params) {
            Connection conn = getConnection(); 
            try {
                    //��������ִ����
                    CallableStatement cstmt = conn.prepareCall(procSQL);
                    //������κͳ���
                    if (params != null && params.length > 0) {
        				
        				for (int i = 0; i < params.length; i++) {
        					cstmt.setString(i + 1, params[i].toString());
        				}        				       				
        			}
                    rs=cstmt.executeQuery(); 
                    //System.out.println("proc OK!!!!!!!!!!!!!!!!!!");
            } catch (SQLException e) {
                    e.printStackTrace();
            } 
            return rs;
    } 

}
