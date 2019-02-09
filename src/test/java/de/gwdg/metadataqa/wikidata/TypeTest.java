package de.gwdg.metadataqa.wikidata;

import org.junit.Test;

public class TypeTest {

  @Test
  public void test() {

    long start1 = System.currentTimeMillis();
    testInstanceOf();
    long duration1 = System.currentTimeMillis() - start1;

    long start2 = System.currentTimeMillis();
    testEquals();
    long duration2 = System.currentTimeMillis() - start2;

    System.err.printf("%d vs %d", duration1, duration2);
  }

  private void testInstanceOf() {
    for (int i = 0; i < 1000000; i++) {
      String a = "";
      // String type = a.getClass().getSimpleName();
      if (a instanceof String) {
        boolean b = true;
      }
    }
  }

  private void testEquals() {
    for (int i = 0; i < 1000000; i++) {
      String a = "";
      String type = a.getClass().getSimpleName();
      if (type.equals("String")) {
        boolean b = true;
      }
    }
  }
}