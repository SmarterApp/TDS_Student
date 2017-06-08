package tds.student.tdslogger;

import org.opentestsystem.delivery.logging.EventParser;
import org.opentestsystem.delivery.logging.EventParserFactory;

import java.util.HashMap;
import java.util.Map;


public class StudentEventParserFactory extends EventParserFactory {

  private static Map<String, Class<? extends EventParser>> classMap = new HashMap<>();

  static {
    // Currently there are no custom event parsers installed. They would be mapped here.
  }

  @Override
  protected Map<String, Class<? extends EventParser>> getEventParserClassMap() {
    return classMap;
  }
}
