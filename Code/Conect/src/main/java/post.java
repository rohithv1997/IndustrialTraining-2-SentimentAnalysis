import java.sql.*;

public class post {

	public static void main(String[] args) {
		int id;
		String sql, name;
		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/marvel", "postgres", "agal");
			System.out.println("Connected Successfully");
			stmt = c.createStatement();
			/*
			 * String sql = "CREATE TABLE hero " +
			 * "(ID INT PRIMARY KEY     NOT NULL," +
			 * " NAME           TEXT    NOT NULL)"; stmt.executeUpdate(sql);
			 */
			/*
			 * String sql = "insert into hero values(1, 'Captain America');";
			 * stmt.executeUpdate(sql); 
			 */
			/*sql="alter table hero add unique();";
			stmt.executeUpdate(sql);*/
			sql = "select * from  hero;";
			rs = stmt.executeQuery(sql);
			while (rs.next())
				System.out.println(rs.getInt("id") + "\t" + rs.getString("name") + "\n");

			c.close();
			stmt.close();
			//rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

	}
}
