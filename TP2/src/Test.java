import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by alhua on 18-11-02.
 */
public class Test {
    /*List<Integer> list = new ArrayList<>();
    int[] data = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    boolean[] isFree = {true, true, true};

    public static void main(String[] args) {
        Test test = new Test();
        test.test();
    }

    private void test() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();

        int i = 0;
        do {
            Future<Integer> future = executorService.submit(new Callable<Integer>() {
                @Override
                public Integer call() {
                    for (int j = 0; j < 3; j++) {
                        if (isFree[j]) {
                            isFree[j] = false;
                            System.out.println("server " + j+1 + " is doing task");
                            return data[i];
                        }
                    }
                }
            });
            futures.add(future);
            i++;
        } while(i < data.length);

        for (Future<Integer> future : futures) {
            try {
                list.add(future.get());
            } catch (Exception e) {
                // error
            }
        }

        for (int element : list) {
            System.out.println(element);
        }
    }*/
}
