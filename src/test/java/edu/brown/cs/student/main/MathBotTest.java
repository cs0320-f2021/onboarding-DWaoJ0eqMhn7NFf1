package edu.brown.cs.student.main;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathBotTest {

  @Test
  public void testAddition() {
    MathBot matherator9000 = new MathBot();
    double output = matherator9000.add(10.5, 3);
    assertEquals(13.5, output, 0.01);
  }

  @Test
  public void testLargerNumbers() {
    MathBot matherator9001 = new MathBot();
    double output = matherator9001.add(100000, 200303);
    assertEquals(300303, output, 0.01);
  }

  @Test
  public void testSubtraction() {
    MathBot matherator9002 = new MathBot();
    double output = matherator9002.subtract(18, 17);
    assertEquals(1, output, 0.01);
  }

  // TODO: add more unit tests of your own
  @Test
  public void testAddWithNegativeNumbers() {
    MathBot matherator9003 = new MathBot();
    double output = matherator9003.add(1, -1);
    assertEquals(0, output, 0.01);
  }

  @Test
  public void testAddWithTwoNegativeNumbers() {
    MathBot matherator9004 = new MathBot();
    double output = matherator9004.add(-1, -1);
    assertEquals(-2, output, 0.01);
  }

  @Test
  public void testSubtractWithNegativeNumbers() {
    MathBot matherator9005 = new MathBot();
    double output = matherator9005.subtract(1, -1);
    assertEquals(2, output, 0.01);
  }

  @Test
  public void testSubtractionWithLargeNumbers() {
    MathBot matherator9006 = new MathBot();
    double output = matherator9006.subtract(200303, 100000);
    assertEquals(100303, output, 0.01);
  }
}
