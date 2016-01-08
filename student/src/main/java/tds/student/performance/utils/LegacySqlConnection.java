package tds.student.performance.utils;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * This class fetches an {@code AIR.Common.DB.SQLConnection} for use with legacy methods.
 * <p>
 *     Many methods in the legacy code accept a {@code SQLConnection} as one of the arguments.  Using the
 *     {@code SQLConnection} from this class will allow us to call legacy methods from the methods we're
 *     rewriting in the {@code performance} package.
 * </p>
 * <p>
 *     Example usage:
 *
 *     public class MyClass {
 *         @Autowired
 *         private LegacySqlConnection legacySqlConnection;
 *
 *         public void myNewMethod(UUID key) {
 *             SQLConnection connection = legacySqlConnection.get();
 *
 *             SqlResult legacyResponse = LegacyDLL.someLegacyMethod(connection, key);
 *
 *             // Map the SqlResult to a meaningful domain object.
 *         }
 *     }
 * </p>
 */
@Component
@Scope("prototype")
public class LegacySqlConnection extends AbstractDAO {
    public SQLConnection get() throws SQLException {
        return this.getSQLConnection();
    }
}