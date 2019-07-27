import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class run extends Serializable{

    public static void main(String[] args) {
    	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Main.class);
    	 Connection c = null;
         try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
               .getConnection("jdbc:postgresql://localhost:5432/ironman",
               "postgres", "agal");
         } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
         }
        private static final String PGSQL_CONNECTION_URL = "jdbc:postgresql://localhost:5432/ironman";
        private static final String PGSQL_USERNAME = "postgres";
        private static final String PGSQL_PWD = "agal";

        private static final SparkSession sparkSession =
                SparkSession.builder().master("local[*]").appName("Spark2JdbcDs").getOrCreate();
        //JDBC connection properties
        final Properties connectionProperties = new Properties();
        connectionProperties.put("user", PGSQL_USERNAME);
        connectionProperties.put("password", PGSQL_PWD);

        final String dbTable =
                "(select mark,name from suits);";

        //Load PGSQL query result as Dataset
        Dataset<Row> jdbcDF =
                sparkSession.read()
                        .jdbc(PGSQL_CONNECTION_URL, dbTable, "mark", igor, connectionProperties);

        List<Row> employeeFullNameRows = jdbcDF.collectAsList();

        for (Row employeeFullNameRow : employeeFullNameRows) {
            LOGGER.info(employeeFullNameRow);
        }
    }
}
