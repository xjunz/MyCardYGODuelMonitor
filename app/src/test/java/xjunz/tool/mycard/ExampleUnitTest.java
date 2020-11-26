package xjunz.tool.mycard;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    int id = 0;

    @Test
    public void var() {
        run(id);
        id += 1;
    }

    private void run(int id) {
        new Thread(() -> {
            try {
                Thread.sleep(100);
                System.out.println(id);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();
    }
}