package rapi.ca.sql;

import java.sql.Types;

import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.type.StandardBasicTypes;

public class MySQLDialect extends MySQL5InnoDBDialect {

    public MySQLDialect() {
        super();
        registerColumnType(Types.BOOLEAN, "bit(1)");
        registerFunction("bitwise_and", new MySQLBitwiseAndSQLFunction("bitwise_and", StandardBasicTypes.INTEGER));
    }
}
