package rapi.ca.sql;

import java.util.List;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class MySQLBitwiseAndSQLFunction extends StandardSQLFunction implements SQLFunction {

    public MySQLBitwiseAndSQLFunction(final String name) {
        super(name);
    }

    public MySQLBitwiseAndSQLFunction(final String name, final Type typeValue) {
        super(name, typeValue);
    }

    @Override
    public String render(final Type firstArgumentType, @SuppressWarnings("rawtypes") final List args, final SessionFactoryImplementor sessionFactory) {
        if (args.size() != 2) {
            throw new IllegalArgumentException("the function must be passed 2 arguments");
        }
        return args.get(0).toString() + " & " + args.get(1).toString();
    }

}
